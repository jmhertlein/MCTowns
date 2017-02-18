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
package cafe.josh.mctowns.listeners;

import java.util.HashMap;
import java.util.List;
import static net.jmhertlein.core.chat.ChatUtil.*;

import cafe.josh.mctowns.command.CommandHandler;
import cafe.josh.mctowns.region.MCTownsRegion;
import cafe.josh.mctowns.region.Town;
import cafe.josh.mctowns.townjoin.TownJoinManager;
import cafe.josh.mctowns.MCTowns;
import cafe.josh.mctowns.MCTownsPlugin;
import cafe.josh.mctowns.command.ActiveSet;
import cafe.josh.mctowns.TownManager;
import cafe.josh.mctowns.region.TownLevel;
import cafe.josh.mctowns.util.MCTConfig;
import cafe.josh.mctowns.util.ProtectedFenceRegion;
import cafe.josh.mctowns.util.ProtectedFenceRegion.IncompleteFenceException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
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
    private final MCTownsPlugin plugin;
    private final TownManager townManager;
    private final TownJoinManager joinManager;
    private final HashMap<Player, ActiveSet> potentialPlotBuyers;

    /**
     *
     * @param plugin
     */
    public MCTPlayerListener(MCTownsPlugin plugin) {
        this.townManager = plugin.getTownManager();
        this.joinManager = plugin.getJoinManager();
        potentialPlotBuyers = plugin.getPotentialPlotBuyers();
        this.plugin = plugin;
    }

    /**
     * Informs the player of any towns they're invited to tells them the MOTD
     * for each town they're in, and if they're the mayor, tells them about
     * pending invites and requests
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        List<Town> towns = townManager.matchPlayerToTowns(p);

        List<Town> townsInvitedTo = joinManager.getTownsPlayerIsInvitedTo(p.getName());
        if(!townsInvitedTo.isEmpty()) {
            p.sendMessage(INFO + "You are currently invited to join the following towns:");

            for(Town t : townsInvitedTo) {
                p.sendMessage(INFO + t.getName());
            }
        }

        for(Town t : towns) {
            p.sendMessage(INFO + "[" + t.getName() + "]: " + t.getTownMOTD());
            if(t.playerIsMayor(p)) {
                if(!joinManager.getPlayersRequestingMembershipToTown(t).isEmpty()) {
                    p.sendMessage(INFO + t.getName() + " has players requesting to join.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerClickPurchaseSign(PlayerInteractEvent event) {

        if(!MCTConfig.ECONOMY_ENABLED.getBoolean()) //if economy isn't enabled, do nothing
        {
            return;
        }

        if(event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.SIGN_POST) //If there is no block, or the block is not a sign, do nothing
        {
            return;
        }

        //if the first line of the sign isn't "[mct]" then do nothing
        if(!((Sign) event.getClickedBlock().getState()).getLine(0).equalsIgnoreCase("[mct]")) {
            return;
        }

        ActiveSet plotToBuy = townManager.getPlotFromSignLocation(event.getClickedBlock().getLocation());

        if(plotToBuy == null) {
            MCTowns.logSevere("Sign was an MCT plot sign, but no matching plot was found.");
            MCTowns.logSevere("Sign's location was: " + event.getClickedBlock().getLocation().toString());
            return;
        }

        if(!plotToBuy.getActivePlot().isForSale()) {
            event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "That plot is not for sale.");
            return;
        }

        if(!MCTowns.getEconomy().has(event.getPlayer().getName(), plotToBuy.getActivePlot().getPrice().floatValue())) {
            event.getPlayer().sendMessage(ChatColor.RED + "Insufficient funds (costs " + plotToBuy.getActivePlot().getPrice() + ").");
            return;
        }

        //if it succeeds...
        potentialPlotBuyers.put(event.getPlayer(), plotToBuy);
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Type \"/mct confirm\" to finish buying this plot.)");
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Please note, this plot costs " + ChatColor.DARK_RED.toString() + MCTowns.getEconomy().format(plotToBuy.getActivePlot().getPrice().floatValue()) + " and typing \"/mct confirm\" will deduct this amount from your holdings.");
    }

    @EventHandler
    public void onPlayerTriggerFenceRegionCreation(org.bukkit.event.block.SignChangeEvent e) {
        if(e.getBlock().getType() != Material.SIGN_POST) {
            return;
        }

        Sign sign = (Sign) e.getBlock().getState();

        if(!e.getLine(0).equals(FENCEREGION_SIGN_PREFIX)) {
            return;
        }

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
        } catch(NullPointerException npe) {
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

            if(nuName.isEmpty()) {
                throw new IndexOutOfBoundsException();
            }

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
            count++;
        }

        if(count >= 100) {
            p.sendMessage(ChatColor.RED + "Error: couldn't find a fence within 100 blocks, aborting.");
            return;
        }

        ProtectedFenceRegion fencedReg;
        try {
            fencedReg = ProtectedFenceRegion.assembleSelectionFromFenceOrigin(MCTownsRegion.formatRegionName(t, TownLevel.PLOT, nuName), signLoc);
        } catch(IncompleteFenceException ex) {
            p.sendMessage(ChatColor.RED + "Error: Fence was not complete. Fence must be a complete polygon.");
            return;
        } catch(ProtectedFenceRegion.MalformedFenceRegionException ifle) {
            p.sendMessage(ChatColor.RED + "Error: " + ifle.getLocalizedMessage());
            return;
        }

        if(!CommandHandler.selectionIsWithinParent(fencedReg, pActive.getActiveTerritory())) {
            p.sendMessage(ChatColor.RED + "Error: The selected region is not within your active territory.");
            return;
        }
        try {
            townManager.addPlot(MCTownsRegion.formatRegionName(t, TownLevel.PLOT, nuName), p.getWorld(), fencedReg, t, pActive.getActiveTerritory());
        } catch(TownManager.InvalidWorldGuardRegionNameException | TownManager.RegionAlreadyExistsException ex) {
            p.sendMessage(ChatColor.RED + ex.getLocalizedMessage());
            return;
        }

        p.sendMessage(ChatColor.GREEN + "Plot created.");

        pActive.setActivePlot(townManager.getPlot(MCTownsRegion.formatRegionName(t, TownLevel.PLOT, nuName)));
        p.sendMessage(ChatColor.LIGHT_PURPLE + "Active plot set to newly created plot.");
    }

    @EventHandler
    public void onPlayerJoinAddToDefaultTown(PlayerJoinEvent e) {
        String tName = MCTConfig.DEFAULT_TOWN.getString();
        if(!e.getPlayer().hasPlayedBefore() && townManager.matchPlayerToTowns(e.getPlayer()).isEmpty()) {
            Town t = townManager.getTown(tName);
            if(t == null) {
                if(tName != null && !tName.isEmpty()) {
                    MCTowns.logWarning("Error: Default town specified in config.yml does not exist.");
                }
                return;
            }
            t.addPlayer(e.getPlayer());
            e.getPlayer().sendMessage(ChatColor.GREEN + "You've been automatically added to the town " + t.getName() + "!");
        }
    }

    @EventHandler
    public void onPlayerJoinSetupActiveSet(PlayerJoinEvent e) {
        HashMap<String, ActiveSet> activeSets = plugin.getActiveSets();
        Player player = e.getPlayer();

        if(!activeSets.containsKey(player.getName())) {
            System.out.println("Setting up active set for " + e.getPlayer().getName());
            activeSets.put(player.getName(), new ActiveSet());
            List<Town> towns = plugin.getTownManager().matchPlayerToTowns(player);
            activeSets.get(player.getName()).setActiveTown(towns.isEmpty() ? null : towns.get(0));
        }
    }
}
