package me.everdras.mctowns.command.handlers;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.command.CommandSenderWrapper;
import me.everdras.mctowns.command.MCTCommand;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.structure.MCTownsRegion;
import me.everdras.mctowns.structure.Town;
import me.everdras.mctowns.structure.TownLevel;
import me.everdras.mctowns.townjoin.TownJoinInfoPair;
import me.everdras.mctowns.townjoin.TownJoinManager;
import me.everdras.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * CommandHandler wraps a CommandSender and various other pertinent objects
 * together in order to allow each slash command to correspond with an
 * equivalent method here.
 *
 * @author joshua
 */
public abstract class CommandHandler {

    protected static final String TERRITORY_INFIX = "_territ_";
    protected static final String DISTRICT_INFIX = "_dist_";
    protected static final String PLOT_INFIX = "_plot_";
    protected static final ChatColor FATAL = ChatColor.DARK_RED,
            ERR = ChatColor.RED,
            WARN = ChatColor.YELLOW,
            SUCC = ChatColor.GREEN,
            INFO = ChatColor.LIGHT_PURPLE;

    protected MCTowns plugin;
    protected CommandSenderWrapper senderWrapper;
    protected TownManager townManager;
    protected TownJoinManager joinManager;
    protected WorldGuardPlugin wgp;
    protected WorldEditPlugin wep;
    protected Economy economy;
    protected Server server;
    protected Config options;
    protected MCTCommand cmd;

    /**
     * Wraps the command sender.
     *
     * @param parent the parent plugin
     * @param t the town manager
     * @param j the join manager
     * @param p the command sender
     * @param activeSets the map of active sets
     */
    public CommandHandler(MCTowns parent, TownManager t, TownJoinManager j, CommandSender p, HashMap<String, ActiveSet> activeSets, WorldGuardPlugin wg, Economy econ, Config opt, MCTCommand cmd) {
        townManager = t;
        joinManager = j;
        server = p.getServer();
        plugin = parent;

        senderWrapper = new CommandSenderWrapper(t, p, activeSets);
        wgp = wg;
        economy = econ;

        options = opt;

        this.cmd = cmd;




        try {
            wep = wgp.getWorldEdit();
        } catch (CommandException ex) {
            wep = null;
        }

        if (wgp == null || wep == null) {
            MCTowns.log.log(Level.SEVERE, "[MCTowns] !!!!!YOU DO NOT HAVE WORLDGUARD INSTALLED. WORLDGUARD IS A REQUIRED DEPENDENCY OF MCTOWNS!!!!!");
        }

    }

    public void flagRegion(String flagName, String[] args, TownLevel regionType) {
        if(!senderWrapper.hasExternalPermissions("mct.flag") && !senderWrapper.hasExternalPermissions("mct.admin")) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        MCTownsRegion reg = null;

        switch (regionType) {
            case TOWN:
                senderWrapper.sendMessage(ERR + "Can't apply flags to towns.");
                return;
            case TERRITORY:
                reg = senderWrapper.getActiveTerritory();
                break;
            case DISTRICT:
                reg = senderWrapper.getActiveDistrict();
                break;
            case PLOT:
                reg = senderWrapper.getActivePlot();
                break;
        }

        if (reg == null) {
            senderWrapper.sendMessage(ERR + "Your active " + regionType.toString() + " is not set.");
            return;
        }

        RegionManager regMan = wgp.getRegionManager(server.getWorld(reg.getWorldName()));

        ProtectedRegion wgReg = regMan.getRegion(reg.getName());

        if (wgReg == null) {
            MCTowns.logSevere("Error in CommandHandler.flagRegion(): The region in WG was null (somehow). Perhaps someone manually deleted a region through WorldGuard?");
            senderWrapper.sendMessage(ERR + "An error occurred. Please see the console output for more information. This command exited safely; nothing was changed by it.");
            return;
        }



        Flag<?> foundFlag = null;

        for (Flag<?> flag : DefaultFlag.getFlags()) {
            if (flag.getName().replace("-", "").equalsIgnoreCase(flagName.replace("-", ""))) {
                foundFlag = flag;
                break;
            }
        }

        if (foundFlag == null) {
            senderWrapper.sendMessage(ERR + "Couldn't find a matching flag.");
            return;
        }

        String s_stateOfFlag = "";
        if (args.length == 1) {
            s_stateOfFlag = args[0];
        } else {
            for (String s : args) {
                s_stateOfFlag += s;
                s_stateOfFlag += " ";
            }
            s_stateOfFlag = s_stateOfFlag.substring(0, s_stateOfFlag.length() - 1);
        }

        try {
            setFlag(wgReg, foundFlag, senderWrapper.getSender(), s_stateOfFlag);
        } catch (InvalidFlagFormat ex) {
            senderWrapper.sendMessage(ERR + "Error parsing flag arguments: " + ex.getMessage());
            return;
        }

        senderWrapper.sendMessage(ChatColor.GREEN + "Region successfully flagged.");

        doRegManSave(regMan);

    }

