/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.remote.view;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.structure.Territory;
import org.bukkit.Bukkit;

/**
 *
 * @author Joshua
 */
public class TerritoryView implements Serializable {
    private final String name, worldName, parentTownName;
    private final List<String> plotNames, playerNames;
    
    public TerritoryView(Territory t) {
        name = t.getName();
        worldName = t.getWorldName();
        parentTownName = t.getParentTown();
        
        plotNames = new LinkedList<>();
        plotNames.addAll(t.getPlotsCollection());
        
        playerNames = new LinkedList<>();
        playerNames.addAll(MCTowns.getWorldGuardPlugin().getRegionManager(Bukkit.getWorld(worldName)).getRegion(name).getOwners().getPlayers());
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getParentTownName() {
        return parentTownName;
    }

    public List<String> getPlotNames() {
        return plotNames;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }
}
