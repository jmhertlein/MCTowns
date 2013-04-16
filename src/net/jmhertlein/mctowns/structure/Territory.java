/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.structure;

import java.util.Collection;
import net.jmhertlein.mctowns.structure.yaml.YamlPlot;

/**
 *
 * @author joshua
 */
public interface Territory extends MCTRegion {

    /**
     * Adds a plot to the territory. Registering the WG region of the territory
     * needs to be done elsewhere.
     *
     * @param dist the plot to be added
     * @return false if the plot was not added because it is already added, true
     * otherwise
     */
    boolean addPlot(Plot plot);

    String getParentTown();

    /**
     *
     * @return the plots owned by this territory
     */
    Collection<String> getPlotsCollection();

    /**
     * Removes the plot from the territory
     *
     * @param plotName the name of the plot to be removed
     * @return if a plot was removed or not
     */
    boolean removePlot(String plotName);
    
}
