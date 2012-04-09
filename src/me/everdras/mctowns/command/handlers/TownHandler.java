/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package me.everdras.mctowns.command.handlers;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.banking.BlockBank;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.command.MCTCommand;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.permission.Perms;
import me.everdras.mctowns.structure.MCTownsRegion;
import me.everdras.mctowns.structure.Territory;
import me.everdras.mctowns.structure.Town;
import me.everdras.mctowns.structure.TownLevel;
import me.everdras.mctowns.townjoin.TownJoinInfoPair;
import me.everdras.mctowns.townjoin.TownJoinManager;
import me.everdras.mctowns.townjoin.TownJoinMethod;
import me.everdras.mctowns.townjoin.TownJoinMethodFormatException;
import me.everdras.mctowns.util.BlockDataValueTranslator;
import me.everdras.mctowns.util.Config;
import me.everdras.mctowns.util.WGUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Everdras
 */
public class TownHandler extends CommandHandler {

    public TownHandler(MCTowns parent, TownManager t, TownJoinManager j, CommandSender p, HashMap<String, ActiveSet> activeSets, WorldGuardPlugin wg, Economy econ, Config opt, MCTCommand cmd) {
        super(parent, t, j, p, activeSets, wg, econ, opt, cmd);
    }



    public void setActiveTown(String townName) {
        if (!senderWrapper.hasExternalPermissions(Perms.ADMIN.toString())) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (townManager.getTown(townName) == null) {
            senderWrapper.sendMessage(ERR + "The town \"" + townName + "\" does not exist.");
            return;
        }

        senderWrapper.setActiveTown(townManager.getTown(townName));
        senderWrapper.sendMessage("Active town set to " + townName);

    }

    public void resetActiveTown() {
        Town t = townManager.matchPlayerToTown((Player) senderWrapper.getSender());
        if (t == null) {
            senderWrapper.sendMessage(ERR + "Unable to match you to a town. Are you sure you belong to one?");
            return;
        }

        senderWrapper.setActiveTown(t);
        senderWrapper.sendMessage(ChatColor.LIGHT_PURPLE + "Active town reset to your default (" + t.getTownName() + ")");
    }

    public void setTownSpawn() {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        if (!t.playerIsInsideTownBorders(wgp, senderWrapper.getPlayer())) {
            senderWrapper.sendMessage(ERR + "You need to be inside your town borders to do that.");
            return;
        }

        t.setSpawn(senderWrapper.getPlayer().getLocation());
        senderWrapper.sendMessage("Town spawn location updated.");
    }

    public void setTownJoinMethod(String s_method) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        TownJoinMethod method;
        try {
            method = TownJoinMethod.parseMethod(s_method);
        } catch (TownJoinMethodFormatException ex) {
            senderWrapper.sendMessage(ERR + ex.getMessage());
            return;
        }

