/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.listeners;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.structure.District;
import me.everdras.mctowns.structure.Plot;
import me.everdras.mctowns.structure.Territory;
import me.everdras.mctowns.structure.Town;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author joshua
 */
public class QuickSelectToolListener implements Listener {

    public static Material SELECT_TOOL;
    private WorldGuardPlugin wgp;
    private MCTowns mctp;

    public QuickSelectToolListener(WorldGuardPlugin wgp, MCTowns mctp) {
        this.wgp = wgp;
        this.mctp = mctp;
    }

    @EventHandler
    public void onToolUse(PlayerInteractEvent e) {
        MCTowns.logDebug("Event triggered.");
        if ((e.getPlayer().getItemInHand().getType().compareTo(SELECT_TOOL)) != 0) {
            MCTowns.logDebug(("ID wrong. Tool ID: " + SELECT_TOOL.getId() + ", Hand ID: " + e.getPlayer().getItemInHand().getType().getId()));
            return;
        }

        MCTowns.logDebug("Item was tool.");
        Player player = e.getPlayer();

        ActiveSet actives = mctp.getActiveSets().get(e.getPlayer().getName());

        if (actives == null) {
            mctp.getActiveSets().put(player.getName(), new ActiveSet());
            actives = mctp.getActiveSets().get(player.getName());
            actives.setActiveTown(mctp.getTownManager().matchPlayerToTown(player));
        }

        Location spotClicked = e.getClickedBlock().getLocation();

        ApplicableRegionSet regs = wgp.getRegionManager(e.getPlayer().getWorld()).getApplicableRegions(spotClicked);

        Town town = null;
        Territory territ = null;
        for (ProtectedRegion pr : regs) {
            for(Town to : mctp.getTownManager().getTownsCollection()) {
                town = to;
                territ = town.getTerritory(pr.getId());
                if (territ != null) {
                    break;
                }
            }
        }

        District dist = null;
        if (territ != null) {
            for (ProtectedRegion pr : regs) {
                dist = territ.getDistrict(pr.getId());
                if (dist != null) {
                    break;
                }
            }
        }

        Plot plot = null;
        if (dist != null) {
            for (ProtectedRegion pr : regs) {
                plot = dist.getPlot(pr.getId());
                if (plot != null) {
                    break;
                }
            }
        }

        actives.setActiveTown(town);
        actives.setActiveTerritory(territ);
        actives.setActiveDistrict(dist);
        actives.setActivePlot(plot);
        MCTowns.logDebug("Active set is now:" + actives);
        e.getPlayer().sendMessage(me.everdras.core.chat.ChatUtil.INFO + "Active set is now: " + actives);
    }
}
