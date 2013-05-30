package net.jmhertlein.mctowns.listeners;

import java.util.HashMap;
import java.util.List;
import static net.jmhertlein.core.chat.ChatUtil.*;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.command.handlers.CommandHandler;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.structure.MCTownsRegion;
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
import org.bukkit.craftbukkit.v1_5_R3.block.CraftSign;
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
     * Informs the player of any towns they're invited to
     * tells them the MOTD for each town they're in, 
     * and if they're the mayor, tells them about pending invites and requests
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        List<Town> towns = townManager.matchPlayerToTowns(p);
        
        List<Town> townsInvitedTo = joinManager.getTownsPlayerIsInvitedTo(p.getName());
        if(!townsInvitedTo.isEmpty())
            p.sendMessage(INFO + "You are currently invited to join the following towns:");

        for(Town t : townsInvitedTo) {
            p.sendMessage(INFO + t.getTownName());
        }
        
        for(Town t : towns) {
            p.sendMessage(INFO + "[" + t.getTownName() + "]: " + t.getTownMOTD());
            if(t.playerIsMayor(p)) {
                if(! joinManager.getPlayersRequestingMembershipToTown(t).isEmpty())
                    p.sendMessage(INFO + t.getTownName() + " has players requesting to join.");
            }
        }
        
        
        
        
    }

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

        //Plot plot = new Plot(MCTownsRegion.formatRegionName(t, TownLevel.PLOT, nuName), p.getWorld().getName());

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
        } catch(ProtectedFenceRegion.MalformedFenceRegionException ifle) {
            p.sendMessage(ChatColor.RED + "Error: " + ifle.getLocalizedMessage());
            return;
        }

        if(! CommandHandler.selectionIsWithinParent(fencedReg, pActive.getActiveTerritory())) {
            p.sendMessage(ChatColor.RED + "Error: The selected region is not within your active territory.");
            return;
        }

        townManager.addPlot(MCTownsRegion.formatRegionName(t, TownLevel.PLOT, nuName), p.getWorld(), fencedReg, t, pActive.getActiveTerritory());

        p.sendMessage(ChatColor.GREEN + "Plot created.");

        pActive.setActivePlot(townManager.getPlot(MCTownsRegion.formatRegionName(t, TownLevel.PLOT, nuName)));
        p.sendMessage(ChatColor.LIGHT_PURPLE + "Active plot set to newly created plot.");



    }
}
