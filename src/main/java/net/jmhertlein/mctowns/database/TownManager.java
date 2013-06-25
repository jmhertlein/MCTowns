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
package net.jmhertlein.mctowns.database;

import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.core.location.Location;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.structure.MCTownsRegion;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.TownLevel;
import org.bukkit.Bukkit;
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

    private static final long serialVersionUID = "TOWNMANAGER".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;
    private HashMap<String, Town> towns;
    private HashMap<String, MCTownsRegion> regions;

    /**
     * Constructs a new, empty town manager.
     */
    public TownManager() {
        towns = new HashMap<>();
        regions = new HashMap<>();
    }

    /**
     * Checks to see if a live player is in a town
     *
     * @param p the live player to be checked
     * @return true if the player is already in any town, false otherwise
     */
    public boolean playerIsAlreadyInATown(Player p) {
        return playerIsAlreadyInATown(p.getName());
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
     * @return true if town was added, false if town was not because it was
     * already existing
     */
    public Town addTown(String townName, Player mayor) {
        Town t = new Town(townName, mayor);

        if (towns.containsKey(t.getTownName())) {
            return null;
        }

        towns.put(t.getTownName(), t);
        return t;

    }

    public Town addTown(String townName, String mayorName, Location spawn) {
        Town t = new Town(townName, mayorName, spawn);

        if (towns.containsKey(t.getTownName())) {
            return null;
        }

        towns.put(t.getTownName(), t);
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
     * @throws InvalidWorldGuardRegionNameException if the name of the
     * ProtectedRegion does not match the desired name
     * @return true if the addition was successful, false if the name is already
     * used
     */
    public boolean addTerritory(String fullTerritoryName, World worldTerritoryIsIn, ProtectedRegion reg, Town parentTown) {
        Territory t = new Territory(fullTerritoryName, worldTerritoryIsIn.getName(), parentTown.getTownName());

        if (!addMCTRegion(fullTerritoryName, t, worldTerritoryIsIn, reg)) {
            return false;
        }

        parentTown.addTerritory(t);
        return true;
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
     * @throws InvalidWorldGuardRegionNameException if the name of the
     * ProtectedRegion does not match the desired name
     * @return true if the addition was successful, false if the name is already
     * used
     */
    public boolean addPlot(String fullPlotName, World worldPlotIsIn, ProtectedRegion reg, Town parentTown, Territory parentTerritory) {
        Plot p = new Plot(fullPlotName, worldPlotIsIn.getName(), parentTerritory.getName(), parentTown.getTownName());

        if (!addMCTRegion(fullPlotName, p, worldPlotIsIn, reg)) {
            return false;
        }

        parentTerritory.addPlot(p);
        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(worldPlotIsIn);
        try {
            reg.setParent(regMan.getRegion(parentTerritory.getName()));
        } catch (CircularInheritanceException ex) {
            Logger.getLogger(TownManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        p.calculateSignLoc(); //note: don't move calculateSignLoc from here.
        //it needs the region to exist in the region manager
        return false;
    }

    private boolean addMCTRegion(String fullPlotName, MCTownsRegion mctReg, World w, ProtectedRegion reg) {
        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(w);
        if (!reg.getId().equals(mctReg.getName())) {
            throw new InvalidWorldGuardRegionNameException(fullPlotName, reg.getId());
        }

        if (regMan.hasRegion(mctReg.getName())) //checking regMan should always return the same value as checking regions, since the regions in regions are a subset of those in regMan... so no need to check regions
        {
            return false;
        }

        regMan.addRegion(reg);
        regions.put(mctReg.getName(), mctReg);

        try {
            regMan.save();
        } catch (ProtectionDatabaseException ex) {
            MCTowns.logSevere("Error saving regions:" + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * Gets a town by its name
     *
     * @param townName the name of the town
     * @return the town, or null is no town by that name exists
     */
    public Town getTown(String townName) {
        return towns.get(townName);
    }

    /**
     * Returns the Territory IFF the territory exists and is a Territory
     *
     * @param territName name of the Territory to get
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
     * @return true if it succeeds, false if town doesn't exist
     */
    public boolean removeTown(String townName) {
        Town t = towns.get(townName);

        if (t == null) {
            return false;
        }

        for (String s : t.getTerritoriesCollection()) {
            removeTerritory(s);
        }

        towns.remove(t.getTownName());

        return true;
    }

    /**
     * Removes the territory, its worldguard region, and all of its child plots
     *
     * @param territoryName
     * @return true if successful, false if the Territiry doesn't exist or isn't
     * a territory
     */
    public boolean removeTerritory(String territoryName) {
        MCTownsRegion mctReg = regions.get(territoryName);

        if (mctReg == null || !(mctReg instanceof Territory)) {
            return false;
        }

        Territory territ = (Territory) mctReg;

        for (String plot : territ.getPlotsCollection()) {
            removePlot(plot);
        }

        getTown(territ.getParentTown()).removeTerritory(territoryName);

        regions.remove(territ.getName());

        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(Bukkit.getWorld(territ.getWorldName()));

        regMan.removeRegion(territ.getName());

        try {
            regMan.save();
        } catch (ProtectionDatabaseException ex) {
            MCTowns.logSevere("Error saving regions:" + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * Removes the plot and its worldguard region
     *
     * @param plotName
     * @return true if removal was successful, false if the plot doesn't exist
     * or isn't a plot
     */
    public boolean removePlot(String plotName) {
        MCTownsRegion plot = regions.get(plotName);

        if (plot == null || !(plot instanceof Plot)) {
            return false;
        }

        regions.remove(plotName);

        getTerritory(((Plot) plot).getParentTerritoryName()).removePlot(plotName);

        RegionManager regMan = MCTowns.getWorldGuardPlugin().getRegionManager(Bukkit.getWorld(plot.getWorldName()));

        regMan.removeRegion(plotName);

        try {
            regMan.save();
        } catch (ProtectionDatabaseException ex) {
            MCTowns.logSevere("Error saving regions:" + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * Matches a live player to his town
     *
     * @param p the player to match to a town
     * @return the town of which the player is a member, or null if player has
     * no town
     */
    public List<Town> matchPlayerToTowns(OfflinePlayer p) {
        return matchPlayerToTowns(p.getName());

    }

    /**
     * Matches a possibly non-live player to a town
     *
     * @param playerName the name of the player to match for
     * @return a list of all towns the player is in
     */
    public List<Town> matchPlayerToTowns(String playerName) {
        ArrayList<Town> ret = new ArrayList<>();
        for (Town town : towns.values()) {
            if (town.playerIsResident(playerName)) {
                ret.add(town);
            }
        }
        return ret;
    }

    /**
     *
     * @param bukkitLoc
     * @return an ActiveSet pointing to the plot to be bought, or null if the
     * sign isn't associated with a town.
     */
    public ActiveSet getPlotFromSignLocation(org.bukkit.Location bukkitLoc) {
        Location mctLoc = Location.convertFromBukkitLocation(bukkitLoc);

        Plot p;
        Territory territ;
        Town town;
        for (MCTownsRegion reg : regions.values()) {
            if (reg instanceof Plot) {
                p = (Plot) reg;
                if (p.getSignLoc() != null && p.getSignLoc().equals(mctLoc)) {
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
     * @return true if the player is already in a town, else false
     */
    public boolean playerIsAlreadyInATown(String invitee) {
        return !matchPlayerToTowns(invitee).isEmpty();
    }

    /**
     *
     * @param rootDirPath
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

        for (Town t : towns.values()) {
            f = new YamlConfiguration();
            t.writeYAML(f);
            f.save(new File(rootDirPath + File.separator + t.getTownName() + ".yml"));
        }

        for (MCTownsRegion reg : regions.values()) {
            f = new YamlConfiguration();
            reg.writeYAML(f);
            f.save(new File(rootDirPath + File.separator + reg.getName() + ".yml"));
        }
    }

    /**
     *
     * @param rootDirPath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws InvalidConfigurationException
     */
    public static TownManager readYAML(String rootDirPath) throws FileNotFoundException, IOException, InvalidConfigurationException {
        File rootDir = new File(rootDirPath);
        FileConfiguration metaF, f;

        TownManager ret = new TownManager();

        metaF = new YamlConfiguration();
        metaF.load(rootDirPath + File.separator + ".meta.yml");

        for (String s : metaF.getStringList("towns")) {
            f = new YamlConfiguration();
            f.load(rootDirPath + File.separator + s + ".yml");
            ret.towns.put(s, Town.readYAML(f));
        }

        for (String s : metaF.getStringList("regions")) {
            f = new YamlConfiguration();
            f.load(rootDirPath + File.separator + s + ".yml");

            if (TownLevel.parseTownLevel(f.getString("type")) == TownLevel.PLOT) {
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
    public class InvalidWorldGuardRegionNameException extends RuntimeException {

        /**
         *
         * @param invalidName
         * @param shouldMatchButDoesnt
         */
        public InvalidWorldGuardRegionNameException(String invalidName, String shouldMatchButDoesnt) {
            super("Problem: Attempted to create a new MCTownsRegion, but the MCTownsRegion name and WorldGuard region name did not match. (" + invalidName + " should match " + shouldMatchButDoesnt + ").");
        }
    }
}
