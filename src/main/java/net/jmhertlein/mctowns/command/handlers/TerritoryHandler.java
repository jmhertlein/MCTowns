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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import static net.jmhertlein.core.chat.ChatUtil.*;
import net.jmhertlein.core.command.ECommand;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.structure.MCTownsRegion;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.TownLevel;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

/**
 * @author Everdras
 */
public class TerritoryHandler extends CommandHandler {

    public TerritoryHandler(MCTownsPlugin parent) {
        super(parent);
    }

    public void addPlotToTerritory(String plotName) {
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

        Territory parTerr = localSender.getActiveTerritory();
        if (parTerr == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }

        plotName = MCTownsRegion.formatRegionName(t, TownLevel.PLOT, plotName);

        World w = localSender.getPlayer().getWorld();

        ProtectedRegion region = getSelectedRegion(plotName);

        if (region == null) {
            localSender.sendMessage(ERR + "You need to make a WorldEdit selection first.");
            return;
        }

        if (!selectionIsWithinParent(region, localSender.getActiveTerritory())) {
            localSender.sendMessage(ERR + "Selection is not in territory!");
            return;
        }
        if (townManager.addPlot(plotName, w, region, t, parTerr)) {
            localSender.sendMessage(SUCC + "Plot added.");
        } else {
            localSender.sendMessage(ERR + "A region by that name already exists, please pick a different name.");
        }

        boolean autoActive = !cmd.hasFlag(ECommand.DISABLE_AUTOACTIVE);
        if (autoActive) {
            localSender.setActivePlot(townManager.getPlot(plotName));
            localSender.sendMessage(INFO + "Active plot set to newly created plot.");
        }

    }

    public void removePlotFromTerritory(String plotName) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Territory t = localSender.getActiveTerritory();

        if (t == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }

        if (!townManager.removePlot(plotName)) {
            localSender.sendMessage(ERR + "That plot doesn't exist. Make sure you're using the full name of the plot (townname_plot_plotshortname).");
            return;
        }

        localSender.sendMessage(SUCC + "Plot removed.");
    }

    public void addPlayerToTerritory(String playerName) {
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

        Territory territ = localSender.getActiveTerritory();
        if (territ == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }

        OfflinePlayer player = server.getOfflinePlayer(playerName);
        if (!player.hasPlayedBefore()) {
            localSender.sendMessage(ERR + playerName + " has never played on this server before.");
            return;
        }

        if (!t.playerIsResident(player)) {
            localSender.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        if (territ.addPlayer(playerName)) {
            localSender.sendMessage("Player added to territory.");
        } else {
            localSender.sendMessage(ERR + "That player is already in that territory.");
        }
    }

    public void removePlayerFromTerritory(String playerName) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        boolean recursive = cmd.hasFlag(ECommand.RECURSIVE);

        Territory territ = localSender.getActiveTerritory();

        if (territ == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }

        OfflinePlayer player = server.getOfflinePlayer(playerName);
        if (!player.hasPlayedBefore()) {
            localSender.sendMessage(ERR + playerName + " has never played on this server before.");
            return;
        }

        if (!territ.removePlayer(playerName)) {
            localSender.sendMessage(ERR + "That player is not in this territory.");
        } else {
            localSender.sendMessage(SUCC + "Player removed from territory.");
        }

        if (recursive) {
            localSender.sendMessage(INFO + "Recursive mode was requested. Removing from child plots...");
            for (String plotName : territ.getPlotsCollection()) {
                if (townManager.getPlot(plotName).removePlayer(player)) {
                    localSender.sendMessage(INFO + "Player removed from " + plotName);
                }
            }
        }
    }

    public void setActiveTerritory(String territName) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        Town t = localSender.getActiveTown();

        if (t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        Territory nuActive = townManager.getTerritory(territName);

        if (nuActive == null) {
            nuActive = townManager.getTerritory(MCTownsRegion.formatRegionName(t, TownLevel.TERRITORY, territName));
        }

        if (nuActive == null) {
            localSender.sendMessage(ERR + "The territory \"" + territName + "\" does not exist.");
            return;
        }

        if (!nuActive.getParentTown().equals(t.getTownName())) {
            localSender.sendMessage(ERR + "The territory \"" + territName + "\" does not exist in your town.");
            return;
        }

        localSender.setActiveTerritory(nuActive);
        localSender.sendMessage("Active territory set to " + nuActive.getName());
    }

    private void listPlots(int page) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        page--; //shift to 0-indexing

        if (page < 0) {
            localSender.sendMessage(ERR + "Invalid page.");
            return;
        }

        Territory t = localSender.getActiveTerritory();

        if (t == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }
        localSender.sendMessage(ChatColor.AQUA + "Existing plots (page " + page + "):");

        String[] plots = t.getPlotsCollection().toArray(new String[t.getPlotsCollection().size()]);

        for (int i = page * RESULTS_PER_PAGE; i < plots.length && i < page * RESULTS_PER_PAGE + RESULTS_PER_PAGE; i++) {
            localSender.sendMessage(ChatColor.YELLOW + plots[i]);
        }
    }

    public void listPlots(String s_page) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        int page;
        try {
            page = Integer.parseInt(s_page);
        } catch (NumberFormatException nfex) {
            localSender.sendMessage(ERR + "Error parsing integer argument. Found \"" + s_page + "\", expected integer.");
            return;
        }

        listPlots(page);
    }

    public void listPlots() {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        listPlots(1);
    }
}
