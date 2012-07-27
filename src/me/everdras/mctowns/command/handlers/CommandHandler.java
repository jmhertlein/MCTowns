package me.everdras.mctowns.command.handlers;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import static me.everdras.core.chat.ChatUtil.ERR;
import static me.everdras.core.chat.ChatUtil.SUCC;
import me.everdras.core.command.ECommand;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.command.MCTCommandSenderWrapper;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.permission.Perms;
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
    protected static final String PLOT_INFIX = "_plot_";

    protected static final int RESULTS_PER_PAGE = 10;

    protected MCTowns plugin;
    protected MCTCommandSenderWrapper senderWrapper;
    protected TownManager townManager;
    protected TownJoinManager joinManager;
    protected WorldGuardPlugin wgp;
    protected WorldEditPlugin wep;
    protected Economy economy;
    protected Server server;
    protected Config options;
    protected ECommand cmd;

    /**
     * Wraps the command sender.
     *
     * @param parent the parent plugin
     * @param t the town manager
     * @param j the join manager
     * @param p the command sender
     * @param activeSets the map of active sets
     */
    public CommandHandler(MCTowns parent, TownManager t, TownJoinManager j, CommandSender p, HashMap<String, ActiveSet> activeSets, WorldGuardPlugin wg, Economy econ, Config opt, ECommand cmd) {
        townManager = t;
        joinManager = j;
        server = p.getServer();
        plugin = parent;

        senderWrapper = new MCTCommandSenderWrapper(t, p, activeSets);
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

    /**
     * Flags the specified type of region with the specified flag and the
     * specified arguments. If no arguments are supplied, the flag is instead
     * cleared.
     *
     * @param flagName name of the WorldGuard flag to set or clear
     * @param args the args to set the flag with, or empty if it's meant to be
     * cleared
     * @param regionType Which region in the ActiveSet hierarchy to apply the
     * flag to.
     */
    public void flagRegion(String flagName, String[] args, TownLevel regionType) {
        if (!senderWrapper.hasExternalPermissions("mct.flag") && !senderWrapper.hasExternalPermissions("mct.admin")) {
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


        //If there are no arguments, clear the flag instead of setting it
        if (args.length == 0) {
            wgReg.setFlag(foundFlag, null);
            senderWrapper.sendMessage(ChatColor.GREEN + "Successfully removed flag.");
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
            senderWrapper.sendMessage(ChatColor.GREEN + "Region successfully flagged.");
        } catch (InvalidFlagFormat ex) {
            senderWrapper.sendMessage(ERR + "Error parsing flag arguments: " + ex.getMessage());
            return;
        }



        doRegManSave(regMan);

    }

    public void listPlayers(TownLevel level) {
        MCTownsRegion reg = null;

        switch (level) {
            case TERRITORY:
                reg = senderWrapper.getActiveTerritory();
                break;
            case PLOT:
                reg = senderWrapper.getActivePlot();
                break;
        }

        if (reg == null) {
            senderWrapper.sendMessage(ERR + "You need to set your active " + level.toString().toLowerCase());
            return;
        }

        ProtectedRegion wgReg = wgp.getRegionManager(server.getWorld(reg.getWorldName())).getRegion(reg.getName());

        String temp = "";
        int counter;
        senderWrapper.sendMessage("Players in region: ");

        senderWrapper.sendMessage("Owners:");

        counter = 0;
        for (String pl : wgReg.getOwners().getPlayers()) {
            temp += pl + ", ";
            counter++;
            if (counter > 4) {
                senderWrapper.sendMessage(temp);
                temp = "";
                counter = 0;
            }
        }
        if (counter != 0) {
            senderWrapper.sendMessage(temp);
        }
        temp = "";

        senderWrapper.sendMessage("Members:");

        for (String pl : wgReg.getMembers().getPlayers()) {
            temp += pl + ", ";
            counter++;
            if (counter > 4) {
                senderWrapper.sendMessage(temp);
                temp = "";
                counter = 0;
            }
        }
        if (counter != 0) {
            senderWrapper.sendMessage(temp);
        }

    }

    //====================================PRIVATE===========================
    protected WorldGuardPlugin getWGPFromSenderWrapper(MCTCommandSenderWrapper csw) {
        return (WorldGuardPlugin) csw.getSender().getServer().getPluginManager().getPlugin("WorldGuard");
    }

    protected ProtectedRegion getSelectedRegion(String desiredName) {
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

        ProtectedRegion region;
        if(selection instanceof Polygonal2DSelection) {
            Polygonal2DSelection sel = (Polygonal2DSelection) selection;

            region = new ProtectedPolygonalRegion(desiredName, sel.getNativePoints(), sel.getMinimumPoint().getBlockY(), sel.getNativeMaximumPoint().getBlockY());
        } else if(selection instanceof CuboidSelection) {
            CuboidSelection sel = (CuboidSelection) selection;

            Location min = sel.getMinimumPoint(), max = sel.getMaximumPoint();

            BlockVector minVect = new BlockVector(min.getBlockX(), min.getBlockY(), min.getBlockZ());
            BlockVector maxVect = new BlockVector(max.getBlockX(), max.getBlockY(), max.getBlockZ());

            region = new ProtectedCuboidRegion(desiredName, minVect, maxVect);
        } else {
            MCTowns.logDebug("Error: The selection was neither a polygon nor a cuboid");
            throw new RuntimeException("Error: The selection was neither a poly nor a cube.");
        }

        return region;
    }

    protected boolean selectionIsWithinParent(ProtectedRegion reg, MCTownsRegion parent) {
        ProtectedRegion parentReg = wgp.getRegionManager(wgp.getServer().getWorld(parent.getWorldName())).getRegion(parent.getName());

        return selectionIsWithinParent(reg, parentReg);
    }

    protected boolean selectionIsWithinParent(ProtectedRegion reg, ProtectedRegion parentReg) {
        if(reg instanceof ProtectedCuboidRegion) {
            return parentReg.contains(reg.getMaximumPoint()) && parentReg.contains(reg.getMinimumPoint());
        } else if(reg instanceof ProtectedPolygonalRegion) {
            ProtectedPolygonalRegion ppr = (ProtectedPolygonalRegion) reg;

            for(BlockVector2D pt : ppr.getPoints()) {
                if(! parentReg.contains(pt))
                    return false;
            }

            if(! ( parentReg.contains(ppr.getMaximumPoint()) && parentReg.contains(ppr.getMinimumPoint() ) )) {
                return false;
            }

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
        broadcastTownJoin(t, whoJoined.getName());
    }

    protected void broadcastTownJoin(Town t, String s_playerWhoJoined) {
        for (String pl : t.getResidentNames()) {
            try {
                //broadcast the join to everyone BUT the player who joined.
                (pl.equals(s_playerWhoJoined) ? null : server.getPlayer(pl)).sendMessage(s_playerWhoJoined + " just joined " + t.getTownName() + "!");
            } catch (NullPointerException ignore) {}
        }
    }

    protected ArrayList<String> getOutputFriendlyTownJoinListMessages(boolean forTown, LinkedList<TownJoinInfoPair> list) {

        ArrayList<String> msgs = new ArrayList<>();
        String temp;
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

    public void redefineActiveRegion(TownLevel regType) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        MCTownsRegion reg;

        switch (regType) {
            case TOWN:
                senderWrapper.sendMessage(ERR + "Can't redefine towns.");
                return;
            case TERRITORY:
                reg = senderWrapper.getActiveTerritory();
                break;
            case PLOT:
                reg = senderWrapper.getActivePlot();
                break;
            default:
                reg = null;
        }

        if(regType == TownLevel.TERRITORY && !senderWrapper.hasExternalPermissions(Perms.ADMIN.toString())) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (reg == null) {
            senderWrapper.sendMessage(ERR + "Your active " + regType.toString() + " is not set.");
            return;
        }

        MCTowns.logDebug("Redefining " + reg.getName());

        RegionManager regMan = wgp.getRegionManager(server.getWorld(reg.getWorldName()));

        Selection nuRegionBounds = wep.getSelection(senderWrapper.getPlayer());

        if (nuRegionBounds == null) {
            senderWrapper.sendMessage(ERR + "You need to select what you want the region's boundaries to be updated to.");
            return;
        }

        ProtectedRegion oldWGReg, nuWGRegion;
        oldWGReg = regMan.getRegion(reg.getName());


        nuWGRegion = new ProtectedCuboidRegion(oldWGReg.getId(),
                nuRegionBounds.getNativeMaximumPoint().toBlockVector(),
                nuRegionBounds.getNativeMinimumPoint().toBlockVector());


        MCTowns.logDebug("Comparing:");
        MCTowns.logDebug("New: " + nuWGRegion.getMaximumPoint().toString() + " | " + nuWGRegion.getMinimumPoint());
        MCTowns.logDebug("Old: " + oldWGReg.getMaximumPoint().toString() + " | " + oldWGReg.getMinimumPoint());
        //To make sure that we can't accidentally "orphan" plots outside the region, only allow
        //new boundaries if the old region is a subset of the new region.
        if (!(nuWGRegion.contains(oldWGReg.getMaximumPoint()) && nuWGRegion.contains(oldWGReg.getMinimumPoint()))) {
            senderWrapper.sendMessage(ERR + "Your new selection must completely contain the old region (Only expansion is allowed, to ensure that subregions are not 'orphaned').");
            return;
        }

        if(!selectionIsWithinParent(nuWGRegion, oldWGReg.getParent())) {
            senderWrapper.sendMessage(ERR + "Your new selection must be within its parent region.");
            return;
        }

        //if everything is all clear...
        //copy over everything important
        nuWGRegion.setMembers(oldWGReg.getMembers());
        nuWGRegion.setOwners(oldWGReg.getOwners());
        nuWGRegion.setFlags(oldWGReg.getFlags());
        nuWGRegion.setPriority(oldWGReg.getPriority());
        try {
            nuWGRegion.setParent(oldWGReg.getParent());
        } catch (CircularInheritanceException ex) {
            MCTowns.logSevere("Error copying parent during redefine: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        //apparently, this will replace the old region by the same name while preserving parent/child relationships
        //Conjecture: RegionManager uses hashes based on the name of the region, and the children only ever store a string (paren't ID) that
        //            maps to the hash of the parent
        //sounds_legit_to_me.jpg
        regMan.addRegion(nuWGRegion);

        doRegManSave(regMan);

        senderWrapper.sendMessage(SUCC + "The region \"" + nuWGRegion.getId() + "\" has been updated.");

    }
}
