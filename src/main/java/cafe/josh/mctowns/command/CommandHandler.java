/*
 * Copyright (C) 2013 Joshua Michael Hertlein <jmhertlein@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cafe.josh.mctowns.command;

import cafe.josh.mctowns.region.MCTownsRegion;
import cafe.josh.mctowns.townjoin.TownJoinManager;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Predicate;

import static net.jmhertlein.core.chat.ChatUtil.*;
import cafe.josh.mctowns.MCTowns;
import cafe.josh.mctowns.MCTownsPlugin;
import cafe.josh.mctowns.TownManager;
import cafe.josh.mctowns.permission.Perms;
import cafe.josh.mctowns.region.Town;
import cafe.josh.mctowns.region.TownLevel;
import cafe.josh.mctowns.util.MCTConfig;
import cafe.josh.mctowns.util.WGUtils;
import cafe.josh.reflective.CommandDefinition;
import org.bukkit.Bukkit;
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
public abstract class CommandHandler implements CommandDefinition {

    protected static final int RESULTS_PER_PAGE = 10;
    protected MCTownsPlugin plugin;
    protected TownManager townManager;
    protected TownJoinManager joinManager;
    protected Server server;
    protected MCTLocalSender localSender;

    /**
     * Wraps the command sender.
     *
     * @param parent the parent plugin
     */
    public CommandHandler(MCTownsPlugin parent) {
        plugin = parent;
        townManager = MCTowns.getTownManager();
        joinManager = parent.getJoinManager();
        server = parent.getServer();
    }

    public void setNewCommand(CommandSender sender) {
        localSender = new MCTLocalSender(townManager, sender, plugin.getActiveSets());
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
        MCTownsRegion reg = null;

        switch(regionType) {
            case TOWN:
                localSender.sendMessage(ERR + "Can't apply flags to towns.");
                return;
            case TERRITORY:
                reg = localSender.getActiveTerritory();
                break;
            case PLOT:
                reg = localSender.getActivePlot();
                break;
        }

        if(reg == null) {
            localSender.sendMessage(ERR + "Your active " + regionType.toString() + " is not set.");
            return;
        }

        //grant permission if player is admin, OR (player has perm for flag AND player is mayor)
        boolean hasPermission = localSender.hasExternalPermissions("mct.admin") //is admin
                || ((localSender.hasExternalPermissions("mct.flag." + flagName) || localSender.hasExternalPermissions("mct.flag.all")) //either has perm for that flag, or all flags
                && localSender.getActiveTown().playerIsMayor(localSender.getPlayer())); //and is mayor

        if(!hasPermission) {
            localSender.notifyInsufPermissions();
            return;
        }

        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(reg.getWorldName()));

        ProtectedRegion wgReg = regMan.getRegion(reg.getName());

        if(wgReg == null) {
            MCTowns.logSevere("Error in CommandHandler.flagRegion(): The region in WG was null (somehow). Perhaps someone manually deleted a region through WorldGuard?");
            localSender.sendMessage(ERR + "An error occurred. Please see the console output for more information. This command exited safely; nothing was changed by it.");
            return;
        }

        Flag<?> foundFlag = null;

        for(Flag<?> flag : DefaultFlag.getFlags()) {
            if(flag.getName().replace("-", "").equalsIgnoreCase(flagName.replace("-", ""))) {
                foundFlag = flag;
                break;
            }
        }

        if(foundFlag == null) {
            localSender.sendMessage(ERR + "Couldn't find a matching flag.");
            return;
        }

        //If there are no arguments, clear the flag instead of setting it
        if(args.length == 0) {
            wgReg.setFlag(foundFlag, null);
            localSender.sendMessage(ChatColor.GREEN + "Successfully removed flag.");
            return;
        }

        String s_stateOfFlag = "";
        if(args.length == 1) {
            s_stateOfFlag = args[0];
        } else {
            for(String s : args) {
                s_stateOfFlag += s;
                s_stateOfFlag += " ";
            }
            s_stateOfFlag = s_stateOfFlag.substring(0, s_stateOfFlag.length() - 1);
        }

        try {
            setFlag(wgReg, foundFlag, localSender.getSender(), s_stateOfFlag);
            localSender.sendMessage(ChatColor.GREEN + "Region successfully flagged.");
        } catch(InvalidFlagFormat ex) {
            localSender.sendMessage(ERR + "Error parsing flag arguments: " + ex.getMessage());
            return;
        }

        doRegManSave(regMan);

    }

    public void listPlayers(TownLevel level) {
        if(localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }
        MCTownsRegion reg = null;

        switch(level) {
            case TERRITORY:
                reg = localSender.getActiveTerritory();
                break;
            case PLOT:
                reg = localSender.getActivePlot();
                break;
        }

        if(reg == null) {
            localSender.sendMessage(ERR + "You need to set your active " + level.toString().toLowerCase());
            return;
        }

        ProtectedRegion wgReg = MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(reg.getWorldName())).getRegion(reg.getName());

        if(wgReg == null) {
            localSender.sendMessage("Unable to get world guard region for " + reg.getName() + ". Perhaps the region was deleted outside of MCTowns?");
            return;
        }

        String temp = "";
        int counter;
        localSender.sendMessage("Players in region: ");

        localSender.sendMessage("Owners:");

        counter = 0;
        for(String pl : wgReg.getOwners().getPlayers()) {
            if(counter > 0) {
                temp += ", ";
            }
            temp += pl;
            counter++;
            if(counter > 4) {
                localSender.sendMessage(temp);
                temp = "";
                counter = 0;
            }
        }
        if(counter != 0) {
            localSender.sendMessage(temp);
        }
        temp = "";

        localSender.sendMessage("Members:");

        for(String pl : wgReg.getMembers().getPlayers()) {
            if(counter > 0) {
                temp += ", ";
            }
            temp += pl;
            counter++;
            if(counter > 4) {
                localSender.sendMessage(temp);
                temp = "";
                counter = 0;
            }
        }
        if(counter != 0) {
            localSender.sendMessage(temp);
        }

    }

    /**
     * Wraps the localSender's current selection in a ProtectedRegion and
     * returns it Supports cuboid and polygon regions
     *
     * Will throw a runtime exception if selection is not a cuboid or poly (I
     * need to make it handle this better)
     *
     * @param desiredName the desired name of the region
     *
     * @return
     */
    protected ProtectedRegion getSelectedRegion(String desiredName) {
        Selection selection;
        try {
            selection = MCTowns.getWorldGuardPlugin().getWorldEdit().getSelection(localSender.getPlayer());
            if(selection == null) {
                throw new NullPointerException();
            }
        } catch(NullPointerException npe) {
            localSender.sendMessage("Error getting your WorldEdit selection. Did you forget to make a selection?");
            return null;
        } catch(CommandException ce) {
            localSender.sendMessage("Error hooking the WorldEdit plugin. Please tell your server owner.");
            ce.printStackTrace();
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
            //throw new RuntimeException("Error: The selection was neither a poly nor a cube.");
            localSender.sendMessage("Only cuboid and polygonal regions are supported right now.");
            return null;
        }

        return region;
    }

    public static boolean selectionIsWithinParent(ProtectedRegion reg, MCTownsRegion parent) {
        ProtectedRegion parentReg = MCTowns.getWorldGuardPlugin().getRegionManager(
                MCTowns.getWorldGuardPlugin().getServer().getWorld(parent.getWorldName())).getRegion(parent.getName());

        if(parentReg == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "!!!WARNING!!! MCTowns detected it is in an invalid state: A WorldGuard region was manually deleted. See message on next line.");
            MCTowns.logSevere("While checking if a region is within its parent region, detected that WorldGuard region for \"" + parent.getName() + "\" does not exist. "
                    + "You should delete this region with MCTowns and re-create it.");
            return false;
        }

        return regionIsWithinRegion(reg, parentReg);
    }

    public static boolean regionIsWithinRegion(ProtectedRegion interior, ProtectedRegion exterior) {
        for(BlockVector2D v : interior.getPoints()) {
            if(!(exterior.contains(v))) {
                return false;
            }
        }

        boolean ret = WGUtils.intersectsEdges(interior, exterior);
        return !ret;
    }

    protected void doRegManSave(RegionManager regMan) {
        try {
            regMan.save();
        } catch(StorageException ex) {
            MCTowns.logSevere("Issue saving WG region list.");
        }
    }

    protected void broadcastTownJoin(Town t, Player whoJoined) {
        broadcastTownJoin(t, whoJoined.getName());
    }

    public static void broadcastTownJoin(Town t, String player) {
        for(String pl : t.getResidentNames()) {
            Player p = Bukkit.getPlayer(pl);
            if(p == null || pl.equals(player)) {
                continue;
            }
            p.sendMessage(player + " just joined " + t.getName() + "!");
        }
    }

    protected ArrayList<String> getOutputFriendlyTownJoinListMessages(Set<String> playerNames) {
        ArrayList<String> msgs = new ArrayList<>();

        if(playerNames.size() <= 3) {
            msgs.addAll(playerNames);
            return msgs;
        }

        int numNamesOnCurrentLine = 0;
        String curLine = "";
        for(String s : playerNames) {
            if(numNamesOnCurrentLine == 3) {
                curLine = curLine.substring(0, curLine.length() - 3);
                msgs.add(curLine);
                numNamesOnCurrentLine = 0;
                curLine = "";
            }

            curLine += s + ", ";
            numNamesOnCurrentLine++;
        }

        if(numNamesOnCurrentLine > 0) {
            curLine = curLine.substring(0, curLine.length() - 3);
            msgs.add(curLine);
        }

        return msgs;
    }

    public <V> void setFlag(ProtectedRegion region, Flag<V> flag, CommandSender sender, String value) throws InvalidFlagFormat {
        region.setFlag(flag, flag.parseInput(MCTowns.getWorldGuardPlugin(), sender, value));
    }

    protected void runCommandAsConsole(String command) {
        server.dispatchCommand(server.getConsoleSender(), command);
    }

    public void redefineActiveRegion(TownLevel regType) {
        if(!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        MCTownsRegion reg;

        switch(regType) {
            case TOWN:
                localSender.sendMessage(ERR + "Can't redefine towns.");
                return;
            case TERRITORY:
                reg = localSender.getActiveTerritory();
                break;
            case PLOT:
                reg = localSender.getActivePlot();
                break;
            default:
                reg = null;
        }

        //only let admins expand territories, unless mayors can buy territories, in which case mayors can too
        if(regType == TownLevel.TERRITORY && !(localSender.hasExternalPermissions(Perms.ADMIN.toString())
                || (MCTConfig.MAYORS_CAN_BUY_TERRITORIES.getBoolean() && localSender.hasMayoralPermissions()))) {
            localSender.notifyInsufPermissions();
            return;
        }

        if(reg == null) {
            localSender.sendMessage(ERR + "Your active " + regType.toString() + " is not set.");
            return;
        }

        Town t = localSender.getActiveTown();

        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(server.getWorld(reg.getWorldName()));

        Selection nuRegionBounds;
        try {
            nuRegionBounds = MCTowns.getWorldGuardPlugin().getWorldEdit().getSelection(localSender.getPlayer());
        } catch(CommandException ce) {
            localSender.sendMessage("Error hooking the world edit plugn. Please inform your server owner.");
            ce.printStackTrace();
            return;
        }

        if(nuRegionBounds == null) {
            localSender.sendMessage(ERR + "You need to select what you want the region's boundaries to be updated to.");
            return;
        }

        ProtectedRegion oldWGReg, nuWGRegion;
        oldWGReg = regMan.getRegion(reg.getName());

        if(oldWGReg == null) {
            localSender.sendMessage(ERR + String.format("Could not find WorldGuard region \"%s\", it was probably deleted manually. You should delete this %s.", reg.getName(), regType.toString().toLowerCase()));
            return;
        }

        if(oldWGReg instanceof ProtectedPolygonalRegion) {
            if(!(nuRegionBounds instanceof Polygonal2DSelection)) {
                localSender.sendMessage(ERR + "Error: selection type does not match region type. Must be a polygonal selection.");
                return;
            }
            Polygonal2DSelection polySel = (Polygonal2DSelection) nuRegionBounds;

            nuWGRegion = new ProtectedPolygonalRegion(oldWGReg.getId(), polySel.getNativePoints(), polySel.getMaximumPoint().getBlockY(), polySel.getNativeMinimumPoint().getBlockY());
        } else if(oldWGReg instanceof ProtectedCuboidRegion) {
            if(!(nuRegionBounds instanceof CuboidSelection)) {
                localSender.sendMessage(ERR + "Error: selection type does not match region type. Must be a cuboid selection.");
                return;
            }
            nuWGRegion = new ProtectedCuboidRegion(oldWGReg.getId(),
                    nuRegionBounds.getNativeMaximumPoint().toBlockVector(),
                    nuRegionBounds.getNativeMinimumPoint().toBlockVector());
        } else {
            localSender.sendMessage(ERR + "Unsupported region type: " + oldWGReg.getTypeName());
            return;
        }

        //To make sure that we can't accidentally "orphan" plots outside the region, only allow
        //new boundaries if the old region is a subset of the new region.
        if(!regionIsWithinRegion(oldWGReg, nuWGRegion)) {
            localSender.sendMessage(ERR + "Your new selection must completely contain the old region (Only expansion is allowed, to ensure that subregions are not 'orphaned').");
            return;
        }

        if(regType != TownLevel.TERRITORY && !regionIsWithinRegion(nuWGRegion, oldWGReg.getParent())) {
            localSender.sendMessage(ERR + "Your new selection must be within its parent region.");
            return;
        }

        //if they're not an admin, charge them for the territory
        if(regType == TownLevel.TERRITORY && !localSender.hasExternalPermissions(Perms.ADMIN.toString())) {
            if(!MCTConfig.ECONOMY_ENABLED.getBoolean()) {
                localSender.sendMessage(ERR + "You're not an admin, and mayors can only redefine territories by buying more blocks, yet the economy is not enabled.");
                return;
            }

            BigDecimal price = new BigDecimal(MCTConfig.PRICE_PER_XZ_BLOCK.getString()).multiply(new BigDecimal(WGUtils.getNumXZBlocksInRegion(nuWGRegion) - WGUtils.getNumXZBlocksInRegion(oldWGReg)));

            if(t.getBank().getCurrencyBalance().compareTo(price) < 0) {
                //If they can't afford it...
                localSender.sendMessage(ERR + "There is not enough money in your " + INFO + "town's bank account" + ERR + " to buy a region that large.");
                localSender.sendMessage(ERR + "Total Price: " + price);
                localSender.sendMessage(INFO + "Add money to your town's bank with: /town bank deposit currency <amount>");
                return;
            }

            //otherwise...
            t.getBank().withdrawCurrency(price);

            localSender.sendMessage(ChatColor.GREEN + "Purchase success! Total price was: " + price.toString());
        }

        //if everything is all clear...
        //copy over everything important
        nuWGRegion.setMembers(oldWGReg.getMembers());
        nuWGRegion.setOwners(oldWGReg.getOwners());
        nuWGRegion.setFlags(oldWGReg.getFlags());
        nuWGRegion.setPriority(oldWGReg.getPriority());
        try {
            nuWGRegion.setParent(oldWGReg.getParent());
        } catch(CircularInheritanceException ex) {
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

        localSender.sendMessage(SUCC + "The region \"" + nuWGRegion.getId() + "\" has been updated.");

    }

    @Override
    public Predicate<CommandSender> getFilter(String filterName) {
        switch(filterName) {
            case "mayoralPerms":
                return MCTLocalSender::hasMayoralPermissions;
            default:
                return null;
        }
    }

}
