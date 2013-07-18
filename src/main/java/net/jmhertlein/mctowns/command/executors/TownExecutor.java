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
package net.jmhertlein.mctowns.command.executors;

import net.jmhertlein.core.command.ArgumentCountException;
import net.jmhertlein.core.command.ECommand;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.command.handlers.TownHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Joshua
 */
public class TownExecutor extends BaseExecutor {

    private TownHandler handler;

    public TownExecutor(MCTownsPlugin parent) {
        super(parent);
        handler = new TownHandler(parent);
    }

    @Override
    public boolean runCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        ECommand command = new ECommand(label, args);

        handler.setNewCommand(cs, command);

        //A hard failure occurs when the failure occurs in the second argument (i.e. the command label was correct but the first argument was off, and so it should be handled by printing the usage listed in the plugin.yml
        //A soft failure occurs when the failure occurs in any argument after the second and should be handled by printing a finer error message. A soft failure will cause command to return true.
        boolean hardFailure = false, softFailure = true;

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
                            
                        case "a":
                        case "assistant":
                            helpMessage = "/town add assistant <assistant name";
                            handler.promoteToAssistant(command.get(3));
                            softFailure = false;
                            break;
                            
                        case "p":
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
                            
                        case "a":
                        case "assistant":
                            helpMessage = "/town remove assistant <assistant name>";
                            handler.demoteFromAssistant(command.get(3));
                            softFailure = false;
                            break;

                        case "p":
                        case "player":
                            helpMessage = "/town remove player <player name>";
                            handler.removePlayerFromTown(command.get(3));
                            softFailure = false;
                            break;

                        case "i":
                        case "invite":
                            helpMessage = "/town remove invite <name of the player the invite is for>";
                            handler.cancelInvitation(command.get(3));
                            softFailure = false;
                            break;

                        case "r":
                        case "request":
                            helpMessage = "/town remove request <name of the player the request is from";
                            handler.rejectRequest(command.get(3));
                            softFailure = false;
                            break;

                        case "s":
                        case "self":
                            handler.removeSelfFromTown();
                            softFailure = false;
                            break;

                    }
                    break;

                case "list":
                case "ls":
                    helpMessage = "/town list (territories | players | requests | invites)";
                    switch (command.get(2).toLowerCase()) {
                        case "players":
                            if (command.hasArgAtIndex(3)) {
                                handler.listResidents(command.get(3));
                            } else {
                                handler.listResidents();
                            }
                            softFailure = false;
                            break;

                        case "te":
                        case "territories":
                            if (command.hasArgAtIndex(3)) {
                                handler.listTerritories(command.get(3));
                            } else {
                                handler.listTerritories();
                            }
                            softFailure = false;
                            break;

                        case "requests":
                            handler.listRequestsForTown();
                            softFailure = false;
                            break;

                        case "invites":
                            handler.listInvitesForTown();
                            softFailure = false;
                            break;

                    }
                    break;

                case "active":
                    helpMessage = "/town active (reset | <town name>)";
                    if (command.get(2).equalsIgnoreCase("reset")) {
                        handler.resetActiveTown();
                    } else {
                        handler.setActiveTown(command.get(2));
                    }
                    softFailure = false;
                    break;
                case "b":
                case "bank":
                    helpMessage = "/town bank (deposit | check)";
                    switch (command.get(2).toLowerCase()) {
                        case "d":
                        case "deposit":
                            helpMessage = "/town bank deposit (currency | blocks)";
                            switch (command.get(3)) {
                                case "c":
                                case "currency":
                                    helpMessage = "/town bank deposit currency <quantity>";
                                    handler.depositCurrencyBank(command.get(4));
                                    softFailure = false;
                                    break;
                                    
                                case "b":
                                case "blocks":
                                    helpMessage = "/town bank deposit";
                                    handler.openBankDepositBox();
                                    softFailure = false;

                            }
                            break;
                        case "w":
                        case "withdraw":
                            helpMessage = "/town bank withdraw (currency | blocks)";
                            switch (command.get(3)) {
                                case "c":
                                case "currency":
                                    helpMessage = "/town bank withdraw currency <quantity>";
                                    handler.withdrawCurrencyBank(command.get(4));
                                    softFailure = false;
                                    break;

                                case "b":
                                case "blocks":
                                    helpMessage = "/town bank withdraw blocks";
                                    handler.openBlockBank();
                                    softFailure = false;

                            }
                            break;
                        case "c":
                        case "check":
                            helpMessage = "/town bank check (currency | blocks)";
                            
                            switch (command.get(3)) {
                                case "c":
                                case "currency":
                                    helpMessage = "/town bank check currency";
                                    handler.checkCurrencyBank();
                                    softFailure = false;
                                    break;
                                case "b":
                                case "blocks":
                                    helpMessage = "/town bank check";
                                    handler.openBlockBank();
                                    softFailure = false;
                            }
                            break;
                    }
                    break;

                case "spawn":
                    if (command.hasArgAtIndex(2)) {
                        if (command.getArgAtIndex(2).equals("set")) {
                            handler.setTownSpawn();
                        } else {
                            handler.warpToOtherSpawn(command.get(2));
                        }
                    } else {
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
                        handler.setMOTD(command.concatAfter(2));
                    } else {
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
                    helpMessage = "/town pvp (friendlyfire)";
                    switch (command.get(2).toLowerCase()) {
                        case "friendlyfire":
                            helpMessage = "/town pvp friendlyfire <on/off>";
                            handler.setTownFriendlyFire(command.get(3));
                            softFailure = false;
                            break;
                    }
                    break;

                default:
                    hardFailure = true;

            }

        } catch (ArgumentCountException ex) {
            if (ex.getErrorIndex() == 1) {
                hardFailure = true;
            } else {
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
