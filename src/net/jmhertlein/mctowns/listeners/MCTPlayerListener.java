/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.listeners;

import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.command.handlers.CommandHandler;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.structure.MCTownsRegion;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.TownLevel;
import net.jmhertlein.mctowns.townjoin.TownJoinManager;
import net.jmhertlein.mctowns.util.Config;
import net.jmhertlein.mctowns.util.ProtectedFenceRegion;
import net.jmhertlein.mctowns.util.ProtectedFenceRegion.IncompleteFenceException;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.CraftSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

/**
 *
 * @author Joshua
 */
public class MCTPlayerListener implements Listener {
    private static final String FENCEREGION_SIGN_PREFIX = "mkreg";

    private MCTowns plugin;
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
        this.plugin = plugin;
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
            if (joinManager.getCurrentInviteForPlayer(p.getName()) != null) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "You have a pending town invitation! To check, type /mct list invite");

            }
            return; //TODO: this return makes flow of control confusing, refactor it out
        }

        //after this point, we know the player belongs to a town
        p.sendMessage(t.getTownMOTD());

        if (isMayor) {

            count = joinManager.getIssuedInvitesForTown(t).length;
            if (count > 0) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + t.getTownName() + " has " + count + " pending player join invitations.");
            }

            count = joinManager.getCurrentRequestsForTown(t).length;
            if (count > 0) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + t.getTownName() + " has " + count + " pending player join requests.");
            }
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
    public void onPlayerTriggerFenceRegionCreation(org.bukkit.event.block.SignChangeEvent e) {
        MCTowns.logDebug("Block placed. SCE");

        if(e.getBlock().getType() != Material.SIGN_POST)
            return;

        MCTowns.logDebug("Material was signpost");

        CraftSign sign = (CraftSign) e.getBlock().getState();

        System.out.println("First line: " + e.getLine(0));

        if(! e.getLine(0).equals(FENCEREGION_SIGN_PREFIX))
            return;

        MCTowns.logDebug("Had our prefix");

        MCTowns.logDebug("Doing shit.");
        Player p = e.getPlayer();

        ActiveSet pActive = plugin.getActiveSets().get(p.getName());

        if(pActive == null) {
            p.sendMessage(ChatColor.RED + "Your active town is not set.");
            return;
        }

        Town t = pActive.getActiveTown();

        if(t == null) {
            p.sendMessage(ChatColor.RED + "Your active town is not set.");
            return;
        }

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

        if(pActive.getActiveTerritory() == null) {
            p.sendMessage(ChatColor.RED + "You need to set your active territory if you want to add a plot.");
            return;
        }

        String nuName;
        try {
            nuName = e.getLine(1);

            if(nuName.isEmpty())
                throw new IndexOutOfBoundsException();

        } catch(IndexOutOfBoundsException ioobe) {
            p.sendMessage(ChatColor.RED + "Error: The second line must contain a name for the new plot.");
            return;
        }

        Plot plot = new Plot(MCTownsRegion.formatRegionName(t, TownLevel.PLOT, nuName), p.getWorld().getName());

        //now, prepare the WG region
        Location signLoc = sign.getLocation();

        Vector deltaVector = signLoc.getDirection().multiply(-1);

        int count = 0;
        while(signLoc.getBlock().getType() != Material.FENCE && count < 100) {
            signLoc = signLoc.add(deltaVector);
            count ++;
        }

        MCTowns.logDebug("Found fence at " + signLoc.toString());

        if(count >= 100) {
            p.sendMessage(ChatColor.RED + "Error: couldn't find a fence within 100 blocks, aborting.");
            return;
        }

        ProtectedFenceRegion fencedReg;
        try {
            fencedReg = ProtectedFenceRegion.assembleSelectionFromFenceOrigin(MCTownsRegion.formatRegionName(t, TownLevel.PLOT, nuName), signLoc);
        } catch (IncompleteFenceException ex) {
            p.sendMessage(ChatColor.RED + "Error: Fence was not complete. Fence must be a complete polygon.");
            return;
        }

        if(! CommandHandler.selectionIsWithinParent(fencedReg, pActive.getActiveTerritory())) {
            p.sendMessage(ChatColor.RED + "Error: The selected region is not within your active territory.");
            return;
        }

        pActive.getActiveTerritory().addPlot(plot);

        RegionManager regMan = MCTowns.getWgp().getRegionManager(p.getWorld());
        regMan.addRegion(fencedReg);

        //set the new plot's parent to the active territory
        try {
            fencedReg.setParent(regMan.getRegion(pActive.getActiveTerritory().getName()));
        } catch (CircularInheritanceException ex) {
            Logger.getLogger(MCTPlayerListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        //force a save of the region database
        try {
            regMan.save();
        } catch (ProtectionDatabaseException ex) {
            Logger.getLogger(MCTPlayerListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        p.sendMessage(ChatColor.GREEN + "Plot created.");

        pActive.setActivePlot(plot);
        p.sendMessage(ChatColor.LIGHT_PURPLE + "Active plot set to newly created plot.");



    }
}
