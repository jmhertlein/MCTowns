/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.listeners;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
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
            for(Town to : MCTowns.getTownManager().getTownsCollection()) {
                town = to;
                territ = MCTowns.getTownManager().getTerritory(pr.getId());
                if (territ != null) {
                    break;
                }
            }
        }

        Plot plot = null;
        if (territ != null) {
            for (ProtectedRegion pr : regs) {
                plot = MCTowns.getTownManager().getPlot(pr.getId());
                if (plot != null) {
                    break;
                }
            }
        }

        actives.setActiveTown(town);
        actives.setActiveTerritory(territ);
        actives.setActivePlot(plot);
        MCTowns.logDebug("Active set is now:" + actives);
        e.getPlayer().sendMessage(net.jmhertlein.core.chat.ChatUtil.INFO + "Active set is now: " + actives);
    }
}
