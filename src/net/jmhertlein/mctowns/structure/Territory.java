/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.structure;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import net.jmhertlein.mctowns.MCTowns;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author joshua
 */
public class Territory extends MCTownsRegion{

    private static final long serialVersionUID = "TERRITORY".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;

    private String parTownName;
    private HashSet<String> plotNames;

    /**
     * Constructs a new territory
     *
     * @param name the desired name of the territory
     * @param worldName the name of the world in which the territory exists
     */
    public Territory(String name, String worldName, String parentTownName) {
        super(name, worldName);
        plotNames = new HashSet<>();
        parTownName = parentTownName;
    }

    /**
     * Adds a plot to the territory. Registering the WG region of the territory
     * needs to be done elsewhere.
     *
     * @param dist the plot to be added
     * @return false if the plot was not added because it is already added, true
     * otherwise
     */
    public boolean addPlot(Plot plot) {
        if (plotNames.contains(plot.getName())) {
            return false;
        }

        plotNames.add(plot.getName());
        return true;
    }

    /**
     *
     * @return the plots owned by this territory
     */
    public Collection<String> getPlotsCollection() {
        return plotNames;
    }

    /**
     * Removes the plot from the territory
     *
     * @param plotName the name of the plot to be removed
     * @return if a plot was removed or not
     */
    public boolean removePlot(String plotName) {
        return plotNames.remove(plotName);
    }

    public String getParentTown() {
        return parTownName;
    }

    @Override
    public void writeYAML(FileConfiguration f) {
        super.writeYAML(f);
        f.set("town", parTownName);
        f.set("plots", getPlotNameList());

    }

    private List<String> getPlotNameList() {
        LinkedList<String> ret = new LinkedList<>();

        for(String s : plotNames) {
            ret.add(s);
        }

        return ret;
    }


}
