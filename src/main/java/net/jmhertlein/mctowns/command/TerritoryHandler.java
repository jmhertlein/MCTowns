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
package net.jmhertlein.mctowns.command;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import static net.jmhertlein.core.chat.ChatUtil.*;
import net.jmhertlein.abcf.CommandDefinition;
import net.jmhertlein.abcf.annotation.CommandMethod;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.structure.MCTownsRegion;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.TownLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

/**
 * @author Everdras
 */
public class TerritoryHandler extends CommandHandler implements CommandDefinition {

    public TerritoryHandler(MCTownsPlugin parent) {
        super(parent);
    }

    @CommandMethod(path = "territory add plot", requiredArgs = 1)
    public void addPlotToTerritory(CommandSender s, String[] args) {
        setNewCommand(s);
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

        String plotName = MCTownsRegion.formatRegionName(t, TownLevel.PLOT, args[0]);

        World w = localSender.getPlayer().getWorld();

        if (w == null) {
            localSender.sendMessage(
                    ERR + "You are in an invalid World. (Player::getWorld() returned null)");
            return;
        }

        ProtectedRegion region = getSelectedRegion(plotName);

        if (region == null) {
            localSender.sendMessage(
                    ERR + "You need to make a WorldEdit selection first.");
            return;
        }

        if (!selectionIsWithinParent(region, localSender.getActiveTerritory())) {
            localSender.sendMessage(ERR + "Selection is not in territory!");
            return;
        }

        try {
            townManager.addPlot(plotName, w, region, t, parTerr);
        } catch (TownManager.InvalidWorldGuardRegionNameException | TownManager.RegionAlreadyExistsException ex) {
            localSender.sendMessage(ERR + ex.getLocalizedMessage());
            return;
        }

        localSender.sendMessage(SUCC + "Plot added.");

        localSender.setActivePlot(townManager.getPlot(plotName));
        localSender.sendMessage(INFO + "Active plot set to newly created plot.");

    }

    @CommandMethod(path = "territory remove plot", requiredArgs = 1)
    public void removePlotFromTerritory(CommandSender s, String[] args) {
        setNewCommand(s);
        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Territory t = localSender.getActiveTerritory();

        if (t == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }

        if (!townManager.removePlot(args[0])) {
            localSender.sendMessage(
                    ERR + "That plot doesn't exist. Make sure you're using the full name of the plot (townname_plot_plotshortname).");
            return;
        }

        localSender.sendMessage(SUCC + "Plot removed.");
    }

    @CommandMethod(path = "territory add player", requiredArgs = 1)
    public void addPlayerToTerritory(CommandSender s, String[] args) {
        setNewCommand(s);
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

        OfflinePlayer player = server.getOfflinePlayer(args[0]);
        if (!player.hasPlayedBefore()) {
            localSender.sendMessage(
                    ERR + args[0] + " has never played on this server before.");
            return;
        }

        if (!t.playerIsResident(player)) {
            localSender.sendMessage(
                    ERR + "That player is not a member of the town.");
            return;
        }

        if (territ.addPlayer(Bukkit.getOfflinePlayer(args[0]))) {
            localSender.sendMessage("Player added to territory.");
        } else {
            localSender.sendMessage(
                    ERR + "Unable to add player to territory. Either they are already in it, or the underlying World or WorldGuard Region has been deleted.");
        }
    }

    @CommandMethod(path = "territory remove player", requiredArgs = 1)
    public void removePlayerFromTerritory(CommandSender s, String[] args) {
        setNewCommand(s);
        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        boolean recursive = true; // TODO: come up with a way to allow recursive flag again

        Territory territ = localSender.getActiveTerritory();

        if (territ == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }

        OfflinePlayer player = server.getOfflinePlayer(args[0]);
        if (!player.hasPlayedBefore()) {
            localSender.sendMessage(
                    ERR + args[0] + " has never played on this server before.");
            return;
        }

        if (!territ.removePlayer(Bukkit.getOfflinePlayer(args[0]))) {
            localSender.sendMessage(
                    ERR + "Unable to remove player from territory. Either they were not in it in the first place, or the underlying World or WorldGuard Region has been deleted.");
        } else {
            localSender.sendMessage(SUCC + "Player removed from territory.");
        }

        if (recursive) {
            localSender.sendMessage(
                    INFO + "Recursive mode was requested. Removing from child plots...");
            for (String plotName : territ.getPlotsCollection()) {
                if (townManager.getPlot(plotName).removePlayer(player)) {
                    localSender.sendMessage(
                            INFO + "Player removed from " + plotName);
                }
            }
        }
    }

    @CommandMethod(path = "territory active", requiredArgs = 1)
    public void setActiveTerritory(CommandSender s, String[] args) {
        setNewCommand(s);
        Town t = localSender.getActiveTown();

        if (t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        Territory nuActive = townManager.getTerritory(args[0]);

        if (nuActive == null) {
            nuActive = townManager.getTerritory(MCTownsRegion.formatRegionName(t, TownLevel.TERRITORY, args[0]));
        }

        if (nuActive == null) {
            localSender.sendMessage(
                    ERR + "The territory \"" + args[0] + "\" does not exist.");
            return;
        }

        if (!nuActive.getParentTown().equals(t.getTownName())) {
            localSender.sendMessage(
                    ERR + "The territory \"" + args[0] + "\" does not exist in your town.");
            return;
        }

        localSender.setActiveTerritory(nuActive);
        localSender.sendMessage("Active territory set to " + nuActive.getName());
    }

    private void listPlots(int page) {
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
        localSender.sendMessage(
                ChatColor.AQUA + "Existing plots (page " + page + "):");

        String[] plots = t.getPlotsCollection().toArray(
                new String[t.getPlotsCollection().size()]);

        for (int i = page * RESULTS_PER_PAGE; i < plots.length && i < page * RESULTS_PER_PAGE + RESULTS_PER_PAGE; i++) {
            localSender.sendMessage(ChatColor.YELLOW + plots[i]);
        }
    }

    @CommandMethod(path = "territory list plots")
    public void listPlots(CommandSender s, String[] args) {
        setNewCommand(s);
        int page;
        try {
            if(args.length > 0)
                page = Integer.parseInt(args[0]);
            else
                page = 1;
        } catch (NumberFormatException nfex) {
            localSender.sendMessage(
                    ERR + "Error parsing integer argument. Found \"" + args[0] + "\", expected integer.");
            return;
        }

        listPlots(page);
    }
}
