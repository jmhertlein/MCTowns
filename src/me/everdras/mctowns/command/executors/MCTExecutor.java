/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command.executors;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.bukkit.plugin.Plugin;

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
        boolean hardFailure = true, softFailure = true;

        String helpMessage = null;

        MCTowns.logInfo("Command as we see it: " + command);

        try {
            if(!command.get(0).equals("mct"))
                MCTowns.logSevere("Assertion failed: MCTExecutor.java, the command's first argument was not \"mct\"");


            switch(command.get(1)) {
                case "info":
                    helpMessage = "/mct info (player | town)";
                    switch(command.get(2)) {
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

                        default:
                            helpMessage = "/mct info (player | town)";

                    }

                    break;

                case "addtown":
                    break;

                case "removetown":
                    break;

                case "list":
                    break;

                case "join":
                    break;

                case "refuse":
                    break;

                case "cancel":
                    break;

                case "confirm":
                    break;

                case "convert":
                    break;

                case "purge":
                    break;
            }
            hardFailure = false;



        } catch(ArgumentCountException ex) {
            MCTowns.logInfo("Error index: " + ex.getErrorIndex());
            if(ex.getErrorIndex() == 1 )
                hardFailure = true;
            else {
                softFailure = true;
                hardFailure = false;
            }

        }

        if(!hardFailure  && softFailure && helpMessage != null) {
            cs.sendMessage(ChatColor.RED + "Invalid command. Acceptable similar formats are: ");
            cs.sendMessage(ChatColor.DARK_AQUA + helpMessage);
        }

        MCTowns.logInfo("Hard Failure: " + hardFailure);
        MCTowns.logInfo("Soft Failure: " + softFailure);
        MCTowns.logInfo("Message: " + helpMessage);

        return !hardFailure;
    }



}
