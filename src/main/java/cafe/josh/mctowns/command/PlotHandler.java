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
package cafe.josh.mctowns.command;

import cafe.josh.mctowns.util.Players;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Pattern;

import static net.jmhertlein.core.chat.ChatUtil.ERR;
import static net.jmhertlein.core.chat.ChatUtil.SUCC;
import cafe.josh.reflective.CommandDefinition;
import cafe.josh.reflective.annotation.CommandMethod;
import cafe.josh.mctowns.MCTowns;
import cafe.josh.mctowns.MCTownsPlugin;
import cafe.josh.mctowns.region.MCTownsRegion;
import cafe.josh.mctowns.region.Plot;
import cafe.josh.mctowns.region.Territory;
import cafe.josh.mctowns.region.Town;
import cafe.josh.mctowns.region.TownLevel;
import cafe.josh.mctowns.util.MCTConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Everdras
 */
public class PlotHandler extends CommandHandler implements CommandDefinition {

    public PlotHandler(MCTownsPlugin parent) {
        super(parent);
    }

    @CommandMethod(path = "plot info")
    public void printPlotInfo(CommandSender s) {
        setNewCommand(s);
        Plot p = localSender.getActivePlot();

        if(p == null) {
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

    @CommandMethod(path = "plot remove player", requiredArgs = 1)
    public void removePlayerFromPlot(CommandSender s, String[] args) {
        setNewCommand(s);
        Plot plot = localSender.getActivePlot();

        if(plot == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(plot.getWorldName()));

        ProtectedRegion wgPlot = regMan.getRegion(plot.getName());

        //if they are neither mayor nor owner
        if(!(localSender.hasMayoralPermissions() || wgPlot.getOwners().contains(MCTowns.getWorldGuardPlugin().wrapPlayer(localSender.getPlayer())))) {
            localSender.notifyInsufPermissions();
            return;
        }

        if(plot.removePlayer(Bukkit.getOfflinePlayer(args[0]))) {
            localSender.sendMessage("Player removed from plot.");
        } else {
            localSender.sendMessage(ERR + "Unable to remove player from plot (either player is not in plot, world doesn't exist, or WorldGuard region doesn't exist.).");
        }
    }

    @CommandMethod(path = "plot add player", requiredArgs = 1, filters = {"mayoralPerms"})
    public void addPlayerToPlot(CommandSender s, String[] args) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        Plot plot = localSender.getActivePlot();
        if(plot == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        OfflinePlayer player = server.getOfflinePlayer(args[0]);
        if(!Players.playedHasEverLoggedIn(player)) {
            localSender.sendMessage(ERR + args[0] + " has never played on this server.");
            return;
        }

        if(!t.playerIsResident(player)) {
            localSender.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        if(plot.addPlayer(player)) {
            localSender.sendMessage("Player added to plot.");
        } else {
            localSender.sendMessage(ERR + "Unable to add player to plot (either player is already in plot, world doesn't exist, or WorldGuard region doesn't exist.).");
        }

    }

    @CommandMethod(path = "plot add guest", requiredArgs = 1)
    public void addPlayerToPlotAsGuest(CommandSender s, String[] args) {
        setNewCommand(s);
        Plot plot = localSender.getActivePlot();
        if(plot == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(plot.getWorldName()));

        ProtectedRegion wgPlot = regMan.getRegion(plot.getName());

        //if they are neither mayor nor owner
        if(!(localSender.hasMayoralPermissions() || wgPlot.getOwners().contains(MCTowns.getWorldGuardPlugin().wrapPlayer(localSender.getPlayer())))) {
            localSender.notifyInsufPermissions();
            return;
        }

        OfflinePlayer player = server.getOfflinePlayer(args[0]);
        if(!Players.playedHasEverLoggedIn(player)) {
            localSender.sendMessage(ChatColor.GOLD + args[0] + " has never played on this server.");
            return;
        }

        if(plot.addGuest(player)) {
            localSender.sendMessage(ChatColor.GREEN + "Successfully added " + player.getName() + " to the plot as a guest.");
        } else {
            localSender.sendMessage(ERR + "Unable to add player to plot as guest (either player is already guest in plot, world doesn't exist, or WorldGuard region doesn't exist.).");
        }
    }

    @CommandMethod(path = "plot economy forsale", requiredArgs = 1, filters = {"mayoralPerms"})
    public void setPlotBuyability(CommandSender s, String[] args) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(!t.usesBuyablePlots()) {
            localSender.sendMessage(ERR + t.getName() + " does not allow the sale of plots.");
            return;
        }

        boolean forSale;
        try {
            forSale = Boolean.parseBoolean(args[0]);
        } catch(Exception e) {
            localSender.sendMessage(ERR + "Error parsing boolean on token: " + args[0]);
            return;
        }

        Plot p = localSender.getActivePlot();

        if(p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        p.setForSale(forSale);
        localSender.sendMessage(ChatColor.GREEN + "The plot " + p.getName() + " is " + (forSale ? "now" : "no longer") + " for sale!");
    }

    @CommandMethod(path = "plot economy setprice", requiredArgs = 1, filters = {"mayoralPerms"})
    public void setPlotPrice(CommandSender s, String rawAmount) {
        setNewCommand(s);

        if(!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        if(!Pattern.compile(MCTConfig.CURRENCY_INPUT_PATTERN.getString()).matcher(rawAmount).matches())
        {
            localSender.sendMessage(ERR + "Invalid currency input: " + rawAmount);
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(rawAmount);
        } catch(Exception e) {
            localSender.sendMessage(ERR + "Error parsing float on token: " + rawAmount);
            return;
        }

        Plot p = localSender.getActivePlot();

        if(p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        p.setPrice(price);
        p.buildSign(localSender.getPlayer().getLocation());
        localSender.sendMessage(ChatColor.GREEN + "Price of " + p.getName() + " set to " + p.getPrice() + ".");
    }

    @CommandMethod(path = "plot sign build", filters = {"mayoralPerms"})
    public void buildSign(CommandSender s) {
        setNewCommand(s);

        if(!MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Plot p = localSender.getActivePlot();

        if(p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        if(p.buildSign(localSender.getPlayer().getLocation())) {
            localSender.sendMessage(SUCC + "Sign built!");
        } else {
            localSender.sendMessage(ERR + "The sign wasn't built because its target location wasn't an air block. Please clear the spot and try again.");
        }
    }

    @CommandMethod(path = "plot sign demolish", filters = {"mayoralPerms"})
    public void demolishSign(CommandSender s) {
        setNewCommand(s);

        if(!MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Plot p = localSender.getActivePlot();

        if(p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        p.demolishSign();
        localSender.sendMessage("Sign demolished.");
    }

    @CommandMethod(path = "plot sign setpos", filters = {"mayoralPerms"})
    public void setPlotSignPosition(CommandSender s) {
        setNewCommand(s);

        Plot p = localSender.getActivePlot();

        if(p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        net.jmhertlein.core.location.Location mctLoc;

        Player player = localSender.getPlayer();

        mctLoc = net.jmhertlein.core.location.Location.convertFromBukkitLocation(player.getTargetBlock((Set<Material>) null, 5).getLocation());

        if(mctLoc == null) {
            localSender.sendMessage(ERR + "Couldn't get the location you're looking at.");
            return;
        }

        //use the block ABOVE the one the player is staring at.
        mctLoc.setY(mctLoc.getY() + 1);
        p.demolishSign();

        p.setSignLoc(mctLoc);

        p.buildSign(localSender.getPlayer().getLocation());

        localSender.sendMessage(ChatColor.GREEN + "Successfully set the location for the sign.");

    }

    // TODO: this was never made accessable, probably needs testing
    //@CommandMethod(path = "plot surrender")
    public void surrenderPlot() {

        Plot p = localSender.getActivePlot();
        if(p == null) {
            localSender.notifyActivePlotNotSet();
            return;
        }

        ProtectedRegion reg = MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName());

        if(!reg.isOwner(MCTowns.getWorldGuardPlugin().wrapPlayer(localSender.getPlayer()))) {
            localSender.sendMessage(ERR + "You don't own this plot, so you can't surrender it!");
            return;
        }

        reg.getOwners().removePlayer(localSender.getPlayer().getName());

        for(String name : reg.getMembers().getPlayers()) {
            reg.getMembers().removePlayer(name);
        }

        p.setForSale(false);
        p.setPrice(BigDecimal.ZERO);
    }

    @CommandMethod(path = "plot active", requiredArgs = 1)
    public void setActivePlot(CommandSender s, String plotName) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        Territory te = localSender.getActiveTerritory();
        if(te == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }

        Plot nuActive = townManager.getPlot(plotName);
        if(nuActive == null) {
            nuActive = townManager.getPlot(MCTownsRegion.formatRegionName(t, TownLevel.PLOT, plotName));

            if(nuActive == null) {
                localSender.sendMessage(ERR + "The plot \"" + plotName + "\" does not exist.");
                return;
            }
        }

        if(!nuActive.getParentTownName().equals(t.getName())) {
            localSender.sendMessage(ERR + "The plot \"" + plotName + "\" does not exist in your town.");
            return;
        }

        localSender.setActivePlot(nuActive);
        if(!localSender.getActiveTerritory().getName().equals(nuActive.getParentTerritoryName())) {
            localSender.setActiveTerritory(townManager.getTerritory(nuActive.getParentTerritoryName()));
            localSender.sendMessage(ChatColor.YELLOW + "Also setting parent territory to " + nuActive.getParentTerritoryName());
        }
        localSender.sendMessage("Active plot set to " + nuActive.getName());
    }

    @CommandMethod(path = "plot redefine")
    public void redefine() {
        redefineActiveRegion(TownLevel.PLOT);
    }
}
