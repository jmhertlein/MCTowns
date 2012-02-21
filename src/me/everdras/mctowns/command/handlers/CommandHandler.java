package me.everdras.mctowns.command.handlers;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.banking.BlockBank;
import me.everdras.mctowns.command.*;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.permission.Perms;
import me.everdras.mctowns.structure.*;
import me.everdras.mctowns.townjoin.*;
import me.everdras.mctowns.util.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Location;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * CommandHandler wraps a CommandSender and various other pertinent objects
 * together in order to allow each slash command to correspond with an
 * equivalent method here.
 *
 * @author joshua
 */
public class CommandHandler {

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

    //=================================WG-MCT BRIDGE UTILITIES==============
    /**
     * Not implemented yet.
     */
    public void checkIfRegionIsManagedByMCTowns() {
    }

    public void flagRegion(String flagName, String[] args, TownLevel regionType) {

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

    /**
     * Precondition: The region passed exists and passed town exists, the
     * desired district name is not the name of an existing WG region
     * Postcondition: The passed region is deleted. A Territory equivalent to
     * the deleted region is made, and a district the same size as the parent
     * territory is made. The child plots of the deleted region are made
     * children of the district and are added to the Town as plots in the
     * district.
     *
     * @param townName the name of the town to add the new territory to
     * @param regionName the region to be converted into a territory
     * @param desiredDistrictName the desired name of the district to be made.
     */
    public void convertRegionToMCTown(String townName, String regionName, String desiredDistrictName) {
        if (!senderWrapper.hasExternalPermissions(Perms.ADMIN.toString())) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = townManager.getTown(townName);

        if (t == null) {
            senderWrapper.sendMessage(townName + " doesn't exist.");
            return;
        }

        RegionManager regMan;
        regMan = wgp.getRegionManager(senderWrapper.getPlayer().getWorld());

        ProtectedRegion parent = regMan.getRegion(regionName);

        if (parent == null) {
            senderWrapper.sendMessage(regionName + " is not an existing region in this world.");
            return;
        }

        Stack<ProtectedRegion> children = new Stack<>();

        Collection<ProtectedRegion> regs = regMan.getRegions().values();


        senderWrapper.sendMessage("MCTowns is now searching through every single WG region that is in this world to find children plots. This may take anywhere from less than a second to over a minute depending on how many regions you have.");
        for (ProtectedRegion protReg : regs) {
            if (protReg.getParent() != null && protReg.getParent().equals(parent)) {
                children.push(protReg);
            }
        }

        Territory nuTerrit = new Territory(t.getTownName() + "_territ_" + parent.getId(), t.getWorldName());


        ProtectedCuboidRegion nuParent = new ProtectedCuboidRegion(nuTerrit.getName(), parent.getMinimumPoint(), parent.getMaximumPoint());
        nuParent.setOwners(parent.getOwners());
        nuParent.setMembers(parent.getMembers());
        nuParent.setFlags(parent.getFlags());
        regMan.addRegion(nuParent);

        District nuDist = new District(t.getTownName() + "_dist_" + desiredDistrictName, t.getWorldName());

        ProtectedCuboidRegion nuDistReg = new ProtectedCuboidRegion(nuDist.getName(), parent.getMinimumPoint(), parent.getMaximumPoint());

        regMan.addRegion(nuDistReg);
        try {
            nuDistReg.setParent(nuParent);
        } catch (CircularInheritanceException ex) {
            ex.printStackTrace(System.err);
        }

        t.addTerritory(nuTerrit);
        nuTerrit.addDistrict(nuDist);

        Plot p = null;
        for (ProtectedRegion plotReg : children) {
            p = new Plot(plotReg.getId(), t.getWorldName());
            nuDist.addPlot(p);

            try {
                plotReg.setParent(nuDistReg);
            } catch (CircularInheritanceException ex) {
                senderWrapper.sendMessage(ChatColor.DARK_RED + "Something bad happened. Please tell everdras that there was a Circular Inheritance Exception in /mct convert");
            }
        }

        regMan.removeRegion(parent.getId());

        try {
            regMan.save();
        } catch (IOException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, "MCTowns was unable to force a save of WG regions for this world. Some changes may not persist.");
        }

        senderWrapper.sendMessage("Done. Your territory should now be set up!");


    }

