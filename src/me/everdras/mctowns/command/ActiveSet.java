/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command;

import me.everdras.mctowns.structure.*;

/**
 * The set of active objects for a player.
 * @author joshua
 */
public class ActiveSet {

    private Town activeTown;
    private Territory activeTerritory;
    private District activeDistrict;
    private Plot activePlot;

    /**
     *
     */
    public ActiveSet() {
        activeTown = null;
        activeTerritory = null;
        activeDistrict = null;
        activePlot = null;
    }

    public ActiveSet(Town activeTown, Territory activeTerritory, District activeDistrict, Plot activePlot) {
        this.activeTown = activeTown;
        this.activeTerritory = activeTerritory;
        this.activeDistrict = activeDistrict;
        this.activePlot = activePlot;
    }

    /**
     *
     * @return the active district
     */
    public District getActiveDistrict() {
        return activeDistrict;
    }

    /**
     *
     * @param activeDistrict the new active district
     */
    public void setActiveDistrict(District activeDistrict) {
        this.activeDistrict = activeDistrict;
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
    	return "Town: " + activeTown + " Territ: " + activeTerritory + " Dist: " + activeDistrict + " Plot: " + activePlot;
    }


}
