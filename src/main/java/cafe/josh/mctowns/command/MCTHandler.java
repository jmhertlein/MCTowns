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

import cafe.josh.mctowns.region.Plot;
import cafe.josh.mctowns.region.Town;
import cafe.josh.mctowns.util.Players;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static net.jmhertlein.core.chat.ChatUtil.*;
import cafe.josh.reflective.CommandDefinition;
import cafe.josh.reflective.annotation.CommandMethod;
import cafe.josh.mctowns.MCTowns;
import cafe.josh.mctowns.MCTownsPlugin;
import cafe.josh.mctowns.util.MCTConfig;
import cafe.josh.reflective.io.DotWriter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Everdras
 */
public class MCTHandler extends CommandHandler implements CommandDefinition {

    public MCTHandler(MCTownsPlugin parent) {
        super(parent);
    }

    @CommandMethod(path = "mct bugs")
    public void printBugReportHelp(CommandSender s) {
        s.sendMessage("To report a bug in MCTowns, go to this link:");
        s.sendMessage(ChatColor.AQUA + "https://github.com/jmhertlein/MCTowns/issues");
        s.sendMessage("Log into your GitHub account and click \"new issue\".");
    }

    @CommandMethod(path = "mct exportgraph", requiredArgs = 1, permNodes = {"mctowns.export"})
    public void exportGraph(CommandSender s, String filename) {
        File f = new File(filename);
        try(DotWriter w = new DotWriter(f, true)) {
            MCTownsPlugin.getPlugin().getCommandExecutor().writeToGraph(w);
        } catch(FileNotFoundException ex) {
            Logger.getLogger(MCTHandler.class.getName()).log(Level.SEVERE, null, ex);
            s.sendMessage("Error saving graph: " + ex.getLocalizedMessage());
        }

        s.sendMessage("DOTfile saved as " + f.getAbsolutePath());
        int exitCode = 0;
        try {
            ProcessBuilder b = new ProcessBuilder("dot", "-Tpng", f.getAbsolutePath());
            b.redirectOutput(new File(f.getAbsolutePath() + ".png"));
            Process p = b.start();
            exitCode = p.waitFor();
        } catch(IOException | InterruptedException ex) {
            s.sendMessage(ChatColor.GREEN + "Graphviz returned " + exitCode);
            s.sendMessage("Error running graphviz: " + ex.getLocalizedMessage());
            s.sendMessage("Please make sure graphviz is installed.");
            return;
        }
        if(exitCode != 0) {
            s.sendMessage(ChatColor.GREEN + "Graphviz returned " + exitCode);
            s.sendMessage("Please make sure graphviz is installed.");
            return;
        }

        s.sendMessage(ChatColor.GREEN + "Exported DOTfile to PNG as " + f.getAbsolutePath() + ".png");
    }

