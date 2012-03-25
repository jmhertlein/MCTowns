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
import me.everdras.mctowns.command.handlers.MCTHandler;
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
public class MCTExecutor extends BaseExecutor {

    public MCTExecutor(MCTowns parent, WorldGuardPlugin wgp, Economy economy, Config options, TownManager townManager, TownJoinManager joinManager, HashMap<String, ActiveSet> activeSets, HashMap<Player, ActiveSet> potentialPlotBuyers) {
        super(parent, wgp, economy, options, townManager, joinManager, activeSets, potentialPlotBuyers);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        MCTCommand command = new MCTCommand(label, args);

        MCTHandler handler = new MCTHandler(parent, townManager, joinManager, cs, activeSets, wgp, economy, options, command);

        //A hard failure occurs when the failure occurs in the second argument (i.e. the command label was correct but the first argument was off, and so it should be handled by printing the usage listed in the plugin.yml
        //A soft failure occurs when the failure occurs in any argument after the second and should be handled by printing a finer error message. A soft failure will cause command to return true.
        boolean hardFailure = false, softFailure = true;

        String helpMessage = null;


        try {
            if (!command.get(0).equals("mct")) {
                MCTowns.logSevere("Assertion failed: MCTExecutor.java, the command's first argument was not \"mct\"");
            }


            switch (command.get(1)) {
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
                    helpMessage = "/mct removetown <town name>";
                    handler.removeTown(command.get(2));
                    softFailure = false;
                    break;

                case "list":
                    helpMessage = "/mct list (towns | requests | invites)";
                    switch (command.get(2)) {
                        case "towns":
                            if (command.hasArgAtIndex(3)) {
                                handler.listTowns(command.get(3));
                            }
                            else {
                                handler.listTowns();
                            }
                            softFailure = false;
                            break;
                        case "requests":
                            handler.listRequestsForPlayer();
                            softFailure = false;
                            break;
                        case "invites":
                            handler.listInvitesForPlayer();
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
                    handler.rejectInvitation(command.get(2));
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

                case "convert":
                    helpMessage = "/mct convert <town name> <region name> <new district name>";
                    handler.convertRegionToMCTown(command.get(2), command.get(3), command.get(3));
                    softFailure = false;
                    break;

                case "purge":
                    cs.sendMessage("Purge is no longer an available command.");
                    softFailure = false;
                    break;
                default:
                    hardFailure = true;
            }



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