    //=========================TOWN CREATION/DELETION=======================
    /**
     * Creates a new town. Checks to make sure town doesn't already exist, mayor
     * is not a member of another town, the mayor exists.
     *
     * @param townName the name the town will be.
     * @param mayorName the name of the player who will be the mayor
     */
    public void createTown(String townName, String mayorName) {
        if (!senderWrapper.canCreateTown()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Player nuMayor = server.getPlayer(mayorName);

        if (nuMayor == null) {
            senderWrapper.sendMessage(ERR + mayorName + " doesn't exist or is not online.");
            return;
        }

        if (townManager.matchPlayerToTown(nuMayor) != null) {
            senderWrapper.sendMessage(ERR + nuMayor.getName() + " is already a member of a town, and as such cannot be the mayor of a new one.");
            return;
        }

        if (townManager.addTown(townName, nuMayor)) {
            senderWrapper.sendMessage("Town " + townName + " has been created.");
            server.broadcastMessage(SUCC + "The town " + townName + " has been founded.");
        } else {
            senderWrapper.sendMessage(ERR + "That town already exists!");
        }



    }

    /**
     * Removes a town if it exists.
     *
     * @param townName the name of the town to be removed, case sensitive
     */
    public void removeTown(String townName) {
        if (!senderWrapper.canDeleteTown()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = townManager.getTown(townName);
        if (t == null) {
            senderWrapper.sendMessage(ERR + "The town \"" + townName + "\" does not exist.");
            return;
        }

        townManager.removeTown(wgp, townName);

        try {
            wgp.getRegionManager(server.getWorld(t.getWorldName())).save();
        } catch (IOException ex) {
            MCTowns.logSevere("Error: unable to force a region manager save in WorldGuard. Details:");
            MCTowns.logSevere(ex.getMessage());
        } catch (NullPointerException npe) {
            MCTowns.logSevere("Couldn't force WG to save its regions. (null)");
            MCTowns.logSevere("Debug analysis:");
            MCTowns.logSevere("WG plugin was null: " + (wgp == null));
            MCTowns.logSevere("Server was null: " + (wgp == null));
            MCTowns.logSevere("Town was null: " + (t == null));
            MCTowns.logSevere("Town's world (String) was null in storage: " + (t.getWorldName() == null));
            MCTowns.logSevere("Town's world was null: " + (server.getWorld(t.getWorldName()) == null));
        }

        senderWrapper.sendMessage("Town removed.");
        server.broadcastMessage(ChatColor.DARK_RED + townName + " has been disbanded.");

    }

    //==========================TOWN INFO MANAGEMENT========================
    /**
     * Displays the town's information
     *
     * @param townName the name of the town to query
     */
    public void queryTownInfo(String townName) {
        Town t = townManager.getTown(townName);

        if (t == null) {
            senderWrapper.sendMessage(ERR + "The town \"" + townName + "\" does not exist.");
            return;
        }

        ChatColor c = ChatColor.AQUA;

        senderWrapper.sendMessage(c + "Name: " + t.getTownName());
        senderWrapper.sendMessage(c + "Mayor: " + t.getMayor());
        senderWrapper.sendMessage(c + "World: " + t.getWorldName());
        senderWrapper.sendMessage(c + "Number of residents: " + t.getSize());
        senderWrapper.sendMessage(c + "Plots are buyable: " + t.usesBuyablePlots());
        senderWrapper.sendMessage(c + "Join method: " + (t.usesEconomyJoins() ? "Plot purchase" : "invitations"));



    }

    /**
     * Displays town-centric information about the player
     *
     * @param playerName name of the player to check
     */
    public void queryPlayerInfo(String playerName) {
        Player p = server.getPlayer(playerName);

        if (p == null && townManager.matchPlayerToTown(playerName) == null) {
            senderWrapper.sendMessage(ERR + "That player is either not online or doesn't exist.");
            return;
        }

        String playerExactName = (p == null ? playerName : p.getName());

        Town t = townManager.matchPlayerToTown(playerExactName);

        if (t == null) {
            senderWrapper.sendMessage("Player: " + playerExactName);
            senderWrapper.sendMessage("Town: None");
            senderWrapper.sendMessage("Is Mayor: n/a");
            senderWrapper.sendMessage("Is Assistant: n/a");
        } else {
            senderWrapper.sendMessage("Player: " + playerExactName);
            senderWrapper.sendMessage("Town: " + t.getTownName());
            senderWrapper.sendMessage("Is Mayor: " + t.getMayor().equals(playerExactName));
            senderWrapper.sendMessage("Is Assistant: " + t.playerIsAssistant(playerExactName));
        }

    }

    /**
     * lists the first page of existing towns
     */
    public void listTowns() {

        listTowns(1);
    }

    /**
     * lists the page-th page of towns
     *
     * @param page
     */
    public void listTowns(String page) {
        int intPage = 0;
        try {
            intPage = Integer.parseInt(page);
        } catch (Exception e) {
            senderWrapper.sendMessage(ERR + "Parsing error: <page> is not a valid integer.");
            return;
        }

        this.listTowns(intPage);
    }

    /**
     * helper function for listTowns(String)
     *
     * @param page the page to be displayed
     */
    private void listTowns(int page) {
        if (page <= 0) {
            senderWrapper.sendMessage(ERR + "Invalid page.");
            return;
        }
        senderWrapper.sendMessage(ChatColor.AQUA + "Existing towns (page " + page + "):");



        Town[] towns = townManager.getTownsCollection().toArray(new Town[townManager.getTownsCollection().size()]);

        for (int i = page - 1; i < towns.length && i < i + 5; i++) {
            senderWrapper.sendMessage(ChatColor.YELLOW + towns[i].getTownName());
        }


    }


    /**
     *
     * @param page
     */
    public void listDistricts(int page) {

        Territory t = senderWrapper.getActiveTerritory();

        if (t == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }
        senderWrapper.sendMessage(ChatColor.AQUA + "Existing districts (page " + page + "):");



        District[] dists = t.getDistrictsCollection().toArray(new District[t.getDistrictsCollection().size()]);

        for (int i = page - 1; i < dists.length && i < i + 5; i++) {
            senderWrapper.sendMessage(ChatColor.YELLOW + dists[i].getName());
        }
    }

    /**
     *
     */
    public void listDistricts() {
        listDistricts(1);
    }

    /**
     *
     * @param page
     */
    public void listPlots(int page) {

        District d = senderWrapper.getActiveDistrict();

        if (d == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }
        senderWrapper.sendMessage(ChatColor.AQUA + "Existing districts (page " + page + "):");



        Plot[] plots = d.getPlotsCollection().toArray(new Plot[d.getPlotsCollection().size()]);

        for (int i = page - 1; i < plots.length && i < i + 5; i++) {
            senderWrapper.sendMessage(ChatColor.YELLOW + plots[i].getName());
        }
    }

    /**
     *
     */
    public void listPlots() {
        listPlots(1);
    }

    /**
     *
     * @param motd
     */


    public void printPlotInfo() {
        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }
        ChatColor c = ChatColor.AQUA;
        senderWrapper.sendMessage(c + "Plot name: " + p.getAbstractName());
        senderWrapper.sendMessage(c + "Corresponding WG Region name: " + p.getName());
        senderWrapper.sendMessage(c + "World name: " + p.getWorldName());
        senderWrapper.sendMessage(c + "Plot is for sale: " + p.isForSale());
        senderWrapper.sendMessage(c + "Plot price: " + p.getPrice());

    }

    //============================PvP=======================================




    /**
     *
     * @param townName
     */
    public void requestAdditionToTown(String townName) {
        if (townManager.playerIsAlreadyInATown(senderWrapper.getPlayer())) {
            senderWrapper.sendMessage(ERR + "You cannot be in more than one town at a time.");
            return;
        }

        Town addTo = townManager.getTown(townName);

        if (addTo == null) {
            senderWrapper.sendMessage(ERR + "\"" + townName + "\" doesn't exist.");
            return;
        }

        if (addTo.usesEconomyJoins()) {
            senderWrapper.sendMessage(addTo.getTownName() + " doesn't use the invitation system.");
            return;
        }

        TownJoinInfoPair infoPair = new TownJoinInfoPair(addTo, senderWrapper.getPlayer());

        if (joinManager.matchRequestToInivteAndDiscard(infoPair)) {
            addTo.addPlayer(senderWrapper.getPlayer());
            senderWrapper.sendMessage("You have joined " + addTo.getTownName() + "!");
            broadcastTownJoin(addTo, senderWrapper.getPlayer());
        } else {
            joinManager.submitRequest(infoPair);
            senderWrapper.sendMessage("You have submitted a request to join " + townName + ".");
            addTo.broadcastMessageToTown(server, senderWrapper.getPlayer().getName() + " has submitted a request to join the town.");

        }
    }

    /**
     * Called by the mayor of a town to reject a request to join the town.
     *
     * @param playerName
     */


    /**
     *
     * @param townName
     */
    public void rejectInvitation(String townName) {

        Player p = senderWrapper.getPlayer();
        Town t = townManager.getTown(townName);

        if (t == null) {
            senderWrapper.sendMessage(ERR + "\"" + townName + "\" doesn't exist.");
            return;
        }

        if (!joinManager.removeInvitation(t, p)) {
            senderWrapper.sendMessage(ERR + "No matching invite found.");
        } else {
            senderWrapper.sendMessage(ChatColor.GOLD + "You have rejected the request to join " + townName);

            t.broadcastMessageToTown(server, ERR + p.getName() + " has declined the invitation to join the town.");
        }

    }

    /**
     * Rejects all invitations pending for the command sender.
     */
    public void rejectAllInvitations() {
        LinkedList<TownJoinInfoPair> invs = joinManager.getInvitesForPlayer(senderWrapper.getPlayer());
        int count = 0;
        for (TownJoinInfoPair tjip : invs) {
            count++;
            joinManager.removeInvitation(townManager.getTown(tjip.getTown()), server.getPlayerExact(tjip.getPlayer()));
        }

        senderWrapper.sendMessage(ChatColor.LIGHT_PURPLE + "All invitations rejected. " + count + " rejected total this sweep.");
    }



    /**
     *
     * @param townName
     */
    public void cancelRequest(String townName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = townManager.getTown(townName);

        if (t == null) {
            senderWrapper.sendMessage(ERR + "That town doesn't exist.");
            return;
        }

        if (joinManager.removeRequest(t, senderWrapper.getPlayer())) {
            senderWrapper.sendMessage(ChatColor.GOLD + "You have withdrawn your request to join " + t.getTownName() + ".");
        } else {
            senderWrapper.sendMessage(ERR + "You haven't submitted a request to join " + t.getTownName() + ".");
        }

    }



    /**
     *
     */
    public void listInvitesForPlayer() {
        LinkedList<TownJoinInfoPair> invs = joinManager.getInvitesForPlayer(senderWrapper.getPlayer());

        senderWrapper.sendMessage(ChatColor.DARK_BLUE + "There are pending invites from the following towns:");


        for (String s : getOutputFriendlyTownJoinListMessages(false, invs)) {
            senderWrapper.sendMessage(ChatColor.YELLOW + s);
        }

    }

    /**
     *
     */
    public void listRequestsForPlayer() {
        LinkedList<TownJoinInfoPair> reqs = joinManager.getRequestsForPlayer(senderWrapper.getPlayer());

        senderWrapper.sendMessage(ChatColor.DARK_BLUE + "You have requested to join the following towns:");

        for (String s : getOutputFriendlyTownJoinListMessages(false, reqs)) {
            senderWrapper.sendMessage(ChatColor.YELLOW + s);
        }
    }


    //===============================DISTRICT REGION MANAGEMENT=============
    /**
     *
     * @param distName
     */
    public void addDistrictToTerritory(String distName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        boolean autoActive = !cmd.hasFlag(MCTCommand.DISABLE_AUTOACTIVE);

        distName = senderWrapper.getActiveTown().getTownName() + DISTRICT_INFIX + distName;

        String worldName = senderWrapper.getActiveTown().getWorldName();
        District d = new District(distName, worldName);
        Territory parTerr = senderWrapper.getActiveTerritory();

        if (parTerr == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }



        ProtectedCuboidRegion region = getSelectedRegion(d.getName());

        if (region == null) {
            return;
        }

        if (!this.selectionIsWithinParent(region, senderWrapper.getActiveTerritory())) {
            senderWrapper.sendMessage(ERR + "Selection is not in territory!");
            return;
        }


        ProtectedRegion parent = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(senderWrapper.getActiveTerritory().getName());
        try {
            region.setParent(parent);
        } catch (CircularInheritanceException ex) {
            Logger.getLogger("Minecraft").log(Level.WARNING, "Circular Inheritence in addDistrictToTown.");
        }
        RegionManager regMan = wgp.getRegionManager(wgp.getServer().getWorld(worldName));

        if (regMan.hasRegion(distName)) {
            senderWrapper.sendMessage(ERR + "That name is already in use. Please pick a different one.");
            return;
        }

        regMan.addRegion(region);

        parTerr.addDistrict(d);

        senderWrapper.sendMessage("District added.");

        doRegManSave(regMan);

        if (autoActive) {
            senderWrapper.setActiveDistrict(d);
            senderWrapper.sendMessage(INFO + "Active district set to newly created district.");

        }

    }

    /**
     *
     * @param districtName
     */
    public void removeDistrictFromTerritory(String districtName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Territory t = senderWrapper.getActiveTerritory();

        if (t == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }

        District removeMe = t.getDistrict(districtName);

        if (removeMe == null) {
            senderWrapper.sendMessage(ERR + "That district doesn't exist. Make sure you're using the full name of the district (townname_district_districtshortname).");
        }

        t.removeDistrict(districtName);

        townManager.unregisterDistrictFromWorldGuard(wgp, removeMe);
        senderWrapper.sendMessage(SUCC + "District removed.");
    }

    //==============================PLOT REGION MANAGEMENT==================
    /**
     *
     * @param plotName
     */
    public void addPlotToDistrict(String plotName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        boolean autoActive = !cmd.hasFlag(MCTCommand.DISABLE_AUTOACTIVE);

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        District d = senderWrapper.getActiveDistrict();


        if (d == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }

        String worldName = senderWrapper.getActiveTown().getWorldName();

        plotName = senderWrapper.getActiveTown().getTownName() + PLOT_INFIX + plotName;
        Plot p = new Plot(plotName, worldName);
        p.setPrice(t.getDefaultPlotPrice());

        ProtectedCuboidRegion plotRegion = getSelectedRegion(p.getName());

        if (plotRegion == null) {
            return;
        }

        if (!this.selectionIsWithinParent(plotRegion, d)) {
            senderWrapper.sendMessage(ERR + "Selection is not in your active district!");
            return;
        }


        ProtectedRegion parent = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(d.getName());
        try {
            plotRegion.setParent(parent);
        } catch (CircularInheritanceException ex) {
            Logger.getLogger("Minecraft").log(Level.WARNING, "Circular Inheritence in addDistrictToTown.");
        }


        RegionManager regMan = wgp.getRegionManager(wgp.getServer().getWorld(worldName));
        if (regMan.hasRegion(plotName)) {
            senderWrapper.sendMessage(ERR + "That name is already in use. Please pick a different one.");
            return;
        }
        regMan.addRegion(plotRegion);
        d.addPlot(p);


        doRegManSave(regMan);
        senderWrapper.sendMessage("Plot added.");
        p.calculateSignLoc(wgp);

        if (autoActive) {
            senderWrapper.setActivePlot(p);
            senderWrapper.sendMessage(ChatColor.LIGHT_PURPLE + "Active plot set to newly created plot.");

        }

        if (options.isEconomyEnabled() && senderWrapper.getActiveTown().usesBuyablePlots()) {
            p.setForSale(true);
            p.buildSign(server);
        }
    }

    /**
     *
     * @param plotName
     */
    public void removePlotFromDistrict(String plotName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }
        District d = senderWrapper.getActiveDistrict();

        if (d == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }

        Plot removeMe = d.getPlot(plotName);

        if (removeMe == null) {
            senderWrapper.sendMessage(ERR + "That plot doesn't exist. Make sure you're using the full name of the plot (townname_plot_plotshortname).");
        }

        d.removePlot(plotName);

        townManager.unregisterPlotFromWorldGuard(wgp, removeMe);

        senderWrapper.sendMessage(SUCC + "Plot removed.");
    }

