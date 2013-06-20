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
package net.jmhertlein.mctowns.remote.view;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import net.jmhertlein.mctowns.MCTowns;
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
