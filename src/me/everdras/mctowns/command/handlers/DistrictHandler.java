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
import me.everdras.core.chat.ChatUtil;
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

    public DistrictHandler(MCTowns parent) {
        super(parent);
    }

    private void listPlots(int page) {
        page--; //shift to 0-indexing
        
        if(page < 0) {
            senderWrapper.sendMessage(ERR + "Invalid page.");
            return;
        }

        District d = senderWrapper.getActiveDistrict();

        if (d == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }
        senderWrapper.sendMessage(ChatColor.AQUA + "Existing districts (page " + page + "):");



        Plot[] plots = d.getPlotsCollection().toArray(new Plot[d.getPlotsCollection().size()]);

        if (cmd.hasFlag(ECommand.VERBOSE)) {
            String[][] rawOutputTable = new String[plots.length+1][4];
            rawOutputTable[0][0] = "Plot Name";
            rawOutputTable[0][1] = "Plot Owner(s)";
            rawOutputTable[0][2] = "Plot's for Sale:";
            rawOutputTable[0][3] = "Plot Member(s)";
            int index = 1;
            for(Plot p : plots) {
                rawOutputTable[index][0] = p.getAbstractName(); //plot name
                //All the owners of the plot
                rawOutputTable[index][1] = wgp.getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName()).getOwners().toUserFriendlyString();
                rawOutputTable[index][2] = p.isForSale() ? "True" : "False";
                //All members of the plot
                rawOutputTable[index][3] = wgp.getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName()).getMembers().toUserFriendlyString();
                index++;
            }
            
            String[] formattedOutputTable = ChatUtil.formatStringsForColumns(rawOutputTable, true);
            
            senderWrapper.sendMessage(ChatColor.AQUA + formattedOutputTable[0]); //header row
            senderWrapper.sendMessage(ChatColor.DARK_AQUA + formattedOutputTable[1]); //seperator = signs
            for(int i = 2 + (page * RESULTS_PER_PAGE); i < formattedOutputTable.length && i < (page*RESULTS_PER_PAGE) + RESULTS_PER_PAGE; i++) {
                senderWrapper.sendMessage(ChatColor.YELLOW + formattedOutputTable[i]);
            }
            
        } else {
            for (int i = (page*RESULTS_PER_PAGE); i < plots.length && i < (page*RESULTS_PER_PAGE) + RESULTS_PER_PAGE; i++) {
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

        TownManager.unregisterPlotFromWorldGuard(wgp, removeMe);

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