        //TODO: Refactor Town so that it holds a TownJoinMethod instead of a boolean that determines economy joins or invites.
        if (method == TownJoinMethod.ECONOMY) {
            t.setEconomyJoins(true);
        } else if (method == TownJoinMethod.INVITATION) {
            t.setEconomyJoins(false);
        }


    }

    public void setTownPlotBuyability(String s_buyability) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        if (!options.isEconomyEnabled()) {
            senderWrapper.sendMessage(ERR + "The economy is not enabled for your server.");
            return;
        }

        boolean buyability;

        try {
            buyability = Boolean.parseBoolean(s_buyability);
        } catch (Exception e) {
            senderWrapper.sendMessage(ERR + "Error in parsing boolean: expected true/false, found " + s_buyability);
            return;
        }

        t.setBuyablePlots(buyability);
        if(buyability)
            senderWrapper.sendMessage(ChatColor.GOLD + t.getTownName() + "'s plots can now be sold and new plots are buyable by default.");
        else
            senderWrapper.sendMessage(ChatColor.GOLD + t.getTownName() + "'s plots are no longer for sale.");


    }

    public void setDefaultPlotPrice(String plotPrice) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }


        BigDecimal price;

        try {
            price = new BigDecimal(plotPrice);
        } catch (NumberFormatException nfe) {
            senderWrapper.sendMessage(ERR + "Error parsing plot price: " + nfe.getMessage());
            return;
        }

        t.setDefaultPlotPrice(price);
        senderWrapper.sendMessage(SUCC + "The default price of plots in " + t.getTownName() + " has been set to " + price);

    }

    public void addTerritorytoTown(String territName) {
        boolean autoActive = !cmd.hasFlag(MCTCommand.DISABLE_AUTOACTIVE);
        boolean admin = cmd.hasFlag(MCTCommand.ADMIN);
        if (!senderWrapper.hasExternalPermissions(Perms.ADMIN.toString()) && admin) {
            senderWrapper.sendMessage(ChatColor.DARK_RED + "You're not permitted to run this command with administrative priviliges!");
            return;
        }

        if (!(options.mayorsCanBuyTerritories() || admin)) {
            senderWrapper.sendMessage(ChatColor.BLUE + "Mayors are not allowed to add territories. If you're an admin, try adding '-admin' to the end of the command.");
            return;
        }







        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        if (!t.getWorldName().equals(senderWrapper.getPlayer().getWorld().getName())) {
            senderWrapper.sendMessage(ERR + "You're not in the same world as the town, so you can't add Territories to it in this world.");
            return;
        }

        if ((t.getSize() < options.getMinNumPlayersToBuyTerritory()) && !admin) {
            senderWrapper.sendMessage(ERR + "You don't have enough people in your town to buy territories yet.");
            senderWrapper.sendMessage(ERR + "You have " + t.getSize() + " people, but you need a total of " + options.getMinNumPlayersToBuyTerritory() + "!");
            return;
        }


        territName = t.getTownName() + TERRITORY_INFIX + territName;

        String worldName = t.getWorldName();
        Territory nuTerrit = new Territory(territName, worldName);

        ProtectedCuboidRegion region = this.getSelectedRegion(nuTerrit.getName());



        if (region == null) {
            senderWrapper.sendMessage(ERR + "No region selected!");
            return;
        }

        RegionManager regMan = wgp.getRegionManager(wgp.getServer().getWorld(worldName));

        if (regMan.hasRegion(territName)) {
            senderWrapper.sendMessage(ERR + "That name is already in use. Please pick a different one.");
            return;
        }

        //charge the player if they're not running this as an admin and buyable territories is enabled and the price is more than 0
        if (!admin && options.getPricePerXZBlock().compareTo(BigDecimal.ZERO) > 0) {
            assert (options.mayorsCanBuyTerritories());



            BigDecimal price = options.getPricePerXZBlock().multiply(new BigDecimal(WGUtils.getNumXZBlocksInRegion(region)));

            if (t.getBank().getCurrencyBalance().compareTo(price) < 0) {
                //If they can't afford it...
                senderWrapper.sendMessage(ERR + "Your town can't afford that large of a region.");
                senderWrapper.sendMessage(ERR + "Total Price: " + price);
                return;
            }

            //otherwise...
            t.getBank().withdrawCurrency(price);

            senderWrapper.sendMessage(ChatColor.GREEN + "Purchase success! Total price was: " + price.toString());
        }

        //IF ALL THE THINGS ARE FINALLY DONE...
        region.getOwners().addPlayer(t.getMayor());

        regMan.addRegion(region);

        doRegManSave(regMan);


        senderWrapper.getActiveTown().addTerritory(nuTerrit);
        senderWrapper.sendMessage("Territory added.");

        if (autoActive) {
            senderWrapper.setActiveTerritory(nuTerrit);
            senderWrapper.sendMessage(ChatColor.LIGHT_PURPLE + "Active territory set to newly created territory.");

        }
    }

    public void removeTerritoryFromTown(String territName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town to = senderWrapper.getActiveTown();

        if (to == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        Territory removeMe = to.getTerritory(territName);

        if (removeMe == null) {
            senderWrapper.sendMessage(ERR + "That territory doesn't exist. Make sure you're using the full name of the territory (townname_territory_territoryshortname).");
        }

        to.removeTerritory(territName);

        townManager.unregisterTerritoryFromWorldGuard(wgp, removeMe);

        senderWrapper.sendMessage(SUCC + "Territory removed.");
    }

    public void invitePlayerToTown(String invitee) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }





        Player p = server.getPlayer(invitee);

        if (townManager.playerIsAlreadyInATown(p)) {
            senderWrapper.sendMessage(ERR + p.getName() + " is already in a town.");
            return;
        }


        Town t = senderWrapper.getActiveTown();

        if (p == null) {
            senderWrapper.sendMessage(ERR + "That player is not online.");
            return;
        }

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        if (t.usesEconomyJoins()) {
            senderWrapper.sendMessage(t.getTownName() + " doesn't use the invitation system.");
            return;
        }

        TownJoinInfoPair infoPair = new TownJoinInfoPair(t, p);



        if (joinManager.matchInviteToRequestAndDiscard(infoPair)) {
            t.addPlayer(p);
            p.sendMessage("You have joined " + t.getTownName() + "!");
            broadcastTownJoin(t, p);
        } else {
            joinManager.submitInvitation(infoPair);
            senderWrapper.sendMessage(SUCC + p.getName() + " has been invited to join " + t.getTownName() + ".");
            p.sendMessage(ChatColor.DARK_GREEN + "You have been invited to join the town " + t.getTownName() + "!");
            p.sendMessage(ChatColor.DARK_GREEN + "To join, type /mct join " + t.getTownName());

        }

    }

    public void promoteToAssistant(String playerName) {
        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }



        if (!(senderWrapper.hasExternalPermissions(Perms.ADMIN.toString()) || t.playerIsMayor(senderWrapper.getPlayer()))) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (server.getPlayer(playerName) == null) {
            senderWrapper.sendMessage(ERR + playerName + " is not online! Make sure you typed their name correctly.");
        }

        if (t.playerIsMayor(playerName)) {
            senderWrapper.sendMessage(ERR + "That player is already the mayor of the town.");
            return;
        }


        if (!t.playerIsResident(playerName)) {
            senderWrapper.sendMessage(ERR + playerName + " is not a resident of " + t.getTownName() + ".");
            return;
        }

        if (t.addAssistant(playerName)) {
            for (Territory territ : t.getTerritoriesCollection()) {
                territ.addPlayerToWGRegion(wgp, playerName);
            }

            senderWrapper.sendMessage(playerName + " has been promoted to an assistant of " + t.getTownName() + ".");

            if (server.getPlayer(playerName) != null) {
                server.getPlayer(playerName).sendMessage("You are now an Assistant Mayor of " + t.getTownName());
            }
        } else {
            senderWrapper.sendMessage(ERR + playerName + " is already an assistant in this town.");
        }



    }

    public void demoteFromAssistant(String playerName) {

        Town t = senderWrapper.getActiveTown();
        Player p = server.getPlayer(playerName);

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }



        if (!(senderWrapper.hasExternalPermissions(Perms.ADMIN.toString()) || t.playerIsMayor(senderWrapper.getPlayer()))) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (p == null) {
            senderWrapper.sendMessage(ERR + playerName + " doesn't exist or is not online.");
            return;
        }


        if (!t.playerIsResident(p)) {
            senderWrapper.sendMessage(ERR + playerName + " is not a resident of " + t.getTownName() + ".");
            return;
        }

        if (t.removeAssistant(p)) {
            senderWrapper.sendMessage(p.getName() + " has been demoted.");
            p.sendMessage(ChatColor.DARK_RED + "You are no longer an assistant mayor for " + t.getTownName());
        } else {
            senderWrapper.sendMessage(ERR + p.getName() + " is not an assistant in this town.");
        }
    }

    public void setMayor(String playerName) {

        Town t = senderWrapper.getActiveTown();
        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        Player p = server.getPlayerExact(playerName);


        if (!(senderWrapper.hasExternalPermissions("ADMIN") || t.getMayor().equals(senderWrapper.getPlayer().getName()))) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (p == null) {
            senderWrapper.sendMessage(ERR + playerName + " either does not exist or is not online.");
            return;
        }

        if (!t.playerIsResident(p)) {
            senderWrapper.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        senderWrapper.getActiveTown().setMayor(p.getName());
        t.broadcastMessageToTown(server, "The mayor of " + t.getTownName() + " is now " + p.getName() + "!");
    }

    public void cancelInvitation(String playerName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        if (joinManager.removeInvitation(t, playerName)) {
            senderWrapper.sendMessage(ChatColor.GOLD + "The invitation for " + playerName + " has been withdrawn.");
        } else {
            senderWrapper.sendMessage(ERR + playerName + " does not have any pending invitations from " + t.getTownName() + ".");
        }
    }

    public void rejectRequest(String playerName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Player p = server.getPlayer(playerName);
        Town t = senderWrapper.getActiveTown();

//        if (p == null) {
//            senderWrapper.sendMessage(ERR + "Player does not exist or is not online.");
//            return;
//        }

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        if (!joinManager.removeRequest(t, (p == null ? playerName : p.getName()))) {
            senderWrapper.sendMessage(ERR + "No matching request found.");
        } else {
            senderWrapper.sendMessage(ChatColor.GOLD + (p == null ? playerName : p.getName()) + "'s request has been rejected.");

            if (p != null) {
                p.sendMessage(ChatColor.DARK_RED + "Your request to join " + t.getTownName() + " has been rejected.");
            }
        }

    }

    public void listRequestsForTown() {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }
        LinkedList<TownJoinInfoPair> reqs = joinManager.getPendingRequestsForTown(t);

        senderWrapper.sendMessage(ChatColor.DARK_BLUE + "There are pending requests from:");

        for (String s : getOutputFriendlyTownJoinListMessages(true, reqs)) {
            senderWrapper.sendMessage(ChatColor.YELLOW + s);
        }

    }

    public void listInvitesForTown() {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }
        LinkedList<TownJoinInfoPair> invs = joinManager.getPendingInvitesForTown(t);

        senderWrapper.sendMessage(ChatColor.DARK_BLUE + "There are pending invites for:");


        for (String s : getOutputFriendlyTownJoinListMessages(true, invs)) {
            senderWrapper.sendMessage(ChatColor.YELLOW + s);
        }



    }

    public void removePlayerFromTown(String playerName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Player removeMe = server.getPlayer(playerName);
        Town removeFrom = senderWrapper.getActiveTown();

        if (removeMe == null) {
            senderWrapper.sendMessage(INFO + playerName + " is not online. Make sure you typed their name correctly.");
        }

        if (removeFrom == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        if (removeFrom.playerIsMayor(playerName)) {
            senderWrapper.sendMessage(ERR + "A mayor cannot be removed from his own town.");
            return;
        }

        if (removeFrom.playerIsAssistant(playerName) && !removeFrom.playerIsMayor(senderWrapper.getPlayer())) {
            senderWrapper.sendMessage(ERR + "Only the mayor can remove assistants from the town.");
            return;
        }


        senderWrapper.getActiveTown().removePlayer(playerName);

        townManager.removePlayerFromTownsWGRegions(wgp, removeFrom, playerName);

        senderWrapper.sendMessage("\"" + playerName + "\" was removed from the town.");
        if (removeMe != null) {
            removeMe.sendMessage(ChatColor.DARK_RED + "You have been removed from " + removeFrom.getTownName() + " by " + senderWrapper.getPlayer().getName());
        }
    }

    public void removeSelfFromTown() {

        Town t = senderWrapper.getActiveTown();
        if (t == null) {
            senderWrapper.sendMessage(ERR + "You're either not a member of a town, or your active town isn't set.");
            senderWrapper.sendMessage("To set your active town to your own town, use /town active reset");
        }

        if (t.playerIsMayor(senderWrapper.getPlayer())) {
            senderWrapper.sendMessage(ERR + "You're the mayor. You need to specify a new mayor before leaving your current town.");
            return;
        }

        t.removePlayer(senderWrapper.getPlayer());


        senderWrapper.sendMessage(ChatColor.DARK_RED + "You have left " + senderWrapper.getActiveTown().getTownName() + ".");
    }

    public void setTownFriendlyFire(String sFriendlyFire) {
        if (senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        boolean friendlyFire;


        friendlyFire = sFriendlyFire.equalsIgnoreCase("on");


        t.setFriendlyFire(friendlyFire);

        senderWrapper.sendMessage(ChatColor.GREEN + "Friendly fire in " + t.getTownName() + " is now " + (friendlyFire ? "on" : "off") + ".");



    }

    public void setMOTD(String motd) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        t.setTownMOTD(motd);
        senderWrapper.sendMessage("Town MOTD has been set.");
    }

    public void printMOTD() {
        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }


        senderWrapper.sendMessage(t.getTownMOTD());
    }

    public void listResidents(String s_page) {
        int i;
        try {
            i = Integer.parseInt(s_page);
        } catch(NumberFormatException ex) {
            senderWrapper.sendMessage(ERR + "Error parsing token \"" + s_page + "\":" + ex.getMessage());
            return;
        }

        listResidents(i);
    }

    public void listResidents(int page) {
        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }
        senderWrapper.sendMessage(ChatColor.AQUA + "Players in " + t.getTownName() + "(page " + page + "):");

        String[] players = t.getResidentNames();

        for (int i = page - 1; i < players.length && i < i + 5; i++) {
            senderWrapper.sendMessage(ChatColor.YELLOW + players[i]);
        }
    }

    public void listResidents() {
        listResidents(1);
    }

    public void warpToSpawn() {
        if (!senderWrapper.hasExternalPermissions(Perms.WARP.toString())) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        senderWrapper.getPlayer().teleport(t.getTownSpawn(server));
        senderWrapper.sendMessage(ChatColor.DARK_GRAY + "Teleported to " + t.getTownName() + "! Welcome!");
    }

    public void warpToOtherSpawn(String townName) {
        if (!senderWrapper.hasExternalPermissions(Perms.WARP_FOREIGN.toString())) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = townManager.getTown(townName);

        if (t == null) {
            senderWrapper.sendMessage(ERR + "That town doesn't exist.");
            return;
        }

        senderWrapper.getPlayer().teleport(t.getTownSpawn(server));

        senderWrapper.sendMessage(INFO + "Teleported to " + t.getTownName() + "! Welcome!");


    }

    public void checkBlockBank(String blockName) {
        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        if (!BlockDataValueTranslator.blockExists(blockName)) {
            senderWrapper.sendMessage(ERR + "That block doesn't exist.");
            return;
        }

        int numBlocks = t.getBank().queryBlocks(BlockDataValueTranslator.getBlockID(blockName));

        senderWrapper.sendMessage(ChatColor.DARK_AQUA + "There are " + (numBlocks == -1 ? "0" : numBlocks) + " blocks of " + blockName + " in the bank.");
    }

    public void withdrawBlockBank(String blockName, String s_quantity) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }
        int quantity;

        try {
            quantity = Integer.parseInt(s_quantity);
        } catch (Exception e) {
            senderWrapper.sendMessage(ERR + "Error on parsing block quantity: not a valid integer.");
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
        }

        if (!t.playerIsInsideTownBorders(wgp, senderWrapper.getPlayer()) && !senderWrapper.hasExternalPermissions(Perms.WITHDRAW_BANK_OUTSIDE_BORDERS.toString())) {
            senderWrapper.sendMessage(ERR + "You must be within the borders of your town to withdraw from the bank.");
            return;
        }


        BlockBank bank = t.getBank();

        if (BlockDataValueTranslator.getBlockID(blockName) == -1) {
            senderWrapper.sendMessage(ERR + blockName + " is not a valid block name.");
            return;
        }

        if (bank.withdrawBlocks(BlockDataValueTranslator.getBlockID(blockName), quantity)) {
            Player p = senderWrapper.getPlayer();
            p.getInventory().addItem(new ItemStack(BlockDataValueTranslator.getBlockID(blockName), quantity));
            senderWrapper.sendMessage("Blocks withdrawn.");
        } else {
            senderWrapper.sendMessage(ERR + "Number out of valid range. Enter a number between 1 and the number of blocks in your bank.");
        }
    }

    public void depositBlockBank(String blockName, String s_quantity) {
        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(s_quantity);
        } catch (Exception e) {
            senderWrapper.sendMessage(ERR + "Error on parsing block quantity: not a valid integer.");
            return;
        }

        BlockBank bank = t.getBank();

        if (BlockDataValueTranslator.getBlockID(blockName) == -1) {
            senderWrapper.sendMessage(ERR + blockName + " is not a valid block name.");
            return;
        }

        if (!senderWrapper.getPlayer().getInventory().contains(BlockDataValueTranslator.getBlockID(blockName), quantity)) {
            senderWrapper.sendMessage(ERR + "You do not have enough " + blockName + " to deposit that much.");
            return;
        }

        if (bank.depositBlocks(BlockDataValueTranslator.getBlockID(blockName), quantity)) {
            Player p = senderWrapper.getPlayer();
            p.getInventory().removeItem(new ItemStack(BlockDataValueTranslator.getBlockID(blockName), quantity));
            senderWrapper.sendMessage("Blocks deposited.");
        } else {
            senderWrapper.sendMessage(ERR + "Invalid quantity. Please input a number greater than 0.");
        }

    }

    public void depositHeldItem(String quantity) {
        String blockName = null;

        if (senderWrapper.getPlayer().getItemInHand() == null) {
            senderWrapper.sendMessage(ERR + "There is no item in your hand!");
            return;
        }

        blockName = senderWrapper.getPlayer().getItemInHand().getType().toString();



        this.depositBlockBank(blockName, quantity);
    }

    public void withdrawCurrencyBank(String quantity) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (!options.isEconomyEnabled()) {
            senderWrapper.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        BigDecimal amt;
        try {
            amt = new BigDecimal(quantity);
        } catch (NumberFormatException nfe) {
            senderWrapper.sendMessage(ERR + "Error parsing quantity \"" + quantity + "\" : " + nfe.getMessage());
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        //DO the withdrawl from the town bank
        amt = t.getBank().withdrawCurrency(amt);


        economy.depositPlayer(senderWrapper.getPlayer().getName(), amt.doubleValue());
        senderWrapper.sendMessage(amt + " was withdrawn from " + t.getTownName() + "'s town bank and deposited into your account.");


    }

    public void depositCurrencyBank(String quantity) {
        if (!options.isEconomyEnabled()) {
            senderWrapper.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        BigDecimal amt;
        try {
            amt = new BigDecimal(quantity);
        } catch (NumberFormatException nfe) {
            senderWrapper.sendMessage(ERR + "Error parsing quantity \"" + quantity + "\" : " + nfe.getMessage());
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        EconomyResponse result = economy.withdrawPlayer(senderWrapper.getPlayer().getName(), amt.doubleValue());

        if (result.transactionSuccess()) {
            t.getBank().depositCurrency(amt);
            senderWrapper.sendMessage(quantity + " was withdrawn from your account and deposited into " + t.getTownName() + "'s town bank.");
        } else {
            senderWrapper.sendMessage(ERR + "Transaction failed; maybe you do not have enough money to do this?");
            senderWrapper.sendMessage(ChatColor.GOLD + "Actual amount deposited: " + result.amount);
        }

    }

    public void checkCurrencyBank() {
        if (!options.isEconomyEnabled()) {
            senderWrapper.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        senderWrapper.sendMessage(ChatColor.BLUE + "Amount of currency in bank: " + t.getBank().getCurrencyBalance());
    }

    public void listTerritories(String s_page) {
        int i;
        try {
            i = Integer.parseInt(s_page);
        } catch(NumberFormatException ex) {
            senderWrapper.sendMessage(ERR + "Error parsing token \"" + s_page + "\":" + ex.getMessage());
            return;
        }

        listTerritories(i);
    }

    public void listTerritories(int page) {

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }
        senderWrapper.sendMessage(ChatColor.AQUA + "Existing territories (page " + page + "):");



        Territory[] territs = t.getTerritoriesCollection().toArray(new Territory[t.getTerritoriesCollection().size()]);

        for (int i = page - 1; i < territs.length && i < i + 5; i++) {
            senderWrapper.sendMessage(ChatColor.YELLOW + territs[i].getName());
        }
    }

    public void listTerritories() {
        listTerritories(1);
    }


}
