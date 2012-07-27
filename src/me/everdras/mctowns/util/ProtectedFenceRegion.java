/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.util;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 *
 * @author joshua
 */
public class ProtectedFenceRegion extends ProtectedPolygonalRegion {

    private static final int NORTH = 0, SOUTH = 1, EAST = 2, WEST = 3, NONE = -1;

    public ProtectedFenceRegion(String id, List<BlockVector2D> points, int minY, int maxY) {
        super(id, points, minY, maxY);
    }

    public static final ProtectedFenceRegion assembleSelectionFromFenceOrigin(String id, Location l) throws IncompleteFenceException {
        LinkedList<BlockVector2D> points = new LinkedList<>();

        Location cur;
        int dirToNext, cameFrom;

        cur = l.clone();
        cameFrom = NONE;
        do {
            dirToNext = getDirToNextFence(cameFrom, cur);

            //if there was a corner in the fence...
            if(cameFrom != dirToNext) {
                //add it to the polygon
                points.add(new BlockVector2D(cur.getBlockX(), cur.getBlockZ()));
            }

            cameFrom = dirToNext;
            switch (dirToNext) {
                case NORTH:
                    cur = cur.add(0, 0, 1);
                    break;
                case SOUTH:
                    cur = cur.add(0, 0, -1);
                    break;
                case EAST:
                    cur = cur.add(1, 0, 0);
                    break;
                case WEST:
                    cur = cur.add(-1, 0, 0);
                    break;
                case NONE:
                    throw new IncompleteFenceException();
            }
        } while (!cur.equals(l));


        return new ProtectedFenceRegion(id, points, 0, l.getWorld().getMaxHeight());
    }

    private static final int getDirToNextFence(int cameFrom, Location l) {
        Location temp = l.clone();

        if (cameFrom != SOUTH) {
            if (temp.subtract(0, 0, 1).getBlock().getType() == Material.FENCE) {
                return SOUTH;
            }
        }

        if (cameFrom != NORTH) {
            if (temp.subtract(0, 0, -1).getBlock().getType() == Material.FENCE) {
                return NORTH;
            }
        }

        if (cameFrom != EAST) {
            if (temp.subtract(-1, 0, 0).getBlock().getType() == Material.FENCE) {
                return EAST;
            }
        }

        if (cameFrom != WEST) {
            if (temp.subtract(1, 0, 0).getBlock().getType() == Material.FENCE) {
                return WEST;
            }
        }

        return NONE;
    }

    public static class IncompleteFenceException extends Exception {

        public IncompleteFenceException() {
            super("The fence was not a complete loop.");
        }
    }
}
