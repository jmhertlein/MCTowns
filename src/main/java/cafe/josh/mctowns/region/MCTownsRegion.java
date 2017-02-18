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
package cafe.josh.mctowns.region;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Objects;
import java.util.UUID;
import cafe.josh.mctowns.MCTowns;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author joshua
 */
public abstract class MCTownsRegion {

    private static final long serialVersionUID = "MCTOWNSREGION".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;
    /**
     * The name of the region, the name of the world in which the region exists
     */
    protected volatile String name, worldName;

    public MCTownsRegion() {
    }

    /**
     * creates a new region with the name name, and sets its world to be
     * worldname
     *
     * @param name the name of the new region
     * @param worldName the world of the new region
     */
    public MCTownsRegion(String name, String worldName) {
        this.name = name.toLowerCase();
        this.worldName = worldName;
    }

    /**
     *
     * @return the name of the region
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the name of the world in which the region resides
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     *
     * @param playerID
     *
     * @return
     */
    public boolean removePlayer(UUID playerID) {
        DefaultDomain members, owners;
        boolean removed = false;
        WorldGuardPlugin wgp = MCTowns.getWorldGuardPlugin();

        World w = wgp.getServer().getWorld(worldName);
        if(w == null) {
            return false;
        }

        ProtectedRegion reg = wgp.getRegionManager(w).getRegion(name);
        if(reg == null) {
            return false;
        }

        members = reg.getMembers();
        owners = reg.getOwners();

        if(members.contains(playerID)) {
            members.removePlayer(playerID);
            removed = true;
        }

        if(owners.contains(playerID)) {
            owners.removePlayer(playerID);
            removed = true;
        }

        return removed;
    }

    public boolean removePlayer(OfflinePlayer p) {
        return this.removePlayer(p.getUniqueId());
    }

    /**
     *
     * @param p
     *
     * @return
     */
    public boolean addPlayer(OfflinePlayer p) {
        return addPlayer(p.getUniqueId());
    }

    public boolean addPlayer(UUID playerID) {
        World w = MCTowns.getWorldGuardPlugin().getServer().getWorld(worldName);
        if(w == null) {
            return false;
        }

        ProtectedRegion reg = MCTowns.getWorldGuardPlugin().getRegionManager(w).getRegion(name);
        if(reg == null) {
            return false;
        }

        DefaultDomain dd = reg.getOwners();

        if(!dd.contains(playerID)) {
            dd.addPlayer(playerID);
            return true;
        }
        return false;
    }

    public boolean addGuest(UUID playerID) {
        World w = MCTowns.getWorldGuardPlugin().getServer().getWorld(worldName);
        if(w == null) {
            return false;
        }

        ProtectedRegion reg = MCTowns.getWorldGuardPlugin().getRegionManager(w).getRegion(name);
        if(reg == null) {
            return false;
        }

        DefaultDomain members = reg.getMembers();

        if(!members.contains(playerID)) {
            members.addPlayer(playerID);
            return true;
        }

        return false;
    }

    public boolean addGuest(OfflinePlayer p) {
        return addGuest(p.getUniqueId());
    }

    public ProtectedRegion getWGRegion() {
        return MCTowns.getWorldGuardPlugin().getRegionManager(Bukkit.getServer().getWorld(worldName)).getRegion(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final MCTownsRegion other = (MCTownsRegion) obj;
        if(!Objects.equals(this.name, other.name)) {
            return false;
        }
        if(!Objects.equals(this.worldName, other.worldName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.worldName);
        return hash;
    }

    public static final String formatRegionName(Town owner, TownLevel type, String plotName) {
        plotName = plotName.toLowerCase();

        String infix;
        if(type == TownLevel.PLOT) {
            infix = TownLevel.PLOT_INFIX;
        } else if(type == TownLevel.TERRITORY) {
            infix = TownLevel.TERRITORY_INFIX;
        } else {
            infix = "";
        }

        return (owner.getName() + infix + plotName).toLowerCase();
    }

    public String getReadableName() {
        return name.substring(name.lastIndexOf('_') + 1);
    }

    public void writeYAML(FileConfiguration f) {
        f.set("name", name);
        f.set("worldName", worldName);
    }
}
