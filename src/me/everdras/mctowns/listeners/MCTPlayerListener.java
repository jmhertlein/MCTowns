/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.structure.Town;
import me.everdras.mctowns.townjoin.TownJoinInfoPair;
import me.everdras.mctowns.townjoin.TownJoinManager;
import me.everdras.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.CraftSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Joshua
 */
public class MCTPlayerListener implements Listener {
    private static final String FENCEREGION_SIGN_PREFIX = "mkreg";

    private TownManager townManager;
    private TownJoinManager joinManager;
    private Config options;
    private Economy economy;
    private HashMap<Player, ActiveSet> potentialPlotBuyers;

    /**
     *
     * @param townManager
     * @param joinManager
     */
    public MCTPlayerListener(MCTowns plugin) {
        options = plugin.getOptions();
        this.townManager = plugin.getTownManager();
        this.joinManager = plugin.getJoinManager();
        economy = MCTowns.getEconomy();
        potentialPlotBuyers = plugin.getPotentialPlotBuyers();
    }

    /**
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        Town t = townManager.matchPlayerToTown(p);
        boolean isMayor;
        try {
            isMayor = t.playerIsMayor(p);
        } catch (NullPointerException npe) {
            isMayor = false;
        }

        int count = 0;


        if (t == null) { //if player doesn't belong to a town...
            LinkedList<TownJoinInfoPair> invs = joinManager.getInvitesForPlayer(p);

            if (invs.size() != 0) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "You have " + invs.size() + " pending town invitations! Type /mct list invites");

            }
            return;
        }


        p.sendMessage(t.getTownMOTD());

        if (isMayor) {

            count = joinManager.getPendingInvitesForTown(t).size();
            if (count > 0) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + t.getTownName() + " has " + count + " pending player join invitations.");
            }

            count = joinManager.getPendingRequestsForTown(t).size();
            if (count > 0) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + t.getTownName() + " has " + joinManager.getPendingRequestsForTown(t).size() + " pending player join requests.");
            }
        } else {
            //nothing else to really do for people on logging in if they're not the mayor... i'll keep this here
            //just in case.
        }


    }

//    /**
//     *
//     * @param event
//     */
//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onPlayerRespawn(PlayerRespawnEvent event) {
//        Player p = event.getPlayer();
//
//        Town t = townManager.matchPlayerToTown(p);
//
//        if (t == null) {
//            return;
//        }
//
//        p.teleport(t.getTownSpawn(p.getServer()));
//
//    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerClickPurchaseSign(PlayerInteractEvent event) {

        if (!options.isEconomyEnabled()) {
            //if economy isn't enabled, do nothing
            return;
        }

        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.SIGN_POST) {
            //If there is no block, or the block is not a sign, do nothing
            return;
        }

        //if the first line of the sign isn't "[mct]" then do nothing
        if (!((CraftSign) event.getClickedBlock().getState()).getLine(0).equalsIgnoreCase("[mct]")) {
            return;
        }


        ActiveSet plotToBuy = townManager.getPlotFromSignLocation(event.getClickedBlock().getLocation());

        if (plotToBuy == null) {
            MCTowns.logSevere("Sign was an MCT plot sign, but no matching plot was found.");
            MCTowns.logSevere("Sign's location was: " + event.getClickedBlock().getLocation().toString());
            return;
        }


        if (!plotToBuy.getActivePlot().isForSale()) {
            event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "That plot is not for sale.");
            return;
        }


        if (!economy.has(event.getPlayer().getName(), plotToBuy.getActivePlot().getPrice().floatValue())) {
            event.getPlayer().sendMessage(ChatColor.RED + "Insufficient funds (costs " + plotToBuy.getActivePlot().getPrice() + ").");
            return;
        }

        //if it succeeds...
        potentialPlotBuyers.put(event.getPlayer(), plotToBuy);
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Type \"/mct confirm\" to finish buying this plot.)");
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Please note, this plot costs " + ChatColor.DARK_RED.toString() + economy.format(plotToBuy.getActivePlot().getPrice().floatValue()) + " and typing \"/mct confirm\" will deduct this amount from your holdings.");
    }

    @EventHandler
    public void onPlayerTriggerFenceRegionCreation(org.bukkit.event.block.BlockPlaceEvent e) {
        if(e.getBlock().getType() != Material.SIGN_POST)
            return;

        CraftSign sign = (CraftSign) e.getBlock().getState();

        if(! sign.getLine(0).equals(FENCEREGION_SIGN_PREFIX))
            return;


        Player p = e.getPlayer();
        Town t = townManager.matchPlayerToTown(p);
        boolean isMayor;
        try {
            isMayor = t.playerIsMayor(p);
        } catch (NullPointerException npe) {
            isMayor = false;
        }

        if(!isMayor) {
            p.sendMessage(ChatColor.RED + "Insufficient permission.");
            return;
        }

        





    }
}
