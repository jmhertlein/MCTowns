/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.structure;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public interface MCTRegion {

    boolean addGuest(String playerName);

    /**
     *
     * @param wgp
     * @param p
     * @return
     */
    boolean addPlayer(Player p);

    boolean addPlayer(String playerName);

    String getReadableName();

    ProtectedRegion getWGRegion();

    /**
     *
     * @return the name of the world in which the region resides
     */
    String getWorldName();

    /**
     *
     * @param wgp
     * @param p
     * @return
     */
    boolean removePlayer(String p);

    boolean removePlayer(Player p);
    
    boolean save();

    /**
     *
     * @return the name of the region
     */
    String getName();
    
}
