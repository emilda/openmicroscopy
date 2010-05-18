/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions;

import java.util.Arrays;

import ome.model.meta.Session;
import ome.services.sessions.stats.NullSessionStats;
import ome.system.Roles;

/**
 * Essentially dummy {@link SessionContext} implementation which uses the values
 * in {@link Role} to define a root-based admin instance.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
class InternalSessionContext extends SessionContextImpl {

    Roles roles;

    InternalSessionContext(Session s, Roles roles) {
        super(s, Arrays.asList(roles.getSystemGroupId()), Arrays.asList(roles
                .getSystemGroupId()),
                Arrays.asList(roles.getSystemGroupName()),
                new NullSessionStats());
        this.roles = roles;
    }

    @Override
    public String getCurrentEventType() {
        return "system";
    }

    @Override
    public Long getCurrentGroupId() {
        return roles.getSystemGroupId();
    }

    @Override
    public String getCurrentGroupName() {
        return roles.getSystemGroupName();
    }

    @Override
    public Long getCurrentUserId() {
        return roles.getRootId();
    }

    @Override
    public String getCurrentUserName() {
        return roles.getRootName();
    }

    @Override
    public boolean isCurrentUserAdmin() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

}