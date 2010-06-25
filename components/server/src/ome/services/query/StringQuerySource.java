/*
 * ome.services.query.StringQuerySource
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.services.query;

// Java imports

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies
import ome.parameters.Parameters;

/**
 * interprets the query id as an HQL query. In this implementation, no parsing
 * is done at lookup or creation-time, but an implementation which does so is
 * conceivable. The id itself is added to the list of parameters with the name
 * {@link ome.services.query.StringQuery#STRING}
 * 
 * This query source should be placed at the end of the array of query sources
 * provided to {@link ome.services.query.QueryFactory} because it will always
 * return a {@link ome.services.query.Query} regardless of the id. An exception
 * will be thrown at execution time if the HQL is invalid.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class StringQuerySource extends QuerySource {

    private static Log log = LogFactory.getLog(StringQuerySource.class);

    @Override
    public Query lookup(String queryID, Parameters parameters) {
        Parameters p = new Parameters(parameters);
        p.addString(StringQuery.STRING, queryID);
        return new StringQuery(p);
    }

}