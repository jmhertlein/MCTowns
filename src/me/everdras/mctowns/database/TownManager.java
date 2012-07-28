/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.database;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import me.everdras.core.location.Location;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.structure.MCTownsRegion;
import me.everdras.mctowns.structure.Plot;
import me.everdras.mctowns.structure.Territory;
import me.everdras.mctowns.structure.Town;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public class TownManager implements Externalizable {

    private static final long serialVersionUID = "TOWNMANAGER".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;
    //list of all towns
    private HashMap<String, Town> towns;

    /**
     * Constructs a new, empty town manager.
     */
    public TownManager() {
        towns = new HashMap<>();


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
     * Removes the town from the manager and unregisters everything it owns from
     * world guard
     *
     * @param wgp the server's WorldGuard instance
     * @param townName the name of the town to be removed
     */
    public void removeTown(WorldGuardPlugin wgp, String townName) {
        Town deleteMe = towns.remove(townName);
        unregisterTownFromWorldGuard(wgp, deleteMe);
    }

    /**
     * Removes the Territory from the manager and unregisters it from worldguard
     *
     * @param wgp the server's WorldGuard instance
     * @param parent the town who is the owner of the territory
     * @param territName the name of the territory to remove
     */
    public void removeTerritory(WorldGuardPlugin wgp, Town parent, String territName) {
        Territory deleteMe = parent.removeTerritory(territName);

        unregisterTerritoryFromWorldGuard(wgp, deleteMe);
    }

    /**
     * Removes the Plot from the manager and unregisters it from worldguard
     *
     * @param wgp the server's WorldGuard instance
     * @param parent the Territory which is the parent of the plot
     * @param plotName the name of the plot to be removed
     */
    public void removePlot(WorldGuardPlugin wgp, Territory parent, String plotName) {
        Plot deleteMe = parent.removePlot(plotName);

        unregisterPlotFromWorldGuard(wgp, deleteMe);
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
     * Removes every single region associated with this Town from worldguard
     *
     * @param wgp the server's WorldGuard instance
     * @param t the town to remove
     * @return false if the town was null, true otherwise
     */
    public static boolean unregisterTownFromWorldGuard(WorldGuardPlugin wgp, Town t) {
        if (t == null) {
            return false;
        }
        for (Territory territ : t.getTerritoriesCollection()) {
            unregisterTerritoryFromWorldGuard(wgp, territ);
        }
        return true;
    }

    /**
     * Removes every single region associated with this Territory from
     * worldguard
     *
     * @param wgp the server's WorldGuard instance
     * @param t the territory to remove
     * @return false if t was null, true otherwise
     */
    public static boolean unregisterTerritoryFromWorldGuard(WorldGuardPlugin wgp, Territory t) {
        if (t == null) {
            return false;
        }
        for (Plot p : t.getPlotsCollection()) {
            unregisterPlotFromWorldGuard(wgp, p);
        }

        wgp.getRegionManager(wgp.getServer().getWorld(t.getWorldName())).removeRegion(t.getName());
        return true;

    }

    /**
     * Removes every region associated with this plot from worldguard. This is
     * usually just the plot itself, since a plot has no children. But if it has
     * children because its region was manually changed, the children will be
     * removed.
     *
     * @param wgp the server's WorldGuard instance
     * @param p the plot to remove
     * @return false if p is null, true otherwise
     */
    public static boolean unregisterPlotFromWorldGuard(WorldGuardPlugin wgp, Plot p) {
        if (p == null) {
            return false;
        }
        wgp.getRegionManager(wgp.getServer().getWorld(p.getWorldName())).removeRegion(p.getName());
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

        for (Town to : getTownsCollection()) {
            for (Territory te : to.getTerritoriesCollection()) {
                for (Plot p : te.getPlotsCollection()) {
                    if (p.getSignLoc() != null && p.getSignLoc().equals(mctLoc)) {
                        return new ActiveSet(to, te, p);
                    }
                }
            }
        }
        MCTowns.log.log(Level.SEVERE, "Couldn't find a match for this plot!");
        return null;
        //throw new RuntimeException("Couldn't find a match for this plot.");
    }

    /**
     * Removes the player from every WG region associated with this town.
     *
     * @param wgp the server's WorldGuard instance
     * @param town
     * @param p
     */
    public void removePlayerFromTownsWGRegions(WorldGuardPlugin wgp, Town town, Player p) {
        removePlayerFromTownsWGRegions(wgp, town, p.getName());
    }

    public void removePlayerFromTownsWGRegions(WorldGuardPlugin wgp, Town town, String playerName) {
        for (Territory territ : town.getTerritoriesCollection()) {
            removePlayerFromTerritorysWGRegions(wgp, territ, playerName);
        }
    }

    /**
     * Removes the player from the Territory's WG region
     *
     * @param wgp the server's WorldGuard instance
     * @param territ
     * @param p
     */
//    public void removePlayerFromTerritorysWGRegions(WorldGuardPlugin wgp, Territory territ, Player p) {
//        removePlayerFromTerritorysWGRegions(wgp, territ, p.getName());
//    }
    public void removePlayerFromTerritorysWGRegions(WorldGuardPlugin wgp, Territory territ, String playerName) {
        removePlayerFromWorldGuardRegion(wgp, territ, playerName);

        for (Plot p : territ.getPlotsCollection()) {
            removePlayerFromPlotsWGRegion(wgp, p, playerName);
        }
    }

    /**
     * Removes the player from the plot's WG region.
     *
     * @param wgp the server's WorldGuard instance
     * @param plot the plot to remove the player from
     * @param p the player to be removed from the plot
     */
    public void removePlayerFromPlotsWGRegion(WorldGuardPlugin wgp, Plot plot, Player p) {
        removePlayerFromWorldGuardRegion(wgp, plot, p.getName());
    }

    public void removePlayerFromPlotsWGRegion(WorldGuardPlugin wgp, Plot plot, String playerName) {
        removePlayerFromWorldGuardRegion(wgp, plot, playerName);

    }

    @SuppressWarnings("unused")
    private void removePlayerFromWorldGuardRegion(WorldGuardPlugin wgp, MCTownsRegion mctRegion, Player p) {
        removePlayerFromWorldGuardRegion(wgp, mctRegion, p.getName());
    }

    private void removePlayerFromWorldGuardRegion(WorldGuardPlugin wgp, MCTownsRegion mctRegion, String playerName) {
        ProtectedRegion reg = wgp.getRegionManager(wgp.getServer().getWorld(mctRegion.getWorldName())).getRegion(mctRegion.getName());
        reg.getMembers().removePlayer(playerName);
        reg.getOwners().removePlayer(playerName);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);

        out.writeObject(towns);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        if (ver == 0) {
            //============Beginning of original variables for version 0=========
            towns = (HashMap<String, Town>) in.readObject();
            //============End of original variables for version 0===============
        } else {
            MCTowns.log.log(Level.SEVERE, "MCTowns: Unsupported version (version " + ver + ") of Town.");
        }
    }

    public boolean playerIsAlreadyInATown(String invitee) {
        return matchPlayerToTown(invitee) != null;
    }
}
