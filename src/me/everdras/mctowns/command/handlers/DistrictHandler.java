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
import static me.everdras.core.chat.ChatUtil.ERR;
import static me.everdras.core.chat.ChatUtil.SUCC;
import me.everdras.core.command.ECommand;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.structure.District;
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
public class DistrictHandler extends CommandHandler {

    public DistrictHandler(MCTowns parent, TownManager t, TownJoinManager j, CommandSender p, HashMap<String, ActiveSet> activeSets, WorldGuardPlugin wg, Economy econ, Config opt, ECommand cmd) {
        super(parent, t, j, p, activeSets, wg, econ, opt, cmd);
    }

    private void listPlots(int page) {

        District d = senderWrapper.getActiveDistrict();

        if (d == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }
        senderWrapper.sendMessage(ChatColor.AQUA + "Existing districts (page " + page + "):");



        Plot[] plots = d.getPlotsCollection().toArray(new Plot[d.getPlotsCollection().size()]);

        if (cmd.hasFlag(ECommand.VERBOSE)) {
            
        } else {
            for (int i = page - 1; i < plots.length && i < i + 5; i++) {
                senderWrapper.sendMessage(ChatColor.YELLOW + plots[i].getName());
            }
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

    public void addPlotToDistrict(String plotName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        boolean autoActive = !cmd.hasFlag(ECommand.DISABLE_AUTOACTIVE);

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        District d = senderWrapper.getActiveDistrict();


        if (d == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }

        String worldName = senderWrapper.getActiveTown().getWorldName();

        plotName = senderWrapper.getActiveTown().getTownName() + PLOT_INFIX + plotName;
        Plot p = new Plot(plotName, worldName);
        p.setPrice(t.getDefaultPlotPrice());

        ProtectedCuboidRegion plotRegion = getSelectedRegion(p.getName());

        if (plotRegion == null) {
            return;
        }

        if (!this.selectionIsWithinParent(plotRegion, d)) {
            senderWrapper.sendMessage(ERR + "Selection is not in your active district!");
            return;
        }


        ProtectedRegion parent = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(d.getName());
        try {
            plotRegion.setParent(parent);
        } catch (ProtectedRegion.CircularInheritanceException ex) {
            Logger.getLogger("Minecraft").log(Level.WARNING, "Circular Inheritence in addDistrictToTown.");
        }


        RegionManager regMan = wgp.getRegionManager(wgp.getServer().getWorld(worldName));
        if (regMan.hasRegion(plotName)) {
            senderWrapper.sendMessage(ERR + "That name is already in use. Please pick a different one.");
            return;
        }
        regMan.addRegion(plotRegion);
        d.addPlot(p);


        doRegManSave(regMan);
        senderWrapper.sendMessage("Plot added.");
        p.calculateSignLoc(wgp);

        if (autoActive) {
            senderWrapper.setActivePlot(p);
            senderWrapper.sendMessage(ChatColor.LIGHT_PURPLE + "Active plot set to newly created plot.");

        }

        if (options.isEconomyEnabled() && senderWrapper.getActiveTown().usesBuyablePlots()) {
            p.setForSale(true);

            if (!cmd.hasFlag(ECommand.NO_AUTOBUILD_PLOT_SIGN)) {
                p.buildSign(server);
            }
        }
    }

    public void removePlotFromDistrict(String plotName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }
        District d = senderWrapper.getActiveDistrict();

        if (d == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }

        Plot removeMe = d.getPlot(plotName);

        if (removeMe == null) {
            senderWrapper.sendMessage(ERR + "That plot doesn't exist. Make sure you're using the full name of the plot (townname_plot_plotshortname).");
        }

        d.removePlot(plotName);

        townManager.unregisterPlotFromWorldGuard(wgp, removeMe);

        senderWrapper.sendMessage(SUCC + "Plot removed.");
    }

    public void addPlayerToDistrict(String playerName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        District dist = senderWrapper.getActiveDistrict();
        Player player = server.getPlayer(playerName);



        if (dist == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }

        if (player == null) {
            senderWrapper.sendMessage(ChatColor.YELLOW + playerName + " is not online. Make sure you typed their name correctly!");
        }

        if (!senderWrapper.getActiveTown().playerIsResident(playerName)) {
            senderWrapper.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        if (dist.addPlayerToWGRegion(wgp, playerName)) {
            senderWrapper.sendMessage("Player added to district.");
        } else {
            senderWrapper.sendMessage(ERR + "That player is already in that district.");
        }
    }

    public void removePlayerFromDistrict(String player) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        boolean recursive = cmd.hasFlag(ECommand.RECURSIVE);

        District dist = senderWrapper.getActiveDistrict();

        if (dist == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }

        if (player == null) {
            senderWrapper.sendMessage(ERR + "That player is not online.");
            return;
        }

        if (recursive) {
            if (!dist.removePlayerFromWGRegion(wgp, player)) {
                senderWrapper.sendMessage(ERR + "That player is not in that district.");
                return;
            }

            for (Plot p : dist.getPlotsCollection()) {
                p.removePlayerFromWGRegion(wgp, player);
            }

            senderWrapper.sendMessage("Player removed from district.");

        } else {
            if (dist.removePlayerFromWGRegion(wgp, player)) {
                senderWrapper.sendMessage("Player removed from district.");
            } else {
                senderWrapper.sendMessage(ERR + "That player is not in that district.");
            }
        }
    }

    public void setActiveDistrict(String distName) {

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        Territory te = senderWrapper.getActiveTerritory();

        if (te == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }



        District nuActive = te.getDistrict(distName);

        if (nuActive == null) {
            nuActive = te.getDistrict((t.getTownName() + DISTRICT_INFIX + distName).toLowerCase());
        }

        if (nuActive == null) {
            senderWrapper.sendMessage(ERR + "The district \"" + distName + "\" does not exist.");
            return;
        }

        senderWrapper.setActiveDistrict(nuActive);
        senderWrapper.sendMessage("Active district set to " + nuActive.getName());
    }
}
