package net.jmhertlein.mctowns.command.executors;

import net.jmhertlein.core.command.ArgumentCountException;
import net.jmhertlein.core.command.ECommand;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.handlers.TerritoryHandler;
import net.jmhertlein.mctowns.structure.TownLevel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Joshua
 */
public class TerritoryExecutor extends BaseExecutor {

    private TerritoryHandler handler;

    public TerritoryExecutor(MCTowns parent) {
        super(parent);
        handler = new TerritoryHandler(parent);
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
            switch (command.get(1)) {
                case "add":
                    helpMessage = "/territory add (player | plot)";
                    switch (command.get(2)) {
                        case "player":
                            helpMessage = "/territory add player <player name>";
                            handler.addPlayerToTerritory(command.get(3));
                            softFailure = false;
                            break;
                        case "plot":
                        case "pl":
                            helpMessage = "/territory add plot <plot name>";
                            handler.addPlotToTerritory(command.get(3));
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
                    helpMessage = "/territory remove (player | plot)";
                    switch (command.get(2)) {
                        case "player":
                            helpMessage = "/territory remove player <player name>";
                            handler.removePlayerFromTerritory(command.get(3));
                            softFailure = false;
                            break;
                        case "plot":
                        case "pl":
                            helpMessage = "/territory remove plot <plot name>";
                            handler.removePlotFromTerritory(command.get(3));
                            softFailure = false;
                            break;
                    }
                    break;
                case "list":
                case "ls":
                    helpMessage = "/territory list (plots | players)";

                    switch (command.get(2)) {
                        case "plots":
                            if (command.hasArgAtIndex(3)) {
                                handler.listPlots(command.get(3));
                            } else {
                                handler.listPlots();
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
