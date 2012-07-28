/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.structure;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import me.everdras.core.location.Location;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.banking.BlockBank;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public class Town implements Externalizable {

    private static final long serialVersionUID = "TOWN".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;
    //the town name
    private String townName;
    private String worldName;
    //the town MOTD
    private String townMOTD;
    private ChatColor motdColor;
    //town spawn point
    private Location townSpawn;
    //town bank
    private BlockBank bank;
    //the territories associated with it
    private HashMap<String, Territory> territories;
    //the players in it
    private HashMap<String, Boolean> residents;
    private ArrayList<String> residentNames;
    //its mayor (string)
    private String mayor;
    //the assistants (strings)
    private HashMap<String, Boolean> assistants;
    //whether or not plots are buyable and thus have a price
    private boolean buyablePlots;
    //turns off the join request/invitation system. Instead, buying a plot in
    //the town makes you a member. Also, if this is false, you need to be a
    //member of the town in order to buy plots.
    private boolean economyJoins;
    private BigDecimal defaultPlotPrice;
    private boolean friendlyFire;

    /**
     *
     */
    public Town() {
    }

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
        townMOTD = "Use /town motd set <msg> to set the town MOTD!";
        townSpawn = Location.convertFromBukkitLocation(mayor.getLocation());
        worldName = mayor.getWorld().getName();

        bank = new BlockBank();

        residents = new HashMap<>();
        assistants = new HashMap<>();
        territories = new HashMap<>();
        residentNames = new ArrayList<>();

        buyablePlots = false;
        economyJoins = false;
        defaultPlotPrice = BigDecimal.TEN;
        friendlyFire = false;



        residents.put(mayor.getName(), Boolean.TRUE);
        residentNames.add(mayor.getName());

        motdColor = ChatColor.GOLD;


    }

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

    /**
     * Returns the town's name
     *
     * @return the town's name
     */
    public String getTownName() {
        return townName;
    }

    /**
     * Returns the name of the world in which this town resides
     *
     * @return the name of the world
     */
    public String getWorldName() {
        return worldName;
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
        if (residents.containsKey(playerName)) {
            return false;
        }

        residents.put(playerName, true);
        residentNames.add(playerName);
        return true;
    }

    /**
     * Removes a player from the town. Postcondition: Player is not a resident
     * of the town, regardless of whether or not they were before. Note: Player
     * must still be removed from the WG regions associated with the town
     *
     * @param p - the player to be removed
     */
    public void removePlayer(Player p) {
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
        residentNames.remove(playerName);
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
        if (territories.containsKey(territ.getName())) {
            return false;
        }

        territories.put(territ.getName(), territ);
        return true;
    }

    /**
     * Removes the territory from the town. Removal of the territ's region will
     * need to be handled elsewhere.
     *
     * @param territName the name of the territory to remove
     * @return the removed territory
     * @see
     * me.everdras.mctowns.database.TownManager.unregisterTownFromWorldGuard(WorldGuardPlugin
     * wgp, Town t)
     */
    public Territory removeTerritory(String territName) {
        return territories.remove(territName);
    }

    /**
     * Returns the territory whose name is the same as name
     *
     * @param name the name of the territory to be gotten
     * @return the territory whose name is name
     */
    public Territory getTerritory(String name) {
        return territories.get(name);
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
        if (assistants.get(playerName) != null && assistants.get(playerName) && residents.get(playerName) != null && residents.get(playerName)) {
            return false;
        }

        assistants.put(playerName, Boolean.TRUE);
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
        if (!assistants.containsKey(player.getName())) {
            return false;
        }

        assistants.remove(player.getName());
        return true;

    }

    /**
     * Returns a hashmap of the territories
     *
     * @return the hashmap of territories
     * @deprecated use getTerritoriesCollection()
     */
    public HashMap<String, Territory> getTerritories() {
        return territories;
    }

    /**
     * Returns the territories this town has.
     *
     * @return the town's territories
     */
    public Collection<Territory> getTerritoriesCollection() {
        return territories.values();
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
     * Returns the list of all
     *
     * @return
     */
    public String[] getResidentNames() {
        return residentNames.toArray(new String[residentNames.size()]);
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
        return assistants.containsKey(p.getName());
    }

    /**
     * Returns whether or not the player is a resident of the town
     *
     * @param p the player to be checked
     * @return if the player is a resident or not
     */
    public boolean playerIsResident(Player p) {
        return residents.containsKey(p.getName());
    }

    /**
     *
     * @param p
     * @return
     */
    public boolean playerIsResident(String p) {
        return residents.containsKey(p);
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Town other = (Town) obj;
        if ((this.townName == null) ? (other.townName != null) : !this.townName.equals(other.townName)) {
            return false;
        }
        if ((this.worldName == null) ? (other.worldName != null) : !this.worldName.equals(other.worldName)) {
            return false;
        }
        return true;
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
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.townName != null ? this.townName.hashCode() : 0);
        hash = 67 * hash + (this.worldName != null ? this.worldName.hashCode() : 0);
        return hash;
    }

    /**
     *
     * @param playerExactName
     * @return
     */
    public boolean playerIsAssistant(String playerExactName) {
        Boolean b = assistants.get(playerExactName);
        return (b == null ? false : b);
    }

    /**
     * Returns the current number of residents in the town.
     *
     * @return the number of residents in the town
     */
    public int getSize() {
        return residents.values().size();
    }

    /**
     *
     * @param server
     * @param message
     */
    public void broadcastMessageToTown(Server server, String message) {
        Player temp;
        message = ChatColor.GOLD + message;

        for (String playerName : residentNames) {
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
    public boolean playerIsInsideTownBorders(WorldGuardPlugin wgp, Player p) {
        org.bukkit.Location playerLoc = p.getLocation();
        RegionManager regMan = wgp.getRegionManager(p.getWorld());

        ProtectedRegion tempReg;
        for (Territory territ : getTerritoriesCollection()) {
            tempReg = regMan.getRegion(territ.getName());
            if (tempReg != null) {
                if (tempReg.contains(new Vector(playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc.getBlockZ()))) {
                    return true;
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
     * @param out
     * @throws IOException
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);

        out.writeUTF(townName);
        out.writeUTF(worldName);
        out.writeUTF(townMOTD);
        out.writeObject(motdColor);
        out.writeObject(townSpawn);
        out.writeObject(bank);
        out.writeObject(territories);
        out.writeObject(residents);
        out.writeObject(residentNames);
        out.writeUTF(mayor);
        out.writeObject(assistants);
        out.writeBoolean(buyablePlots);
        out.writeBoolean(economyJoins);
        out.writeObject(defaultPlotPrice);
        out.writeBoolean(friendlyFire);


    }

    /**
     *
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        if (ver == 0) {
            //============Beginning of original variables for version 0=========
            townName = in.readUTF();
            worldName = in.readUTF();
            townMOTD = in.readUTF();
            motdColor = (ChatColor) in.readObject();
            townSpawn = (Location) in.readObject();
            bank = (BlockBank) in.readObject();
            territories = (HashMap<String, Territory>) in.readObject();
            residents = (HashMap<String, Boolean>) in.readObject();
            residentNames = (ArrayList<String>) in.readObject();
            mayor = in.readUTF();
            assistants = (HashMap<String, Boolean>) in.readObject();
            buyablePlots = in.readBoolean();
            economyJoins = in.readBoolean();
            defaultPlotPrice = (BigDecimal) in.readObject();
            friendlyFire = in.readBoolean();
            //============End of original variables for version 0===============
        } else {
            MCTowns.log.log(Level.SEVERE, "MCTowns: Unsupported version (version " + ver + ") of Town.");
        }

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
}
