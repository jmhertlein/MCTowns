/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command;

import java.util.HashMap;
import me.everdras.core.command.CommandSenderWrapper;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.permission.Perms;
import me.everdras.mctowns.structure.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A wrapper class for the CommandSender, providing town-based utilities.
 *
 * @author joshua
 */
public class MCTCommandSenderWrapper extends CommandSenderWrapper {

    private TownManager manager;
    private ActiveSet activeSet;

    /**
     * Wraps a CommandSender, tying him to his ActiveSet and the townManager
     *
     * @param tMan the town manager
     * @param sender the sender to be wrapped
     * @param activeSets the database of active sets as a hashmap
     */
    public MCTCommandSenderWrapper(TownManager tMan, CommandSender sender, HashMap<String, ActiveSet> activeSets) {
        super(sender);
        this.sender = sender;
        manager = tMan;

        console = !(sender instanceof Player);

        if (!console) {
            player = (Player) sender;
            if (!activeSets.containsKey(player.getName())) {
                activeSets.put(player.getName(), new ActiveSet());
                activeSets.get(player.getName()).setActiveTown(manager.matchPlayerToTown(player));

            }

            this.activeSet = activeSets.get(player.getName());




        } else {
            player = null;
            activeSet = null;
        }
    }

    /**
     * Returns the currently active town
     *
     * @return
     */
    public Town getActiveTown() {
        return activeSet.getActiveTown();
    }

    /**
     *
     * @return the currently active plot
     */
    public Plot getActivePlot() {
        return activeSet.getActivePlot();
    }

    /**
     * Sets activePlot as the active plot
     *
     * @param activePlot
     */
    public void setActivePlot(Plot activePlot) {
        activeSet.setActivePlot(activePlot);
    }

    /**
     *
     * @return the currently active territory
     */
    public Territory getActiveTerritory() {
        return activeSet.getActiveTerritory();
    }

    /**
     * Sets the active territory to activeTerritory
     *
     * @param activeTerritory
     */
    public void setActiveTerritory(Territory activeTerritory) {
        activeSet.setActiveTerritory(activeTerritory);
    }

    /**
     * Sets the active town to activeTown
     *
     * @param activeTown the town to be set as active
     */
    public void setActiveTown(Town activeTown) {
        activeSet.setActiveTown(activeTown);
    }

    /**
     * Returns whether or not the player is a mayor or pseudo-mayor. A player is
     * a pseudo-mayor if they are an admin, op, or a mayor of their active town.
     * If the active town is null, returns true.
     *
     * @return whether or not the player has permission
     */
    public boolean hasMayoralPermissions() {
        return hasExternalPermissions(Perms.ADMIN.toString()) || (activeSet.getActiveTown() == null ? true : (activeSet.getActiveTown().playerIsMayor(player) ? true : activeSet.getActiveTown().playerIsAssistant(player)));
    }

    /**
     * Returns true if the player is an admin, or the mayor of his active town
     * and has the manageregion permission.
     *
     * @return whether or not the player can manage regions
     */
//    public boolean canManageRegions() {
//        return hasExternalPermissions(Perms.ADMIN.toString()) || (hasMayoralPermissions() && hasExternalPermissions(Perms.MANAGE_REGION.toString()));
//    }
    /**
     * Returns whether or not the player can delete the active town.
     *
     * @return true if admin, or mayor of active town and has permission node to
     * remove towns
     */
    public boolean canDeleteTown() {
        return hasExternalPermissions(Perms.ADMIN.toString()) || (hasMayoralPermissions() && hasExternalPermissions(Perms.REMOVE_TOWN.toString()));
    }

    /**
     * Returns whether or not the player can make towns.
     *
     * @return true if admin or has createtown permission node.
     */
    public boolean canCreateTown() {
        return hasExternalPermissions(Perms.ADMIN.toString()) || hasExternalPermissions(Perms.CREATE_TOWN.toString());
    }

    /**
     *
     */
    public void notifyActiveTownNotSet() {
        notifyActiveNotSet(TownLevel.TOWN);
    }

    /**
     *
     */
    public void notifyActiveTerritoryNotSet() {
        notifyActiveNotSet(TownLevel.TERRITORY);
    }

    /**
     *
     */
    public void notifyActivePlotNotSet() {
        notifyActiveNotSet(TownLevel.PLOT);
    }

    private void notifyActiveNotSet(TownLevel level) {
        sender.sendMessage("Your active " + level.toString().toLowerCase() + " is not set.");
    }
}
