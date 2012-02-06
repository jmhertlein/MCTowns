package me.everdras.mctowns;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.io.*;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.command.CommandHandler;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.listeners.MCTPlayerListener;
import me.everdras.mctowns.listeners.MCTPvPListener;
import me.everdras.mctowns.permission.Perms;
import me.everdras.mctowns.structure.TownLevel;
import me.everdras.mctowns.townjoin.TownJoinManager;
import me.everdras.mctowns.townjoin.TownJoinMethod;
import me.everdras.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class of the MCTowns plugin.
 * @author joshua
 */
public class MCTowns extends JavaPlugin {

    /**
     * The logger for Minecraft.
     */
    public static final Logger log = Logger.getLogger("Minecraft");
    private static final String MCT_DATA_FOLDER = "plugins" + File.separator + "MCTowns";
    private static final String TOWN_DATABASE_SAVE_PATH = MCT_DATA_FOLDER + File.separator + "MCTownsExternalTownDatabase.mct";
    private static final String BACKUP_TOWN_DATABASE_SAVE_PATH = MCT_DATA_FOLDER + File.separator + "MCTownsExternalTownDatabase.bak";
    private static final String MCT_TEXT_CONFIG_PATH = MCT_DATA_FOLDER + File.separator + "config.txt";
    private TownManager townManager;
    private TownJoinManager joinManager;
    private HashMap<String, ActiveSet> activeSets;
    private static WorldGuardPlugin wgp;
    private static Economy economy;
    private static Config options;
    private HashMap<Player, ActiveSet> potentialPlotBuyers;

    /**
     * Persist any data that needs to be persisted.
     */
    @Override
    public void onDisable() {
        serializeTownManager();
        serializeBackup();
        log.info("[MCTowns]: MCTowns has been successfully disabled.");

        //release as much memory as I can, to make reloads suck less.
        townManager = null;
        joinManager = null;
        activeSets = null;
        economy = null;
        options = null;
        potentialPlotBuyers = null;
    }

    /**
     * Sets up files needed for persistence, registers listeners and permissions, etc
     */
    @Override
    public void onEnable() {
        log.log(Level.INFO, "MCTowns is now setting up...");

        checkFiles();
        loadConfig();

        setupTownManager();

        joinManager = new TownJoinManager(townManager);

        activeSets = new HashMap<>();

        if (options.isEconomyEnabled()) {
            potentialPlotBuyers = new HashMap<>();
        }

        Perms.registerPermNodes(getServer().getPluginManager());

        hookInDependencies();

        regEventListeners();

        log.info("MCTowns is now fully loaded.");

    }

