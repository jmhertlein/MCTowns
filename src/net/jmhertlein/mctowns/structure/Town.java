package net.jmhertlein.mctowns.structure;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.jmhertlein.core.location.Location;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.banking.BlockBank;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.remote.view.TownView;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public class Town {
    private static final long serialVersionUID = "TOWN".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;
    //the town name
    private volatile String townName;
    //the town MOTD
    private volatile String townMOTD;
    private volatile ChatColor motdColor;
    //town spawn point
    private volatile Location townSpawn;
    //town bank
    private volatile BlockBank bank;
    //the territories associated with it
    private Set<String> territories;
    //the players in it
    private Set<String> residents;
    //its mayor (string)
    private String mayor;
    //the assistants (strings)
    private Set<String> assistants;
    //whether or not plots are buyable and thus have a price
    private boolean buyablePlots;
    //turns off the join request/invitation system. Instead, buying a plot in
    //the town makes you a member. Also, if this is false, you need to be a
    //member of the town in order to buy plots.
    private boolean economyJoins;
    private volatile BigDecimal defaultPlotPrice;
    private boolean friendlyFire;

    /**
     * Creates a new town, setting the name to townName, the mayor to the player
     * passed as mayor, and adds the mayor to the list of residents. The MOTD is
     * set to a default motd.
     *
     * @param townName the desired name of the town
     * @param mayor the player to be made the mayor of the town
     *
     */
    public Town(String townName, Player mayor) {
        this.townName = townName;
        this.mayor = mayor.getName();
        townSpawn = Location.convertFromBukkitLocation(mayor.getLocation());

        //use Collections method to get concurrency benefits from ConcurrentHashMap
        residents = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        assistants = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        territories = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

        bank = new BlockBank();
        townMOTD = "Use /town motd <msg> to set the town MOTD!";
        buyablePlots = false;
        economyJoins = false;
        defaultPlotPrice = BigDecimal.TEN;
        friendlyFire = false;
        motdColor = ChatColor.GOLD;
        
        residents.add(mayor.getName());
    }
    
    public Town(String townName, String mayorName, Location townSpawnLoc) {
        this.townName = townName;
        mayor = mayorName;
        townSpawn = townSpawnLoc;

        //use Collections method to get concurrency benefits from ConcurrentHashMap
        residents = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        assistants = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        territories = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

        bank = new BlockBank();
        townMOTD = "Use /town motd <msg> to set the town MOTD!";
        buyablePlots = false;
        economyJoins = false;
        defaultPlotPrice = BigDecimal.TEN;
        friendlyFire = false;
        motdColor = ChatColor.GOLD;
        
        residents.add(mayor);
    }

    private Town() {}

    /**
     *
     * @return
     */
    public BlockBank getBank() {
        return bank;
    }

    /**
     * Sets the motd to the specified MOTD
     *
     * @param townMOTD - the new MOTD
     */
    public void setTownMOTD(String townMOTD) {
        this.townMOTD = townMOTD;
    }

    /**
     * Gets the name of the mayor of the town.
     *
     * @return the name of the town's mayor
     */
    public String getMayor() {
        return mayor;
    }

    /**
     * Sets the town's mayor to the given name
     *
     * @param mayor the new mayor's name
     */
    public void setMayor(String mayor) {
        this.mayor = mayor;
    }

    /**
     * Returns the town MOTD, with color formatting
     *
     * @return the town MOTD
     */
    public String getTownMOTD() {
        return motdColor + townMOTD;
    }

    public ChatColor getMotdColor() {
        return motdColor;
    }
    
    

    /**
     * Returns the town's name
     *
     * @return the town's name
     */
    public String getTownName() {
        return townName;
    }

    /**
     * Adds a player as a resident of the town
     *
     * @param p the player to be added
     * @return false if player was not added because player is already added,
     * true otherwise
     */
    public boolean addPlayer(Player p) {
        return addPlayer(p.getName());
    }

    public boolean addPlayer(String playerName) {
        if (residents.contains(playerName)) {
            return false;
        }

        residents.add(playerName);
        return true;
    }

    /**
     * Removes a player from the town. Postcondition: Player is not a resident
     * of the town, regardless of whether or not they were before. Note: Player
     * must still be removed from the WG regions associated with the town
     *
     * @param p - the player to be removed
     */
    public void removePlayer(OfflinePlayer p) {
        removePlayer(p.getName());
    }

    /**
     * Removes the player from the town's list of residents and assistants. Does
     * not remove them from regions.
     *
     * @param playerName
     */
    public void removePlayer(String playerName) {
        residents.remove(playerName);
        assistants.remove(playerName);
    }

    /**
     * Adds the territory to the town. The region of the territory will need to
     * be handled separately.
     *
     * @param territ the territory to be added
     * @return false if territ was not added because it is already added, true
     * otherwise
     */
    public boolean addTerritory(Territory territ) {
        if (territories.contains(territ.getName())) {
            return false;
        }

        territories.add(territ.getName());
        return true;
    }

    /**
     * Removes the territory from the town.
     *
     * @param territName the name of the territory to remove
     * @return the removed territory
     */
    public boolean removeTerritory(String territName) {
        return territories.remove(territName);
    }

    /**
     * Adds a player as an assistant to the town
     *
     * @param player the player to be added
     * @return false if player was not added because player was already added,
     * true otherwise
     */
    public boolean addAssistant(Player player) {
        return addAssistant(player.getName());
    }

    /**
     * Promotes the resident to an assistant.
     *
     * @param playerName
     * @return true if player was added as assistant, false if they're already
     * an assistant or they're not a resident of the town.
     */
    public boolean addAssistant(String playerName) {
        if (assistants.contains(playerName) || !residents.contains(playerName)) {
            return false;
        }

        assistants.add(playerName);
        return true;
    }

    /**
     * Removes the assistant from his position as an assistant
     *
     * @param player the player to be demoted
     * @return false if the player was not removed because the player is not an
     * assistant, true otherwise
     */
    public boolean removeAssistant(Player player) {
        return removeAssistant(player);

    }
    
    public boolean removeAssistant(String player) {
        if (!assistants.contains(player)) {
            return false;
        }

        assistants.remove(player);
        return true;

    }

    /**
     * Returns the territories this town has.
     * 
     * Modifying membership of returned set does not modify which territs
     * are in this Town
     * 
     * Sets returned by this method will not update themselves if subsequent Town method
     * calls add Territories to it
     * 
     * Returned Set is a LinkedHashSet and as such performs well for
     * iteration and set membership checks
     *
     * @return the town's territories
     */
    public Set<String> getTerritoriesCollection() {
        return new LinkedHashSet<>(territories);
    }

    /**
     * Returns whether the player is the mayor or not
     *
     * @param p the player to be checked
     * @return whether the player is mayor or not
     */
    public boolean playerIsMayor(Player p) {
        return p.getName().equals(mayor);
    }

    /**
     * Returns whether the player is the mayor of the town.
     *
     * @param playerName
     * @return
     */
    public boolean playerIsMayor(String playerName) {
        return playerName.equals(mayor);
    }

    /**
     * Returns the list of all assistants in the town
     * 
     * Modifying membership of returned set does not modify which players
     * are assistants in this Town
     * 
     * Sets returned by this method will not update themselves if subsequent Town method
     * calls add assistants to it
     * 
     * Returned Set is a LinkedHashSet and as such performs well for
     * iteration and set membership checks
     *
     * @return
     */
    public String[] getResidentNames() {
        return residents.toArray(new String[residents.size()]);
    }
    
    public Set<String> getAssistantNames() {
        return new LinkedHashSet<>(assistants);
    }

    /**
     *
     * @return
     */
    public boolean allowsFriendlyFire() {
        return friendlyFire;
    }

    /**
     *
     * @param friendlyFire
     */
    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    /**
     * Returns whether or not the player is an assistant in the town
     *
     * @param p the player to be checked
     * @return if the player is an assistant or not
     */
    public boolean playerIsAssistant(Player p) {
        return assistants.contains(p.getName());
    }

    /**
     * Returns whether or not the player is a resident of the town
     *
     * @param p the player to be checked
     * @return if the player is a resident or not
     */
    public boolean playerIsResident(Player p) {
        return residents.contains(p.getName());
    }
    
     /**
     * Returns whether or not the player is a resident of the town
     *
     * @param p the name of the player to be checked
     * @return if the player is a resident or not
     */
    public boolean playerIsResident(String pName) {
        return residents.contains(pName);
    }

    /**
     *
     * @param s
     * @return
     */
    public org.bukkit.Location getTownSpawn(Server s) {
        return Location.convertToBukkitLocation(s, townSpawn);
    }

    /**
     *
     * @param playerExactName
     * @return
     */
    public boolean playerIsAssistant(String playerExactName) {
        return assistants.contains(playerExactName);
    }

    /**
     * Returns the current number of residents in the town.
     *
     * @return the number of residents in the town
     */
    public int getSize() {
        return residents.size();
    }

    /**
     *
     * @param server
     * @param message
     */
    public void broadcastMessageToTown(Server server, String message) {
        Player temp;
        message = ChatColor.GOLD + message;

        for (String playerName : residents) {
            temp = server.getPlayerExact(playerName);
            if (temp != null) {
                temp.sendMessage(message);
            }
        }
    }

    /**
     *
     * @param wgp
     * @param p
     * @return
     */
    public boolean playerIsInsideTownBorders(Player p) {
        org.bukkit.Location playerLoc = p.getLocation();
        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(p.getWorld());

        ProtectedRegion tempReg;
        for (MCTownsRegion mctReg : MCTownsPlugin.getTownManager().getRegionsCollection()) {
            if(mctReg instanceof Territory) {
                tempReg = regMan.getRegion( ((Territory)mctReg).getName());
                if (tempReg != null) {
                    if (tempReg.contains(new Vector(playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc.getBlockZ()))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     *
     * @param loc
     */
    public void setSpawn(org.bukkit.Location loc) {
        townSpawn = Location.convertFromBukkitLocation(loc);
    }

    /**
     *
     * @param s
     * @return
     */
    public org.bukkit.Location getSpawn(Server s) {
        return Location.convertToBukkitLocation(s, townSpawn);
    }

    /**
     *
     * @return
     */
    public boolean usesBuyablePlots() {
        return buyablePlots;
    }

    /**
     *
     * @param buyablePlots
     */
    public void setBuyablePlots(boolean buyablePlots) {
        this.buyablePlots = buyablePlots;
    }

    /**
     *
     * @return
     */
    public boolean usesEconomyJoins() {
        return economyJoins;
    }

    /**
     *
     * @param economyJoins
     */
    public void setEconomyJoins(boolean economyJoins) {
        this.economyJoins = economyJoins;
    }

    /**
     *
     * @return
     */
    public BigDecimal getDefaultPlotPrice() {
        return defaultPlotPrice;
    }

    /**
     *
     * @param defaultPlotPrice
     */
    public void setDefaultPlotPrice(BigDecimal defaultPlotPrice) {
        this.defaultPlotPrice = defaultPlotPrice;
    }

    @Override
    public String toString() {
        return this.townName;
    }

    public void writeYAML(FileConfiguration f) {
        f.set("townName", townName);
        f.set("motd", townMOTD);
        f.set("motdColor", motdColor.name());
        f.set("spawnLocation", townSpawn.toList());
        f.set("mayor", mayor);
        f.set("territs", getTerritoryNames());

        List<String> list = new LinkedList<>();
        list.addAll(assistants);
        f.set("assistants", list);
        
        List<String> resList = new LinkedList<>();
        resList.addAll(residents);
        f.set("residents", resList);

        f.set("friendlyFire", friendlyFire);
        f.set("defaultPlotPrice", defaultPlotPrice.toString());
        f.set("economyJoins", economyJoins);
        f.set("buyablePlots", buyablePlots);
        
        bank.writeYAML(f);
    }

    public static Town readYAML(FileConfiguration f) {
        Town t = new Town();

        t.townName = f.getString("townName");
        t.townMOTD = f.getString("motd");
        t.motdColor = ChatColor.valueOf(f.getString("motdColor"));
        t.townSpawn = Location.fromList(f.getStringList("spawnLocation"));
        t.mayor = f.getString("mayor");
        t.territories = parseListToHashSet(f.getStringList("territs"));

        t.assistants = new HashSet<>();
        t.assistants.addAll(f.getStringList("assistants"));
        
        t.residents = new HashSet<>();
        t.residents.addAll(f.getStringList("residents"));

        t.friendlyFire = f.getBoolean("friendlyFire");
        t.defaultPlotPrice = new BigDecimal(f.getString("defaultPlotPrice"));
        t.economyJoins = f.getBoolean("economyJoins");
        t.buyablePlots = f.getBoolean("buyablePlots");

        t.bank = BlockBank.readYAML(f);
        return t;
    }
    
    public static void recursivelyRemovePlayerFromTown(OfflinePlayer p, Town t) {
        TownManager tMan = MCTownsPlugin.getTownManager();
        
        for(String teName : t.getTerritoriesCollection()) {
            Territory te = tMan.getTerritory(teName);
            for(String plName : te.getPlotsCollection()) {
                Plot pl = tMan.getPlot(plName);
                pl.removePlayer(p);
            }
            te.removePlayer(p);
        }
        
        t.removePlayer(p);
    }

    private List<String> getTerritoryNames() {
        LinkedList<String> ret = new LinkedList<>();

        ret.addAll(this.territories);

        return ret;

    }

    private static HashSet<String> parseListToHashSet(List<String> s) {
        HashSet<String> ret = new HashSet<>();

        ret.addAll(s);

        return ret;
    }
    
    /**
     * Updates the Town so that it reflects any changes made to the TownView
     * NOTE: This method does NOT change town memberships, territories, assistants, or the town bank. Use their corresponding Town methods to change them.
     * @param view 
     */
    public void updateTown(TownView view) {
        this.buyablePlots = view.isBuyablePlots();
        this.defaultPlotPrice = view.getDefaultPlotPrice();
        this.economyJoins = view.isEconomyJoins();
        this.friendlyFire = view.isFriendlyFire();
        this.mayor = view.getMayorName();
        this.motdColor = view.getMotdColor();
        this.townMOTD = view.getMotd();
        this.townSpawn = view.getSpawnLoc();
    }
}
