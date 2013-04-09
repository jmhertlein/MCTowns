package net.jmhertlein.mctowns.command.handlers;

import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import static net.jmhertlein.core.chat.ChatUtil.*;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.ActiveSet;
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
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!localSender.canCreateTown()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Player nuMayor = server.getPlayer(mayorName);

        if (nuMayor == null) {
            localSender.sendMessage(ERR + mayorName + " doesn't exist or is not online.");
            return;
        }

        if (!options.playersCanJoinMultipleTowns() && !townManager.matchPlayerToTowns(nuMayor).isEmpty()) {
            localSender.sendMessage(ERR + nuMayor.getName() + " is already a member of a town, and as such cannot be the mayor of a new one.");
            return;
        }

        Town t = townManager.addTown(townName, nuMayor);
        if (t != null) {
            localSender.sendMessage("Town " + townName + " has been created.");
            server.broadcastMessage(SUCC + "The town " + townName + " has been founded.");

            localSender.setActiveTown(t);
            localSender.sendMessage(INFO + "Active town set to newly created town.");

            localSender.sendMessage(INFO_ALT + "The town's spawn has been set to your current location. Change it with /town spawn set.");
        } else {
            localSender.sendMessage(ERR + "That town already exists!");
        }



    }

    public void removeTown(String townName) {
        if (!localSender.canDeleteTown()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Town t = townManager.getTown(townName);
        if (t == null) {
            localSender.sendMessage(ERR + "The town \"" + townName + "\" does not exist.");
            return;
        }

        townManager.removeTown(townName);

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

        localSender.sendMessage("Town removed.");
        server.broadcastMessage(ChatColor.DARK_RED + townName + " has been disbanded.");

    }

    public void queryTownInfo(String townName) {
        Town t = townManager.getTown(townName);

        if (t == null) {
            localSender.sendMessage(ERR + "The town \"" + townName + "\" does not exist.");
            return;
        }

        ChatColor c = ChatColor.AQUA;

        localSender.sendMessage(c + "Name: " + t.getTownName());
        localSender.sendMessage(c + "Mayor: " + t.getMayor());
        localSender.sendMessage(c + "World: " + t.getWorldName());
        localSender.sendMessage(c + "Number of residents: " + t.getSize());
        localSender.sendMessage(c + "Plots are buyable: " + t.usesBuyablePlots());
        localSender.sendMessage(c + "Join method: " + (t.usesEconomyJoins() ? "Plot purchase" : "invitations"));



    }

    public void queryPlayerInfo(String playerName) {
        Player p = server.getPlayer(playerName);

        if (p == null && townManager.matchPlayerToTowns(playerName).isEmpty()) {
            localSender.sendMessage(ERR + "That player is either not online or doesn't exist.");
            return;
        }

        String playerExactName = (p == null ? playerName : p.getName());

        List<Town> towns = townManager.matchPlayerToTowns(playerExactName);

        for (Town t : towns) {
            if (t == null) {
                localSender.sendMessage("Player: " + playerExactName);
                localSender.sendMessage("Town: None");
                localSender.sendMessage("Is Mayor: n/a");
                localSender.sendMessage("Is Assistant: n/a");
            } else {
                localSender.sendMessage("Player: " + playerExactName);
                localSender.sendMessage("Town: " + t.getTownName());
                localSender.sendMessage("Is Mayor: " + t.getMayor().equals(playerExactName));
                localSender.sendMessage("Is Assistant: " + t.playerIsAssistant(playerExactName));
            }
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
            localSender.sendMessage(ERR + "Parsing error: <page> is not a valid integer.");
            return;
        }

        this.listTowns(intPage);
    }

    private void listTowns(int page) {
        page--; //shift to 0-indexing

        if (page < 0) {
            localSender.sendMessage(ERR + "Invalid page.");
            return;
        }
        localSender.sendMessage(ChatColor.AQUA + "Existing towns (page " + page + "):");



        Town[] towns = townManager.getTownsCollection().toArray(new Town[townManager.getTownsCollection().size()]);

        for (int i = page * RESULTS_PER_PAGE; i < towns.length && i < page * RESULTS_PER_PAGE + RESULTS_PER_PAGE; i++) {
            localSender.sendMessage(ChatColor.YELLOW + towns[i].getTownName());
        }


    }

    public void requestAdditionToTown(String townName) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!options.playersCanJoinMultipleTowns() && townManager.playerIsAlreadyInATown(localSender.getPlayer())) {
            localSender.sendMessage(ERR + "You cannot be in more than one town at a time.");
            return;
        }

        Town addTo = townManager.getTown(townName);
        String pName = localSender.getPlayer().getName();

        if (addTo == null) {
            localSender.sendMessage(ERR + "\"" + townName + "\" doesn't exist.");
            return;
        }

        if (addTo.usesEconomyJoins()) {
            localSender.sendMessage(addTo.getTownName() + " doesn't use the invitation system.");
            return;
        }



        if (joinManager.playerIsInvitedToTown(pName, addTo)) {
            addTo.addPlayer(localSender.getPlayer());
            localSender.sendMessage("You have joined " + addTo.getTownName() + "!");
            broadcastTownJoin(addTo, localSender.getPlayer());

            joinManager.clearInvitationForPlayerFromTown(pName, addTo);
        } else {
            joinManager.addPlayerRequestForTown(addTo, pName);
            localSender.sendMessage("You have submitted a request to join " + townName + ".");
            addTo.broadcastMessageToTown(server, localSender.getPlayer().getName() + " has submitted a request to join the town.");

        }
    }

    public void rejectInvitationFromTown(String townName) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        String pName = localSender.getPlayer().getName();


        Town t = townManager.getTown(townName);

        if (t == null) {
            localSender.sendMessage(ERR + "You're not invited to any towns right now.");
        } else {
            joinManager.clearInvitationForPlayerFromTown(pName, t);
            localSender.sendMessage(ChatColor.GOLD + "You have rejected the invitation to join " + t.getTownName());
            t.broadcastMessageToTown(server, ERR + pName + " has declined the invitation to join the town.");
        }

    }

    public void cancelRequest(String townName) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Town t = townManager.getTown(townName);

        if (t == null) {
            localSender.sendMessage(ERR + "That town doesn't exist.");
            return;
        }

        if (joinManager.clearRequestForTownFromPlayer(t, localSender.getPlayer().getName())) {
            localSender.sendMessage(ChatColor.GOLD + "You have withdrawn your request to join " + t.getTownName() + ".");
        } else {
            localSender.sendMessage(ERR + "You haven't submitted a request to join " + t.getTownName() + ".");
        }

    }

    public void checkPendingInvite() {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        List<Town> towns = joinManager.getTownsPlayerIsInvitedTo(localSender.getPlayer().getName());

        localSender.sendMessage(INFO + "You are currently invited to the following towns:");
        for (Town t : towns) {
            localSender.sendMessage(INFO_ALT + t.getTownName());
        }
    }

    public void confirmPlotPurchase(HashMap<Player, ActiveSet> buyers) {
        if (localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }

        if (!options.isEconomyEnabled()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        ActiveSet plotToBuy = buyers.get(localSender.getPlayer());

        if (plotToBuy == null) {
            localSender.sendMessage(ERR + "You haven't selected a plot to buy yet.");
            return;
        }

        if (townManager.playerIsAlreadyInATown(localSender.getPlayer())) {
            //if players can't join multiple towns AND the town they're buying from isn't their current town
            if (!options.playersCanJoinMultipleTowns() && !townManager.matchPlayerToTowns(localSender.getPlayer()).get(0).equals(plotToBuy.getActiveTown())) {
                localSender.sendMessage(ERR + "You're already in a different town.");
                return;
            }
        }

        if (!plotToBuy.getActiveTown().playerIsResident(localSender.getPlayer())) {
            if (!plotToBuy.getActiveTown().usesEconomyJoins()) {
                localSender.sendMessage(ERR + "You aren't a member of this town.");
                return;
            }
        }

        if (!plotToBuy.getActiveTown().usesBuyablePlots()) {
            localSender.sendMessage(ERR + "This town's plots aren't buyable.");
            return;
        }

        Plot p = plotToBuy.getActivePlot();

        if (!p.isForSale()) {
            localSender.sendMessage(ERR + "This plot isn't for sale.");
            return;
        }

        if (!economy.withdrawPlayer(localSender.getPlayer().getName(), p.getPrice().floatValue()).transactionSuccess()) {
            localSender.sendMessage(ERR + "Insufficient funds.");
            return;
        }

        plotToBuy.getActiveTown().getBank().depositCurrency(p.getPrice());

        p.setPrice(BigDecimal.ZERO);
        p.setForSale(false);
        ProtectedRegion plotReg = wgp.getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName());
        p.demolishSign();

        plotReg.getOwners().addPlayer(localSender.getPlayer().getName());

        localSender.sendMessage(ChatColor.GREEN + "You are now the proud owner of this plot.");
        doRegManSave(wgp.getRegionManager(server.getWorld(p.getWorldName())));


        if (!townManager.playerIsAlreadyInATown(localSender.getPlayer())) {
            plotToBuy.getActiveTown().addPlayer(localSender.getPlayer());
            localSender.sendMessage(ChatColor.GREEN + "You have joined the town " + plotToBuy.getActiveTown().getTownName());
        }


    }

    public void toggleAbortSave() {
        plugin.setAbortSave(!plugin.willAbortSave());
        localSender.sendMessage(SUCC + "MCTowns will " + (plugin.willAbortSave() ? "NOT save any" : "now save") + " data for this session.");

    }

    public void printDonationPlug() {
        localSender.sendMessage(ChatColor.LIGHT_PURPLE + "MCTowns is Free & Open Source Software.");
        localSender.sendMessage(ChatColor.AQUA + "I develop MCTowns in my free time, as a hobby. If you enjoy MCTowns, you might consider making a small donation to fund its continued development.");
        localSender.sendMessage(ChatColor.AQUA + "Donate however much you feel comfortable donating, no matter how small the amount.");
        localSender.sendMessage(ChatColor.GREEN + "To donate, just go to MCTowns' BukkitDev homepage ( http://dev.bukkit.org/server-mods/mctowns/ ) and click \"Donate\" in the top right-hand corner.");
    }
}
