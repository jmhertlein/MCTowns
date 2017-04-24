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

import cafe.josh.mctowns.TownManager;
import cafe.josh.mctowns.permission.Perms;
import cafe.josh.mctowns.region.MCTownsRegion;
import cafe.josh.mctowns.region.Town;
import cafe.josh.mctowns.region.TownLevel;
import cafe.josh.mctowns.townjoin.TownJoinMethodFormatException;
import cafe.josh.mctowns.util.Players;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import cafe.josh.mctowns.MCTowns;
import cafe.josh.mctowns.MCTownsPlugin;
import cafe.josh.mctowns.townjoin.TownJoinMethod;
import cafe.josh.mctowns.util.UUIDs;
import cafe.josh.mctowns.util.WGUtils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static net.jmhertlein.core.chat.ChatUtil.*;
import cafe.josh.reflective.CommandDefinition;
import cafe.josh.reflective.annotation.CommandMethod;
import cafe.josh.mctowns.util.MCTConfig;
import org.bukkit.command.CommandSender;

/**
 * @author Everdras
 */
public class TownHandler extends CommandHandler implements CommandDefinition {

    public TownHandler(MCTownsPlugin parent) {
        super(parent);
    }

    @CommandMethod(path = "town active", requiredArgs = 1)
    public void setActiveTown(CommandSender s, String[] args) {
        setNewCommand(s);
        Town t = townManager.getTown(args[0]);
        if(t == null) {
            localSender.sendMessage(ERR + "The town \"" + args[0] + "\" does not exist.");
            return;
        }

        if(!localSender.hasExternalPermissions(Perms.ADMIN.toString()) && !t.playerIsResident(localSender.getPlayer())) {
            localSender.notifyInsufPermissions();
            return;
        }

        localSender.setActiveTown(townManager.getTown(args[0]));
        localSender.sendMessage("Active town set to " + args[0]);

    }

    @CommandMethod(path = "town active reset")
    public void resetActiveTown(CommandSender s) {
        setNewCommand(s);
        List<Town> t = townManager.matchPlayerToTowns((Player) localSender.getSender());

        if(t.isEmpty()) {
            localSender.sendMessage(ERR + "Unable to match you to a town. Are you sure you belong to one?");
            return;
        }

        localSender.setActiveTown(t.get(0));
        localSender.sendMessage(ChatColor.LIGHT_PURPLE + "Active town reset to " + t.get(0).getName() + ".");
    }

    @CommandMethod(path = "town spawn set", filters = {"mayoralPerms"})
    public void setTownSpawn(CommandSender s) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(!t.playerIsInsideTownBorders(localSender.getPlayer())) {
            localSender.sendMessage(ERR + "You need to be inside your town borders to do that.");
            return;
        }

