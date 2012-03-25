/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command.executors;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.HashMap;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.command.ArgumentCountException;
import me.everdras.mctowns.command.MCTCommand;
import me.everdras.mctowns.command.handlers.TownHandler;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.townjoin.TownJoinManager;
import me.everdras.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Joshua
 */
public class TownExecutor extends BaseExecutor {

    public TownExecutor(MCTowns parent, WorldGuardPlugin wgp, Economy economy, Config options, TownManager townManager, TownJoinManager joinManager, HashMap<String, ActiveSet> activeSets, HashMap<Player, ActiveSet> potentialPlotBuyers) {
        super(parent, wgp, economy, options, townManager, joinManager, activeSets, potentialPlotBuyers);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        MCTCommand command = new MCTCommand(label, args);

        TownHandler handler = new TownHandler(parent, townManager, joinManager, cs, activeSets, wgp, economy, options, command);

        //A hard failure occurs when the failure occurs in the second argument (i.e. the command label was correct but the first argument was off, and so it should be handled by printing the usage listed in the plugin.yml
        //A soft failure occurs when the failure occurs in any argument after the second and should be handled by printing a finer error message. A soft failure will cause command to return true.
        boolean hardFailure = true, softFailure = true;

        String helpMessage = null;


        try {
            switch (command.get(1).toLowerCase()) {
                case "add":
                    helpMessage = "/town add (territory | assistant | player)";
                    switch (command.get(2).toLowerCase()) {
                        case "te":
                        case "territory":
                            helpMessage = "/town add territory <territory name>";
                            handler.addTerritorytoTown(command.get(3));
                            softFailure = false;
                            break;
                        case "assistant":
                            helpMessage = "/town add assistant <assistant name";
                            handler.promoteToAssistant(command.get(3));
                            softFailure = false;
                            break;
                        case "player":
                            helpMessage = "/town add player <player name>";
                            handler.invitePlayerToTown(command.get(3));
                            softFailure = false;
                    }
                    break;

                case "rm":
                case "remove":
                    helpMessage = "/town remove (territory | assistant | player | invite | request | self)";
                    switch (command.get(2).toLowerCase()) {
                        case "te":
                        case "territory":
                            helpMessage = "/town remove territory <territory name>";
                            handler.removeTerritoryFromTown(command.get(3));
                            softFailure = false;
                            break;

                        case "assistant":
                            helpMessage = "/town remove assistant <assistant name>";
                            handler.demoteFromAssistant(command.get(3));
                            softFailure = false;
                            break;

                        case "player":
                            helpMessage = "/town remove player <player name>";
                            handler.removePlayerFromTown(command.get(3));
                            softFailure = false;
                            break;

                        case "invite":
                            helpMessage = "/town remove invite <name of the player the invite is for>";
                            handler.cancelInvitation(command.get(3));
                            softFailure = false;
                            break;

                        case "request":
                            helpMessage = "/town remove request <name of the player the request is from";
                            handler.rejectRequest(command.get(3));
                            softFailure = false;
                            break;

                        case "self":
                            handler.removeSelfFromTown();
                            softFailure = false;
                            break;

                    }
                    break;

                case "list":
                case "ls":
                    switch (command.get(2).toLowerCase()) {
                        case "players":
                            if (command.hasArgAtIndex(3)) {
                                handler.listResidents(command.get(3));
                            }
                            else {
                                handler.listResidents();
                            }
                            break;

                        case "te":
                        case "territories":
                            if (command.hasArgAtIndex(3)) {
                                handler.listTerritories(command.get(3));
                            }
                            else {
                                handler.listTerritories();
                            }
                            break;

                        case "requests":
                            handler.listRequestsForTown();
                            break;

                        case "invites":
                            handler.listInvitesForTown();
                            break;

                    }
                    break;

                case "active":
                    helpMessage = "/town active <town name>";
                    handler.setActiveTown(command.get(2));
                    break;

                case "bank":
                    helpMessage = "/town bank (deposit | withdraw | check)";
                    switch (command.get(2).toLowerCase()) {
                        case "deposit":
                            helpMessage = "/town bank deposit (currency | <block name>)";
                            switch (command.get(3)) {
                                case "currency":
                                    helpMessage = "/town bank deposit currency <quantity>";
                                    handler.depositCurrencyBank(command.get(3));
                                    softFailure = false;
                                    break;

                                case "hand":
                                    helpMessage = "/town bank deposit hand <quantity>";
                                    handler.depositHeldItem(command.get(3));
                                    softFailure = false;
                                    break;
                                default:
                                    helpMessage = "/town bank deposit <block name> <quantity>";
                                    handler.depositBlockBank(command.get(3), command.get(4));
                                    softFailure = false;

                            }
                            break;

                        case "withdraw":
                            helpMessage = "/town bank withdraw (currency | <block name>)";
                            switch (command.get(3)) {
                                case "currency":
                                    helpMessage = "/town bank withdraw currency <quantity>";
                                    handler.withdrawCurrencyBank(command.get(3));
                                    softFailure = false;
                                    break;

                                default:
                                    helpMessage = "/town bank withdraw <block name> <quantity>";
                                    handler.withdrawBlockBank(command.get(3), command.get(4));
                                    softFailure = false;

                            }
                            break;

                        case "check":
                            helpMessage = "/town bank check (currency | <block name>)";
                            switch (command.get(3)) {
                                case "currency":
                                    helpMessage = "/town bank check currency";
                                    handler.checkCurrencyBank();
                                    softFailure = false;
                                    break;

                                default:
                                    helpMessage = "/town bank deposit <block name>";
                                    handler.checkBlockBank(command.get(3));
                                    softFailure = false;
                            }
                            break;
                    }
                    break;

                case "spawn":
                    if (command.hasArgAtIndex(2)) {
                        if (command.getArgAtIndex(2).equals("set")) {
                            handler.setTownSpawn();
                        }
                        else {
                            handler.warpToOtherSpawn(command.get(2));
                        }
                    }
                    else {
                        handler.warpToSpawn();
                    }
                    softFailure = false;
                    break;

                case "econ":
                case "economy":
                    helpMessage = "/town economy (buyableplots)";
                    switch (command.get(2).toLowerCase()) {
                        case "buyableplots":
                            helpMessage = "/town economy buyableplots (true/false)";
                            handler.setTownPlotBuyability(command.get(3));
                            softFailure = false;
                            break;
                    }
                    break;
                case "joinmethod":
                    helpMessage = "/town joinmethod <invitation/economy>";
                    handler.setTownJoinMethod(command.get(2));
                    softFailure = false;
                    break;

                case "motd":
                    if (command.hasArgAtIndex(2)) {
                        handler.setMOTD(command.concatAfter(3));
                    }
                    else {
                        handler.printMOTD();
                    }
                    softFailure = false;
                    break;

                case "setmayor":
                    helpMessage = "/town setmayor <resident name>";
                    handler.setMayor(command.get(2));
                    softFailure = false;
                    break;

                case "pvp":
                    switch (command.get(2).toLowerCase()) {
                        case "friendlyfire":
                            helpMessage = "/town pvp friendlyfire <true/false>";
                            handler.setTownFriendlyFire(command.get(3));
                            softFailure = false;
                            break;
                    }
                    break;

            }
            hardFailure = false;

        } catch (ArgumentCountException ex) {
            if (ex.getErrorIndex() == 1) {
                hardFailure = true;
            }
            else {
                softFailure = true;
                hardFailure = false;
            }

        }

        if (!hardFailure && softFailure && helpMessage != null) {
            cs.sendMessage(ChatColor.RED + "Invalid command. Acceptable similar formats are: ");
            cs.sendMessage(ChatColor.DARK_AQUA + helpMessage);
        }


        return !hardFailure;
    }
}