/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.database;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.core.location.Location;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.listeners.MCTPlayerListener;
import net.jmhertlein.mctowns.structure.MCTownsRegion;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public class TownManager {
    private static WorldGuardPlugin wgp = MCTowns.getWgp();

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
    public boolean addTown(String townName, Player mayor) {
        Town t = new Town(townName, mayor);

        if (towns.containsKey(townName)) {
            return false;
        }

        towns.put(t.getTownName(), t);
        return true;

    }

    public boolean addTerritory(String fullTerritoryName, World worldTerritoryIsIn, ProtectedRegion reg, Town parentTown) {
        Territory t = new Territory(fullTerritoryName, worldTerritoryIsIn.getName(), parentTown.getTownName());

        if(! addMCTRegion(fullTerritoryName, t, worldTerritoryIsIn, reg))
            return false;

        parentTown.addTerritory(t);
        return true;
    }

    public boolean addPlot(String fullPlotName, World worldPlotIsIn, ProtectedRegion reg, Town parentTown, Territory parentTerritory) {
        Plot p = new Plot(fullPlotName, worldPlotIsIn.getName(), parentTerritory.getName(), parentTown.getTownName());

        if(! addMCTRegion(fullPlotName, p, worldPlotIsIn, reg))
            return false;

        parentTerritory.addPlot(p);
        RegionManager regMan = wgp.getRegionManager(worldPlotIsIn);
        try {
            reg.setParent(regMan.getRegion(parentTerritory.getName()));
        } catch (CircularInheritanceException ex) {
            Logger.getLogger(TownManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean addMCTRegion(String fullPlotName, MCTownsRegion mctReg, World w, ProtectedRegion reg) {
        RegionManager regMan = wgp.getRegionManager(w);
        if(! reg.getId().equals(mctReg.getName()))
            throw new InvalidWorldGuardRegionNameException(fullPlotName, reg.getId());

        if(regMan.hasRegion(mctReg.getName())) //checking regMan should always return the same value as checking regions, since the regions in regions are a subset of those in regMan... so no need to check regions
            return false;

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
     * @param territName name of the Territory to get
     * @return the Territory if it exists, or null otherwise
     */
    public Territory getTerritory(String territName) {
        MCTownsRegion ret = regions.get(territName);

        return (ret instanceof Territory ? (Territory) ret : null);
    }

    /**
     * Returns the Plot IFF the territory exists and is a Territory
     * @param plotName name of the Plot to get
     * @return the Plot if it exists, or null otherwise
     */
    public Plot getPlot(String plotName) {
        MCTownsRegion ret = regions.get(plotName);

        return (ret instanceof Plot ? (Plot) ret : null);
    }

    public boolean removeTown(String townName) {
        MCTowns.logDebug("removeTown()");

        Town t = towns.get(townName);

        if(t == null)
            return false;

        for(String s : t.getTerritoriesCollection()) {
            removeTerritory(s);
        }

        towns.remove(t.getTownName());

        return true;
    }

    public boolean removeTerritory(String territoryName) {
        MCTowns.logDebug("removeTerritory()");
        MCTownsRegion mctReg = regions.get(territoryName);

        if(mctReg == null || !(mctReg instanceof Territory))
            return false;

        Territory territ = (Territory) mctReg;

        for(String plot : territ.getPlotsCollection()) {
            removePlot(plot);
        }

        getTown(territ.getParentTown()).removeTerritory(territoryName);

        regions.remove(territ.getName());
        MCTowns.logDebug(territoryName);

        RegionManager regMan = wgp.getRegionManager(Bukkit.getWorld(territ.getWorldName()));

        regMan.removeRegion(territ.getName());

        try {
            regMan.save();
        } catch (ProtectionDatabaseException ex) {
            MCTowns.logSevere("Error saving regions:" + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        return true;
    }

    public boolean removePlot(String plotName) {
        MCTowns.logDebug("removePlot()");
        MCTownsRegion plot = regions.get(plotName);

        if(plot == null || !(plot instanceof Plot))
            return false;

        regions.remove(plotName);

        getTerritory(((Plot)plot).getParentTerritoryName()).removePlot(plotName);

        RegionManager regMan = wgp.getRegionManager(Bukkit.getWorld(plot.getWorldName()));

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
    public Town matchPlayerToTown(Player p) {
        return matchPlayerToTown(p.getName());

    }

    /**
     * Matches a possibly non-live player to a town
     *
     * @param playerName the name of the player to match for
     * @return the Town the player is a member of, or null if player is not a
     * member of any town
     */
    public Town matchPlayerToTown(String playerName) {
        for (Town town : towns.values()) {
            if (town.playerIsResident(playerName)) {
                return town;
            }
        }

        return null;
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
        for(MCTownsRegion reg : regions.values()) {
            if(reg instanceof Plot) {
                p = (Plot) reg;
                if (p.getSignLoc() != null && p.getSignLoc().equals(mctLoc)) {
                    territ = getTerritory(p.getParentTerritoryName());
                    town = getTown(territ.getParentTown());
                    return new ActiveSet(town, territ, p);
                }
            }
        }

        MCTowns.log.log(Level.SEVERE, "Couldn't find a match for this plot!");
        return null;
        //throw new RuntimeException("Couldn't find a match for this plot.");
    }

    public boolean playerIsAlreadyInATown(String invitee) {
        return matchPlayerToTown(invitee) != null;
    }

    public void writeYAML(String rootDirPath) throws IOException {
        FileConfiguration f;

        for(Town t : towns.values()) {
            f = new YamlConfiguration();
            t.writeYAML(f);
            f.save(new File(rootDirPath + File.separator + t.getTownName() + ".yml"));
        }

        for(MCTownsRegion reg : regions.values()) {
            f = new YamlConfiguration();
            if(reg instanceof Territory) {
                ((Territory) reg).writeYAML(f);
            } else {
                ((Plot) reg).writeYAML(f);
            }
            f.save(new File(rootDirPath + File.separator + reg.getName() + ".yml"));
        }
    }

    public class InvalidWorldGuardRegionNameException extends RuntimeException {
        public InvalidWorldGuardRegionNameException(String invalidName, String shouldMatchButDoesnt) {
            super("Problem: Attempted to create a new MCTownsRegion, but the MCTownsRegion name and WorldGuard region name did not match. (" + invalidName + " should match " + shouldMatchButDoesnt + ").");
        }
    }
}
