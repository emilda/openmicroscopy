/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.LinkedList;

import ome.api.IDelete;

/**
 * Manager for option instances for an entire delete graph. As method calls are
 * made, this instance gets passed around and the appropriate {@link Op ops}
 * are {@link #push(Op) pushed} or {@link #pop() popped} changing the current
 * state of affairs.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class DeleteOpts {

    public enum Op {

        /**
         * Default operation. If a delete is not possible, i.e. it fails with a
         * {@link org.hibernate.exception.ConstraintViolationException} or
         * similar, then the failure will cause the entire command to fail as an
         * error.
         */
        HARD,

        /**
         * Delete is attempted, but the exceptions which would make a
         * {@link #HARD} operation fail lead only to warnings.
         */
        SOFT,

        /**
         * Prevents the delete from being carried out. If an entry has a subspec,
         * then the entire subgraph will not be deleted. In some cases,
         * specifically {@link AnnotationDeleteSpec} this value may be
         * vetoed by {@link DeleteSpec#overrideKeep()}.
         */
        KEEP,

        /**
         * Permits the use of force to remove objects even against the
         * permission system. (This option cannot override low-level
         * DB constraints)
         */
        FORCE,

        REAP,

        ORPHAN,

        /**
         * Nulls a particular field of the target rather than deleting it.
         * This is useful for situations where one user has generated data
         * from another user, as with projections.
         *
         * <em>WARNING:</em>Currently, NULL can only be used for the
         * Pixels.relatedTo relationship.
         */
        NULL;
    }

    private final LinkedList<Op> list = new LinkedList<Op>();

    public void push(Op op) {
        list.add(op);
    }

    public void pop() {
        list.removeLast();
    }

    public boolean isForce() {
        return list.contains(Op.FORCE);
    }

    public boolean isSoft() {
        return list.contains(Op.SOFT);

    }

}