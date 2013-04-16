/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.structure;

import java.math.BigDecimal;
import java.util.Collection;
import net.jmhertlein.mctowns.banking.BlockBank;
import net.jmhertlein.mctowns.structure.yaml.YamlTerritory;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public interface Town {

    /**
     * Adds a player as an assistant to the town
     *
     * @param player the player to be added
     * @return false if player was not added because player was already added,
     * true otherwise
     */
    boolean addAssistant(Player player);

    /**
     * Promotes the resident to an assistant.
     *
     * @param playerName
     * @return true if player was added as assistant, false if they're already
     * an assistant or they're not a resident of the town.
     */
    boolean addAssistant(String playerName);

    /**
     * Adds a player as a resident of the town
     *
     * @param p the player to be added
     * @return false if player was not added because player is already added,
     * true otherwise
     */
    boolean addPlayer(Player p);

    boolean addPlayer(String playerName);

    /**
     * Adds the territory to the town. The region of the territory will need to
     * be handled separately.
     *
     * @param territ the territory to be added
     * @return false if territ was not added because it is already added, true
     * otherwise
     */
    boolean addTerritory(YamlTerritory territ);

    /**
     *
     * @return
     */
    boolean allowsFriendlyFire();

    /**
     *
     * @param server
     * @param message
     */
    void broadcastMessageToTown(Server server, String message);

    /**
     *
     * @return
     */
    BlockBank getBank();

    /**
     *
     * @return
     */
    BigDecimal getDefaultPlotPrice();

    /**
     * Gets the name of the mayor of the town.
     *
     * @return the name of the town's mayor
     */
    String getMayor();

    /**
     * Returns the list of all
     *
     * @return
     */
    String[] getResidentNames();

    /**
     * Returns the current number of residents in the town.
     *
     * @return the number of residents in the town
     */
    int getSize();

    /**
     *
     * @param s
     * @return
     */
    Location getSpawn(Server s);

    /**
     * Returns the territories this town has.
     *
     * @return the town's territories
     */
    Collection<String> getTerritoriesCollection();

    /**
     * Returns the town MOTD, with color formatting
     *
     * @return the town MOTD
     */
    String getTownMOTD();

    /**
     * Returns the town's name
     *
     * @return the town's name
     */
    String getTownName();

    /**
     *
     * @param s
     * @return
     */
    Location getTownSpawn(Server s);

    /**
     * Returns the name of the world in which this town resides
     *
     * @return the name of the world
     */
    String getWorldName();

    /**
     * Returns whether or not the player is an assistant in the town
     *
     * @param p the player to be checked
     * @return if the player is an assistant or not
     */
    boolean playerIsAssistant(Player p);

    /**
     *
     * @param playerExactName
     * @return
     */
    boolean playerIsAssistant(String playerExactName);

    /**
     *
     * @param wgp
     * @param p
     * @return
     */
    boolean playerIsInsideTownBorders(Player p);

    /**
     * Returns whether the player is the mayor or not
     *
     * @param p the player to be checked
     * @return whether the player is mayor or not
     */
    boolean playerIsMayor(Player p);

    /**
     * Returns whether the player is the mayor of the town.
     *
     * @param playerName
     * @return
     */
    boolean playerIsMayor(String playerName);

    /**
     * Returns whether or not the player is a resident of the town
     *
     * @param p the player to be checked
     * @return if the player is a resident or not
     */
    boolean playerIsResident(Player p);

    /**
     * Returns whether or not the player is a resident of the town
     *
     * @param p the name of the player to be checked
     * @return if the player is a resident or not
     */
    boolean playerIsResident(String pName);

    /**
     * Removes the assistant from his position as an assistant
     *
     * @param player the player to be demoted
     * @return false if the player was not removed because the player is not an
     * assistant, true otherwise
     */
    boolean removeAssistant(Player player);

    /**
     * Removes a player from the town. Postcondition: Player is not a resident
     * of the town, regardless of whether or not they were before. Note: Player
     * must still be removed from the WG regions associated with the town
     *
     * @param p - the player to be removed
     */
    void removePlayer(Player p);

    /**
     * Removes the player from the town's list of residents and assistants. Does
     * not remove them from regions.
     *
     * @param playerName
     */
    void removePlayer(String playerName);

    /**
     * Removes the territory from the town.
     *
     * @param territName the name of the territory to remove
     * @return the removed territory
     */
    boolean removeTerritory(String territName);

    /**
     *
     * @param buyablePlots
     */
    void setBuyablePlots(boolean buyablePlots);

    /**
     *
     * @param defaultPlotPrice
     */
    void setDefaultPlotPrice(BigDecimal defaultPlotPrice);

    /**
     *
     * @param economyJoins
     */
    void setEconomyJoins(boolean economyJoins);

    /**
     *
     * @param friendlyFire
     */
    void setFriendlyFire(boolean friendlyFire);

    /**
     * Sets the town's mayor to the given name
     *
     * @param mayor the new mayor's name
     */
    void setMayor(String mayor);

    /**
     *
     * @param loc
     */
    void setSpawn(Location loc);

    /**
     *
     * @return
     */
    boolean usesBuyablePlots();

    /**
     *
     * @return
     */
    boolean usesEconomyJoins();
    
}