    /**
     *
     */
    public void movePlotInTown() {
        //Pushed off to post-1.0
    }

    //=====================ADD/REMOVE PLAYERS FROM REGIONS==================
    /**
     *
     * @param playerName
     */
    public void removePlayerFromPlot(String player) {
        Plot p = senderWrapper.getActivePlot();
        player = player.toLowerCase();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        RegionManager regMan = wgp.getRegionManager(server.getWorld(p.getWorldName()));

        ProtectedRegion wg_plot = regMan.getRegion(p.getName());

        //if they are neither mayor nor owner
        if (!(senderWrapper.hasMayoralPermissions() || wg_plot.getOwners().contains(wgp.wrapPlayer(senderWrapper.getPlayer())))) {
            senderWrapper.notifyInsufPermissions();
            return;
        }



        if (p.removePlayerFromWGRegion(wgp, player)) {
            senderWrapper.sendMessage("Player removed from plot.");
        } else {
            senderWrapper.sendMessage(ERR + player + " is not a member of this region.");
        }



    }

    /**
     *
     * @param playerName
     */
    public void addPlayerToPlot(String playerName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Plot p = senderWrapper.getActivePlot();
        Player player = server.getPlayer(playerName);

        if (!senderWrapper.getActiveTown().playerIsResident(player)) {
            senderWrapper.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        if (player == null) {
            senderWrapper.sendMessage(ERR + playerName + " is not online. Make sure you typed their name correctly!");
        }

        if (p.addPlayerToWGRegion(wgp, playerName)) {
            senderWrapper.sendMessage("Player added to plot.");
        } else {
            senderWrapper.sendMessage(ERR + "That player is already in that plot.");
        }

    }

    /**
     *
     * @param playerName
     */
    public void addPlayerToDistrict(String playerName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        District dist = senderWrapper.getActiveDistrict();
        Player player = server.getPlayer(playerName);



        if (dist == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }

        if (player == null) {
            senderWrapper.sendMessage(ChatColor.YELLOW + playerName + " is not online. Make sure you typed their name correctly!");
        }

        if (!senderWrapper.getActiveTown().playerIsResident(playerName)) {
            senderWrapper.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        if (dist.addPlayerToWGRegion(wgp, playerName)) {
            senderWrapper.sendMessage("Player added to district.");
        } else {
            senderWrapper.sendMessage(ERR + "That player is already in that district.");
        }
    }

    /**
     *
     * @param playerName
     * @param recursive
     */
    public void removePlayerFromDistrict(String player) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        boolean recursive = cmd.hasFlag(MCTCommand.RECURSIVE);

        District dist = senderWrapper.getActiveDistrict();

        if (dist == null) {
            senderWrapper.notifyActiveDistrictNotSet();
            return;
        }

        if (player == null) {
            senderWrapper.sendMessage(ERR + "That player is not online.");
            return;
        }

        if (recursive) {
            if (!dist.removePlayerFromWGRegion(wgp, player)) {
                senderWrapper.sendMessage(ERR + "That player is not in that district.");
                return;
            }

            for (Plot p : dist.getPlotsCollection()) {
                p.removePlayerFromWGRegion(wgp, player);
            }

            senderWrapper.sendMessage("Player removed from district.");

        } else {
            if (dist.removePlayerFromWGRegion(wgp, player)) {
                senderWrapper.sendMessage("Player removed from district.");
            } else {
                senderWrapper.sendMessage(ERR + "That player is not in that district.");
            }
        }
    }

    /**
     *
     * @param playerName
     */
    public void addPlayerToTerritory(String playerName) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Territory territ = senderWrapper.getActiveTerritory();
        Player player = server.getPlayer(playerName);

        if (player == null) {
            senderWrapper.sendMessage(ChatColor.YELLOW + playerName + " is not online. Make sure you typed their name correctly!");
        }

        if (!senderWrapper.getActiveTown().playerIsResident(player)) {
            senderWrapper.sendMessage(ERR + "That player is not a member of the town.");
            return;
        }

        if (territ == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }

        if (territ.addPlayerToWGRegion(wgp, playerName)) {
            senderWrapper.sendMessage("Player added to territory.");
        } else {
            senderWrapper.sendMessage(ERR + "That player is already in that territory.");
        }
    }

