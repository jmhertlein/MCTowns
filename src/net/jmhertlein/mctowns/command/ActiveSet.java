package net.jmhertlein.mctowns.command;

import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.yaml.YamlPlot;
import net.jmhertlein.mctowns.structure.yaml.YamlTerritory;
import net.jmhertlein.mctowns.structure.yaml.YamlTown;

/**
 * The set of active objects for a player.
 *
 * @author joshua
 */
public class ActiveSet {

    private Town activeTown;
    private Territory activeTerritory;
    private Plot activePlot;

    /**
     *
     */
    public ActiveSet() {
        activeTown = null;
        activeTerritory = null;
        activePlot = null;
    }

    public ActiveSet(Town activeTown, Territory activeTerritory, Plot activePlot) {
        this.activeTown = activeTown;
        this.activeTerritory = activeTerritory;
        this.activePlot = activePlot;
    }

    /**
     *
     * @return the active plot
     */
    public Plot getActivePlot() {
        return activePlot;
    }

    /**
     *
     * @param activePlot the new active plot
     */
    public void setActivePlot(Plot activePlot) {
        this.activePlot = activePlot;
    }

    /**
     *
     * @return the active territory
     */
    public Territory getActiveTerritory() {
        return activeTerritory;
    }

    /**
     *
     * @param activeTerritory the new active territory
     */
    public void setActiveTerritory(Territory activeTerritory) {
        this.activeTerritory = activeTerritory;
    }

    /**
     *
     * @return the active town
     */
    public Town getActiveTown() {
        return activeTown;
    }

    /**
     *
     * @param activeTown the new active town
     */
    public void setActiveTown(Town activeTown) {
        this.activeTown = activeTown;
    }

    @Override
    public String toString() {
        return "Town: " + activeTown + " Territ: " + activeTerritory + " Plot: " + activePlot;
    }
}
