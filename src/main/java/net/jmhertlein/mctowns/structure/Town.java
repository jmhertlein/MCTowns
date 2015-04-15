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
package net.jmhertlein.mctowns.structure;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.jmhertlein.core.location.Location;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.banking.BlockBank;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.util.UUIDs;
import org.bukkit.Bukkit;
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
    private volatile String townName;
    private volatile String townMOTD;
    private volatile ChatColor motdColor;
    private volatile Location townSpawn;
    private volatile BlockBank bank;
    //the territories associated with the town
    private Set<String> territories;
    private Set<UUID> residents;
    private UUID mayor;
    private Set<UUID> assistants;
    //whether or not plots are buyable and thus have a price
    private boolean buyablePlots;
    //turns off the join request/invitation system. Instead, buying a plot in
    //the town makes you a member. Also, if this is false, you need to be a
    //member of the town in order to buy plots.
    private boolean economyJoins;
    private volatile BigDecimal defaultPlotPrice;
    private boolean friendlyFire;

    /**
     * Creates a new town, setting the name to townName, the mayor to the player passed as mayor,
     * and adds the mayor to the list of residents. The MOTD is set to a default motd.
     *
     * @param townName the desired name of the town
     * @param mayor the player to be made the mayor of the town
     *
     */
    public Town(String townName, Player mayor) {
        this.townName = townName;
        this.mayor = mayor.getUniqueId();
        townSpawn = Location.convertFromBukkitLocation(mayor.getLocation());

        //use Collections method to get concurrency benefits from ConcurrentHashMap
        residents = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
        assistants = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
        territories = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

        bank = new BlockBank(MCTownsPlugin.getPlugin().getOpenDepositInventories());
        townMOTD = "Use /town motd <msg> to set the town MOTD!";
        buyablePlots = false;
        economyJoins = false;
        defaultPlotPrice = BigDecimal.TEN;
        friendlyFire = false;
        motdColor = ChatColor.GOLD;

        residents.add(mayor.getUniqueId());
    }

    public Town(String townName, Player mayorName, Location townSpawnLoc) {
        this.townName = townName;
        mayor = mayorName.getUniqueId();
        townSpawn = townSpawnLoc;

        //use Collections method to get concurrency benefits from ConcurrentHashMap
        residents = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
        assistants = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
        territories = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

        bank = new BlockBank(MCTownsPlugin.getPlugin().getOpenDepositInventories());
        townMOTD = "Use /town motd <msg> to set the town MOTD!";
        buyablePlots = false;
        economyJoins = false;
        defaultPlotPrice = BigDecimal.TEN;
        friendlyFire = false;
        motdColor = ChatColor.GOLD;

        residents.add(mayor);
    }

    private Town() {
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
     * Gets the UUID of the mayor of the town.
     *
     * @return the UUID of the town's mayor
     */
    public UUID getMayor() {
        return mayor;
    }

    /**
     * Sets the town's mayor to the given name
     *
     * @param mayor the new mayor's name
     */
    public void setMayor(UUID mayor) {
        this.mayor = mayor;
    }

    /**
     * Sets the town's mayor to the given name
     *
     * @param mayor the new mayor
     */
    public void setMayor(OfflinePlayer mayor) {
        this.mayor = UUIDs.getUUIDForOfflinePlayer(mayor);
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
     *
     * @return false if player was not added because player is already added, true otherwise
     */
    public boolean addPlayer(Player p) {
        return addPlayer(p.getUniqueId());
    }

    public boolean addPlayer(UUID u) {
        if(residents.contains(u))
            return false;
        residents.add(u);
        return true;
    }

    public boolean addPlayer(OfflinePlayer playerId) {
        return addPlayer(UUIDs.getUUIDForOfflinePlayer(playerId));
    }

    /**
     * Removes a player from the town. Postcondition: Player is not a resident of the town,
     * regardless of whether or not they were before. Note: Player must still be removed from the WG
     * regions associated with the town
     *
     * @param p - the player to be removed
     */
    public void removePlayer(OfflinePlayer p) {
        removePlayer(UUIDs.getUUIDForOfflinePlayer(p));
    }

    /**
     * Removes the player from the town's list of residents and assistants. Does not remove them
     * from regions.
     *
     * @param playerId
     */
    public void removePlayer(UUID playerId) {
        residents.remove(playerId);
        assistants.remove(playerId);
    }

    /**
     * Adds the territory to the town. The region of the territory will need to be handled
     * separately.
     *
     * @param territ the territory to be added
     *
     * @return false if territ was not added because it is already added, true otherwise
     */
    public boolean addTerritory(Territory territ) {
        if(territories.contains(territ.getName()))
            return false;

        territories.add(territ.getName());
        return true;
    }

    /**
     * Removes the territory from the town.
     *
     * @param territName the name of the territory to remove
     *
     * @return the removed territory
     */
    public boolean removeTerritory(String territName) {
        return territories.remove(territName);
    }

    /**
     * Adds a player as an assistant to the town
     *
     * @param player the player to be added
     *
     * @return false if player was not added because player was already added, true otherwise
     */
    public boolean addAssistant(OfflinePlayer player) {
        return addAssistant(UUIDs.getUUIDForOfflinePlayer(player));
    }

    /**
     * Promotes the resident to an assistant.
     *
     * @param playerId
     *
     * @return true if player was added as assistant, false if they're already an assistant or
     * they're not a resident of the town.
     */
    public boolean addAssistant(UUID playerId) {
        if(assistants.contains(playerId) || !residents.contains(playerId))
            return false;

        assistants.add(playerId);
        return true;
    }

    /**
     * Removes the assistant from his position as an assistant
     *
     * @param player the player to be demoted
     *
     * @return false if the player was not removed because the player is not an assistant, true
     * otherwise
     */
    public boolean removeAssistant(OfflinePlayer player) {
        return removeAssistant(UUIDs.getUUIDForOfflinePlayer(player));

    }

    public boolean removeAssistant(UUID playerId) {
        if(!assistants.contains(playerId))
            return false;

        assistants.remove(playerId);
        return true;

    }

    /**
     * Returns the territories this town has.
     *
     * Modifying membership of returned set does not modify which territs are in this Town
     *
     * Sets returned by this method will not update themselves if subsequent Town method calls add
     * Territories to it
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
     *
     * @return whether the player is mayor or not
     */
    public boolean playerIsMayor(OfflinePlayer p) {
        return p.getUniqueId().equals(mayor);
    }

    /**
     * Returns the list of all assistants in the town
     *
     * Modifying membership of returned set does not modify which players are assistants in this
     * Town
     *
     * Sets returned by this method will not update themselves if subsequent Town method calls add
     * assistants to it
     *
     * @return
     */
    public Set<String> getResidentNames() {
        Set<String> ret = new HashSet<>();
        for(UUID u : residents)
            ret.add(UUIDs.getNameForUUID(u));
        return ret;
    }

    public Set<OfflinePlayer> getResidents() {
        return residents.stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid))
                .collect(Collectors.toSet());
    }

    public Set<String> getAssistantNames() {
        Set<String> ret = new HashSet<>();
        for(UUID u : assistants)
            ret.add(UUIDs.getNameForUUID(u));
        return ret;
    }

    /**
     *
     * @return
     */
    public boolean allowsFriendlyFire() {
        return friendlyFire;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.townName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Town other = (Town) obj;
        if(!Objects.equals(this.townName, other.townName))
            return false;
        return true;
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
     *
     * @return if the player is an assistant or not
     */
    public boolean playerIsAssistant(OfflinePlayer p) {
        return assistants.contains(UUIDs.getUUIDForOfflinePlayer(p));
    }

    /**
     * Returns whether or not the player is a resident of the town
     *
     * @param p the player to be checked
     *
     * @return if the player is a resident or not
     */
    public boolean playerIsResident(OfflinePlayer p) {
        return residents.contains(UUIDs.getUUIDForOfflinePlayer(p));
    }

    /**
     * Returns whether or not the player is a resident of the town
     *
     * @param id the id of the player to be checked
     *
     * @return if the player is a resident or not
     */
    public boolean playerIsResident(UUID id) {
        return residents.contains(id);
    }

    /**
     *
     * @param s
     *
     * @return
     */
    public org.bukkit.Location getTownSpawn() {
        return Location.convertToBukkitLocation(Bukkit.getServer(), townSpawn);
    }

    /**
     *
     * @param id
     *
     * @return
     */
    public boolean playerIsAssistant(UUID id) {
        return assistants.contains(id);
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
     * @param message
     */
    public void broadcastMessageToTown(String message) {
        message = ChatColor.GOLD + message;

        for(Player p : Bukkit.getOnlinePlayers())
            if(residents.contains(p.getUniqueId()))
                p.sendMessage(message);
    }

    /**
     *
     * @param p
     *
     * @return
     */
    public boolean playerIsInsideTownBorders(Player p) {
        org.bukkit.Location playerLoc = p.getLocation();
        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(p.getWorld());

        ProtectedRegion tempReg;
        for(MCTownsRegion mctReg : MCTowns.getTownManager().getRegionsCollection()) {
            if(mctReg instanceof Territory) {
                tempReg = regMan.getRegion((mctReg).getName());
                if(tempReg != null)
                    if(tempReg.contains(new Vector(playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc.getBlockZ())))
                        return true;
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
     *
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
        f.set("mayor", mayor.toString());
        f.set("territs", getTerritoryNames());

        List<String> list = new LinkedList<>();
        list.addAll(UUIDs.idsToStrings(assistants));
        f.set("assistants", list);

        List<String> resList = new LinkedList<>();
        resList.addAll(UUIDs.idsToStrings(residents));
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
        t.mayor = UUIDs.stringToId(f.getString("mayor"));
        t.territories = parseListToHashSet(f.getStringList("territs"));

        t.assistants = UUIDs.stringsToIds(f.getStringList("assistants"));

        t.residents = UUIDs.stringsToIds(f.getStringList("residents"));

        t.friendlyFire = f.getBoolean("friendlyFire");
        t.defaultPlotPrice = new BigDecimal(f.getString("defaultPlotPrice"));
        t.economyJoins = f.getBoolean("economyJoins");
        t.buyablePlots = f.getBoolean("buyablePlots");

        t.bank = BlockBank.readYAML(f, MCTownsPlugin.getPlugin().getOpenDepositInventories());
        return t;
    }

    public static void recursivelyRemovePlayerFromTown(OfflinePlayer p, Town t) {
        TownManager tMan = MCTowns.getTownManager();

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
}