        t.setSpawn(localSender.getPlayer().getLocation());
        localSender.sendMessage("Town spawn location updated.");
    }

    @CommandMethod(path = "town joinmethod", requiredArgs = 1, filters = {"mayoralPerms"})
    public void setTownJoinMethod(CommandSender s, String[] args) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        TownJoinMethod method;
        try {
            method = TownJoinMethod.parseMethod(args[0]);
        } catch(TownJoinMethodFormatException ex) {
            localSender.sendMessage(ERR + ex.getMessage());
            return;
        }

        if(method == TownJoinMethod.ECONOMY) {
            t.setEconomyJoins(true);
        } else if(method == TownJoinMethod.INVITATION) {
            t.setEconomyJoins(false);
        }

        localSender.sendMessage(SUCC + "Town join method updated.");

    }

    @CommandMethod(path = "town economy buyableplots", requiredArgs = 1, filters = {"mayoralPerms"})
    public void setTownPlotBuyability(CommandSender s, String[] args) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(!MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            localSender.sendMessage(ERR + "The economy is not enabled for your server.");
            return;
        }

        boolean buyability;

        try {
            buyability = Boolean.parseBoolean(args[0]);
        } catch(Exception e) {
            localSender.sendMessage(ERR + "Error in parsing boolean: expected true/false, found " + args[0]);
            return;
        }

        t.setBuyablePlots(buyability);
        if(buyability) {
            localSender.sendMessage(ChatColor.GOLD + t.getName() + "'s plots can now be sold and new plots are buyable by default.");
        } else {
            localSender.sendMessage(ChatColor.GOLD + t.getName() + "'s plots are no longer for sale.");
        }

    }

    @CommandMethod(path = "town economy defaultplotprice", requiredArgs = 1, filters = {"mayoralPerms"})
    public void setDefaultPlotPrice(CommandSender s, String rawAmount) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
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
        } catch(NumberFormatException nfe) {
            localSender.sendMessage(ERR + "Error parsing plot price: " + nfe.getMessage());
            return;
        }

        t.setDefaultPlotPrice(price);
        localSender.sendMessage(SUCC + "The default price of plots in " + t.getName() + " has been set to " + price);

    }

    @CommandMethod(path = "town add territory", requiredArgs = 1)
    public void addTerritorytoTown(CommandSender s, String[] args) {
        setNewCommand(s);
        boolean admin = localSender.hasExternalPermissions(Perms.ADMIN.toString());

        if(!(MCTConfig.MAYORS_CAN_BUY_TERRITORIES.getBoolean() || admin)) {
            localSender.sendMessage(ChatColor.BLUE + "Mayors are not allowed to add territories and you're not an admin.");
            return;
        }

        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if((t.getSize() < MCTConfig.MIN_NUM_PLAYERS_TO_BUY_TERRITORY.getInt()) && !admin) {
            localSender.sendMessage(ERR + "You don't have enough people in your town to buy territories yet.");
            localSender.sendMessage(ERR + "You have " + t.getSize() + " people, but you need a total of " + MCTConfig.MIN_NUM_PLAYERS_TO_BUY_TERRITORY.getInt() + "!");
            return;
        }

        String territName = MCTownsRegion.formatRegionName(t, TownLevel.TERRITORY, args[0]);

        World w = localSender.getPlayer().getWorld();

        if(w == null) {
            localSender.sendMessage(ERR + "You are in an invalid World. (Player#getWorld() returned null)");
            return;
        }

        ProtectedRegion region = this.getSelectedRegion(territName);

        if(region == null) {
            localSender.sendMessage(ERR + "No region selected!");
            return;
        }

        int max = MCTConfig.TERRITORY_XZ_SIZE_LIMIT.getInt(),
                cur = WGUtils.getNumXZBlocksInRegion(region);
        if(cur > max && !localSender.hasExternalPermissions(Perms.ADMIN.toString())) {
            localSender.sendMessageF("%sYou're not allowed to make a territory that big. (Current: %s, Limit: %s)", ERR, cur, max);
            return;
        }

        BigDecimal colPrice = new BigDecimal(MCTConfig.PRICE_PER_XZ_BLOCK.getString());
        //charge the player if they're not running this as an admin and buyable territories is enabled and the price is more than 0
        if(!admin && colPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal price = colPrice.multiply(new BigDecimal(WGUtils.getNumXZBlocksInRegion(region)));

            if(t.getBank().getCurrencyBalance().compareTo(price) < 0) {
                //If they can't afford it...
                localSender.sendMessage(ERR + "There is not enough money in your " + INFO + "town's bank account" + ERR + " to buy a region that large.");
                localSender.sendMessage(ERR + "Total Price: " + price);
                localSender.sendMessage(INFO + "Add money to your town's bank with: /town bank deposit currency <amount>");
                return;
            }

            //otherwise...
            t.getBank().withdrawCurrency(price);

            localSender.sendMessage(ChatColor.GREEN + "Purchase success! Total price was: " + price.toString());
        }

        try {
            townManager.addTerritory(territName, w, region, t);
        } catch(TownManager.InvalidWorldGuardRegionNameException | TownManager.RegionAlreadyExistsException ex) {
            localSender.sendMessage(ERR + ex.getLocalizedMessage());
            return;
        }

        region.getOwners().addPlayer(UUIDs.getNameForUUID(t.getMayor()));
        for(String assistantName : t.getAssistantNames()) {
            region.getOwners().addPlayer(assistantName);
        }

        localSender.sendMessage(SUCC + "Territory added.");

        localSender.setActiveTerritory(townManager.getTerritory(territName));
        localSender.sendMessage(ChatColor.LIGHT_PURPLE + "Active territory set to newly created territory.");

    }

    @CommandMethod(path = "town remove territory", requiredArgs = 1, filters = {"mayoralPerms"})
    public void removeTerritoryFromTown(CommandSender s, String[] args) {
        setNewCommand(s);

        Town to = localSender.getActiveTown();

        if(to == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        String territName = MCTownsRegion.formatRegionName(to, TownLevel.TERRITORY, args[0]);

        if(!townManager.removeTerritory(territName)) {
            localSender.sendMessage(ERR + "Error: Territory \"" + territName + "\" does not exist and was not removed (because it doesn't exist!)");
        } else {
            localSender.sendMessage(SUCC + "Territory removed.");
        }
    }

    @CommandMethod(path = "town add player", requiredArgs = 1, filters = {"mayoralPerms"})
    public void invitePlayerToTown(CommandSender s, String[] args) {
        setNewCommand(s);
        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(t.usesEconomyJoins()) {
            localSender.sendMessage(t.getName() + " doesn't use the invitation system.");
            return;
        }

        OfflinePlayer p = server.getOfflinePlayer(args[0]);
        if(!p.isOnline() && !Players.playedHasEverLoggedIn(p)) {
            localSender.sendMessage(ERR + args[0] + " has never played on this server before.");
            return;
        }

        if(!MCTConfig.PLAYERS_CAN_JOIN_MULTIPLE_TOWNS.getBoolean() && townManager.playerIsAlreadyInATown(p)) {
            localSender.sendMessage(ERR + p.getName() + " is already in a town.");
            return;
        }

        if(t.playerIsResident(p)) {
            localSender.sendMessage(ERR + p.getName() + " is already a member of " + t.getName());
            return;
        }

        if(joinManager.getIssuedInvitesForTown(t).contains(p.getName())) {
            localSender.sendMessage(ERR + p.getName() + " is already invited to join " + t.getName());
            return;
        }

        for(Player pl : Bukkit.getOnlinePlayers()) {
            if(pl.getName().equalsIgnoreCase(p.getName()) && !pl.getName().equals(p.getName())) {
                localSender.sendMessage(INFO + "NOTE: You invited " + p.getName() + ", did you mean to invite " + pl.getName() + "? (Names are CaSe SeNsItIvE!)");
            }
        }

        if(joinManager.requestExists(p.getName(), t)) {
            t.addPlayer(p);
            if(p.isOnline()) {
                p.getPlayer().sendMessage("You have joined " + t.getName() + "!");
            }
            broadcastTownJoin(t, p.getName());
            joinManager.clearRequest(p.getName(), t);
        } else {
            joinManager.invitePlayerToTown(p.getName(), t);
            localSender.sendMessage(SUCC + p.getName() + " has been invited to join " + t.getName() + ".");
            if(p.isOnline()) {
                p.getPlayer().sendMessage(ChatColor.DARK_GREEN + "You have been invited to join the town " + t.getName() + "!");
                p.getPlayer().sendMessage(ChatColor.DARK_GREEN + "To join, type /mct join " + t.getName());
            }
        }
    }

    @CommandMethod(path = "town add assistant", requiredArgs = 1)
    public void promoteToAssistant(CommandSender s, String[] args) {
        setNewCommand(s);
        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(!(localSender.hasExternalPermissions(Perms.ADMIN.toString()) || t.playerIsMayor(localSender.getPlayer()))) {
            localSender.notifyInsufPermissions();
            return;
        }

        OfflinePlayer p = server.getOfflinePlayer(args[0]);
        if(!Players.playedHasEverLoggedIn(p)) {
            localSender.sendMessage(ERR + args[0] + " has never played on this server before.");
            return;
        }

        if(t.playerIsMayor(p)) {
            localSender.sendMessage(ERR + "That player is already the mayor of the town.");
            return;
        }

        if(!t.playerIsResident(p)) {
            localSender.sendMessage(ERR + p.getName() + " is not a resident of " + t.getName() + ".");
            return;
        }

        if(t.addAssistant(p)) {
            for(String territName : t.getTerritoriesCollection()) {
                townManager.getTerritory(territName).addPlayer(p);
            }

            localSender.sendMessage(args[0] + " has been promoted to an assistant of " + t.getName() + ".");

            if(p.isOnline()) {
                p.getPlayer().sendMessage("You are now an Assistant Mayor of " + t.getName());
            }
        } else {
            localSender.sendMessage(ERR + args[0] + " is already an assistant in this town.");
        }
    }

    @CommandMethod(path = "town remove assistant", requiredArgs = 1)
    public void demoteFromAssistant(CommandSender s, String[] args) {
        setNewCommand(s);
        Town t = localSender.getActiveTown();
        OfflinePlayer p = server.getOfflinePlayer(args[0]);

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(!(localSender.hasExternalPermissions(Perms.ADMIN.toString()) || t.playerIsMayor(localSender.getPlayer()))) {
            localSender.notifyInsufPermissions();
            return;
        }

        if(!Players.playedHasEverLoggedIn(p)) {
            localSender.sendMessage(ERR + args[0] + " has never played on this server before.");
            return;
        }

        if(!t.playerIsResident(p)) {
            localSender.sendMessage(ERR + args[0] + " is not a resident of " + t.getName() + ".");
            return;
        }

        if(t.removeAssistant(p)) {
            localSender.sendMessage(p.getName() + " has been demoted.");
            if(p.isOnline()) {
                p.getPlayer().sendMessage(ChatColor.DARK_RED + "You are no longer an assistant mayor for " + t.getName());
            }

            for(String territName : t.getTerritoriesCollection()) {
                townManager.getTerritory(territName).removePlayer(p);
            }
        } else {
            localSender.sendMessage(ERR + p.getName() + " is not an assistant in this town.");
        }
    }

    @CommandMethod(path = "town mayor set", requiredArgs = 1)
    public void setMayor(CommandSender s, String[] args) {
        setNewCommand(s);
        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        Player p = server.getPlayerExact(args[0]);

        if(!(localSender.hasExternalPermissions("ADMIN") || t.getMayor().equals(localSender.getPlayer().getUniqueId()))) {
            localSender.notifyInsufPermissions();
            return;
        }

        if(p == null) {
            localSender.sendMessage(ERR + args[0] + " either does not exist or is not online.");
            return;
        }

        if(!t.playerIsResident(p)) {
            localSender.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        localSender.getActiveTown().setMayor(p);
        t.broadcastMessageToTown("The mayor of " + t.getName() + " is now " + p.getName() + "!");
    }

    @CommandMethod(path = "town remove invite", requiredArgs = 1, filters = {"mayoralPerms"})
    public void cancelInvitation(CommandSender s, String[] args) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(joinManager.clearInvitationForPlayerFromTown(args[0], t)) {
            localSender.sendMessage(ChatColor.GOLD + "The invitation for " + args[0] + " has been withdrawn.");
        } else {
            localSender.sendMessage(ERR + args[0] + " does not have any pending invitations from " + t.getName() + ".");
        }
    }

    @CommandMethod(path = "town remove request", requiredArgs = 1, filters = {"mayoralPerms"})
    public void rejectRequest(CommandSender s, String[] args) {
        setNewCommand(s);
        String playerName = args[0];

        Player p = server.getPlayer(playerName);
        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(!joinManager.clearRequest((p == null ? playerName : p.getName()), t)) {
            localSender.sendMessage(ERR + "No matching request found.");
        } else {
            localSender.sendMessage(ChatColor.GOLD + (p == null ? playerName : p.getName()) + "'s request has been rejected.");

            if(p != null) {
                p.sendMessage(ChatColor.DARK_RED + "Your request to join " + t.getName() + " has been rejected.");
            }
        }

    }

    @CommandMethod(path = "town list requests", filters = {"mayoralPerms"})
    public void listRequestsForTown(CommandSender s) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }
        Set<String> playerNames = joinManager.getPlayersRequestingMembershipToTown(t);

        localSender.sendMessage(ChatColor.DARK_BLUE + "There are pending requests from:");

        for(String str : getOutputFriendlyTownJoinListMessages(playerNames)) {
            localSender.sendMessage(ChatColor.YELLOW + str);
        }

    }

    @CommandMethod(path = "town list invites", filters = {"mayoralPerms"})
    public void listInvitesForTown(CommandSender s) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        Set<String> invitedPlayers = joinManager.getIssuedInvitesForTown(t);

        localSender.sendMessage(ChatColor.DARK_BLUE + "There are pending invites for:");

        for(String str : getOutputFriendlyTownJoinListMessages(invitedPlayers)) {
            localSender.sendMessage(ChatColor.YELLOW + str);
        }
    }

    @CommandMethod(path = "town remove player", requiredArgs = 1, filters = {"mayoralPerms"})
    public void removePlayerFromTown(CommandSender s, String[] args) {
        setNewCommand(s);

        OfflinePlayer removeMe = server.getOfflinePlayer(args[0]);
        Town removeFrom = localSender.getActiveTown();

        if(!Players.playedHasEverLoggedIn(removeMe)) {
            localSender.sendMessage(ERR + "No player named '" + args[0] + "' has ever played on this server.");
            return;
        }

        if(removeFrom == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(!removeFrom.playerIsResident(removeMe)) {
            localSender.sendMessage(ERR + removeMe.getName() + " is not a resident of " + removeFrom.getName() + ".");
            return;
        }

        if(removeFrom.playerIsMayor(removeMe)) {
            localSender.sendMessage(ERR + "A mayor cannot be removed from his own town.");
            return;
        }

        if(removeFrom.playerIsAssistant(removeMe) && !localSender.hasExternalPermissions(Perms.ADMIN.toString()) && !removeFrom.playerIsMayor(localSender.getPlayer())) {
            localSender.sendMessage(ERR + "Only the mayor or admins can remove assistants from the town.");
            return;
        }

        localSender.getActiveTown().removePlayer(removeMe);

        Town.recursivelyRemovePlayerFromTown(removeMe, removeFrom);

        localSender.sendMessage("\"" + removeMe.getName() + "\" was removed from the town.");
        Player onlinePlayer = removeMe.getPlayer();
        if(onlinePlayer != null) {
            onlinePlayer.sendMessage(ChatColor.DARK_RED + "You have been removed from " + removeFrom.getName() + " by " + localSender.getPlayer().getName());
        }
    }

    @CommandMethod(path = "town remove self")
    public void removeSelfFromTown(CommandSender s) {
        setNewCommand(s);
        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.sendMessage(ERR + "You're either not a member of a town, or your active town isn't set.");
            localSender.sendMessage("To set your active town to your own town, use /town active reset");
            return;
        }

        if(t.playerIsMayor(localSender.getPlayer())) {
            localSender.sendMessage(ERR + "You're the mayor. You need to specify a new mayor before leaving your current town.");
            return;
        }

        t.removePlayer(localSender.getPlayer());

        localSender.sendMessage(ChatColor.DARK_RED + "You have left " + localSender.getActiveTown().getName() + ".");
    }

    @CommandMethod(path = "town pvp friendlyfire", requiredArgs = 1, filters = {"mayoralPerms"})
    public void setTownFriendlyFire(CommandSender s, String[] args) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        boolean friendlyFire;

        friendlyFire = args[0].equalsIgnoreCase("on");

        t.setFriendlyFire(friendlyFire);

        localSender.sendMessage(ChatColor.GREEN + "Friendly fire in " + t.getName() + " is now " + (friendlyFire ? "on" : "off") + ".");

    }

    @CommandMethod(path = "town motd set", requiredArgs = 1, filters = {"mayoralPerms"})
    public void setMOTD(CommandSender s, String[] args) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        t.setTownMOTD(String.join(" ", args));
        localSender.sendMessage("Town MOTD has been set.");
    }

    @CommandMethod(path = "town motd")
    public void printMOTD(CommandSender s) {
        setNewCommand(s);
        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        localSender.sendMessage(t.getTownMOTD());
    }

    @CommandMethod(path = "town list players")
    public void listResidents(CommandSender s, String[] args) {
        setNewCommand(s);
        int page;
        if(args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch(NumberFormatException nfe) {
                localSender.sendMessage(String.format("Couldn't parse \"%s\" into an integer.", args[0]));
                return;
            }
        } else {
            page = 1;
        }

        page--; //shift to 0-indexing

        if(page < 0) {
            localSender.sendMessage(ERR + "Invalid page.");
            return;
        }

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }
        localSender.sendMessage(ChatColor.AQUA + "Players in " + t.getName() + "(page " + page + "):");

        Set<String> names = t.getResidentNames();
        String[] players = names.toArray(new String[names.size()]);

        for(int i = page * RESULTS_PER_PAGE; i < players.length && i < page * RESULTS_PER_PAGE + RESULTS_PER_PAGE; i++) {
            localSender.sendMessage(ChatColor.YELLOW + players[i]);
        }
    }

    @CommandMethod(path = "town add warp", requiredArgs = 1, filters = {"mayoralPerms"})
    public void addWarp(Player p, String warpName) {
        setNewCommand(p);

        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(!t.playerIsInsideTownBorders(p)) {
            p.sendMessage(ChatColor.RED + "You need to be inside your town to do this.");
            return;
        }

        if(t.putWarp(warpName, p.getLocation()) == null) {
            p.sendMessage(ChatColor.GREEN + "Warp added.");
        } else {
            p.sendMessage(ChatColor.GREEN + "Warp updated.");
        }
    }

    @CommandMethod(path = "town remove warp", requiredArgs = 1, filters = {"mayoralPerms"})
    public void deleteWarp(Player s, String warpName) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        if(t.removeWarp(warpName) == null) {
            localSender.sendMessage(ChatColor.YELLOW + "No warp named \"" + warpName + "\" to delete.");
        } else {
            localSender.sendMessage(ChatColor.GREEN + "Warp \"" + warpName + "\" deleted.");
        }
    }

    @CommandMethod(path = "town list warps")
    public void listWarps(Player p) {
        setNewCommand(p);
        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        localSender.sendMessage(ChatColor.GOLD + "Warps in " + t.getName() + ":");
        t.getWarps().stream().forEach(w -> localSender.sendMessage(ChatColor.GOLD + w));
    }

    @CommandMethod(path = "town warp", requiredArgs = 1)
    public void useWarp(Player p, String warpName) {
        setNewCommand(p);
        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        Location dest = t.getWarp(warpName);
        if(dest == null) {
            p.sendMessage(ChatColor.RED + "No warp named " + warpName);
            p.sendMessage(ChatColor.RED + "Similar warps:");
            t.getWarps().stream()
                    .filter(w -> w.startsWith(warpName.substring(0, 1)))
                    .forEach(w -> p.sendMessage(w));
            return;
        }

        if(t.playerIsResident(p)) {
            p.teleport(dest);
        } else {
            p.sendMessage(ChatColor.RED + "You need to be a member of this town to use its warps.");
        }
    }

    @CommandMethod(path = "town spawn")
    public void warpToOtherSpawn(CommandSender s, String[] args) {
        setNewCommand(s);
        Town t;
        if(args.length > 0) {
            t = townManager.getTown(args[0]);
        } else {
            t = localSender.getActiveTown();
        }

        if(t == null) {
            if(args.length == 0) {
                localSender.sendMessage(ERR + "You don't have an active town.");
            } else {
                localSender.sendMessage(ERR + "That town doesn't exist.");
            }

            return;
        }

        if(!t.playerIsResident(localSender.getPlayer()) && !localSender.hasExternalPermissions(Perms.WARP_FOREIGN.toString())) {
            localSender.notifyInsufPermissions();
            return;
        }

        localSender.getPlayer().teleport(t.getSpawn());
        localSender.sendMessage(INFO + "Teleported to " + t.getName() + "! Welcome!");

    }

    @CommandMethod(path = "town bank withdraw blocks", filters = {"mayoralPerms"})
    public void openBlockBank(CommandSender s) {
        setNewCommand(s);

        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        localSender.getPlayer().openInventory(t.getBank().getBankInventory());
    }

    @CommandMethod(path = "town bank deposit blocks")
    public void openBankDepositBox(CommandSender s) {
        setNewCommand(s);
        Town t = localSender.getActiveTown();
        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        localSender.getPlayer().openInventory(t.getBank().getNewDepositBox(localSender.getPlayer()));
    }

    @CommandMethod(path = "town bank withdraw currency", requiredArgs = 1, filters = {"mayoralPerms"})
    public void withdrawCurrencyBank(CommandSender s, String rawAmount) {
        setNewCommand(s);

        if(!MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        if(!Pattern.compile(MCTConfig.CURRENCY_INPUT_PATTERN.getString()).matcher(rawAmount).matches())
        {
            localSender.sendMessage(ERR + "Invalid currency input: " + rawAmount);
            return;
        }

        BigDecimal amt;
        try {
            amt = new BigDecimal(rawAmount);
        } catch(NumberFormatException nfe) {
            localSender.sendMessage(ERR + "Error parsing quantity \"" + rawAmount + "\" : " + nfe.getMessage());
            return;
        }

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        //Do the withdrawl from the town bank
        amt = t.getBank().withdrawCurrency(amt);

        MCTowns.getEconomy().depositPlayer(localSender.getPlayer().getName(), amt.doubleValue());
        localSender.sendMessage(amt + " was withdrawn from " + t.getName() + "'s town bank and deposited into your account.");
    }

    @CommandMethod(path = "town bank deposit currency")
    public void depositCurrencyBank(CommandSender s, String rawAmount) {
        setNewCommand(s);
        if(!MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        if(!Pattern.compile(MCTConfig.CURRENCY_INPUT_PATTERN.getString()).matcher(rawAmount).matches())
        {
            localSender.sendMessage(ERR + "Invalid currency input: " + rawAmount);
            return;
        }

        BigDecimal amt;
        try {
            amt = new BigDecimal(rawAmount);
        } catch(NumberFormatException nfe) {
            localSender.sendMessage(ERR + "Error parsing quantity \"" + rawAmount + "\" : " + nfe.getMessage());
            return;
        }

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        EconomyResponse result = MCTowns.getEconomy().withdrawPlayer(localSender.getPlayer().getName(), amt.doubleValue());

        if(result.transactionSuccess()) {
            t.getBank().depositCurrency(amt);
            localSender.sendMessage(rawAmount + " was withdrawn from your account and deposited into " + t.getName() + "'s town bank.");
        } else {
            localSender.sendMessage(ERR + "Transaction failed; maybe you do not have enough money to do this?");
            localSender.sendMessage(ChatColor.GOLD + "Actual amount deposited: " + result.amount);
        }

    }

    @CommandMethod(path = "town bank currency check")
    public void checkCurrencyBank(CommandSender s) {
        setNewCommand(s);
        if(!MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            localSender.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        localSender.sendMessage(ChatColor.BLUE + "Amount of currency in bank: " + t.getBank().getCurrencyBalance());
    }

    @CommandMethod(path = "town list territories")
    public void listTerritories(CommandSender s, String[] args) {
        setNewCommand(s);
        int i;
        if(args.length > 0) {
            try {
                i = Integer.parseInt(args[0]);
            } catch(NumberFormatException ex) {
                localSender.sendMessage(ERR + "Error parsing token \"" + args[0] + "\":" + ex.getMessage());
                return;
            }
        } else {
            i = 1;
        }
        listTerritories(i);
    }

    private void listTerritories(int page) {
        page--; //shift to 0-indexing

        if(page < 0) {
            localSender.sendMessage(ERR + "Invalid page.");
            return;
        }

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }
        localSender.sendMessage(ChatColor.AQUA + "Existing territories (page " + page + "):");

        String[] territs = t.getTerritoriesCollection().toArray(new String[t.getTerritoriesCollection().size()]);

        for(int i = page * RESULTS_PER_PAGE; i < territs.length && i < page * RESULTS_PER_PAGE + RESULTS_PER_PAGE; i++) {
            localSender.sendMessage(ChatColor.YELLOW + territs[i]);
        }
    }
}
