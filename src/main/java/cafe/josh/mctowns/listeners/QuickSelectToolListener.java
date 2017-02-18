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

import cafe.josh.mctowns.TownManager;
import cafe.josh.mctowns.command.ActiveSet;
import cafe.josh.mctowns.region.Plot;
import cafe.josh.mctowns.region.Territory;
import cafe.josh.mctowns.region.Town;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.List;
import cafe.josh.mctowns.MCTowns;
import cafe.josh.mctowns.MCTownsPlugin;
import cafe.josh.mctowns.util.MCTConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author joshua
 */
public class QuickSelectToolListener implements Listener {

    private static Material SELECT_TOOL;
    private final WorldGuardPlugin wgp;
    private final MCTownsPlugin mctp;
    private final TownManager townMan;

    public QuickSelectToolListener(WorldGuardPlugin wgp, MCTownsPlugin mctp) {
        this.wgp = wgp;
        this.mctp = mctp;
        this.townMan = MCTowns.getTownManager();

        SELECT_TOOL = Material.getMaterial(MCTConfig.QUICKSELECT_TOOL.getString());
    }

    @EventHandler
    public void onToolUse(PlayerInteractEvent e) {
        if(SELECT_TOOL == null) {
            MCTowns.logWarning("Error: quickselect tool config value is invalid.");
            return;
        }

        if((e.getPlayer().getItemInHand().getType().compareTo(SELECT_TOOL)) != 0) {
            return;
        }

        Player player = e.getPlayer();

        ActiveSet actives = mctp.getActiveSets().get(e.getPlayer().getName());

        if(actives == null) {
            actives = new ActiveSet();
            mctp.getActiveSets().put(player.getName(), actives);
            List<Town> towns = townMan.matchPlayerToTowns(player);
            actives.setActiveTown(towns.isEmpty() ? null : towns.get(0));
        }

        e.setCancelled(true);
        if(actives.getActiveTown() == null) {
            e.getPlayer().sendMessage(ChatColor.RED + "You need to set your active town first.");
            return;
        }

        Block b = e.getClickedBlock();

        if(b == null) {
            return;
        }

        Location spotClicked = b.getLocation();

        ApplicableRegionSet regs = wgp.getRegionManager(e.getPlayer().getWorld()).getApplicableRegions(spotClicked);

        Town town = actives.getActiveTown();

        if(town == null) {
            player.sendMessage(ChatColor.RED + "You need to set your active town first.");
            return;
        }

        Territory territ = null;
        for(ProtectedRegion pr : regs) {
            territ = townMan.getTerritory(pr.getId());
            if(territ != null && territ.getParentTown().equals(town.getName())) {
                break;
            } else {
                territ = null;
            }
        }

        Plot plot = null;
        if(territ != null) {
            for(ProtectedRegion pr : regs) {
                plot = townMan.getPlot(pr.getId());
                if(plot != null && plot.getParentTerritoryName().equals(territ.getName())) {
                    break;
                } else {
                    plot = null;
                }
            }
        }

        actives.setActiveTown(town);
        actives.setActiveTerritory(territ);
        actives.setActivePlot(plot);
        e.getPlayer().sendMessage(net.jmhertlein.core.chat.ChatUtil.INFO + "Active set is now: " + actives);
    }
}
