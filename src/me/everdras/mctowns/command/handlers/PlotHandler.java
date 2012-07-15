/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package me.everdras.mctowns.command.handlers;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import static me.everdras.core.chat.ChatUtil.ERR;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.structure.District;
import me.everdras.mctowns.structure.Plot;
import me.everdras.mctowns.structure.Territory;
import me.everdras.mctowns.structure.Town;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author Everdras
 */
public class PlotHandler extends CommandHandler {

    public PlotHandler(MCTowns parent) {
        super(parent);
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
        p.buildSign(server);
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

        me.everdras.core.location.Location mctLoc;

        Player player = senderWrapper.getPlayer();

        mctLoc = me.everdras.core.location.Location.convertFromBukkitLocation(player.getTargetBlock(null, 5).getLocation());

        if (mctLoc == null) {
            senderWrapper.sendMessage(ERR + "Couldn't get the location you're looking at.");
            return;
        }

        //use the block ABOVE the one the player is staring at.
        mctLoc.setY(mctLoc.getY() + 1);
        p.demolishSign(server);
        p.setSignLoc(mctLoc);
        p.buildSign(server);

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

    public void setActivePlot(String plotName) {
        Town t = senderWrapper.getActiveTown();

        boolean quickSelect = cmd.hasFlag("-q");

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
