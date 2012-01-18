/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command;

import java.util.HashMap;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.permission.Perms;
import me.everdras.mctowns.structure.District;
import me.everdras.mctowns.structure.Plot;
import me.everdras.mctowns.structure.Territory;
import me.everdras.mctowns.structure.Town;
import me.everdras.mctowns.structure.TownLevel;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A wrapper class for the CommandSender, providing town-based utilities.
 * @author joshua
 */
public class CommandSenderWrapper {

    private CommandSender sender;
    private Player player;
    private TownManager manager;
    private boolean console;
    private ActiveSet activeSet;

    /**
     * Wraps a CommandSender, tying him to his ActiveSet and the townManager
     * @param tMan the town manager
     * @param sender the sender to be wrapped
     * @param activeSets the database of active sets as a hashmap
     */
    public CommandSenderWrapper(TownManager tMan, CommandSender sender, HashMap<String, ActiveSet> activeSets) {
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




        }
        else {
            player = null;
            activeSet = null;
        }
    }

    /**
     *
     * @return whether or not the CommandSender is the console
     */
    public boolean isConsole() {
        return console;
    }

    /**
     *  Returns the CommandSender that this class wraps
     * @return
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Returns the currently active town
     * @return
     */
    public Town getActiveTown() {
        return activeSet.getActiveTown();
    }

    /**
     * Returns the currently active district
     * @return
     */
    public District getActiveDistrict() {
        return activeSet.getActiveDistrict();
    }

    /**
     * Sets activeDistrict to be the currently active district
     * @param activeDistrict
     */
    public void setActiveDistrict(District activeDistrict) {
        activeSet.setActiveDistrict(activeDistrict);
    }

    /**
     *
     * @return the currently active plot
     */
    public Plot getActivePlot() {
        return activeSet.getActivePlot();
    }

    /**
     *  Sets activePlot as the active plot
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
     * @param activeTerritory
     */
    public void setActiveTerritory(Territory activeTerritory) {
        activeSet.setActiveTerritory(activeTerritory);
    }

    /**
     * Sets the active town to activeTown
     * @param activeTown the town to be set as active
     */
    public void setActiveTown(Town activeTown) {
        activeSet.setActiveTown(activeTown);
    }

    /**
     * Returns the player representation of the sender.
     * @return the Player, or null if the sender is not a player
     */
    public Player getPlayer() {
        return player;
    }

    //**************************************************************************
    //PERMISSIONS-RELATED METHODS
    //**************************************************************************
    /**
     *
     * @param node
     * @return
     */
    public boolean hasExternalPermissions(String node) {
        return (!(this.getSender() instanceof Player)) || this.getSender().isOp() || this.getSender().hasPermission(node);
    }

    /**
     * Returns whether or not the player is a mayor or pseudo-mayor.
     * A player is a pseudo-mayor if they are an admin, op, or a mayor of their active town.
     * If the active town is null, returns true.
     * @return whether or not the player has permission
     */
    public boolean hasMayoralPermissions() {
        return hasExternalPermissions(Perms.ADMIN.toString()) || (activeSet.getActiveTown() == null ? true : (activeSet.getActiveTown().playerIsMayor(player) ? true : activeSet.getActiveTown().playerIsAssistant(player)));
    }

    /**
     * Returns true if the player is an admin, or the mayor of his active town and has the manageregion permission.
     * @return whether or not the player can manage regions
     */
//    public boolean canManageRegions() {
//        return hasExternalPermissions(Perms.ADMIN.toString()) || (hasMayoralPermissions() && hasExternalPermissions(Perms.MANAGE_REGION.toString()));
//    }
    /**
     * Returns whether or not the player can delete the active town.
     * @return true if admin, or mayor of active town and has permission node to remove towns
     */
    public boolean canDeleteTown() {
        return hasExternalPermissions(Perms.ADMIN.toString()) || (hasMayoralPermissions() && hasExternalPermissions(Perms.REMOVE_TOWN.toString()));
    }

    /**
     * Sends the user a message indicating that he does not have sufficent permission.
     */
    public void notifyInsufPermissions() {
        sender.sendMessage(ChatColor.RED + Perms.INSUF_PERM_MSG);
    }

    /**
     * Returns whether or not the player can make towns.
     * @return true if admin or has createtown permission node.
     */
    public boolean canCreateTown() {
        return hasExternalPermissions(Perms.ADMIN.toString()) || hasExternalPermissions(Perms.CREATE_TOWN.toString());
    }

    /**
     * Sends the sender a message.
     * @param msg The message to be send
     */
    public void sendMessage(String msg) {
        sender.sendMessage(msg);
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
    public void notifyActiveDistrictNotSet() {
        notifyActiveNotSet(TownLevel.DISTRICT);
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