    /**
     *
     * @param playerName
     * @param recursive
     */
    public void removePlayerFromTerritory(String player) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        boolean recursive = cmd.hasFlag(MCTCommand.RECURSIVE);

        Territory territ = senderWrapper.getActiveTerritory();

        if (territ == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }

        if (player == null) {
            senderWrapper.sendMessage(ERR + "That player is not online.");
            return;
        }

        if (recursive) {
            if (!territ.removePlayerFromWGRegion(wgp, player)) {
                senderWrapper.sendMessage(ERR + "That player is not in this territory.");
                return;
            }


            for (District d : territ.getDistrictsCollection()) {
                d.removePlayerFromWGRegion(wgp, player);
                for (Plot p : d.getPlotsCollection()) {
                    p.removePlayerFromWGRegion(wgp, player);
                }
            }
            senderWrapper.sendMessage("Player removed from territory.");

        } else {
            if (!territ.removePlayerFromWGRegion(wgp, player)) {
                senderWrapper.sendMessage(ERR + "That player is not in this territory.");
                return;
            }
            senderWrapper.sendMessage("Player removed from territory.");
        }
    }

    public void addPlayerToPlotAsGuest(String playername) {
        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        RegionManager regMan = wgp.getRegionManager(server.getWorld(p.getWorldName()));

        ProtectedRegion wg_plot = regMan.getRegion(p.getName());

        //if they are neither mayor nor owner
        if (!(senderWrapper.hasMayoralPermissions() || wg_plot.getOwners().contains(wgp.wrapPlayer(senderWrapper.getPlayer())))) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (server.getPlayer(playername) == null) {
            senderWrapper.sendMessage(ChatColor.GOLD + "The player " + playername + " is not online! Make sure their name is spelled correctly!");
        }

        wg_plot.getMembers().addPlayer(playername);

        senderWrapper.sendMessage(ChatColor.GREEN + "Successfully added " + playername + " to the plot as a guest.");
    }

    public void setPlotBuyability(String s_forSale) {

        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        if (!t.usesBuyablePlots()) {
            senderWrapper.sendMessage(ERR + t.getTownName() + " does not allow the sale of plots.");
            return;
        }

        boolean forSale;
        try {
            forSale = Boolean.parseBoolean(s_forSale);
        } catch (Exception e) {
            senderWrapper.sendMessage(ERR + "Error parsing boolean on token: " + s_forSale);
            return;
        }



        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        p.setForSale(forSale);
        senderWrapper.sendMessage(ChatColor.GREEN + "The plot " + p.getName() + " is " + (forSale ? "now" : "no longer") + " for sale!");

    }

    public void setPlotPrice(String s_price) {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        BigDecimal price;

        try {
            price = new BigDecimal(s_price);
        } catch (Exception e) {
            senderWrapper.sendMessage(ERR + "Error parsing float on token: " + s_price);
            return;
        }

        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        p.setPrice(price);
        senderWrapper.sendMessage(ChatColor.GREEN + "Price of " + p.getName() + " set to " + p.getPrice() + ".");
    }



    public void buildSign() {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (!options.isEconomyEnabled()) {
            senderWrapper.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        p.buildSign(server);
        senderWrapper.sendMessage("Sign built!");
    }

    public void demolishSign() {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        if (!options.isEconomyEnabled()) {
            senderWrapper.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        p.demolishSign(server);
        senderWrapper.sendMessage("Sign demolished.");
    }

    public void setPlotSignPosition() {
        if (!senderWrapper.hasMayoralPermissions()) {
            senderWrapper.notifyInsufPermissions();
            return;
        }

        Plot p = senderWrapper.getActivePlot();

        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        me.everdras.mctowns.structure.Location mctLoc;

        Player player = senderWrapper.getPlayer();

        mctLoc = me.everdras.mctowns.structure.Location.convertFromBukkitLocation(player.getTargetBlock(null, 5).getLocation());

        if (mctLoc == null) {
            senderWrapper.sendMessage(ERR + "Couldn't get the location you're looking at.");
            return;
        }

        //use the block ABOVE the one the player is staring at.
        mctLoc.setY(mctLoc.getY() + 1);

        p.setSignLoc(mctLoc);

        senderWrapper.sendMessage(ChatColor.GREEN + " successfully set the location for the sign.");


    }

    public void surrenderPlot() {
        Plot p = senderWrapper.getActivePlot();
        if (p == null) {
            senderWrapper.notifyActivePlotNotSet();
            return;
        }

        ProtectedRegion reg = wgp.getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName());

        if (!reg.isOwner(wgp.wrapPlayer(senderWrapper.getPlayer()))) {
            senderWrapper.sendMessage(ERR + "You don't own this plot, so you can't surrender it!");
            return;
        }

        reg.getOwners().removePlayer(senderWrapper.getPlayer().getName());

        for (String name : reg.getMembers().getPlayers()) {
            reg.getMembers().removePlayer(name);
        }



        p.setForSale(false);
        p.setPrice(BigDecimal.ZERO);


    }

    public void confirmPlotPurchase(HashMap<Player, ActiveSet> buyers) {
        if (!options.isEconomyEnabled()) {
            senderWrapper.sendMessage(ERR + "The economy isn't enabled for your server.");
            return;
        }

        ActiveSet plotToBuy = buyers.get(senderWrapper.getPlayer());

        if (plotToBuy == null) {
            senderWrapper.sendMessage(ERR + "You haven't selected a plot to buy yet.");
            return;
        }

        if (townManager.playerIsAlreadyInATown(senderWrapper.getPlayer())) {
            if (!plotToBuy.getActiveTown().equals(townManager.matchPlayerToTown(senderWrapper.getPlayer()))) {
                senderWrapper.sendMessage(ERR + "You're already in a different town.");
                return;
            }
        }

        if (!plotToBuy.getActiveTown().playerIsResident(senderWrapper.getPlayer())) {
            if (!plotToBuy.getActiveTown().usesEconomyJoins()) {
                senderWrapper.sendMessage(ERR + "You aren't a member of this town.");
                return;
            }
        }

        if (!plotToBuy.getActiveTown().usesBuyablePlots()) {
            senderWrapper.sendMessage(ERR + "This town's plots aren't buyable.");
            return;
        }

        Plot p = plotToBuy.getActivePlot();

        if (!p.isForSale()) {
            senderWrapper.sendMessage(ERR + "This plot isn't for sale.");
            return;
        }

        if (!economy.withdrawPlayer(senderWrapper.getPlayer().getName(), p.getPrice().floatValue()).transactionSuccess()) {
            senderWrapper.sendMessage(ERR + "Insufficient funds.");
            return;
        }

        plotToBuy.getActiveTown().getBank().depositCurrency(p.getPrice());

        p.setPrice(BigDecimal.ZERO);
        p.setForSale(false);
        ProtectedRegion plotReg = wgp.getRegionManager(server.getWorld(p.getWorldName())).getRegion(p.getName());
        p.demolishSign(server);

        plotReg.getOwners().addPlayer(senderWrapper.getPlayer().getName());

        senderWrapper.sendMessage(ChatColor.GREEN + "You are now the proud owner of this plot.");
        doRegManSave(wgp.getRegionManager(server.getWorld(p.getWorldName())));


        if (!townManager.playerIsAlreadyInATown(senderWrapper.getPlayer())) {
            plotToBuy.getActiveTown().addPlayer(senderWrapper.getPlayer());
            senderWrapper.sendMessage(ChatColor.GREEN + "You have joined the town " + plotToBuy.getActiveTown().getTownName());
        }


    }



    /**
     *
     * @param territName
     */
    public void setActiveTerritory(String territName) {
        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }



        Territory nuActive = t.getTerritory(territName);

        if (nuActive == null) {
            nuActive = t.getTerritory((t.getTownName() + TERRITORY_INFIX + territName).toLowerCase());
        }

        if (nuActive == null) {
            senderWrapper.sendMessage(ERR + "The territory \"" + territName + "\" does not exist.");
            return;
        }

        senderWrapper.setActiveTerritory(nuActive);
        senderWrapper.sendMessage("Active territory set to " + nuActive.getName());
    }

    /**
     *
     * @param distName
     */
    public void setActiveDistrict(String distName) {

        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        Territory te = senderWrapper.getActiveTerritory();

        if (te == null) {
            senderWrapper.notifyActiveTerritoryNotSet();
            return;
        }



        District nuActive = te.getDistrict(distName);

        if (nuActive == null) {
            nuActive = te.getDistrict((t.getTownName() + DISTRICT_INFIX + distName).toLowerCase());
        }

        if (nuActive == null) {
            senderWrapper.sendMessage(ERR + "The district \"" + distName + "\" does not exist.");
            return;
        }

        senderWrapper.setActiveDistrict(nuActive);
        senderWrapper.sendMessage("Active district set to " + nuActive.getName());
    }

    /**
     *
     * @param plotName
     */
    public void setActivePlot(String plotName, boolean quickSelect) {
        Town t = senderWrapper.getActiveTown();

        if (t == null) {
            senderWrapper.notifyActiveTownNotSet();
            return;
        }

        Plot nuActive = null;

        if (!quickSelect) {
            Territory te = senderWrapper.getActiveTerritory();

            if (te == null) {
                senderWrapper.notifyActiveTerritoryNotSet();
                return;
            }

            District d = senderWrapper.getActiveDistrict();

            if (d == null) {
                senderWrapper.notifyActiveDistrictNotSet();
            }



            nuActive = d.getPlot(plotName);

            if (nuActive == null) {
                nuActive = d.getPlot((t.getTownName() + PLOT_INFIX + plotName).toLowerCase());
            }

            if (nuActive == null) {
                senderWrapper.sendMessage(ERR + "The plot \"" + plotName + "\" does not exist.");
                return;
            }
        } else {
            plotName = t.getTownName() + PLOT_INFIX + plotName;
            plotName = plotName.toLowerCase();

            territloop:
            for (Territory territ : t.getTerritoriesCollection()) {
                for (District dist : territ.getDistrictsCollection()) {
                    if (dist.getPlot(plotName) != null) {
                        nuActive = dist.getPlot(plotName);
                        senderWrapper.setActiveDistrict(dist);
                        senderWrapper.setActiveTerritory(territ);
                        break territloop;
                    }
                }
            }

            if (nuActive == null) {
                senderWrapper.sendMessage(ERR + "The plot \"" + plotName + "\" does not exist.");
                return;
            }
        }

        senderWrapper.setActivePlot(nuActive);
        senderWrapper.sendMessage("Active plot set to " + nuActive.getName());
    }



    /**
     *
     */
    public void checkPendingInvites() {
        LinkedList<TownJoinInfoPair> invs = joinManager.getInvitesForPlayer(senderWrapper.getPlayer());

        senderWrapper.sendMessage(ChatColor.BLUE + "You have pending invitations from:");
        String temp = "";
        for (TownJoinInfoPair tjip : invs) {
            temp += tjip.getTown();
            temp += " ";
        }
        temp = ChatColor.AQUA + temp;

        senderWrapper.sendMessage(temp);
        senderWrapper.sendMessage(ChatColor.BLUE + "Type /mct join <townname> to join one, or /mct refuse <town name> to refuse.");
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
        } catch (IOException ex) {
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
