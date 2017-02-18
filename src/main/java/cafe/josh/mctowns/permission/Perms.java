/*
 * Copyright (C) 2013 Joshua Michael Hertlein <jmhertlein@gmail.com>
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
package cafe.josh.mctowns.permission;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author joshua
 */
public enum Perms {

    /**
     * The node for admin commands
     */
    ADMIN,
    /**
     * allows a mayor to delete their own town
     */
    REMOVE_TOWN,
    /**
     * Lets player create town
     */
    CREATE_TOWN,
    /**
     * allows the player to warp to their home town
     */
    WARP,
    /**
     * allow the player to warp to other towns' spawns
     */
    WARP_FOREIGN,
    /**
     * allow the player to withdraw from their town bank outside their town's
     * borders
     */
    WITHDRAW_BANK_OUTSIDE_BORDERS;

    /**
     * The message to be displayed when a player attempts to perform an action
     * for which they do not have permission.
     */
    @Override
    public String toString() {
        switch(this) {
            case ADMIN:
                return "mct.admin";
            case REMOVE_TOWN:
                return "mct.removetown";
            case WARP:
                return "mct.warp";
            case WARP_FOREIGN:
                return "mct.warpforeign";
            case CREATE_TOWN:
                return "mct.createtown";
            case WITHDRAW_BANK_OUTSIDE_BORDERS:
                return "mct.withdrawoutside";
            default:
                return null;
        }
    }

    /**
     * Registers all permissions in the plugin manager
     *
     * @param pm the plugin manager in which to register permissions
     */
    public static void registerPermNodes(PluginManager pm) {
        try {
            pm.addPermission(new Permission(REMOVE_TOWN.toString()));
            pm.addPermission(new Permission(WARP.toString()));
            pm.addPermission(new Permission(WARP_FOREIGN.toString()));
            pm.addPermission(new Permission(CREATE_TOWN.toString()));
            pm.addPermission(new Permission(ADMIN.toString()));
        } catch(Exception e) {
            Logger.getLogger("Minecraft").log(Level.WARNING, "Unable to register permissions, already registered.");
        }

    }
}
