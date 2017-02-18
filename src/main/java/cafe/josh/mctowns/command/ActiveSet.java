/*
 * Copyright (C) 2013 Joshua Michael Hertlein <jmhertlein@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cafe.josh.mctowns.command;

import cafe.josh.mctowns.region.Plot;
import cafe.josh.mctowns.region.Territory;
import cafe.josh.mctowns.region.Town;

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

    /**
     *
     * @return Town: <town name> Territ: <territ name> Plot: <plot name>
     */
    @Override
    public String toString() {
        return "Town: " + activeTown + " Territ: " + activeTerritory + " Plot: " + activePlot;
    }

    void clear() {
        this.activePlot = null;
        this.activeTerritory = null;
        this.activeTown = null;
    }
}
