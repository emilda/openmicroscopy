/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ome.model.IObject;
import ome.services.messages.EventLogMessage;
import ome.system.EventContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Single action performed by {@link GraphState}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 */
public abstract class GraphStep {

    public interface Callback {

        void add();

        void savepoint(String savepoint);

        int size();

        void rollback(String savepoint, int count) throws GraphException;

        int collapse(boolean keep);

        void release(String savepoint, int count) throws GraphException;

        Class<IObject> getClass(String key);

        Iterable<Map.Entry<String, Set<Long>>> entrySet();

    }

    private static Log log = LogFactory.getLog(GraphStep.class);

    /**
     * Used to mark {@link #savepoint} after usage.
     */
    private final static String INVALIDATED = "INVALIDATED:";

    /**
     * Location of this step in {@link GraphState#steps}.
     */
    public final int idx;

    /**
     * Stack of other {@link GraphStep} instances which show where this step is
     * in the entire graph.
     */
    public final LinkedList<GraphStep> stack;

    /**
     * Final member of {@link #stack} which is the direct ancestor of this
     * step.
     */
    public final GraphStep parent;

    /**
     * {@link GraphSpec} instance which is active for this step.
     */
    public final GraphSpec spec;

    /**
     * {@link GraphEntry} instance which is active for this step.
     */
    public final GraphEntry entry;

    /**
     * Ids of each element in the path to this node. For example, if we are
     * querying /Dataset/DatasetImageLink/Image then this contains: [4, 2, 1]
     * where 4 is the id of the dataset, and 1 is the id of the image.
     */
    private final long[] ids;

    /**
     * The actual id to be deleted as opposed to {@link GraphEntry#getId()}
     * which is the id of the root object.
     *
     * @see #ids
     */
    public final long id;

    /**
     * Parsed table name used for the SQL/HQL statements.
     */
    public final String table;

    /**
     * Type of object which is being deleted, using during
     * {@link GraphState#release(String)} to send an {@link EventLogMessage}.
     */
    public final Class<IObject> iObjectType;

    /**
     * String representation of the path to this {@link GraphEntry} used for
     * logging.
     */
    public final String pathMsg;

    /**
     * Information as to the current login.
     */
    public final EventContext ec;

    /**
     * Not final. Set during {@link GraphState#execute(int)}. If anything goes
     * wrong, it and possibly other instances from {@link #stack} will have
     * their savepoints rolled back.
     */
    private String savepoint = null;

    private boolean rollbackOnly = false;

    public GraphStep(int idx, List<GraphStep> stack, GraphSpec spec, GraphEntry entry,
            long[] ids) {
        this.idx = idx;
        this.stack = new LinkedList<GraphStep>(stack);
        if (this.stack.size() > 0) {
            this.parent = this.stack.getLast();
        } else {
            this.parent = null;
        }
        this.spec = spec;
        this.entry = entry;
        this.ids = ids;
        this.id = ids == null ? -1L : ids[ids.length - 1];
        this.ec = spec.getCurrentDetails().getCurrentEventContext();

        if (entry != null) {
            final String[] path = entry.path(entry.getSuperSpec());
            table = path[path.length - 1];
            pathMsg = StringUtils.join(path, "/");
            iObjectType = spec.getHibernateClass(table);
        } else {
            table = null;
            pathMsg = null;
            iObjectType = null;
        }
    }

    public void push(GraphOpts opts) throws GraphException {
        for (GraphStep parent : stack) {
            parent.entry.push(opts, parent.ec);
        }
        entry.push(opts, ec);
    }

    public void pop(GraphOpts opts) {
        for (GraphStep parent : stack) {
            parent.entry.pop(opts);
        }
        entry.pop(opts);
    }

    //
    // Transactions
    //

    public void rollbackOnly() {
        rollbackOnly = true;
    }

    public boolean hasSavepoint() {
        return savepoint != null;
    }

    public String start(Callback cb) throws GraphException {
        if (savepoint != null && savepoint.startsWith(INVALIDATED)) {
            log.debug("Skipping closed savepoint: " + savepoint);
            return "";
        }

        if (rollbackOnly) {
            if (savepoint != null) {
                rollback(cb);
            }
            log.debug("Skipping due to rollbackOnly: " + this);
            return ""; // EARLY EXIT
        }

        if (ids == null) { // Finalization marker
            if (savepoint != null) {
                release(cb); // We know it's not rollback only.
            }
            return ""; // EARLY EXIT
        }

        return null; // ALL NORMAL!
    }

    public String savepoint(Callback cb) {
        cb.add();
        savepoint = UUID.randomUUID().toString();
        savepoint = savepoint.replaceAll("-", "");
        cb.savepoint(savepoint);
        return savepoint;
    }

    public void release(Callback cb) throws GraphException {

        if (cb.size() == 0) {
            throw new GraphException("Action at depth 0!");
        }

        int count = cb.collapse(true);

        // If this is the last map, i.e. the truly deleted ones, then
        // raise the EventLogMessage
        if (cb.size() == 0) {
            for (Map.Entry<String, Set<Long>> entry : cb.entrySet()) {
                String key = entry.getKey();
                Class<IObject> k = cb.getClass(key);
                onRelease(k, entry.getValue());
            }
        }
        cb.release(savepoint, count);
        savepoint = INVALIDATED + savepoint;
    }

    public abstract void onRelease(Class<IObject> k, Set<Long> ids) throws GraphException;

    public void rollback(Callback cb) throws GraphException {

        if (cb.size() == 0) {
            throw new GraphException("Action at depth 0!");
        }

        int count = cb.collapse(false);
        rollbackOnly = true;
        cb.rollback(savepoint, count);
        savepoint =  INVALIDATED + savepoint;
    }

}
