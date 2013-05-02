package net.jmhertlein.mctowns.database;

import net.jmhertlein.cs238.SQLManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.structure.MCTRegion;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.TownLevel;
import net.jmhertlein.mctowns.structure.factory.MCTFactory;
import net.jmhertlein.mctowns.structure.yaml.YamlPlot;
import net.jmhertlein.mctowns.structure.yaml.YamlTerritory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public abstract class TownManager {
    protected static WorldGuardPlugin wgp = MCTowns.getWgp();
    protected HashMap<String, MCTRegion> regions;
    protected HashMap<String, Town> towns;
    protected MCTFactory factory;
    
    public TownManager(MCTFactory factory) {
        this.factory = factory;
        regions = new HashMap<>();
        towns = new HashMap<>();
    }

    protected boolean addMCTRegion(String fullPlotName, MCTRegion mctReg, World w, ProtectedRegion reg) {
        RegionManager regMan = wgp.getRegionManager(w);
        if (!reg.getId().equals(mctReg.getName()))
            throw new InvalidWorldGuardRegionNameException(fullPlotName, reg.getId());
        if (regMan.hasRegion(mctReg.getName()))
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
     * Creates a new territory, adds it to the manager, and registers its region in WorldGuard.
     * @param fullPlotName the desired, formatted name of the region
     * @param worldPlotIsIn
     * @param reg the desired region for the plot to occupy
     * @param parentTown the parent town of this region
     * @param parentTerritory the parent territory of this region
     * @throws InvalidWorldGuardRegionNameException if the name of the ProtectedRegion does not match the desired name
     * @return true if the addition was successful, false if the name is already used
     */
    public boolean addPlot(String fullPlotName, World worldPlotIsIn, ProtectedRegion reg, Town parentTown, Territory parentTerritory) {
        Plot p = factory.newPlot(fullPlotName, worldPlotIsIn.getName(), parentTerritory.getName(), parentTown.getTownName());
        if (!addMCTRegion(fullPlotName, p, worldPlotIsIn, reg))
            return false;
        parentTerritory.addPlot(p);
        RegionManager regMan = wgp.getRegionManager(worldPlotIsIn);
        try {
            reg.setParent(regMan.getRegion(parentTerritory.getName()));
        } catch (ProtectedRegion.CircularInheritanceException ex) {
            Logger.getLogger(TownManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        p.calculateSignLoc(); //note: don't move calculateSignLoc from here.
        //it needs the region to exist in the region manager
        return false;
    }

    /**
     * Creates a new territory, adds it to the manager, and registers its region in WorldGuard.
     * @param fullTerritoryName the desired, formatted name of the region
     * @param worldTerritoryIsIn
     * @param reg the desired region for the territory to occupy, names MUST match
     * @param parentTown the parent town of the Territory
     * @throws InvalidWorldGuardRegionNameException if the name of the ProtectedRegion does not match the desired name
     * @return true if the addition was successful, false if the name is already used
     */
    public boolean addTerritory(String fullTerritoryName, World worldTerritoryIsIn, ProtectedRegion reg, Town parentTown) {
        Territory t = factory.newTerritory(fullTerritoryName, worldTerritoryIsIn.getName(), parentTown.getTownName());
        if (!addMCTRegion(fullTerritoryName, t, worldTerritoryIsIn, reg))
            return false;
        parentTown.addTerritory(t);
        return true;
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
        Town t = factory.newTown(townName, mayor);
        if (towns.containsKey(townName)) {
            return null;
        }
        towns.put(t.getTownName(), t);
        return t;
    }

    /**
     * Returns the Plot IFF the territory exists and is a Territory
     * @param plotName name of the Plot to get
     * @return the Plot if it exists, or null otherwise
     */
    public YamlPlot getPlot(String plotName) {
        MCTRegion ret = regions.get(plotName);
        return ret instanceof YamlPlot ? (YamlPlot) ret : null;
    }

    /**
     *
     * @param bukkitLoc
     * @return an ActiveSet pointing to the plot to be bought, or null if the
     * sign isn't associated with a town.
     */
    public ActiveSet getPlotFromSignLocation(Location bukkitLoc) {
        net.jmhertlein.core.location.Location mctLoc = net.jmhertlein.core.location.Location.convertFromBukkitLocation(bukkitLoc);
        Plot p;
        Territory territ;
        Town town;
        for (MCTRegion reg : regions.values()) {
            if (reg instanceof YamlPlot) {
                p = (YamlPlot) reg;
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

    /**
     *
     * @return
     */
    public Collection<MCTRegion> getRegionsCollection() {
        return regions.values();
    }

    /**
     * Returns the Territory IFF the territory exists and is a Territory
     * @param territName name of the Territory to get
     * @return the Territory if it exists, or null otherwise
     */
    public Territory getTerritory(String territName) {
        MCTRegion ret = regions.get(territName);
        return ret instanceof Territory ? (Territory) ret : null;
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
     * Returns the towns that this manager manages
     *
     * @return the towns
     */
    public Collection<Town> getTownsCollection() {
        return towns.values();
    }

    /**
     * Matches a live player to his town
     *
     * @param p the player to match to a town
     * @return the town of which the player is a member, or null if player has
     * no town
     */
    public List<Town> matchPlayerToTowns(Player p) {
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
     * Checks to see if a live player is in a town
     *
     * @param p the live player to be checked
     * @return true if the player is already in any town, false otherwise
     */
    public boolean playerIsAlreadyInATown(Player p) {
        return playerIsAlreadyInATown(p.getName());
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
     * Removes the plot and its worldguard region
     * @param plotName
     * @return true if removal was successful, false if the plot doesn't exist or isn't a plot
     */
    public boolean removePlot(String plotName) {
        MCTowns.logDebug("removePlot()");
        MCTRegion plot = regions.get(plotName);
        if (plot == null || !(plot instanceof YamlPlot))
            return false;
        regions.remove(plotName);
        getTerritory(((YamlPlot) plot).getParentTerritoryName()).removePlot(plotName);
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
     * Removes the territory, its worldguard region, and all of its child plots
     * @param territoryName
     * @return true if successful, false if the Territiry doesn't exist or isn't a territory
     */
    public boolean removeTerritory(String territoryName) {
        MCTowns.logDebug("removeTerritory()");
        MCTRegion mctReg = regions.get(territoryName);
        if (mctReg == null || !(mctReg instanceof YamlTerritory))
            return false;
        YamlTerritory territ = (YamlTerritory) mctReg;
        for (String plot : territ.getPlotsCollection()) {
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

    /**
     * Removes the Town and all its child territories if it has any
     * @param townName
     * @return true if it succeeds, false if town doesn't exist
     */
    public boolean removeTown(String townName) {
        Town t = towns.get(townName);
        if (t == null)
            return false;
        for (String s : t.getTerritoriesCollection()) {
            removeTerritory(s);
        }
        towns.remove(t.getTownName());
        return true;
    }
    
    public static TownManager getSQLTownManager(SQLManager sqlManager) {
        throw new UnsupportedOperationException("Not yet implemented.");
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
    
    public enum TownManagerType {
        SQL,
        YAML;
    }
    
    public static final String formatRegionName(Town owner, TownLevel type, String plotName) {
        return formatRegionName(owner.getTownName(), type, plotName);
    }
    
    public static final String formatRegionName(String ownerTownName, TownLevel type, String plotName) {
        plotName = plotName.toLowerCase();

        String infix;
        if(type == TownLevel.PLOT)
            infix = TownLevel.PLOT_INFIX;
        else if(type == TownLevel.TERRITORY)
            infix = TownLevel.TERRITORY_INFIX;
        else
            infix = "";

        return (ownerTownName + infix + plotName).toLowerCase();
    }
    
    public abstract void save() throws IOException;

}
