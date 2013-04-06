package net.jmhertlein.mctowns.command.executors;

import net.jmhertlein.core.command.ArgumentCountException;
import net.jmhertlein.core.command.ECommand;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.handlers.PlotHandler;
import net.jmhertlein.mctowns.structure.TownLevel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Joshua
 */
public class PlotExecutor extends BaseExecutor {

    private PlotHandler handler;

    public PlotExecutor(MCTowns parent) {
        super(parent);
        handler = new PlotHandler(parent);
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

                case "redefine":
                    handler.redefineActiveRegion(TownLevel.PLOT);
                    softFailure = false;
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
                            handler.listPlayers(TownLevel.PLOT);
                            softFailure = false;
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
