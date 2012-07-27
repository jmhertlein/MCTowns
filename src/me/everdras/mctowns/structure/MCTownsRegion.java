/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.structure;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import java.io.*;
import java.util.logging.Level;
import me.everdras.mctowns.MCTowns;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public abstract class MCTownsRegion implements Externalizable {

    private static final long serialVersionUID = "MCTOWNSREGION".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;
    /**
     * The name of the region, the name of the world in which the region exists
     */
    protected String name, worldName;

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
    public boolean removePlayerFromWGRegion(WorldGuardPlugin wgp, String p) {
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

    /**
     *
     * @param wgp
     * @param p
     * @return
     */
    public boolean addPlayerToWGRegion(WorldGuardPlugin wgp, Player p) {
        return addPlayerToWGRegion(wgp, p.getName());
    }

    public boolean addPlayerToWGRegion(WorldGuardPlugin wgp, String playerName) {
        DefaultDomain dd = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(name).getOwners();

        if (!dd.getPlayers().contains(playerName)) {
            dd.addPlayer(playerName);
            return true;
        }
        return false;
    }

    public boolean addGuestToWGRegion(WorldGuardPlugin wgp, String playerName) {
        DefaultDomain members = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(name).getMembers();

        if (!members.getPlayers().contains(playerName)) {
            members.addPlayer(playerName);
            return true;
        }

        return false;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);
        out.writeUTF(name);
        out.writeUTF(worldName);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        if (ver == 0) {

            //============Beginning of original variables for version 0=========
            name = in.readUTF();
            worldName = in.readUTF();
            //============End of original variables for version 0===============
        } else {
            MCTowns.log.log(Level.SEVERE, "MCTowns: Unsupported version (version " + ver + ") of MCTownsRegion.");
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
