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
import me.everdras.mctowns.command.handlers.PlotHandler;
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
public class PlotExecutor extends BaseExecutor {

    public PlotExecutor(MCTowns parent, WorldGuardPlugin wgp, Economy economy, Config options, TownManager townManager, TownJoinManager joinManager, HashMap<String, ActiveSet> activeSets, HashMap<Player, ActiveSet> potentialPlotBuyers) {
        super(parent, wgp, economy, options, townManager, joinManager, activeSets, potentialPlotBuyers);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        MCTCommand command = new MCTCommand(label, args);

        PlotHandler handler = new PlotHandler(parent, townManager, joinManager, cs, activeSets, wgp, economy, options, command);

        //A hard failure occurs when the failure occurs in the second argument (i.e. the command label was correct but the first argument was off, and so it should be handled by printing the usage listed in the plugin.yml
        //A soft failure occurs when the failure occurs in any argument after the second and should be handled by printing a finer error message. A soft failure will cause command to return true.
        boolean hardFailure = false, softFailure = true;

        String helpMessage = null;


        try {
            switch (command.get(1).toLowerCase()) {
                case "add":
                    helpMessage = "/plot add (player | guest)";
                    switch (command.get(2).toLowerCase()) {
                        case "player":
                            helpMessage = "/plot add player <player name>";
                            handler.addPlayerToPlot(command.get(3));
                            softFailure = false;
                            break;
                        case "guest":
                            helpMessage = "/plot add guest <guest name>";
                            handler.addPlayerToPlotAsGuest(command.get(3));
                            softFailure = false;
                            break;
                    }
                    break;

                case "remove":
                case "rm":
                    helpMessage = "/plot remove player <player/guest name>";
                    switch (command.get(2).toLowerCase()) {
                        case "player":
                            helpMessage = "/plot remove player <player name>";
                            handler.removePlayerFromPlot(command.get(3));
                            softFailure = false;
                            break;

                    }
                    break;
                case "list":
                case "ls":
                    helpMessage = "/plot list players";

                    switch (command.get(2).toLowerCase()) {
                        case "players":
                            softFailure = false;
                            handler.listPlayers(TownLevel.PLOT);
                            break;
                    }

                    break;
                case "active":
                    helpMessage = "/plot active <plot name>";
                    handler.setActivePlot(command.get(2));
                    softFailure = false;
                    break;
                case "flag":
                    helpMessage = "/plot flag <flag name> (args)";
                    handler.flagRegion(command.get(2), command.getFlagArguments(), TownLevel.PLOT);
                    softFailure = false;
                    break;

                case "economy":
                case "econ":
                    helpMessage = "/plot economy (forsale | setprice)";
                    switch (command.get(2).toLowerCase()) {
                        case "forsale":
                            helpMessage = "/plot economy forsale <true/false>";
                            handler.setPlotBuyability(command.get(3));
                            softFailure = false;
                            break;
                        case "setprice":
                            helpMessage = "/plot economy setprice <price>";
                            handler.setPlotPrice(command.get(3));
                            softFailure = false;
                            break;

                    }
                    break;

                case "sign":
                    helpMessage = "/plot sign (build | demolish | setpos)";
                    switch (command.get(2).toLowerCase()) {
                        case "build":
                            handler.buildSign();
                            softFailure = false;
                            break;

                        case "demolish":
                            handler.demolishSign();
                            softFailure = false;
                            break;

                        case "setpos":
                            handler.setPlotSignPosition();
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
            }
            else {
                softFailure = true;
                hardFailure = false;
            }

        }


        MCTowns.logAssert(helpMessage != null, "In PlotExecutor, the helpMessage was null.");
        if (!hardFailure && softFailure && helpMessage != null) {
            cs.sendMessage(ChatColor.RED + "Invalid command. Acceptable similar formats are: ");
            cs.sendMessage(ChatColor.DARK_AQUA + helpMessage);
        }


        return !hardFailure;
    }
}
