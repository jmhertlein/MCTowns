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
package net.jmhertlein.mctowns.command.handlers;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import java.util.Set;
import static net.jmhertlein.core.chat.ChatUtil.ERR;
import static net.jmhertlein.core.chat.ChatUtil.SUCC;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.structure.MCTownsRegion;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.TownLevel;
import net.jmhertlein.mctowns.util.MCTConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * @author Everdras
 */
public class PlotHandler extends CommandHandler {

    public PlotHandler(MCTownsPlugin parent) {
        super(parent);
    }

    public void printPlotInfo() {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        Plot p = localSender.getActivePlot();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }
        ChatColor c = ChatColor.AQUA;
        localSender.sendMessage(c + "Plot name: " + p.getTerseName());
        localSender.sendMessage(c + "Corresponding WG Region name: " + p.getName());
        localSender.sendMessage(c + "World name: " + p.getWorldName());
        localSender.sendMessage(c + "Plot is for sale: " + p.isForSale());
        localSender.sendMessage(c + "Plot price: " + p.getPrice());

    }

    public void removePlayerFromPlot(String playerName) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        Plot plot = localSender.getActivePlot();

        if (plot == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(plot.getWorldName()));

        ProtectedRegion wgPlot = regMan.getRegion(plot.getName());

        //if they are neither mayor nor owner
        if (!(localSender.hasMayoralPermissions() || wgPlot.getOwners().contains(MCTowns.getWorldGuardPlugin().wrapPlayer(localSender.getPlayer())))) {
            localSender.notifyInsufPermissions();
            return;
        }

        if (plot.removePlayer(Bukkit.getOfflinePlayer(playerName)))
            localSender.sendMessage("Player removed from plot.");
        else
            localSender.sendMessage(ERR + "Unable to remove player from plot (either player is not in plot, world doesn't exist, or WorldGuard region doesn't exist.).");
    }

    public void addPlayerToPlot(String playerName) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Town t = localSender.getActiveTown();
        if (t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        Plot plot = localSender.getActivePlot();
        if (plot == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        OfflinePlayer player = server.getOfflinePlayer(playerName);
        if (!player.hasPlayedBefore()) {
            localSender.sendMessage(ERR + playerName + " has never played on this server.");
            return;
        }

        if (!t.playerIsResident(player)) {
            localSender.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        if (plot.addPlayer(player))
            localSender.sendMessage("Player added to plot.");
        else
            localSender.sendMessage(ERR + "Unable to add player to plot (either player is already in plot, world doesn't exist, or WorldGuard region doesn't exist.).");

    }

    public void addPlayerToPlotAsGuest(String playerName) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        Plot plot = localSender.getActivePlot();
        if (plot == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(plot.getWorldName()));

        ProtectedRegion wgPlot = regMan.getRegion(plot.getName());

        //if they are neither mayor nor owner
        if (!(localSender.hasMayoralPermissions() || wgPlot.getOwners().contains(MCTowns.getWorldGuardPlugin().wrapPlayer(localSender.getPlayer())))) {
            localSender.notifyInsufPermissions();
            return;
        }

        OfflinePlayer player = server.getOfflinePlayer(playerName);
        if (!player.hasPlayedBefore()) {
            localSender.sendMessage(ChatColor.GOLD + playerName + " has never played on this server.");
            return;
        }

        if (plot.addGuest(player))
            localSender.sendMessage(ChatColor.GREEN + "Successfully added " + player.getName() + " to the plot as a guest.");
        else
            localSender.sendMessage(ERR + "Unable to add player to plot as guest (either player is already guest in plot, world doesn't exist, or WorldGuard region doesn't exist.).");
    }

    public void setPlotBuyability(String s_forSale) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Town t = localSender.getActiveTown();

        if (t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if (!t.usesBuyablePlots()) {
            localSender.sendMessage(ERR + t.getTownName() + " does not allow the sale of plots.");
            return;
        }

        boolean forSale;
        try {
            forSale = Boolean.parseBoolean(s_forSale);
        } catch (Exception e) {
            localSender.sendMessage(ERR + "Error parsing boolean on token: " + s_forSale);
            return;
        }

        Plot p = localSender.getActivePlot();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        p.setForSale(forSale);
        localSender.sendMessage(ChatColor.GREEN + "The plot " + p.getName() + " is " + (forSale ? "now" : "no longer") + " for sale!");
    }

    public void setPlotPrice(String s_price) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        BigDecimal price;

        try {
            price = new BigDecimal(s_price);
        } catch (Exception e) {
            localSender.sendMessage(ERR + "Error parsing float on token: " + s_price);
            return;
        }

        Plot p = localSender.getActivePlot();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        p.setPrice(price);
        p.buildSign(localSender.getPlayer().getLocation());
        localSender.sendMessage(ChatColor.GREEN + "Price of " + p.getName() + " set to " + p.getPrice() + ".");
    }

    public void buildSign() {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        if (!MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Plot p = localSender.getActivePlot();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        if (p.buildSign(localSender.getPlayer().getLocation()))
            localSender.sendMessage(SUCC + "Sign built!");
        else
            localSender.sendMessage(ERR + "The sign wasn't built because its target location wasn't an air block. Please clear the spot and try again.");
    }

    public void demolishSign() {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        if (!MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Plot p = localSender.getActivePlot();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        p.demolishSign();
        localSender.sendMessage("Sign demolished.");
    }

    public void setPlotSignPosition() {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Plot p = localSender.getActivePlot();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        net.jmhertlein.core.location.Location mctLoc;

        Player player = localSender.getPlayer();

        mctLoc = net.jmhertlein.core.location.Location.convertFromBukkitLocation(player.getTargetBlock((Set<Material>) null, 5).getLocation());

        if (mctLoc == null) {
            localSender.sendMessage(ERR + "Couldn't get the location you're looking at.");
            return;
        }

        //use the block ABOVE the one the player is staring at.
        mctLoc.setY(mctLoc.getY() + 1);
        if (!cmd.hasFlag("--no-rebuild"))
            p.demolishSign();

        p.setSignLoc(mctLoc);

        if (!cmd.hasFlag("--no-rebuild"))
            p.buildSign(localSender.getPlayer().getLocation());

        localSender.sendMessage(ChatColor.GREEN + "Successfully set the location for the sign.");

    }

    public void surrenderPlot() {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        Plot p = localSender.getActivePlot();
        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        ProtectedRegion reg = MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName());

        if (!reg.isOwner(MCTowns.getWorldGuardPlugin().wrapPlayer(localSender.getPlayer()))) {
            localSender.sendMessage(ERR + "You don't own this plot, so you can't surrender it!");
            return;
        }

        reg.getOwners().removePlayer(localSender.getPlayer().getName());

        for (String name : reg.getMembers().getPlayers()) {
            reg.getMembers().removePlayer(name);
        }

        p.setForSale(false);
        p.setPrice(BigDecimal.ZERO);

    }

    public void setActivePlot(String plotName) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        Town t = localSender.getActiveTown();

        boolean quickSelect = cmd.hasFlag("-q");

        if (t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        Plot nuActive = null;
        Territory nuActiveTerrit = null;

        if (!quickSelect) {
            Territory te = localSender.getActiveTerritory();

            if (te == null) {
                localSender.notifyActiveTerritoryNotSet();
                return;
            }

            nuActive = townManager.getPlot(plotName);

            if (nuActive == null)
                nuActive = townManager.getPlot(MCTownsRegion.formatRegionName(t, TownLevel.PLOT, plotName));
        } else {
            plotName = MCTownsRegion.formatRegionName(t, TownLevel.PLOT, plotName);

            for (MCTownsRegion reg : townManager.getRegionsCollection()) {
                if (reg instanceof Territory) {
                    nuActiveTerrit = (Territory) reg;
                    if (nuActiveTerrit.getPlotsCollection().contains(plotName)) {
                        nuActive = townManager.getPlot(plotName);
                        break;
                    }
                }
            }
        }

        if (nuActive == null) {
            localSender.sendMessage(ERR + "The plot \"" + plotName + "\" does not exist.");
            return;
        }

        if (!nuActive.getParentTownName().equals(t.getTownName())) {
            localSender.sendMessage(ERR + "The plot \"" + plotName + "\" does not exist in your town.");
            return;
        }

        localSender.setActivePlot(nuActive);
        if (nuActiveTerrit != null)
            localSender.setActiveTerritory(nuActiveTerrit);
        localSender.sendMessage("Active plot set to " + nuActive.getName());
    }
}
