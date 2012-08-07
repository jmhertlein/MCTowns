/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.command.handlers;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import static net.jmhertlein.core.chat.ChatUtil.ERR;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.TownLevel;
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
        Plot p = localSender.getActivePlot();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }
        ChatColor c = ChatColor.AQUA;
        localSender.sendMessage(c + "Plot name: " + p.getAbstractName());
        localSender.sendMessage(c + "Corresponding WG Region name: " + p.getName());
        localSender.sendMessage(c + "World name: " + p.getWorldName());
        localSender.sendMessage(c + "Plot is for sale: " + p.isForSale());
        localSender.sendMessage(c + "Plot price: " + p.getPrice());

    }

    public void movePlotInTown() {
        //Pushed off to post-1.0
    }

    public void removePlayerFromPlot(String player) {
        Plot p = localSender.getActivePlot();
        player = player.toLowerCase();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        RegionManager regMan = wgp.getRegionManager(server.getWorld(p.getWorldName()));

        ProtectedRegion wg_plot = regMan.getRegion(p.getName());

        //if they are neither mayor nor owner
        if (!(localSender.hasMayoralPermissions() || wg_plot.getOwners().contains(wgp.wrapPlayer(localSender.getPlayer())))) {
            localSender.notifyInsufPermissions();
            return;
        }



        if (p.removePlayer(player)) {
            localSender.sendMessage("Player removed from plot.");
        } else {
            localSender.sendMessage(ERR + player + " is not a member of this region.");
        }



    }

    public void addPlayerToPlot(String playerName) {
        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Plot p = localSender.getActivePlot();
        Player player = server.getPlayer(playerName);

        if (!localSender.getActiveTown().playerIsResident(player)) {
            localSender.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        if (player == null) {
            localSender.sendMessage(ERR + playerName + " is not online. Make sure you typed their name correctly!");
        }

        if (p.addPlayer(playerName)) {
            localSender.sendMessage("Player added to plot.");
        } else {
            localSender.sendMessage(ERR + "That player is already in that plot.");
        }

    }

    public void addPlayerToPlotAsGuest(String playername) {
        Plot p = localSender.getActivePlot();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        RegionManager regMan = wgp.getRegionManager(server.getWorld(p.getWorldName()));

        ProtectedRegion wg_plot = regMan.getRegion(p.getName());

        //if they are neither mayor nor owner
        if (!(localSender.hasMayoralPermissions() || wg_plot.getOwners().contains(wgp.wrapPlayer(localSender.getPlayer())))) {
            localSender.notifyInsufPermissions();
            return;
        }

        if (server.getPlayer(playername) == null) {
            localSender.sendMessage(ChatColor.GOLD + "The player " + playername + " is not online! Make sure their name is spelled correctly!");
        }

        wg_plot.getMembers().addPlayer(playername);

        localSender.sendMessage(ChatColor.GREEN + "Successfully added " + playername + " to the plot as a guest.");
    }

    public void setPlotBuyability(String s_forSale) {

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
        p.buildSign(server);
        localSender.sendMessage(ChatColor.GREEN + "Price of " + p.getName() + " set to " + p.getPrice() + ".");
    }

    public void buildSign() {
        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        if (!options.isEconomyEnabled()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Plot p = localSender.getActivePlot();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        p.buildSign(server);
        localSender.sendMessage("Sign built!");
    }

    public void demolishSign() {
        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        if (!options.isEconomyEnabled()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Plot p = localSender.getActivePlot();

        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        p.demolishSign(server);
        localSender.sendMessage("Sign demolished.");
    }

    public void setPlotSignPosition() {
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

        mctLoc = net.jmhertlein.core.location.Location.convertFromBukkitLocation(player.getTargetBlock(null, 5).getLocation());

        if (mctLoc == null) {
            localSender.sendMessage(ERR + "Couldn't get the location you're looking at.");
            return;
        }

        //use the block ABOVE the one the player is staring at.
        mctLoc.setY(mctLoc.getY() + 1);
        p.demolishSign(server);
        p.setSignLoc(mctLoc);
        p.buildSign(server);

        localSender.sendMessage(ChatColor.GREEN + " successfully set the location for the sign.");


    }

    public void surrenderPlot() {
        Plot p = localSender.getActivePlot();
        if (p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        ProtectedRegion reg = wgp.getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName());

        if (!reg.isOwner(wgp.wrapPlayer(localSender.getPlayer()))) {
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
        Town t = localSender.getActiveTown();

        boolean quickSelect = cmd.hasFlag("-q");

        if (t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        Plot nuActive = null;

        if (!quickSelect) {
            Territory te = localSender.getActiveTerritory();

            if (te == null) {
                localSender.notifyActiveTerritoryNotSet();
                return;
            }

            nuActive = te.getPlot(plotName);

            if (nuActive == null) {
                nuActive = te.getPlot((t.getTownName() + TownLevel.PLOT_INFIX + plotName).toLowerCase());
            }
        } else {
            plotName = t.getTownName() + TownLevel.PLOT_INFIX + plotName;
            plotName = plotName.toLowerCase();

            territloop:
            for (Territory territ : t.getTerritoriesCollection()) {
                if (territ.getPlot(plotName) != null) {
                    nuActive = territ.getPlot(plotName);
                    localSender.setActiveTerritory(territ);
                    break territloop;
                }

            }
        }

        if (nuActive == null) {
            localSender.sendMessage(ERR + "The plot \"" + plotName + "\" does not exist.");
            return;
        }

        localSender.setActivePlot(nuActive);
        localSender.sendMessage("Active plot set to " + nuActive.getName());
    }
}
