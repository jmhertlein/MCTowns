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
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.command.handlers.MCTHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Joshua
 */
public class MCTExecutor extends BaseExecutor {

    private MCTHandler handler;

    public MCTExecutor(MCTownsPlugin parent) {
        super(parent);
        handler = new MCTHandler(parent);
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
            if (!command.get(0).equals("mct")) {
                MCTowns.logSevere("Assertion failed: MCTExecutor.java, the command's first argument was not \"mct\"");
            }


            switch (command.get(1)) {
                case "break":
                    if (MCTownsPlugin.isDebugging()) {
                        MCTowns.logWarning(cs.getName() + " intentionally broke the server.");
                        throw new RuntimeException("Intentionally broke.");
                    }
                    break;
                case "info":
                    helpMessage = "/mct info (player | town)";
                    switch (command.get(2)) {
                        case "player":
                            helpMessage = "/mct info player <player name>";
                            handler.queryPlayerInfo(command.get(3));
                            softFailure = false;
                            break;

                        case "town":
                            helpMessage = "/mct info town <town name>";
                            handler.queryTownInfo(command.get(3));
                            softFailure = false;
                            break;

                    }

                    break;

                case "addtown":
                    helpMessage = "/mct addtown <town name> <mayor name>";
                    handler.createTown(command.get(2), command.get(3));
                    softFailure = false;
                    break;

                case "removetown":
                case "rm":
                    helpMessage = "/mct removetown <town name>";
                    handler.removeTown(command.get(2));
                    softFailure = false;
                    break;

                case "list":
                case "ls":
                    helpMessage = "/mct list (towns | invite)";
                    switch (command.get(2)) {
                        case "towns":
                            if (command.hasArgAtIndex(3)) {
                                handler.listTowns(command.get(3));
                            } else {
                                handler.listTowns();
                            }
                            softFailure = false;
                            break;
                        case "invite":
                            handler.checkPendingInvite();
                            softFailure = false;
                            break;
                    }

                    break;

                case "join":
                    helpMessage = "/mct join <town name>";
                    handler.requestAdditionToTown(command.get(2));
                    softFailure = false;
                    break;

                case "refuse":
                    helpMessage = "/mct refuse <town name>";
                    handler.rejectInvitationFromTown(command.get(2));
                    softFailure = false;
                    break;

                case "cancel":
                    helpMessage = "/mct cancel <town name>";
                    handler.cancelRequest(command.get(2));
                    softFailure = false;
                    break;

                case "confirm":
                    handler.confirmPlotPurchase(potentialPlotBuyers);
                    softFailure = false;
                    break;

                case "togglesave":
                    handler.toggleAbortSave();
                    softFailure = false;
                    break;
                case "donate":
                    handler.printDonationPlug();
                    softFailure = false;
                    break;
                default:
                    hardFailure = true;
                    softFailure = false;
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