    @CommandMethod(path = "mct addtown", requiredArgs = 2)
    public void createTown(CommandSender s, String[] args) {
        setNewCommand(s);
        String townName = args[0], mayorName = args[1];
        if(!localSender.canCreateTown()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Player nuMayor = server.getPlayer(mayorName);

        if(nuMayor == null) {
            localSender.sendMessage(ERR + mayorName + " doesn't exist or is not online.");
            return;
        }

        if(!MCTConfig.PLAYERS_CAN_JOIN_MULTIPLE_TOWNS.getBoolean() && !townManager.matchPlayerToTowns(nuMayor).isEmpty()) {
            localSender.sendMessage(ERR + nuMayor.getName() + " is already a member of a town, and as such cannot be the mayor of a new one.");
            return;
        }

        Town t = townManager.addTown(townName, nuMayor);
        if(t != null) {
            localSender.sendMessage("Town " + townName + " has been created.");
            server.broadcastMessage(SUCC + "The town " + townName + " has been founded.");

            localSender.setActiveTown(t);
            localSender.sendMessage(INFO + "Active town set to newly created town.");

            localSender.sendMessage(INFO_ALT + "The town's spawn has been set to your current location. Change it with /town spawn set.");
        } else {
            localSender.sendMessage(ERR + "That town already exists!");
        }
    }

    @CommandMethod(path = "mct removetown", requiredArgs = 1)
    public void removeTown(CommandSender s, String[] args) {
        setNewCommand(s);
        if(!localSender.canDeleteTown()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Town t = townManager.getTown(args[0]);
        if(t == null) {
            localSender.sendMessage(ERR + "The town \"" + args[0] + "\" does not exist.");
            return;
        }

        townManager.removeTown(args[0]);

        try {
            for(World w : Bukkit.getWorlds()) {
                MCTowns.getWorldGuardPlugin().getRegionManager(w).save();
            }
        } catch(StorageException ex) {
            MCTowns.logSevere("Error: unable to force a region manager save in WorldGuard. Details:");
            MCTowns.logSevere(ex.getMessage());
        } catch(NullPointerException npe) {
            MCTowns.logSevere("Couldn't force WG to save its regions. (null)");
            MCTowns.logSevere("Debug analysis:");
            MCTowns.logSevere("WG plugin was null: " + (MCTowns.getWorldGuardPlugin() == null));
            MCTowns.logSevere("Server was null: " + (MCTowns.getWorldGuardPlugin() == null));
        }

        t.getResidents().stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .map(p -> plugin.getActiveSets().get(p.getName()))
                .forEach(ActiveSet::clear);

        localSender.sendMessage("Town removed.");
        server.broadcastMessage(ChatColor.DARK_RED + args[0] + " has been disbanded.");

    }

    @CommandMethod(path = "mct info town", requiredArgs = 1)
    public void queryTownInfo(CommandSender s, String[] args) {
        setNewCommand(s);
        Town t = townManager.getTown(args[0]);

        if(t == null) {
            localSender.sendMessage(ERR + "The town \"" + args[0] + "\" does not exist.");
            return;
        }

        ChatColor c = ChatColor.AQUA;

        localSender.sendMessage(c + "Name: " + t.getName());
        localSender.sendMessage(c + "Mayor: " + t.getMayor());
        localSender.sendMessage(c + "Number of residents: " + t.getSize());
        localSender.sendMessage(c + "Plots are buyable: " + t.usesBuyablePlots());
        localSender.sendMessage(c + "Join method: " + (t.usesEconomyJoins() ? "Plot purchase" : "invitations"));
    }

    @CommandMethod(path = "mct info player", requiredArgs = 1)
    public void queryPlayerInfo(CommandSender s, String[] args) {
        setNewCommand(s);
        OfflinePlayer p = server.getOfflinePlayer(args[0]);

        if(!Players.playedHasEverLoggedIn(p)) {
            localSender.sendMessage(ERR + args[0] + " has never played on this server before.");
            return;
        }

        localSender.sendMessage(INFO + "Player: " + p.getName());

        List<Town> towns = townManager.matchPlayerToTowns(p);
        if(towns.isEmpty()) {
            localSender.sendMessage(INFO + p.getName() + " is not a member of any towns.");
        } else {
            for(Town t : towns) {
                localSender.sendMessage("Town: " + t.getName());
                localSender.sendMessage("Is Mayor: " + t.playerIsMayor(p));
                localSender.sendMessage("Is Assistant: " + t.playerIsAssistant(p));
            }
        }
    }

    @CommandMethod(path = "mct list towns")
    public void listTowns(CommandSender s) {
        setNewCommand(s);

        townManager.getTownsCollection().stream()
                .collect(Collectors.toMap(
                        t -> t.getName(),
                        t -> getOnlinePlayerCounts(t)))
                .forEach((town, counts) -> s.sendMessage(String.format("%s%s (%s/%s online)", ChatColor.YELLOW, town, counts[0], counts[1])));
    }

    @CommandMethod(path = "mct join", requiredArgs = 1)
    public void requestAdditionToTown(CommandSender s, String[] args) {
        setNewCommand(s);
        if(!MCTConfig.PLAYERS_CAN_JOIN_MULTIPLE_TOWNS.getBoolean() && townManager.playerIsAlreadyInATown(localSender.getPlayer())) {
            localSender.sendMessage(ERR + "You cannot be in more than one town at a time.");
            return;
        }

        Town addTo = townManager.getTown(args[0]);
        String pName = localSender.getPlayer().getName();

        if(addTo == null) {
            localSender.sendMessage(ERR + "\"" + args[0] + "\" doesn't exist.");
            return;
        }

        if(addTo.usesEconomyJoins()) {
            localSender.sendMessage(addTo.getName() + " doesn't use the invitation system.");
            return;
        }

        if(addTo.playerIsResident(localSender.getPlayer())) {
            localSender.sendMessage(ERR + "You are already a member of " + addTo.getName());
            return;
        }

        if(joinManager.getPlayersRequestingMembershipToTown(addTo).contains(localSender.getPlayer().getName())) {
            localSender.sendMessage(ERR + "You've already requested to join " + addTo.getName());
            return;
        }

        if(joinManager.invitationExists(pName, addTo)) {
            addTo.addPlayer(localSender.getPlayer());
            localSender.sendMessage("You have joined " + addTo.getName() + "!");
            broadcastTownJoin(addTo, localSender.getPlayer());

            joinManager.clearInvitationForPlayerFromTown(pName, addTo);
        } else {
            joinManager.addJoinRequest(pName, addTo);
            localSender.sendMessage("You have submitted a request to join " + args[0] + ".");
            addTo.broadcastMessageToTown(localSender.getPlayer().getName() + " has submitted a request to join the town.");
        }
    }

    @CommandMethod(path = "mct refuse", requiredArgs = 1)
    public void rejectInvitationFromTown(CommandSender s, String[] args) {
        setNewCommand(s);
        String pName = localSender.getPlayer().getName();

        Town t = townManager.getTown(args[0]);

        if(t == null) {
            localSender.sendMessage(ERR + "You're not invited to any towns right now.");
        } else {
            joinManager.clearInvitationForPlayerFromTown(pName, t);
            localSender.sendMessage(ChatColor.GOLD + "You have rejected the invitation to join " + t.getName());
            t.broadcastMessageToTown(ERR + pName + " has declined the invitation to join the town.");
        }

    }

    @CommandMethod(path = "mct cancel", requiredArgs = 1)
    public void cancelRequest(CommandSender s, String[] args) {
        setNewCommand(s);

        Town t = townManager.getTown(args[0]);

        if(t == null) {
            localSender.sendMessage(ERR + "That town doesn't exist.");
            return;
        }

        if(joinManager.clearRequest(localSender.getPlayer().getName(), t)) {
            localSender.sendMessage(ChatColor.GOLD + "You have withdrawn your request to join " + t.getName() + ".");
        } else {
            localSender.sendMessage(ERR + "You haven't submitted a request to join " + t.getName() + ".");
        }

    }

    @CommandMethod(path = "mct list invites")
    public void checkPendingInvite(CommandSender s) {
        setNewCommand(s);
        List<Town> towns = joinManager.getTownsPlayerIsInvitedTo(localSender.getPlayer().getName());

        localSender.sendMessage(INFO + "You are currently invited to the following towns:");
        for(Town t : towns) {
            localSender.sendMessage(INFO_ALT + t.getName());
        }
    }

    @CommandMethod(path = "mct confirm")
    public void confirmPlotPurchase(CommandSender s) {
        setNewCommand(s);
        if(!MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        ActiveSet plotToBuy = plugin.getPotentialPlotBuyers().get(localSender.getPlayer());

        if(plotToBuy == null) {
            localSender.sendMessage(ERR + "You haven't selected a plot to buy yet.");
            return;
        }

        if(townManager.playerIsAlreadyInATown(localSender.getPlayer())) //if players can't join multiple towns AND the town they're buying from isn't their current town
        {
            if(!MCTConfig.PLAYERS_CAN_JOIN_MULTIPLE_TOWNS.getBoolean() && !townManager.matchPlayerToTowns(localSender.getPlayer()).get(0).equals(plotToBuy.getActiveTown())) {
                localSender.sendMessage(ERR + "You're already in a different town.");
                return;
            }
        }

        if(!plotToBuy.getActiveTown().playerIsResident(localSender.getPlayer())) {
            if(!plotToBuy.getActiveTown().usesEconomyJoins()) {
                localSender.sendMessage(ERR + "You aren't a member of this town.");
                return;
            }
        }

        if(!plotToBuy.getActiveTown().usesBuyablePlots()) {
            localSender.sendMessage(ERR + "This town's plots aren't buyable.");
            return;
        }

        Plot p = plotToBuy.getActivePlot();

        if(!p.isForSale()) {
            localSender.sendMessage(ERR + "This plot isn't for sale.");
            return;
        }

        if(!MCTowns.getEconomy().withdrawPlayer(localSender.getPlayer().getName(), p.getPrice().floatValue()).transactionSuccess()) {
            localSender.sendMessage(ERR + "Insufficient funds.");
            return;
        }

        ProtectedRegion plotReg = MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName());
        if(plotReg == null) {
            localSender.sendMessage(ERR + "The WorldGuard region for the plot you're trying to buy seems to have been deleted. Please notify your mayor.");
            return;
        }

        plotToBuy.getActiveTown().getBank().depositCurrency(p.getPrice());

        p.setPrice(BigDecimal.ZERO);
        p.setForSale(false);
        p.demolishSign();

        plotReg.getOwners().addPlayer(localSender.getPlayer().getName());

        localSender.sendMessage(ChatColor.GREEN + "You are now the proud owner of this plot.");
        doRegManSave(MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(p.getWorldName())));

        if(!townManager.matchPlayerToTowns(localSender.getPlayer()).contains(plotToBuy.getActiveTown())) {
            plotToBuy.getActiveTown().addPlayer(localSender.getPlayer());
            localSender.sendMessage(ChatColor.GREEN + "You have joined the town " + plotToBuy.getActiveTown().getName());
        }
    }

    private static long[] getOnlinePlayerCounts(Town t) {
        long online = t.getResidents().stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .count();
        long total = t.getSize();
        return new long[]{online, total};
    }
}
