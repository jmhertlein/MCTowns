package net.jmhertlein.mctowns.command;

import net.jmhertlein.mctowns.structure.yaml.YamlPlot;
import net.jmhertlein.mctowns.structure.yaml.YamlTerritory;
import net.jmhertlein.mctowns.structure.yaml.YamlTown;

/**
 * The set of active objects for a player.
 *
 * @author joshua
 */
public class ActiveSet {

    private YamlTown activeTown;
    private YamlTerritory activeTerritory;
    private YamlPlot activePlot;

    /**
     *
     */
    public ActiveSet() {
        activeTown = null;
        activeTerritory = null;
        activePlot = null;
    }

    public ActiveSet(YamlTown activeTown, YamlTerritory activeTerritory, YamlPlot activePlot) {
        this.activeTown = activeTown;
        this.activeTerritory = activeTerritory;
        this.activePlot = activePlot;
    }

    /**
     *
     * @return the active plot
     */
    public YamlPlot getActivePlot() {
        return activePlot;
    }

    /**
     *
     * @param activePlot the new active plot
     */
    public void setActivePlot(YamlPlot activePlot) {
        this.activePlot = activePlot;
    }

    /**
     *
     * @return the active territory
     */
    public YamlTerritory getActiveTerritory() {
        return activeTerritory;
    }

    /**
     *
     * @param activeTerritory the new active territory
     */
    public void setActiveTerritory(YamlTerritory activeTerritory) {
        this.activeTerritory = activeTerritory;
    }

    /**
     *
     * @return the active town
     */
    public YamlTown getActiveTown() {
        return activeTown;
    }

    /**
     *
     * @param activeTown the new active town
     */
    public void setActiveTown(YamlTown activeTown) {
        this.activeTown = activeTown;
    }

    @Override
    public String toString() {
        return "Town: " + activeTown + " Territ: " + activeTerritory + " Plot: " + activePlot;
    }
}
