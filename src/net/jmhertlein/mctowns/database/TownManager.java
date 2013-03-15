/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.database;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.structure.MCTownsRegion;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public interface TownManager {

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
    boolean addPlot(String fullPlotName, World worldPlotIsIn, ProtectedRegion reg, Town parentTown, Territory parentTerritory);

    /**
     * Creates a new territory, adds it to the manager, and registers its region in WorldGuard.
     * @param fullTerritoryName the desired, formatted name of the region
     * @param worldTerritoryIsIn
     * @param reg the desired region for the territory to occupy, names MUST match
     * @param parentTown the parent town of the Territory
     * @throws InvalidWorldGuardRegionNameException if the name of the ProtectedRegion does not match the desired name
     * @return true if the addition was successful, false if the name is already used
     */
    boolean addTerritory(String fullTerritoryName, World worldTerritoryIsIn, ProtectedRegion reg, Town parentTown);

    /**
     * Attempts to add a town to the town manager (effectively creating a new
     * town as far as the TownManager is concerned).
     *
     * @param townName the desired name of the new town
     * @param mayor the live player to be made the mayor of the new town
     * @return true if town was added, false if town was not because it was
     * already existing
     */
    Town addTown(String townName, Player mayor);

    /**
     * Returns the Plot IFF the territory exists and is a Territory
     * @param plotName name of the Plot to get
     * @return the Plot if it exists, or null otherwise
     */
    Plot getPlot(String plotName);

    /**
     *
     * @param bukkitLoc
     * @return an ActiveSet pointing to the plot to be bought, or null if the
     * sign isn't associated with a town.
     */
    ActiveSet getPlotFromSignLocation(Location bukkitLoc);

    /**
     *
     * @return
     */
    Collection<MCTownsRegion> getRegionsCollection();

    /**
     * Returns the Territory IFF the territory exists and is a Territory
     * @param territName name of the Territory to get
     * @return the Territory if it exists, or null otherwise
     */
    Territory getTerritory(String territName);

    /**
     * Gets a town by its name
     *
     * @param townName the name of the town
     * @return the town, or null is no town by that name exists
     */
    Town getTown(String townName);

    /**
     * Returns the towns that this manager manages
     *
     * @return the towns
     */
    Collection<Town> getTownsCollection();

    /**
     * Matches a live player to his town
     *
     * @param p the player to match to a town
     * @return the town of which the player is a member, or null if player has
     * no town
     */
    List<Town> matchPlayerToTowns(Player p);

    /**
     * Matches a possibly non-live player to a town
     *
     * @param playerName the name of the player to match for
     * @return a list of all towns the player is in
     */
    List<Town> matchPlayerToTowns(String playerName);

    /**
     * Checks to see if a live player is in a town
     *
     * @param p the live player to be checked
     * @return true if the player is already in any town, false otherwise
     */
    boolean playerIsAlreadyInATown(Player p);

    /**
     *
     * @param invitee
     * @return true if the player is already in a town, else false
     */
    boolean playerIsAlreadyInATown(String invitee);

    /**
     * Removes the plot and its worldguard region
     * @param plotName
     * @return true if removal was successful, false if the plot doesn't exist or isn't a plot
     */
    boolean removePlot(String plotName);

    /**
     * Removes the territory, its worldguard region, and all of its child plots
     * @param territoryName
     * @return true if successful, false if the Territiry doesn't exist or isn't a territory
     */
    boolean removeTerritory(String territoryName);

    /**
     * Removes the Town and all its child territories if it has any
     * @param townName
     * @return true if it succeeds, false if town doesn't exist
     */
    boolean removeTown(String townName);

    /**
     *
     * @param rootDirPath
     * @throws IOException
     */
    void writeYAML(String rootDirPath) throws IOException;
    
}
