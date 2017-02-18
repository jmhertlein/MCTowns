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
package cafe.josh.mctowns;

import cafe.josh.mctowns.region.MCTownsRegion;
import cafe.josh.mctowns.region.Plot;
import cafe.josh.mctowns.region.Town;
import cafe.josh.mctowns.region.TownLevel;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import cafe.josh.mctowns.command.ActiveSet;
import cafe.josh.mctowns.region.Territory;
import cafe.josh.mctowns.util.UUIDs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public class TownManager {
    private final HashMap<String, Town> towns;
    private final HashMap<String, MCTownsRegion> regions;

    /**
     * Constructs a new, empty town manager.
     */
    public TownManager() {
        towns = new HashMap<>();
        regions = new HashMap<>();
    }

    /**
     * Returns the towns that this manager manages
     *
     * @return the towns
     */
    public Collection<Town> getTownsCollection() {
        return towns.values();
    }

    /**
     *
     * @return
     */
    public Collection<MCTownsRegion> getRegionsCollection() {
        return regions.values();
    }

    /**
     * Attempts to add a town to the town manager (effectively creating a new
     * town as far as the TownManager is concerned).
     *
     * @param townName the desired name of the new town
     * @param mayor the live player to be made the mayor of the new town
     *
     * @return true if town was added, false if town was not because it was
     * already existing
     */
    public Town addTown(String townName, Player mayor) {
        Town t = new Town(townName, mayor);

        if(towns.containsKey(t.getName())) {
            return null;
        }

        towns.put(t.getName(), t);
        return t;

    }

    public Town addTown(String townName, Player mayorName, Location spawn) {
        Town t = new Town(townName, mayorName, spawn);

        if(towns.containsKey(t.getName())) {
            return null;
        }

        towns.put(t.getName(), t);
        return t;
    }

    /**
     * Creates a new territory, adds it to the manager, and registers its region
     * in WorldGuard.
     *
     * @param fullTerritoryName the desired, formatted name of the region
     * @param worldTerritoryIsIn
     * @param reg the desired region for the territory to occupy, names MUST
     * match
     * @param parentTown the parent town of the Territory
     *
     * @throws InvalidWorldGuardRegionNameException if the name of the
     * ProtectedRegion contains invalid characters
     * @throws RegionAlreadyExistsException if the region already exists
     */
    public void addTerritory(String fullTerritoryName, World worldTerritoryIsIn, ProtectedRegion reg, Town parentTown) throws InvalidWorldGuardRegionNameException, RegionAlreadyExistsException {
        Territory t = new Territory(fullTerritoryName,
                worldTerritoryIsIn.getName(),
                parentTown.getName());

        addMCTRegion(t, worldTerritoryIsIn, reg);
        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(
                worldTerritoryIsIn);
        try {
            regMan.save();
        } catch(StorageException ex) {
            MCTowns.logSevere("Error saving regions:" + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        parentTown.addTerritory(t);
    }

    /**
     * Creates a new territory, adds it to the manager, and registers its region
     * in WorldGuard.
     *
     * @param fullPlotName the desired, formatted name of the region
     * @param worldPlotIsIn
     * @param reg the desired region for the plot to occupy
     * @param parentTown the parent town of this region
     * @param parentTerritory the parent territory of this region
     *
     * @throws InvalidWorldGuardRegionNameException if the name of the
     * ProtectedRegion does not match the desired name
     * @throws RegionAlreadyExistsException if a region with that name already
     * exists
     */
    public void addPlot(String fullPlotName, World worldPlotIsIn, ProtectedRegion reg, Town parentTown, Territory parentTerritory) throws InvalidWorldGuardRegionNameException, RegionAlreadyExistsException {
        Plot p = new Plot(fullPlotName, worldPlotIsIn.getName(),
                parentTerritory.getName(), parentTown.getName());

        addMCTRegion(p, worldPlotIsIn, reg);

        parentTerritory.addPlot(p);
        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(
                worldPlotIsIn);
        try {
            reg.setParent(regMan.getRegion(parentTerritory.getName()));
        } catch(CircularInheritanceException ex) {
            Logger.getLogger(TownManager.class.getName()).log(Level.SEVERE, null,
                    ex);
        }

        try {
            regMan.save();
        } catch(StorageException ex) {
            MCTowns.logSevere("Error saving regions:" + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        p.calculateSignLoc(); //note: don't move calculateSignLoc from here.
        //it needs the region to exist in the region manager
    }

    private void addMCTRegion(MCTownsRegion mctReg, World w, ProtectedRegion reg) throws InvalidWorldGuardRegionNameException, RegionAlreadyExistsException {
        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(w);
        if(!ProtectedRegion.isValidId(mctReg.getName())) {
            throw new InvalidWorldGuardRegionNameException(mctReg.getName());
        }

        //checking regMan should always return the same value as checking regions,
        //since the regions in regions are a subset of those in regMan... so no need to check regions
        if(regMan.hasRegion(mctReg.getName())) {
            throw new RegionAlreadyExistsException(mctReg.getName());
        }

        regMan.addRegion(reg);
        regions.put(mctReg.getName(), mctReg);
    }

    /**
     * Gets a town by its name
     *
     * @param townName the name of the town
     *
     * @return the town, or null is no town by that name exists
     */
    public Town getTown(String townName) {
        return towns.get(townName);
    }

    /**
     * Returns the Territory IFF the territory exists and is a Territory
     *
     * @param territName name of the Territory to get
     *
     * @return the Territory if it exists, or null otherwise
     */
    public Territory getTerritory(String territName) {
        MCTownsRegion ret = regions.get(territName);

        return (ret instanceof Territory ? (Territory) ret : null);
    }

    /**
     * Returns the Plot IFF the territory exists and is a Territory
     *
     * @param plotName name of the Plot to get
     *
     * @return the Plot if it exists, or null otherwise
     */
    public Plot getPlot(String plotName) {
        MCTownsRegion ret = regions.get(plotName);

        return (ret instanceof Plot ? (Plot) ret : null);
    }

    /**
     * Removes the Town and all its child territories if it has any
     *
     * @param townName
     *
     * @return true if it succeeds, false if town doesn't exist
     */
    public boolean removeTown(String townName) {
        Town t = towns.get(townName);

        if(t == null) {
            return false;
        }

        for(String s : t.getTerritoriesCollection()) {
            removeTerritory(s);
        }

        towns.remove(t.getName());

        return true;
    }

    /**
     * Removes the territory, its worldguard region, and all of its child plots
     *
     * @param territoryName
     *
     * @return true if successful, false if the Territiry doesn't exist or isn't
     * a territory
     */
    public boolean removeTerritory(String territoryName) {
        MCTownsRegion mctReg = regions.get(territoryName);

        if(mctReg == null || !(mctReg instanceof Territory)) {
            return false;
        }

        Territory territ = (Territory) mctReg;

        for(String plot : territ.getPlotsCollection()) {
            removePlot(plot);
        }

        getTown(territ.getParentTown()).removeTerritory(territoryName);

        regions.remove(territ.getName());

        World w = Bukkit.getWorld(territ.getWorldName());

        //Just in case they deleted the world...
        if(w == null) {
            return true;
        }

        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(w);

        regMan.removeRegion(territ.getName());

        try {
            regMan.save();
        } catch(StorageException ex) {
            MCTowns.logSevere("Error saving regions:" + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * Removes the plot and its worldguard region
     *
     * @param plotName
     *
     * @return true if removal was successful, false if the plot doesn't exist
     * or isn't a plot
     */
    public boolean removePlot(String plotName) {
        MCTownsRegion plot = regions.get(plotName);

        if(plot == null || !(plot instanceof Plot)) {
            return false;
        }

        regions.remove(plotName);

        getTerritory(((Plot) plot).getParentTerritoryName()).removePlot(plotName);

        World w = Bukkit.getWorld(plot.getWorldName());

        if(w == null) {
            return true;
        }

        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(w);

        regMan.removeRegion(plotName);

        try {
            regMan.save();
        } catch(StorageException ex) {
            MCTowns.logSevere("Error saving regions:" + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * Matches a live player to his town
     *
     * @param p the player to match to a town
     *
     * @return a list of all towns the player is in, empty list if player is in
     * no towns
     */
    public List<Town> matchPlayerToTowns(OfflinePlayer p) {
        return matchPlayerToTowns(UUIDs.getUUIDForOfflinePlayer(p));

    }

    /**
     * Matches a possibly non-live player to a town
     *
     * @param id the name of the player to match for
     *
     * @return a list of all towns the player is in
     */
    public List<Town> matchPlayerToTowns(UUID id) {
        ArrayList<Town> ret = new ArrayList<>();
        for(Town town : towns.values()) {
            if(town.playerIsResident(id)) {
                ret.add(town);
            }
        }
        return ret;
    }

    /**
     *
     * @param bukkitLoc
     *
     * @return an ActiveSet pointing to the plot to be bought, or null if the
     * sign isn't associated with a town.
     */
    public ActiveSet getPlotFromSignLocation(org.bukkit.Location bukkitLoc) {
        Plot p;
        Territory territ;
        Town town;
        for(MCTownsRegion reg : regions.values()) {
            if(reg instanceof Plot) {
                p = (Plot) reg;
                if(p.getSignLoc() != null && p.getSignLoc().equals(net.jmhertlein.core.location.Location.convertFromBukkitLocation(bukkitLoc))) {
                    territ = getTerritory(p.getParentTerritoryName());
                    town = getTown(territ.getParentTown());
                    return new ActiveSet(town, territ, p);
                }
            }
        }

        MCTowns.logSevere("Couldn't find a match for this plot!");
        return null;
        //throw new RuntimeException("Couldn't find a match for this plot.");
    }

    /**
     *
     * @param invitee
     *
     * @return true if the player is already in a town, else false
     */
    public boolean playerIsAlreadyInATown(OfflinePlayer invitee) {
        return !matchPlayerToTowns(invitee).isEmpty();
    }

    /**
     *
     * @param rootDirPath
     *
     * @throws IOException
     */
    public void writeYAML(String rootDirPath) throws IOException {
        FileConfiguration f;

        f = new YamlConfiguration();
        List<String> l = new LinkedList<>();

        l.addAll(towns.keySet());
        f.set("towns", l);

        l = new LinkedList<>();

        l.addAll(regions.keySet());
        f.set("regions", l);

        f.save(new File(rootDirPath + File.separator + ".meta.yml"));

        for(Town t : towns.values()) {
            f = new YamlConfiguration();
            t.writeYAML(f);
            f.save(new File(
                    rootDirPath + File.separator + t.getName() + ".yml"));
        }

        for(MCTownsRegion reg : regions.values()) {
            f = new YamlConfiguration();
            reg.writeYAML(f);
            f.save(new File(
                    rootDirPath + File.separator + reg.getName() + ".yml"));
        }
    }

    /**
     *
     * @param rootDirPath
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws InvalidConfigurationException
     */
    public static TownManager readYAML(String rootDirPath) throws FileNotFoundException, IOException, InvalidConfigurationException {
        return readYAML(new File(rootDirPath));
    }

    /**
     *
     * @param rootDir
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws InvalidConfigurationException
     */
    public static TownManager readYAML(File rootDir) throws FileNotFoundException, IOException, InvalidConfigurationException {
        FileConfiguration metaF, f;

        TownManager ret = new TownManager();

        metaF = new YamlConfiguration();
        metaF.load(new File(rootDir, ".meta.yml"));

        for(String s : metaF.getStringList("towns")) {
            f = new YamlConfiguration();
            f.load(new File(rootDir, s + ".yml"));
            ret.towns.put(s, Town.readYAML(f));
        }

        for(String s : metaF.getStringList("regions")) {
            f = new YamlConfiguration();
            f.load(new File(rootDir, s + ".yml"));

            if(TownLevel.parseTownLevel(f.getString("type")) == TownLevel.PLOT) {
                ret.regions.put(s, Plot.readYAML(f));
            } else {
                ret.regions.put(s, Territory.readYAML(f));
            }
        }

        return ret;
    }

    /**
     *
     */
    public static class InvalidWorldGuardRegionNameException extends Exception {

        /**
         *
         * @param invalidName
         */
        public InvalidWorldGuardRegionNameException(String invalidName) {
            super(String.format("The region name \"%s\" has invalid characters.",
                    invalidName));
        }
    }

    public static class RegionAlreadyExistsException extends Exception {
        public RegionAlreadyExistsException(String regName) {
            super(String.format("A region named \"%s\" already exists.", regName));
        }
    }
}
