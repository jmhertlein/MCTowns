/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.command.handlers;

import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import static net.jmhertlein.core.chat.ChatUtil.*;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.permission.Perms;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author Everdras
 */
public class MCTHandler extends CommandHandler {

    public MCTHandler(MCTowns parent) {
        super(parent);
    }

    public void checkIfRegionIsManagedByMCTowns() {
    }

    public void createTown(String townName, String mayorName) {
        if (!senderWrapper.canCreateTown()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Player nuMayor = server.getPlayer(mayorName);

        if (nuMayor == null) {
            senderWrapper.sendMessage(ERR + mayorName + " doesn't exist or is not online.");
            return;
        }

        if (townManager.matchPlayerToTown(nuMayor) != null) {
            senderWrapper.sendMessage(ERR + nuMayor.getName() + " is already a member of a town, and as such cannot be the mayor of a new one.");
            return;
        }

        if (townManager.addTown(townName, nuMayor)) {
            senderWrapper.sendMessage("Town " + townName + " has been created.");
            server.broadcastMessage(SUCC + "The town " + townName + " has been founded.");
            
            senderWrapper.setActiveTown(townManager.matchPlayerToTown(nuMayor));
            senderWrapper.sendMessage(INFO + "Active town set to newly created town.");
        } else {
            senderWrapper.sendMessage(ERR + "That town already exists!");
        }



    }

    public void removeTown(String townName) {
        if (!senderWrapper.canDeleteTown()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = townManager.getTown(townName);
        if (t == null) {
            senderWrapper.sendMessage(ERR + "The town \"" + townName + "\" does not exist.");
            return;
        }

        townManager.removeTown(wgp, townName);

        try {
            wgp.getRegionManager(server.getWorld(t.getWorldName())).save();
        } catch (ProtectionDatabaseException ex) {
            MCTowns.logSevere("Error: unable to force a region manager save in WorldGuard. Details:");
            MCTowns.logSevere(ex.getMessage());
        } catch (NullPointerException npe) {
            MCTowns.logSevere("Couldn't force WG to save its regions. (null)");
            MCTowns.logSevere("Debug analysis:");
            MCTowns.logSevere("WG plugin was null: " + (wgp == null));
            MCTowns.logSevere("Server was null: " + (wgp == null));
            MCTowns.logSevere("Town was null: " + (t == null));
            MCTowns.logSevere("Town's world (String) was null in storage: " + (t.getWorldName() == null));
            MCTowns.logSevere("Town's world was null: " + (server.getWorld(t.getWorldName()) == null));
        }

        //clear active sets to remove pointers to deleted town
        plugin.getActiveSets().clear();

        senderWrapper.sendMessage("Town removed.");
        server.broadcastMessage(ChatColor.DARK_RED + townName + " has been disbanded.");

    }

    public void queryTownInfo(String townName) {
        Town t = townManager.getTown(townName);

        if (t == null) {
            senderWrapper.sendMessage(ERR + "The town \"" + townName + "\" does not exist.");
            return;
        }

        ChatColor c = ChatColor.AQUA;

        senderWrapper.sendMessage(c + "Name: " + t.getTownName());
        senderWrapper.sendMessage(c + "Mayor: " + t.getMayor());
        senderWrapper.sendMessage(c + "World: " + t.getWorldName());
        senderWrapper.sendMessage(c + "Number of residents: " + t.getSize());
        senderWrapper.sendMessage(c + "Plots are buyable: " + t.usesBuyablePlots());
        senderWrapper.sendMessage(c + "Join method: " + (t.usesEconomyJoins() ? "Plot purchase" : "invitations"));



    }

    public void queryPlayerInfo(String playerName) {
        Player p = server.getPlayer(playerName);

        if (p == null && townManager.matchPlayerToTown(playerName) == null) {
            senderWrapper.sendMessage(ERR + "That player is either not online or doesn't exist.");
            return;
        }

        String playerExactName = (p == null ? playerName : p.getName());

        Town t = townManager.matchPlayerToTown(playerExactName);

        if (t == null) {
            senderWrapper.sendMessage("Player: " + playerExactName);
            senderWrapper.sendMessage("Town: None");
            senderWrapper.sendMessage("Is Mayor: n/a");
            senderWrapper.sendMessage("Is Assistant: n/a");
        } else {
            senderWrapper.sendMessage("Player: " + playerExactName);
            senderWrapper.sendMessage("Town: " + t.getTownName());
            senderWrapper.sendMessage("Is Mayor: " + t.getMayor().equals(playerExactName));
            senderWrapper.sendMessage("Is Assistant: " + t.playerIsAssistant(playerExactName));
        }

    }

    public void listTowns() {

        listTowns(1);
    }

    public void listTowns(String page) {
        int intPage;
        try {
            intPage = Integer.parseInt(page);
        } catch (Exception e) {
            senderWrapper.sendMessage(ERR + "Parsing error: <page> is not a valid integer.");
            return;
        }

        this.listTowns(intPage);
    }

    private void listTowns(int page) {
        page--; //shift to 0-indexing

        if (page < 0) {
            senderWrapper.sendMessage(ERR + "Invalid page.");
            return;
        }
        senderWrapper.sendMessage(ChatColor.AQUA + "Existing towns (page " + page + "):");



        Town[] towns = townManager.getTownsCollection().toArray(new Town[townManager.getTownsCollection().size()]);

        for (int i = page * RESULTS_PER_PAGE; i < towns.length && i < page * RESULTS_PER_PAGE + RESULTS_PER_PAGE; i++) {
            senderWrapper.sendMessage(ChatColor.YELLOW + towns[i].getTownName());
        }


    }

    public void requestAdditionToTown(String townName) {
        if (townManager.playerIsAlreadyInATown(senderWrapper.getPlayer())) {
            senderWrapper.sendMessage(ERR + "You cannot be in more than one town at a time.");
            return;
        }

        Town addTo = townManager.getTown(townName);
        String pName = senderWrapper.getPlayer().getName();

        if (addTo == null) {
            senderWrapper.sendMessage(ERR + "\"" + townName + "\" doesn't exist.");
            return;
        }

        if (addTo.usesEconomyJoins()) {
            senderWrapper.sendMessage(addTo.getTownName() + " doesn't use the invitation system.");
            return;
        }



        if (joinManager.playerIsInvitedToTown(pName, addTo)) {
            addTo.addPlayer(senderWrapper.getPlayer());
            senderWrapper.sendMessage("You have joined " + addTo.getTownName() + "!");
            broadcastTownJoin(addTo, senderWrapper.getPlayer());

            joinManager.clearInvitationForPlayerFromTown(pName, addTo);
        } else {
            joinManager.addPlayerRequestForTown(addTo, pName);
            senderWrapper.sendMessage("You have submitted a request to join " + townName + ".");
            addTo.broadcastMessageToTown(server, senderWrapper.getPlayer().getName() + " has submitted a request to join the town.");

        }
    }

    public void rejectInvitation() {

        String pName = senderWrapper.getPlayer().getName();

        Town t = joinManager.getCurrentInviteForPlayer(pName);

        if (t == null) {
            senderWrapper.sendMessage(ERR + "You're not invited to any towns right now.");
        } else {
            joinManager.clearInvitationForPlayer(pName);
            senderWrapper.sendMessage(ChatColor.GOLD + "You have rejected the invitation to join " + t.getTownName());
            t.broadcastMessageToTown(server, ERR + pName + " has declined the invitation to join the town.");
        }

    }

    public void cancelRequest(String townName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = townManager.getTown(townName);

        if (t == null) {
            senderWrapper.sendMessage(ERR + "That town doesn't exist.");
            return;
        }

        if (joinManager.clearRequestForTownFromPlayer(t, senderWrapper.getPlayer().getName())) {
            senderWrapper.sendMessage(ChatColor.GOLD + "You have withdrawn your request to join " + t.getTownName() + ".");
        } else {
            senderWrapper.sendMessage(ERR + "You haven't submitted a request to join " + t.getTownName() + ".");
        }

    }



    public void checkPendingInvite() {
        Town t = joinManager.getCurrentInviteForPlayer(senderWrapper.getPlayer().getName());

        senderWrapper.sendMessage(INFO_ALT + "You are currently " + (t == null ? " not invited to a town." : "invited to " + t.getTownName() + "."));
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

    public void toggleAbortSave() {
        plugin.setAbortSave(!plugin.willAbortSave());
        senderWrapper.sendMessage(SUCC + "MCTowns will " + (plugin.willAbortSave() ? "NOT save any" : "now save") + " data for this session.");

    }
}
