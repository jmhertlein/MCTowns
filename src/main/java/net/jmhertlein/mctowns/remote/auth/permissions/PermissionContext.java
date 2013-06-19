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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.jmhertlein.mctowns.remote.RemoteAction;
import net.jmhertlein.mctowns.remote.auth.PublicIdentity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * A PermissionContext is a set of permission groups, and is usually defined in a yaml file.
 * 
 * A PermissionContext will ALWAYS have a "default" group. If one is not specified in the yaml file,
 * one will be created for you, will no inheritance, type resident, and no authorized actions.
 * 
 * Example yaml file: 
 * 
 * groups:
 *   #normal players can look at things
 *   default:
 *    type: resident
 *    inherits:
 *    authorized:
 *      - KEY_EXCHANGE
 *      - TERMINATE_SESSION
 *      - GET_TOWN_VIEW
 *      - GET_TOWN_LIST
 *      ...
 * 
 * This will make a group named "default" of type "resident" which does not
 * inherit actions from any other group, and which is authorized for the four actions
 * listed under "authorized".
 * 
 * @author joshua
 */
public class PermissionContext {
    private Map<String, PermissionGroup> groups;
    
    public PermissionContext(File f) throws FileNotFoundException, IOException, InvalidConfigurationException {
        groups = new HashMap<>();
        FileConfiguration c = new YamlConfiguration();
        c.load(f);
        
        initializeFromFileConfiguration(c);
    }
    
    public PermissionContext(FileConfiguration f) {
        initializeFromFileConfiguration(f);
    }
    
    public PermissionContext(List<PermissionGroup> groups) {
        this.groups = new HashMap<>();
        for(PermissionGroup g : groups) {
            this.groups.put(g.getName(), g);
        }
        
        if(!this.groups.containsKey("default")) {
            this.groups.put("default", new PermissionGroup("default", PermissionGroupType.RESIDENT, new HashSet<RemoteAction>()));
        }
    }

    public Map<String, PermissionGroup> getGroups() {
        return groups;
    }
    
    public boolean groupHasPermission(String groupName, RemoteAction action) {
        PermissionGroup group = groups.get(groupName);
        
        if(group.getAuthorizedActions().contains(action))
            return true;
        else if(group.getParentGroupName() != null)
            return groupHasPermission(group.getParentGroupName(), action);
        else
            return false;
    }
    
    public boolean userHasPermission(PublicIdentity i, RemoteAction action) {
        return groupHasPermission(i.getPermissionGroup(), action);
    }
    
    private void initializeFromFileConfiguration(FileConfiguration c) {
        //grab the subsection for "groups"
        ConfigurationSection groupsSection = c.getConfigurationSection("groups");
        
        //populate this.groups
        for(String groupName : groupsSection.getKeys(false)) {
            //get the subsection for the current group
            ConfigurationSection curGroupSection = groupsSection.getConfigurationSection(groupName);
            
            //load the type and parent group name
            PermissionGroupType type = PermissionGroupType.valueOf(curGroupSection.getString("type").toUpperCase());
            String parentGroupName = curGroupSection.getString("inherits");
            
            //grab all the actions that are permitted
            Set<RemoteAction> authActions = new HashSet<>();
            for(String action : curGroupSection.getStringList("authorized")) {
                authActions.add(RemoteAction.valueOf(action));
            }
            
            //finally, stick our constructed group in this.groups
            groups.put(groupName, new PermissionGroup(groupName, parentGroupName, type, authActions));
        }
        
        if(!groups.containsKey("default")) {
            groups.put("default", new PermissionGroup("default", PermissionGroupType.RESIDENT, new HashSet<RemoteAction>()));
        }
    }
}
