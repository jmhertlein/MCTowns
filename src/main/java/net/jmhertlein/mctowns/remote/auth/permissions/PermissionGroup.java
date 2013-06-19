/*
 * Copyright (C) 2013 joshua
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jmhertlein.mctowns.remote.auth.permissions;

import java.util.Set;
import net.jmhertlein.mctowns.remote.RemoteAction;

/**
 *
 * @author joshua
 */
public class PermissionGroup {
    private final String name, parentGroupName;
    private final PermissionGroupType type;
    private final Set<RemoteAction> authorizedActions;

    public PermissionGroup(String name, String parentGroupName, PermissionGroupType type, Set<RemoteAction> authorizedActions) {
        this.name = name;
        this.parentGroupName = parentGroupName;
        this.type = type;
        this.authorizedActions = authorizedActions;
    }

    public PermissionGroup(String name, PermissionGroupType type, Set<RemoteAction> authorizedActions) {
        this.name = name;
        this.type = type;
        this.authorizedActions = authorizedActions;
        this.parentGroupName = null;
    }

    public String getName() {
        return name;
    }

    public String getParentGroupName() {
        return parentGroupName;
    }

    public PermissionGroupType getType() {
        return type;
    }

    public Set<RemoteAction> getAuthorizedActions() {
        return authorizedActions;
    }
    
    
    
    
}