    /**
     * Processes commands by wrapping the sender and other pertinent info in a CommandSenderWrapper.
     * @param sender the command sender
     * @param command the command
     * @param label the command root
     * @param args the arguments to the command
     * @return true if the command was processed, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        CommandHandler handler = new CommandHandler(this, townManager, joinManager, sender, activeSets, wgp, economy, options);

        if (!(args.length > 0)) {
            return false;
        }

        switch (label) {
            case "mct":
                return handleMCTCommand(sender, handler, args);


            case "gov":
                sender.sendMessage("Governments coming in version 0.8.0!");
                return true;

            case "to":
            case "town":
                return handleTownCommand(sender, handler, args);

            case "te":
            case "territory":
                return handleTerritoryCommand(sender, handler, args);

            case "di":
            case "district":
                return handleDistrictCommand(sender, handler, args);

            case "pl":
            case "plot":
                return handlePlotCommand(sender, handler, args);
        }


        return false;
    }

    private boolean handleTerritoryCommand(CommandSender sender, CommandHandler handler, String[] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {

            case "flag":
                if (args.length == 1) {
                    sender.sendMessage("/territory flag <flag name> (flag arguments)");
                    sender.sendMessage("Provide no arguments to remove a flag.");
                    return true;
                }

                if (args.length == 2) {
                    handler.unflagRegion(args[1], TownLevel.TERRITORY);
                    return true;
                }

                String[] nuArgs = new String[args.length - 2];
                System.arraycopy(args, 2, nuArgs, 0, args.length - 2);
                handler.flagRegion(args[1], nuArgs, TownLevel.TERRITORY);
                return true;

            case "active":
                if (args.length < 2) {
                    sender.sendMessage("/territory active set <territory name>");
                    return true;
                }
                if (args[1].equals("set")) {
                    handler.setActiveTerritory(args[2]);
                    return true;
                }
                else {
                    sender.sendMessage("/territory active set <territory name>");
                    return true;
                }
            case "add":
                if (args.length < 2) {
                    sender.sendMessage("/territory add (district | player)");
                    return true;
                }
                switch (args[1]) {
                    case "district":
                        if (args.length < 3) {
                            sender.sendMessage("/territory add district <district name>");
                            return true;
                        }
                        handler.addDistrictToTerritory(args[2], true);
                        return true;
                    case "player":
                        if (args.length < 3) {
                            sender.sendMessage("/territory add player <player name>");
                            return true;
                        }
                        handler.addPlayerToTerritory(args[2]);
                        return true;

                    default:
                        sender.sendMessage("/territory add district <district name>");
                        return true;

                }
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage("/territory remove (district | player");
                    return true;
                }
                switch (args[1]) {
                    case "district":
                        if (args.length < 3) {
                            sender.sendMessage("/territory remove district <district name>");
                            return true;
                        }
                        handler.removeDistrictFromTerritory(args[2]);
                        return true;
                    case "player":
                        if (args.length < 3) {
                            sender.sendMessage("/territory add player <player name>");
                            return true;
                        }
                        if (args.length == 3) {
                            handler.removePlayerFromTerritory(args[2], false);
                        }
                        else if (args.length == 4 && args[3].equalsIgnoreCase("r")) {
                            handler.removePlayerFromTerritory(args[2], true);
                        }
                        return true;

                    default:
                        sender.sendMessage("/territory remove district <district name>");
                        return true;
                }
            case "list":
                if (args.length < 2) {
                    sender.sendMessage("/territory list (players | districts)");
                    return true;
                }
                switch (args[1]) {
                    case "players":
                        handler.listPlayers(TownLevel.TERRITORY);
                        return true;
                    case "districts":
                        if (args.length > 2) {
                            try {
                                handler.listDistricts(Integer.parseInt(args[2]));
                            } catch (Exception e) {
                                sender.sendMessage(ChatColor.RED + "Error parsing integer.");
                            }
                        }
                        else {
                            handler.listDistricts();

                        }
                        return true;

                    default:
                        sender.sendMessage("/territory list (players | districts)");
                        return true;
                }

            default:
                return false;
        }
    }

    private boolean handleMCTCommand(CommandSender sender, CommandHandler handler, String[] args) {
        switch (args[0]) {
            case "confirm":
                handler.confirmPlotPurchase(potentialPlotBuyers);
                return true;
            case "convert":
                if (args.length < 4) {
                    sender.sendMessage("/mct convert <town name> <parent region name> <desired district name>");
                    return true;
                }
                handler.convertRegionToMCTown(args[1], args[2], args[3]);
                return true;

            case "purge":
                handler.purge();
                return true;
            case "addtown":
                if (args.length < 3) {
                    sender.sendMessage("/mct addtown <town name> <mayor name>");
                    return true;
                }
                handler.createTown(args[1], args[2]);
                return true;

            case "removetown":
                if (args.length < 2) {
                    sender.sendMessage("/mct removetown <town name>");
                    return true;
                }
                handler.removeTown(args[1]);
                return true;

            case "info":
                if (args.length < 2) {
                    sender.sendMessage("/mct info (player | town)");
                    return true;
                }
                switch (args[1]) {
                    case "town":
                        if (args.length < 3) {
                            sender.sendMessage("/mct info town <town name>");
                            return true;
                        }
                        handler.queryTownInfo(args[2]);
                        return true;
                    case "player":
                        if (args.length < 3) {
                            sender.sendMessage("/mct info player <player name>");
                            return true;
                        }
                        handler.queryPlayerInfo(args[2]);
                        return true;

                    default:
                        sender.sendMessage("/mct info (player | town)");
                        return true;
                }

            case "join":
                if (args.length < 2) {
                    sender.sendMessage("/mct join <town name>");
                    return true;
                }

                handler.requestAdditionToTown(args[1]);
                return true;

            case "cancel":
                if (args.length == 1) {
                    sender.sendMessage("/mct cancel <town name>");
                    return true;
                }

                handler.cancelRequest(args[1]);
                return true;

            case "refuse":
                if (args.length < 2) {
                    sender.sendMessage("/mct refuse <town name>");
                    return true;
                }

                if (args[1].equals("all")) {
                    handler.rejectAllInvitations();
                    return true;
                }

                handler.rejectInvitation(args[1]);
                return true;


            case "list":
                if (args.length == 1) {
                    sender.sendMessage("/mct list (towns | invites | requests)");
                    return true;
                }
                switch (args[1]) {
                    case "towns":
                        if (args.length == 2) {
                            handler.listTowns();
                        }
                        else {
                            handler.listTowns(args[2]);
                        }
                        return true;

                    case "invites":
                        handler.listInvitesForPlayer();
                        return true;
                    case "requests":
                        handler.listRequestsForPlayer();
                        return true;
                }

                default:
                    return false;
        }


    }

    private boolean handleTownCommand(CommandSender sender, CommandHandler handler, String[] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {
            case "add":
                if (args.length < 2) {
                    sender.sendMessage("/town add (player | assistant | territory)");
                    return true;
                }
                switch (args[1]) {
                    case "territory":
                        if (args.length < 3) {
                            sender.sendMessage("/town add territory <territory name>");
                            return true;
                        }
                        if (args.length == 3) {
                            handler.addTerritorytoTown(args[2], false, true);
                        }
                        else if (args.length == 4) {
                            handler.addTerritorytoTown(args[2], args[3].equalsIgnoreCase("-admin"), true);
                        }
                        return true;
                    case "player":
                        if (args.length < 3) {
                            sender.sendMessage("/town add player <player name>");
                            return true;
                        }

                        handler.invitePlayerToTown(args[2]);
                        return true;
                    case "assistant":
                        if (args.length < 3) {
                            sender.sendMessage("/town add assistant <resident name>");
                            return true;
                        }
                        handler.promoteToAssistant(args[2]);
                        return true;

                    default:
                        sender.sendMessage("/town add territory <territory name>");
                        return true;

                }
            case "reject":
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage("/town remove (self | territory | player | assistant | request | invite)");
                    return true;
                }
                switch (args[1]) {
                    case "territory":
                        if (args.length < 3) {
                            sender.sendMessage("/town remove territory <territory name>");
                            return true;
                        }
                        handler.removeTerritoryFromTown(args[2]);
                        return true;
                    case "player":
                        if (args.length < 3) {
                            sender.sendMessage("/town remove player <player name>");
                            return true;
                        }
                        handler.removePlayerFromTown(args[2]);
                        return true;
                    case "assistant":
                        if (args.length < 3) {
                            sender.sendMessage("/town remove assistant <assistant name>");
                            return true;
                        }
                        handler.demoteFromAssistant(args[2]);
                        return true;


                    case "request":
                        if (args.length < 3) {
                            sender.sendMessage("/town reject request <player name>");
                            return true;
                        }
                        handler.rejectRequest(args[2]);
                        return true;

                    case "invite":
                        if (args.length < 3) {
                            sender.sendMessage("/town remove invite <player name>");
                            return true;
                        }
                        handler.cancelInvitation(args[2]);
                        return true;

                    case "self":
                        handler.removeSelfFromTown();
                        return true;

                    default:
                        sender.sendMessage("/town remove territory <territory name>");
                        return true;
                }
            case "active":
                if (args.length < 2) {
                    sender.sendMessage("/town active (set | reset)");
                    return true;
                }
                if (args[1].equals("reset")) {
                    handler.resetActiveTown();
                    return true;
                }
                if (args[1].equals("set")) {
                    if (args.length < 3) {
                        sender.sendMessage("/town active set <town name>");
                        return true;
                    }
                    handler.setActiveTown(args[2]);

                }
                else {
                    sender.sendMessage("/town active (set | reset)");

                }
                return true;

            case "list":
                if (args.length < 2) {
                    sender.sendMessage("/town list (players | territories | invites | requests)");
                    return true;
                }
                switch (args[1]) {
                    case "players":
                        if (args.length > 2) {
                            handler.listResidents(Integer.parseInt(args[2]));
                        }
                        else {
                            handler.listResidents();
                        }
                        return true;
                    case "territories":
                        if (args.length > 2) {
                            handler.listTerritories(Integer.parseInt(args[2]));
                        }
                        else {
                            handler.listTerritories();
                        }
                        return true;
                    case "requests":
                        handler.listRequestsForTown();
                        return true;
                    case "invites":
                        handler.listInvitesForTown();
                        return true;

                    default:
                        sender.sendMessage("/town list (players | territories | invites | requests)");
                        return true;
                }
            case "motd":
                if (args.length == 1) {
                    handler.printMOTD();
                }
                else {
                    String temp = "";
                    for (int i = 2; i < args.length; i++) {
                        temp += args[i] + " ";
                    }

                    handler.setMOTD(temp);

                }
                return true;
            case "setmayor":
                if (args.length < 2) {
                    sender.sendMessage("/town setmayor <mayor name>");
                    return true;
                }
                handler.setMayor(args[1]);
                return true;
            case "bank":
                if (args.length < 2) {
                    sender.sendMessage("/town bank (deposit | withdraw | check");
                    return true;
                }
                switch (args[1]) {
                    case "deposit":
                        if (args.length < 4) {
                            sender.sendMessage("/town bank deposit (<block name>|hand|currency) <amount>");
                            return true;
                        }

                        if (args[2].equals("hand")) {
                            handler.depositHeldItem(args[3]);
                        }
                        else if (args[2].equalsIgnoreCase("currency")) {
                            //deposit the amount of currency in the bank
                            handler.depositCurrencyBank(args[3]);
                        }
                        else {
                            handler.depositBlockBank(args[2], args[3]);
                        }
                        return true;
                    case "withdraw":
                        if (args.length < 4) {
                            sender.sendMessage("/town bank withdraw (<block name>|currency) <amount>");
                            return true;
                        }

                        if (args[2].equalsIgnoreCase("currency")) {
                            handler.withdrawCurrencyBank(args[3]);
                            return true;
                        }
                        handler.withdrawBlockBank(args[2], args[3]);
                        return true;
                    case "check":
                        if (args.length < 3) {
                            sender.sendMessage("/town bank check (<block name>|currency)");
                            return true;
                        }

                        if (args[2].equalsIgnoreCase("currency")) {
                            //check the amount of currency in the bank
                            handler.checkCurrencyBank();
                            return true;
                        }
                        handler.checkBlockBank(args[2]);
                        return true;

                    default:
                        sender.sendMessage("/town bank (deposit | withdraw | check");
                        return true;
                }
            case "spawn":
                if (args.length == 1) {
                    handler.warpToSpawn();
                    return true;
                }
                else if (args.length == 2) {
                    if (!args[1].equals("set")) {
                        handler.warpToOtherSpawn(args[1]);
                        return true;
                    }
                    else if (args[1].equals("set")) {
                        handler.setTownSpawn();
                        return true;
                    }

                }
                return true;


            case "pvp":
                if (args.length == 1) {
                    sender.sendMessage("/town pvp (friendlyfire)");
                    return true;
                }
                else if (args[1].equals("friendlyfire")) {
                    if (args.length < 3) {
                        sender.sendMessage("/town pvp friendlyfire <on/off>");
                        return true;
                    }

                    handler.setTownFriendlyFire(args[2]);

                }
                else {
                    sender.sendMessage("/town pvp (friendlyfire)");

                }
                return true;

            case "economy":
                if (args.length == 1) {
                    sender.sendMessage("/town economy (buyableplots | economyjoins)");
                    return true;
                }

                switch (args[1]) {
                    case "setdefault":
                        if (args.length < 3) {
                            sender.sendMessage("/town economy setdefault (plotprice)");
                            return true;
                        }
                        switch (args[2]) {
                            case "plotprice":
                                if (args.length < 4) {
                                    sender.sendMessage("/town economy setdefault plotprice <number>");
                                    return true;
                                }

                                //TODO: implement setting default plot price...?
                                sender.sendMessage("Not yet implemented.");
                                return true;

                            default:
                                sender.sendMessage("/town economy setdefault (plotprice)");
                                return true;
                        }

                    case "buyableplots":
                        if (args.length < 3) {
                            sender.sendMessage("/town economy buyableplots <true/false>");
                            return true;
                        }
                        handler.setTownPlotBuyability(args[2]);

                        return true;

                    case "economyjoins":
                        if (args.length < 3) {
                            sender.sendMessage("/town economy economyjoins <true/false>");
                            return true;
                        }

                        try {
                            if (Boolean.parseBoolean(args[2])) {
                                handler.setTownJoinMethod(TownJoinMethod.ECONOMY.toString());
                            }
                            else {
                                handler.setTownJoinMethod(TownJoinMethod.INVITATION.toString());
                            }
                        } catch (Exception e) {
                            sender.sendMessage(ChatColor.RED + "Error parsing boolean: Expected true/false, found: " + args[2]);
                        }


                        return true;
                    default:
                        sender.sendMessage("/town economy setdefault (plotprice)");
                        return true;
                }
            default:
                return false;
        }
    }

    private boolean handleDistrictCommand(CommandSender sender, CommandHandler handler, String[] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {

            case "flag":
                if (args.length == 1) {
                    sender.sendMessage("/district flag <flag name> (flag arguments)");
                    sender.sendMessage("Provide no arguments to remove a flag.");
                    return true;
                }

                if (args.length == 2) {
                    handler.unflagRegion(args[1], TownLevel.DISTRICT);
                    return true;
                }

                String[] nuArgs = new String[args.length - 2];
                System.arraycopy(args, 2, nuArgs, 0, args.length - 2);
                handler.flagRegion(args[1], nuArgs, TownLevel.DISTRICT);
                return true;

            case "active":
                if (args.length < 3) {
                    sender.sendMessage("/district active set <district name>");
                    return true;
                }
                if (args[1].equals("set")) {
                    handler.setActiveDistrict(args[2]);
                    return true;
                }
            case "add":
                if (args.length < 2) {
                    sender.sendMessage("/district add (plot | player)");
                    return true;
                }
                switch (args[1]) {
                    case "plot":
                        if (args.length < 3) {
                            sender.sendMessage("/district add plot <plot name>");
                            return true;
                        }
                        handler.addPlotToDistrict(args[2], true);
                        return true;
                    case "player":
                        if (args.length < 3) {
                            sender.sendMessage("/district add player <player name>");
                            return true;
                        }
                        handler.addPlayerToDistrict(args[2]);
                        return true;

                    default:
                        sender.sendMessage("/district add (plot | player)");
                        return true;

                }
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage("/district remove (plot | player)");
                    return true;
                }
                switch (args[1]) {
                    case "plot":
                        if (args.length < 3) {
                            sender.sendMessage("/district remove plot <plot name>");
                            return true;
                        }
                        handler.removePlotFromDistrict(args[2]);
                        return true;
                    case "player":
                        if (args.length < 3) {
                            sender.sendMessage("/district remove player <player name>");
                            return true;
                        }
                        if (args.length == 3) {
                            handler.removePlayerFromDistrict(args[2], false);
                        }
                        else if (args.length == 4 && args[3].equalsIgnoreCase("r")) {
                            handler.removePlayerFromDistrict(args[2], true);
                        }
                        return true;

                    default:
                        sender.sendMessage("/district remove (plot | player)");
                        return true;
                }
            case "list":
                if (args.length < 2) {
                    sender.sendMessage("/district list (plots | players)");
                    return true;
                }
                switch (args[1]) {
                    case "players":
                        handler.listPlayers(TownLevel.DISTRICT);
                        return true;
                    case "plots":
                        if (args.length > 2) {
                            try {
                                handler.listPlots(Integer.parseInt(args[2]));
                            } catch (Exception e) {
                                sender.sendMessage(ChatColor.RED + "Error parsing integer.");
                            }
                        }
                        else {
                            handler.listPlots();
                        }
                        return true;
                    default:
                        sender.sendMessage("/district list (plots | players)");
                        return true;
                }
            default:
                return false;
        }
    }

    private boolean handlePlotCommand(CommandSender sender, CommandHandler handler, String[] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {

            case "flag":
                if (args.length == 1) {
                    sender.sendMessage("/plot flag <flag name> (flag arguments)");
                    sender.sendMessage("Provide no arguments to remove a flag.");
                    return true;
                }

                if (args.length == 2) {
                    handler.unflagRegion(args[1], TownLevel.PLOT);
                    return true;
                }

                String[] nuArgs = new String[args.length - 2];
                System.arraycopy(args, 2, nuArgs, 0, args.length - 2);
                handler.flagRegion(args[1], nuArgs, TownLevel.PLOT);
                return true;

            case "info":
                handler.printPlotInfo();
                return true;
            case "add":
                if (args.length == 1) {
                    sender.sendMessage("/plot add (player|guest)");
                    return true;
                }

                switch (args[1]) {
                    case "player":
                        if (args.length == 2) {
                            sender.sendMessage("/plot add player <player name>");
                            return true;
                        }
                        handler.addPlayerToPlot(args[2]);
                        return true;
                    case "guest":
                        if (args.length == 2) {
                            sender.sendMessage("/plot add guest <player name>");
                            return true;
                        }
                        handler.addPlayerToPlotAsGuest(args[2]);
                        return true;

                    default:
                        sender.sendMessage("/plot add (player|guest)");
                        return true;
                }
            case "sign":
                if (args.length < 2) {
                    sender.sendMessage("/plot sign (build | demolish)");
                    return true;
                }
                switch (args[1]) {
                    case "build":
                        handler.buildSign();
                        return true;
                    case "demolish":
                        handler.demolishSign();
                        return true;

                    case "setpos":
                        //set the position of the sign on top of the block the person is looking at.
                        handler.setPlotSignPosition();
                        return true;
                    default:
                        sender.sendMessage("/plot sign (build | demolish)");
                        return true;
                }
            case "economy":
                if (args.length == 1) {
                    sender.sendMessage("/plot economy (price | forsale)");
                    return true;
                }
                switch (args[1]) {
                    case "price":
                        if (args.length > 3) {
                            sender.sendMessage("/plot economy price <price>");
                            return true;
                        }
                        handler.setPlotPrice(args[2]);
                        return true;

                    case "forsale":
                        if (args.length > 3) {
                            sender.sendMessage("/plot economy forsale <true/false>");
                            return true;
                        }
                        handler.setPlotBuyability(args[2]);
                        return true;

                    default:
                        sender.sendMessage("/plot economy (price | forsale)");
                        return true;
                }

            case "remove":
                if (args.length < 3) {
                    sender.sendMessage("/plot remove player <player name>");
                    return true;
                }
                if (args[1].equals("player")) {
                    handler.removePlayerFromPlot(args[2]);
                    return true;

                }
                else {
                    sender.sendMessage("/plot remove player <player name>");
                    return true;
                }
            case "list":
                if (args.length < 2) {
                    sender.sendMessage("/plot list players");
                    return true;
                }
                if (args[1].equals("players")) {
                    handler.listPlayers(TownLevel.PLOT);
                    return true;
                }
                else {
                    sender.sendMessage("/plot list players");
                    return true;
                }
            case "active":
                if (args.length < 3) {
                    sender.sendMessage("/plot active set <plot name>");
                    return true;
                }
                if (args[1].equals("set")) {
                    if (args.length == 3) {
                        handler.setActivePlot(args[2], false);
                    }
                    else {
                        handler.setActivePlot(args[2], args[3].equalsIgnoreCase("q"));
                    }
                    return true;
                }
                else {
                    sender.sendMessage("/plot active set <plot name>");
                    return true;
                }

            default:
                return false;
        }
    }

    private void checkFiles() {


        ArrayDeque<File> files = new ArrayDeque<>();
        ArrayDeque<File> dirs = new ArrayDeque<>();

        //add dirs in descending path order
        dirs.add(new File(MCT_DATA_FOLDER));

        //add files
        files.add(new File(TOWN_DATABASE_SAVE_PATH));
        files.add(new File(MCT_TEXT_CONFIG_PATH));
        files.add(new File(BACKUP_TOWN_DATABASE_SAVE_PATH));

        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        for (File file : files) {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    if (file.getPath().equals(MCT_TEXT_CONFIG_PATH)) {
                        Config.resetConfigFileToDefault(MCT_TEXT_CONFIG_PATH);
                        log.log(Level.INFO, "Created a default config file.");
                    }
                } catch (IOException ex) {
                    log.log(Level.WARNING, "MCTowns: Unable to create necessary files. Will not save.");
                }
            }
        }
    }

    private void setupTownManager() {
        File path = new File(TOWN_DATABASE_SAVE_PATH);

        FileInputStream fis;
        ObjectInputStream ois;

        try {
            fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);

            townManager = new TownManager();
            townManager.readExternal(ois);

            ois.close();
            fis.close();

        } catch (Exception e) {
            log.log(Level.WARNING, "MCTowns: Couldn't load the town database. Ignore if this is the first time the plugin has been run.");
            townManager = new TownManager();
        }




    }

    private void hookInDependencies() {
        try {
            wgp = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
        } catch (Exception e) {
            log.log(Level.SEVERE, "[MCTowns] Error occurred in hooking in to WorldGuard. Is both WorldGuard and WorldEdit installed?");
            log.log(Level.SEVERE, "[MCTowns] !!!!!WARNING!!!!! MCTOWNS WILL NOT RUN CORRECTLY OR POSSIBLY NOT RUN AT ALL.  !!!!!WARNING!!!!!");
        }

        if (options.isEconomyEnabled()) {
            try {
                boolean success = setupEconomy();
                if (!success) {
                    log.log(Level.SEVERE, "MCTowns: Unable to hook-in to Vault (1)!");
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "MCTowns: Unable to hook-in to Vault.");
            }
        }
    }

    private void regEventListeners() {
        MCTPlayerListener playerListener = new MCTPlayerListener(townManager, joinManager, options, economy, potentialPlotBuyers);
        MCTPvPListener townPvPListener = new MCTPvPListener(townManager, options);

        if(options.allowsTownFriendlyFireManagement())
            getServer().getPluginManager().registerEvents(townPvPListener, this);

        getServer().getPluginManager().registerEvents(playerListener, this);

        //TODO: Fix this, see ticket no. 13
        //getServer().getPluginManager().registerEvent(Type.PLAYER_RESPAWN, respawnListener, Priority.Monitor, this);
    }

    private void loadConfig() {
        options = new Config(MCT_TEXT_CONFIG_PATH);

        if (options.badConfig()) {
            log.log(Level.SEVERE, "MCTowns: " + options.getFailReason());
        }
    }

    private void serializeTownManager() {
        File path = new File(TOWN_DATABASE_SAVE_PATH);

        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);

            townManager.writeExternal(oos);

            oos.close();
            fos.close();

        } catch (IOException e) {
            log.log(Level.WARNING, "MCTowns: Error saving the town database.");


        }




    }

    private void serializeBackup() {

        File path = new File(BACKUP_TOWN_DATABASE_SAVE_PATH);

        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);

            townManager.writeExternal(oos);

            oos.close();
            fos.close();

        } catch (IOException e) {
            log.log(Level.WARNING, "Error saving the town database backup.");
        }
    }

    /**
     * Resets the active sets and joinManager. Is a soft-reload for purposes of
     * memory usage.
     */
    public void purge() {
        activeSets = new HashMap<>();
        joinManager = new TownJoinManager(townManager);

        if (options.isEconomyEnabled()) {
            potentialPlotBuyers = new HashMap<>();
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public static void logSevere(String msg) {
        log.log(Level.SEVERE, "[MCTowns]: " + msg);
    }

    public static void logInfo(String msg) {
        log.log(Level.INFO, "[MCTowns]: " + msg);
    }
}
