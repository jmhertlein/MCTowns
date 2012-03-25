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
import me.everdras.mctowns.command.handlers.DistrictHandler;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.structure.TownLevel;
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
public class DistrictExecutor extends BaseExecutor {

    public DistrictExecutor(MCTowns parent, WorldGuardPlugin wgp, Economy economy, Config options, TownManager townManager, TownJoinManager joinManager, HashMap<String, ActiveSet> activeSets, HashMap<Player, ActiveSet> potentialPlotBuyers) {
        super(parent, wgp, economy, options, townManager, joinManager, activeSets, potentialPlotBuyers);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        MCTCommand command = new MCTCommand(label, args);

        DistrictHandler handler = new DistrictHandler(parent, townManager, joinManager, cs, activeSets, wgp, economy, options, command);

        //A hard failure occurs when the failure occurs in the second argument (i.e. the command label was correct but the first argument was off, and so it should be handled by printing the usage listed in the plugin.yml
        //A soft failure occurs when the failure occurs in any argument after the second and should be handled by printing a finer error message. A soft failure will cause command to return true.
        boolean hardFailure = false, softFailure = true;

        String helpMessage = null;


        try {
            switch (command.get(1)) {
                case "add":
                    helpMessage = "/district add (player | district)";
                    switch (command.get(2)) {
                        case "player":
                            helpMessage = "/district add player <player name>";
                            handler.addPlayerToDistrict(command.get(3));
                            softFailure = false;
                            break;
                        case "plot":
                        case "pl":
                            helpMessage = "/district add plot <district name>";
                            handler.addPlotToDistrict(command.get(3));
                            softFailure = false;
                            break;
                    }
                    break;

                case "remove":
                case "rm":
                    helpMessage = "/district remove (player | district)";
                    switch (command.get(2)) {
                        case "player":
                            helpMessage = "/district remove player <player name>";
                            handler.removePlayerFromDistrict(command.get(3));
                            softFailure = false;
                            break;
                        case "district":
                        case "dist":
                            helpMessage = "/district remove plot <district name>";
                            handler.removePlotFromDistrict(command.get(3));
                            softFailure = false;
                            break;
                    }
                    break;
                case "list":
                case "ls":
                    helpMessage = "/district list (plotss | players)";

                    switch (command.get(2)) {
                        case "plots":
                            if (command.hasArgAtIndex(3)) {
                                handler.listPlots(command.get(3));
                            }
                            else {
                                handler.listPlots();
                            }
                            softFailure = false;
                            break;
                        case "players":
                            softFailure = false;
                            handler.listPlayers(TownLevel.DISTRICT);
                            break;
                    }

                    break;
                case "active":
                    helpMessage = "/district active <territory name>";
                    handler.setActiveDistrict(command.get(2));
                    softFailure = false;
                    break;
                case "flag":
                    helpMessage = "/district flag <flag name> (args)";
                    handler.flagRegion(command.get(2), command.getFlagArguments(), TownLevel.DISTRICT);
                    softFailure = false;
                    break;
                default:
                    hardFailure = true;
                    softFailure = false;

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
