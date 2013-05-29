package net.jmhertlein.mctowns.structure;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Objects;
import net.jmhertlein.mctowns.MCTowns;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public abstract class MCTownsRegion {
    private static final long serialVersionUID = "MCTOWNSREGION".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;
    protected static WorldGuardPlugin wgp = MCTowns.getWgp();

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
     * @param wgp
     * @param p
     * @return
     */
    public boolean removePlayer(String p) {
        DefaultDomain members, owners;
        boolean removed = false;

        members = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(name).getMembers();
        owners = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(name).getOwners();

        if (members.getPlayers().contains(p)) {
            members.removePlayer(p);
            removed = true;
        }

        if (owners.getPlayers().contains(p)) {
            owners.removePlayer(p);
            removed = true;
        }

        return removed;
    }
    
    public boolean removePlayer(Player p) {
        return this.removePlayer(p.getName());
    }

    /**
     *
     * @param wgp
     * @param p
     * @return
     */
    public boolean addPlayer(Player p) {
        return addPlayer(p.getName());
    }

    public boolean addPlayer(String playerName) {
        DefaultDomain dd = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(name).getOwners();

        if (!dd.getPlayers().contains(playerName)) {
            dd.addPlayer(playerName);
            return true;
        }
        return false;
    }

    public boolean addGuest(String playerName) {
        DefaultDomain members = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(name).getMembers();

        if (!members.getPlayers().contains(playerName)) {
            members.addPlayer(playerName);
            return true;
        }

        return false;
    }

    public ProtectedRegion getWGRegion() {
        return wgp.getRegionManager(Bukkit.getServer().getWorld(worldName)).getRegionExact(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MCTownsRegion other = (MCTownsRegion) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.worldName, other.worldName)) {
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
        if(type == TownLevel.PLOT)
            infix = TownLevel.PLOT_INFIX;
        else if(type == TownLevel.TERRITORY)
            infix = TownLevel.TERRITORY_INFIX;
        else
            infix = "";

        return (owner.getTownName() + infix + plotName).toLowerCase();
    }
    
    public String getReadableName() {
        return name.substring(name.lastIndexOf('_')+1);
    }

    public void writeYAML(FileConfiguration f) {
        f.set("name", name);
        f.set("worldName", worldName);
    }

}
