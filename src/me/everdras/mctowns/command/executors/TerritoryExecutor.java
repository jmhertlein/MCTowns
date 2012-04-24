/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command.executors;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.HashMap;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.core.command.ArgumentCountException;
import me.everdras.core.command.ECommand;
import me.everdras.mctowns.command.handlers.TerritoryHandler;
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
public class TerritoryExecutor extends BaseExecutor {

    public TerritoryExecutor(MCTowns parent, WorldGuardPlugin wgp, Economy economy, Config options, TownManager townManager, TownJoinManager joinManager, HashMap<String, ActiveSet> activeSets, HashMap<Player, ActiveSet> potentialPlotBuyers) {
        super(parent, wgp, economy, options, townManager, joinManager, activeSets, potentialPlotBuyers);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
        ECommand command = new ECommand(label, args);

        TerritoryHandler handler = new TerritoryHandler(parent, townManager, joinManager, cs, activeSets, wgp, economy, options, command);

        //A hard failure occurs when the failure occurs in the second argument (i.e. the command label was correct but the first argument was off, and so it should be handled by printing the usage listed in the plugin.yml
        //A soft failure occurs when the failure occurs in any argument after the second and should be handled by printing a finer error message. A soft failure will cause command to return true.
        boolean hardFailure = false, softFailure = true;

        String helpMessage = null;


        try {
            switch (command.get(1)) {
                case "add":
                    helpMessage = "/territory add (player | district)";
                    switch (command.get(2)) {
                        case "player":
                            helpMessage = "/territory add player <player name>";
                            handler.addPlayerToTerritory(command.get(3));
                            softFailure = false;
                            break;
                        case "district":
                        case "di":
                            helpMessage = "/territory add district <district name>";
                            handler.addDistrictToTerritory(command.get(3));
                            softFailure = false;
                            break;
                    }
                    break;
                    
                case "redefine":
                    handler.redefineActiveRegion(TownLevel.TERRITORY);
                    softFailure = false;
                    break;

                case "remove":
                case "rm":
                    helpMessage = "/territory remove (player | district)";
                    switch (command.get(2)) {
                        case "player":
                            helpMessage = "/territory remove player <player name>";
                            handler.removePlayerFromTerritory(command.get(3));
                            softFailure = false;
                            break;
                        case "district":
                        case "dist":
                            helpMessage = "/territory remove district <district name>";
                            handler.removeDistrictFromTerritory(command.get(3));
                            softFailure = false;
                            break;
                    }
                    break;
                case "list":
                case "ls":
                    helpMessage = "/territory list (districts | players)";

                    switch (command.get(2)) {
                        case "districts":
                            if (command.hasArgAtIndex(3)) {
                                handler.listDistricts(command.get(3));
                            }
                            else {
                                handler.listDistricts();
                            }
                            softFailure = false;
                            break;
                        case "players":
                            softFailure = false;
                            handler.listPlayers(TownLevel.TERRITORY);
                            break;
                    }

                    break;
                case "active":
                    helpMessage = "/territory active <territory name>";
                    handler.setActiveTerritory(command.get(2));
                    softFailure = false;
                    break;
                case "flag":
                    helpMessage = "/territory flag <flag name> (args)";
                    handler.flagRegion(command.get(2), command.getFlagArguments(), TownLevel.TERRITORY);
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
