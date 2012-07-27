/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command.handlers;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static me.everdras.core.chat.ChatUtil.*;
import me.everdras.core.command.ECommand;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.structure.Plot;
import me.everdras.mctowns.structure.Territory;
import me.everdras.mctowns.structure.Town;
import me.everdras.mctowns.townjoin.TownJoinManager;
import me.everdras.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Everdras
 */
public class TerritoryHandler extends CommandHandler {

    public TerritoryHandler(MCTowns parent, TownManager t, TownJoinManager j, CommandSender p, HashMap<String, ActiveSet> activeSets, WorldGuardPlugin wg, Economy econ, Config opt, ECommand cmd) {
        super(parent, t, j, p, activeSets, wg, econ, opt, cmd);
    }

    public void addPlotToTerritory(String plotName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        boolean autoActive = !cmd.hasFlag(ECommand.DISABLE_AUTOACTIVE);

        plotName = senderWrapper.getActiveTown().getTownName() + PLOT_INFIX + plotName;

        String worldName = senderWrapper.getActiveTown().getWorldName();
        Plot p = new Plot(plotName, worldName);
        Territory parTerr = senderWrapper.getActiveTerritory();

        if (parTerr == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }



        ProtectedRegion region = getSelectedRegion(p.getName());

        if (region == null) {
            return;
        }

        if (!this.selectionIsWithinParent(region, senderWrapper.getActiveTerritory())) {
            senderWrapper.sendMessage(ERR + "Selection is not in territory!");
            return;
        }


        ProtectedRegion parent = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(senderWrapper.getActiveTerritory().getName());
        try {
            region.setParent(parent);
        } catch (ProtectedRegion.CircularInheritanceException ex) {
            Logger.getLogger("Minecraft").log(Level.WARNING, "Circular Inheritence in addPlotToTerritory.");
        }
        RegionManager regMan = wgp.getRegionManager(wgp.getServer().getWorld(worldName));

        if (regMan.hasRegion(plotName)) {
            senderWrapper.sendMessage(ERR + "That name is already in use. Please pick a different one.");
            return;
        }

        regMan.addRegion(region);

        parTerr.addPlot(p);

        senderWrapper.sendMessage("Plot added.");

        doRegManSave(regMan);

        if (autoActive) {
            senderWrapper.setActivePlot(p);
            senderWrapper.sendMessage(INFO + "Active plot set to newly created plot.");

        }

    }

    public void removePlotFromTerritory(String plotName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Territory t = senderWrapper.getActiveTerritory();

        if (t == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }

        Plot removeMe = t.getPlot(plotName);

        if (removeMe == null) {
            senderWrapper.sendMessage(ERR + "That plot doesn't exist. Make sure you're using the full name of the plot (townname_plot_plotshortname).");
        }

        t.removePlot(plotName);

        TownManager.unregisterPlotFromWorldGuard(wgp, removeMe);
        senderWrapper.sendMessage(SUCC + "Plot removed.");
    }

    public void addPlayerToTerritory(String playerName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Territory territ = senderWrapper.getActiveTerritory();
        Player player = server.getPlayer(playerName);

        if (player == null) {
            senderWrapper.sendMessage(ChatColor.YELLOW + playerName + " is not online. Make sure you typed their name correctly!");
        }

        if (!senderWrapper.getActiveTown().playerIsResident(player)) {
            senderWrapper.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        if (territ == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }

        if (territ.addPlayerToWGRegion(wgp, playerName)) {
            senderWrapper.sendMessage("Player added to territory.");
        } else {
            senderWrapper.sendMessage(ERR + "That player is already in that territory.");
        }
    }

    public void removePlayerFromTerritory(String player) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        boolean recursive = cmd.hasFlag(ECommand.RECURSIVE);

        Territory territ = senderWrapper.getActiveTerritory();

        if (territ == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }

        if (player == null) {
            senderWrapper.sendMessage(ERR + "That player is not online.");
            return;
        }

        if (recursive) {
            if (!territ.removePlayerFromWGRegion(wgp, player)) {
                senderWrapper.sendMessage(ERR + "That player is not in this territory.");
                return;
            }

            for (Plot p : territ.getPlotsCollection()) {
                p.removePlayerFromWGRegion(wgp, player);
            }

            senderWrapper.sendMessage("Player removed from territory.");

        } else {
            if (!territ.removePlayerFromWGRegion(wgp, player)) {
                senderWrapper.sendMessage(ERR + "That player is not in this territory.");
                return;
            }
            senderWrapper.sendMessage("Player removed from territory.");
        }
    }

    public void setActiveTerritory(String territName) {
        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }



        Territory nuActive = t.getTerritory(territName);

        if (nuActive == null) {
            nuActive = t.getTerritory((t.getTownName() + TERRITORY_INFIX + territName).toLowerCase());
        }

        if (nuActive == null) {
            senderWrapper.sendMessage(ERR + "The territory \"" + territName + "\" does not exist.");
            return;
        }

        senderWrapper.setActiveTerritory(nuActive);
        senderWrapper.sendMessage("Active territory set to " + nuActive.getName());
    }

    private void listPlots(int page) {
        page--; //shift to 0-indexing

        if (page < 0) {
            senderWrapper.sendMessage(ERR + "Invalid page.");
            return;
        }

        Territory t = senderWrapper.getActiveTerritory();

        if (t == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }
        senderWrapper.sendMessage(ChatColor.AQUA + "Existing plots (page " + page + "):");



        Plot[] plots = t.getPlotsCollection().toArray(new Plot[t.getPlotsCollection().size()]);

        for (int i = page * RESULTS_PER_PAGE; i < plots.length && i < page * RESULTS_PER_PAGE + RESULTS_PER_PAGE; i++) {
            senderWrapper.sendMessage(ChatColor.YELLOW + plots[i].getName());
        }
    }

    public void listPlots(String s_page) {
        int page;
        try {
            page = Integer.parseInt(s_page);
        } catch (NumberFormatException nfex) {
            senderWrapper.sendMessage(ERR + "Error parsing integer argument. Found \"" + s_page + "\", expected integer.");
            return;
        }

        listPlots(page);
    }

    public void listPlots() {
        listPlots(1);
    }
}
