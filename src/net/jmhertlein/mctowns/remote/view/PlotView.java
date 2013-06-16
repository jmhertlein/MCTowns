/*
 * Copyright (C) 2013 joshua
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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import net.jmhertlein.core.location.Location;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.structure.Plot;
import org.bukkit.Bukkit;

/**
 *
 * @author joshua
 */
public class PlotView implements Serializable {
    private final String parTownName, parTerrName,
            worldName, plotName;
    private final boolean forSale;
    private final BigDecimal price;
    private final Location signLoc;
    
    private final List<String> playerNames, guestNames;

    public PlotView(String plotName, String worldName, boolean forSale, BigDecimal price, Location signLoc) {
        this.parTownName = null;
        this.parTerrName = null;
        this.worldName = worldName;
        this.plotName = plotName;
        this.forSale = forSale;
        this.price = price;
        this.signLoc = signLoc;
        playerNames = null;
        guestNames = null;
    }
    
    public PlotView(Plot p) {
        worldName = p.getWorldName();
        plotName = p.getName();
        parTownName = p.getParentTownName();
        parTerrName = p.getParentTerritoryName();
        forSale = p.isForSale();
        price = p.getPrice();
        signLoc = p.getSignLoc();
        
        ProtectedRegion r = MCTowns.getWgp().getRegionManager(Bukkit.getWorld(worldName)).getRegion(plotName);
        playerNames = new LinkedList<>(r.getOwners().getPlayers());
        guestNames = new LinkedList<>(r.getMembers().getPlayers());
    }

    public String getParTownName() {
        return parTownName;
    }

    public String getParTerrName() {
        return parTerrName;
    }

    public boolean isForSale() {
        return forSale;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Location getSignLoc() {
        return signLoc;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getPlotName() {
        return plotName;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public List<String> getGuestNames() {
        return guestNames;
    }
    
    
}
