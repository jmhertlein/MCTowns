/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package me.everdras.mctowns.command.handlers;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import java.util.HashMap;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.command.MCTCommand;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.structure.*;
import me.everdras.mctowns.townjoin.TownJoinManager;
import me.everdras.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Everdras
 */
public class PlotHandler extends CommandHandler {

    public PlotHandler(MCTowns parent, TownManager t, TownJoinManager j, CommandSender p, HashMap<String, ActiveSet> activeSets, WorldGuardPlugin wg, Economy econ, Config opt, MCTCommand cmd) {
        super(parent, t, j, p, activeSets, wg, econ, opt, cmd);
    }


    public void printPlotInfo() {
        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }
        ChatColor c = ChatColor.AQUA;
        senderWrapper.sendMessage(c + "Plot name: " + p.getAbstractName());
        senderWrapper.sendMessage(c + "Corresponding WG Region name: " + p.getName());
        senderWrapper.sendMessage(c + "World name: " + p.getWorldName());
        senderWrapper.sendMessage(c + "Plot is for sale: " + p.isForSale());
        senderWrapper.sendMessage(c + "Plot price: " + p.getPrice());

    }

    public void movePlotInTown() {
        //Pushed off to post-1.0
    }

    public void removePlayerFromPlot(String player) {
        Plot p = senderWrapper.getActivePlot();
        player = player.toLowerCase();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        RegionManager regMan = wgp.getRegionManager(server.getWorld(p.getWorldName()));

        ProtectedRegion wg_plot = regMan.getRegion(p.getName());

        //if they are neither mayor nor owner
        if (!(senderWrapper.hasMayoralPermissions() || wg_plot.getOwners().contains(wgp.wrapPlayer(senderWrapper.getPlayer())))) {
            senderWrapper.notifyInsufPermissions();
            return;
        }



        if (p.removePlayerFromWGRegion(wgp, player)) {
            senderWrapper.sendMessage("Player removed from plot.");
        } else {
            senderWrapper.sendMessage(ERR + player + " is not a member of this region.");
        }



    }

    public void addPlayerToPlot(String playerName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Plot p = senderWrapper.getActivePlot();
        Player player = server.getPlayer(playerName);

        if (!senderWrapper.getActiveTown().playerIsResident(player)) {
            senderWrapper.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        if (player == null) {
            senderWrapper.sendMessage(ERR + playerName + " is not online. Make sure you typed their name correctly!");
        }

        if (p.addPlayerToWGRegion(wgp, playerName)) {
            senderWrapper.sendMessage("Player added to plot.");
        } else {
            senderWrapper.sendMessage(ERR + "That player is already in that plot.");
        }

    }

    public void addPlayerToPlotAsGuest(String playername) {
        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        RegionManager regMan = wgp.getRegionManager(server.getWorld(p.getWorldName()));

        ProtectedRegion wg_plot = regMan.getRegion(p.getName());

        //if they are neither mayor nor owner
        if (!(senderWrapper.hasMayoralPermissions() || wg_plot.getOwners().contains(wgp.wrapPlayer(senderWrapper.getPlayer())))) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (server.getPlayer(playername) == null) {
            senderWrapper.sendMessage(ChatColor.GOLD + "The player " + playername + " is not online! Make sure their name is spelled correctly!");
        }

        wg_plot.getMembers().addPlayer(playername);

        senderWrapper.sendMessage(ChatColor.GREEN + "Successfully added " + playername + " to the plot as a guest.");
    }

    public void setPlotBuyability(String s_forSale) {

        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        if (!t.usesBuyablePlots()) {
            senderWrapper.sendMessage(ERR + t.getTownName() + " does not allow the sale of plots.");
            return;
        }

        boolean forSale;
        try {
            forSale = Boolean.parseBoolean(s_forSale);
        } catch (Exception e) {
            senderWrapper.sendMessage(ERR + "Error parsing boolean on token: " + s_forSale);
            return;
        }



        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        p.setForSale(forSale);
        senderWrapper.sendMessage(ChatColor.GREEN + "The plot " + p.getName() + " is " + (forSale ? "now" : "no longer") + " for sale!");

    }

    public void setPlotPrice(String s_price) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        BigDecimal price;

        try {
            price = new BigDecimal(s_price);
        } catch (Exception e) {
            senderWrapper.sendMessage(ERR + "Error parsing float on token: " + s_price);
            return;
        }

        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        p.setPrice(price);
        senderWrapper.sendMessage(ChatColor.GREEN + "Price of " + p.getName() + " set to " + p.getPrice() + ".");
    }

    public void buildSign() {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (!options.isEconomyEnabled()) {
            senderWrapper.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        p.buildSign(server);
        senderWrapper.sendMessage("Sign built!");
    }

    public void demolishSign() {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (!options.isEconomyEnabled()) {
            senderWrapper.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        p.demolishSign(server);
        senderWrapper.sendMessage("Sign demolished.");
    }

    public void setPlotSignPosition() {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        me.everdras.mctowns.structure.Location mctLoc;

        Player player = senderWrapper.getPlayer();

        mctLoc = me.everdras.mctowns.structure.Location.convertFromBukkitLocation(player.getTargetBlock(null, 5).getLocation());

        if (mctLoc == null) {
            senderWrapper.sendMessage(ERR + "Couldn't get the location you're looking at.");
            return;
        }

        //use the block ABOVE the one the player is staring at.
        mctLoc.setY(mctLoc.getY() + 1);

        p.setSignLoc(mctLoc);

        senderWrapper.sendMessage(ChatColor.GREEN + " successfully set the location for the sign.");


    }

    public void surrenderPlot() {
        Plot p = senderWrapper.getActivePlot();
        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        ProtectedRegion reg = wgp.getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName());

        if (!reg.isOwner(wgp.wrapPlayer(senderWrapper.getPlayer()))) {
            senderWrapper.sendMessage(ERR + "You don't own this plot, so you can't surrender it!");
            return;
        }

        reg.getOwners().removePlayer(senderWrapper.getPlayer().getName());

        for (String name : reg.getMembers().getPlayers()) {
            reg.getMembers().removePlayer(name);
        }



        p.setForSale(false);
        p.setPrice(BigDecimal.ZERO);


    }

    public void confirmPlotPurchase(HashMap<Player, ActiveSet> buyers) {
        if (!options.isEconomyEnabled()) {
            senderWrapper.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        ActiveSet plotToBuy = buyers.get(senderWrapper.getPlayer());

        if (plotToBuy == null) {
            senderWrapper.sendMessage(ERR + "You haven't selected a plot to buy yet.");
            return;
        }

        if (townManager.playerIsAlreadyInATown(senderWrapper.getPlayer())) {
            if (!plotToBuy.getActiveTown().equals(townManager.matchPlayerToTown(senderWrapper.getPlayer()))) {
                senderWrapper.sendMessage(ERR + "You're already in a different town.");
                return;
            }
        }

        if (!plotToBuy.getActiveTown().playerIsResident(senderWrapper.getPlayer())) {
            if (!plotToBuy.getActiveTown().usesEconomyJoins()) {
                senderWrapper.sendMessage(ERR + "You aren't a member of this town.");
                return;
            }
        }

        if (!plotToBuy.getActiveTown().usesBuyablePlots()) {
            senderWrapper.sendMessage(ERR + "This town's plots aren't buyable.");
            return;
        }

        Plot p = plotToBuy.getActivePlot();

        if (!p.isForSale()) {
            senderWrapper.sendMessage(ERR + "This plot isn't for sale.");
            return;
        }

        if (!economy.withdrawPlayer(senderWrapper.getPlayer().getName(), p.getPrice().floatValue()).transactionSuccess()) {
            senderWrapper.sendMessage(ERR + "Insufficient funds.");
            return;
        }

        plotToBuy.getActiveTown().getBank().depositCurrency(p.getPrice());

        p.setPrice(BigDecimal.ZERO);
        p.setForSale(false);
        ProtectedRegion plotReg = wgp.getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName());
        p.demolishSign(server);

        plotReg.getOwners().addPlayer(senderWrapper.getPlayer().getName());

        senderWrapper.sendMessage(ChatColor.GREEN + "You are now the proud owner of this plot.");
        doRegManSave(wgp.getRegionManager(server.getWorld(p.getWorldName())));


        if (!townManager.playerIsAlreadyInATown(senderWrapper.getPlayer())) {
            plotToBuy.getActiveTown().addPlayer(senderWrapper.getPlayer());
            senderWrapper.sendMessage(ChatColor.GREEN + "You have joined the town " + plotToBuy.getActiveTown().getTownName());
        }


    }

    public void setActivePlot(String plotName, boolean quickSelect) {
        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        Plot nuActive = null;

        if (!quickSelect) {
            Territory te = senderWrapper.getActiveTerritory();

            if (te == null) {
                senderWrapper.notifyActiveTerritoryNotSet();
                return;
            }

            District d = senderWrapper.getActiveDistrict();

            if (d == null) {
                senderWrapper.notifyActiveDistrictNotSet();
            }



            nuActive = d.getPlot(plotName);

            if (nuActive == null) {
                nuActive = d.getPlot((t.getTownName() + PLOT_INFIX + plotName).toLowerCase());
            }

            if (nuActive == null) {
                senderWrapper.sendMessage(ERR + "The plot \"" + plotName + "\" does not exist.");
                return;
            }
        } else {
            plotName = t.getTownName() + PLOT_INFIX + plotName;
            plotName = plotName.toLowerCase();

            territloop:
            for (Territory territ : t.getTerritoriesCollection()) {
                for (District dist : territ.getDistrictsCollection()) {
                    if (dist.getPlot(plotName) != null) {
                        nuActive = dist.getPlot(plotName);
                        senderWrapper.setActiveDistrict(dist);
                        senderWrapper.setActiveTerritory(territ);
                        break territloop;
                    }
                }
            }

            if (nuActive == null) {
                senderWrapper.sendMessage(ERR + "The plot \"" + plotName + "\" does not exist.");
                return;
            }
        }

        senderWrapper.setActivePlot(nuActive);
        senderWrapper.sendMessage("Active plot set to " + nuActive.getName());
    }

}
