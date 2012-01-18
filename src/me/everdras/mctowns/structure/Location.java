/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.structure;

import java.io.Serializable;
import org.bukkit.Server;

/**
 *
 * @author joshua
 */
public class Location implements Serializable {

    private String world;
    private int x, y, z;

    /**
     *
     * @param world
     * @param x
     * @param y
     * @param z
     */
    public Location(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     *
     * @return
     */
    public String getWorld() {
        return world;
    }

    /**
     *
     * @param world
     */
    public void setWorld(String world) {
        this.world = world;
    }

    /**
     *
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     *
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @param y
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     *
     * @return
     */
    public int getZ() {
        return z;
    }

    /**
     *
     * @param z
     */
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Location other = (Location) obj;
        if ((this.world == null) ? (other.world != null) : !this.world.equals(other.world)) {
            return false;
        }
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.z != other.z) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.world != null ? this.world.hashCode() : 0);
        hash = 47 * hash + this.x;
        hash = 47 * hash + this.y;
        hash = 47 * hash + this.z;
        return hash;
    }

    /**
     *
     * @param loc
     * @return
     */
    public static Location convertFromBukkitLocation(org.bukkit.Location loc) {
        return new Location(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     *
     * @param s
     * @param loc
     * @return
     */
    public static org.bukkit.Location convertToBukkitLocation(Server s, Location loc) {
        return new org.bukkit.Location(s.getWorld(loc.world), loc.getX(), loc.getY(), loc.getZ());
    }
}