    public void unflagRegion(String flagName, TownLevel regionType) {
        MCTownsRegion reg = null;

        switch (regionType) {
            case TOWN:
                senderWrapper.sendMessage(ERR + "Can't apply flags to towns.");
                return;
            case TERRITORY:
                reg = senderWrapper.getActiveTerritory();
                break;
            case DISTRICT:
                reg = senderWrapper.getActiveDistrict();
                break;
            case PLOT:
                reg = senderWrapper.getActivePlot();
                break;
        }

        if (reg == null) {
            senderWrapper.sendMessage(ERR + "Your active " + regionType.toString() + " is not set.");
            return;
        }

        RegionManager regMan = wgp.getRegionManager(server.getWorld(reg.getWorldName()));

        ProtectedRegion wgReg = regMan.getRegion(reg.getName());

        if (wgReg == null) {
            MCTowns.logSevere("Error in CommandHandler.flagRegion(): The region in WG was null (somehow). Perhaps someone manually deleted a region through WorldGuard?");
            senderWrapper.sendMessage(ERR + "An error occurred. Please see the console output for more information. This command exited safely; nothing was changed by it.");
            return;
        }

        wgReg.setFlag(DefaultFlag.CHEST_ACCESS, StateFlag.State.ALLOW); //this is going to be so annoying

        Flag<?> foundFlag = null;

        for (Flag<?> flag : DefaultFlag.getFlags()) {
            if (flag.getName().replace("-", "").equalsIgnoreCase(flagName.replace("-", ""))) {
                foundFlag = flag;
                break;
            }
        }

        if (foundFlag == null) {
            senderWrapper.sendMessage(ERR + "Couldn't find a matching flag.");
            return;
        }

        wgReg.setFlag(foundFlag, null);

        senderWrapper.sendMessage(ChatColor.GREEN + "Successfully removed flag.");



    }

    //====================================PRIVATE===========================
    protected WorldGuardPlugin getWGPFromSenderWrapper(CommandSenderWrapper csw) {
        return (WorldGuardPlugin) csw.getSender().getServer().getPluginManager().getPlugin("WorldGuard");
    }

    protected ProtectedCuboidRegion getSelectedRegion(String desiredName) {
        Selection selection;
        try {
            selection = wep.getSelection((Player) senderWrapper.getSender());
            if (selection == null) {
                throw new NullPointerException();
            }
        } catch (NullPointerException npe) {

            senderWrapper.sendMessage("Error getting your WorldEdit selection. Did you forget to make a selection?");
            return null;
        }



        Location min = selection.getMinimumPoint(), max = selection.getMaximumPoint();
        BlockVector minVect = new BlockVector(min.getBlockX(), min.getBlockY(), min.getBlockZ());
        BlockVector maxVect = new BlockVector(max.getBlockX(), max.getBlockY(), max.getBlockZ());

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(desiredName, minVect, maxVect);

        return region;
    }

    protected boolean selectionIsWithinParent(ProtectedCuboidRegion reg, MCTownsRegion parent) {
        ProtectedCuboidRegion parentReg = (ProtectedCuboidRegion) wgp.getRegionManager(wgp.getServer().getWorld(parent.getWorldName())).getRegion(parent.getName());

        if (parentReg.contains(reg.getMaximumPoint()) && parentReg.contains(reg.getMinimumPoint())) {
            return true;
        }

        return false;
    }

    protected void doRegManSave(RegionManager regMan) {
        try {
            regMan.save();
        } catch (ProtectionDatabaseException ex) {
            MCTowns.logSevere("Issue saving WG region list.");
        }
    }

    protected void broadcastTownJoin(Town t, Player whoJoined) {
        for (String pl : t.getResidentNames()) {
            try {

                (server.getPlayer(pl).equals(whoJoined) ? null : server.getPlayer(pl)).sendMessage(whoJoined.getName() + " just joined " + t.getTownName() + "!");
            } catch (NullPointerException npe) {
            }
        }
    }

    protected ArrayList<String> getOutputFriendlyTownJoinListMessages(boolean forTown, LinkedList<TownJoinInfoPair> list) {

        ArrayList<String> msgs = new ArrayList<>();
        String temp = "";
        for (int i = 0; i < list.size(); i += 3) {
            temp = "";
            for (int j = i; j < list.size() && j < i + 3; j++) {

                temp += (forTown ? list.get(j).getPlayer() : list.get(j).getTown());
                temp += " ";
            }
            msgs.add(temp);
        }

        return msgs;
    }

    public <V> void setFlag(ProtectedRegion region, Flag<V> flag, CommandSender sender, String value) throws InvalidFlagFormat {
        region.setFlag(flag, flag.parseInput(wgp, sender, value));
    }

    protected void runCommandAsConsole(String command) {
        server.dispatchCommand(server.getConsoleSender(), command);
    }
}
