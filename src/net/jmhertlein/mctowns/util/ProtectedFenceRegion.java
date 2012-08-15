/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.util;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import java.util.LinkedList;
import java.util.List;
import net.jmhertlein.mctowns.MCTowns;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 *
 * @author joshua
 */
public class ProtectedFenceRegion extends ProtectedPolygonalRegion {

    private static final int NORTH = 0, SOUTH = 1, EAST = 2, WEST = 3, NONE = -1;
    private static final int FENCE_SEGMENT_THRESHOLD = 1000;

    public ProtectedFenceRegion(String id, List<BlockVector2D> points, int minY, int maxY) {
        super(id, points, minY, maxY);
    }

    public static final ProtectedFenceRegion assembleSelectionFromFenceOrigin(String id, Location l) throws IncompleteFenceException, InfiniteFenceLoopException {
        LinkedList<BlockVector2D> points = new LinkedList<>();

        Location cur;
        int dirToNext, cameFrom;

        cur = l.clone();
        cameFrom = NONE;
        int numFenceSegmentsTried = 0;
        do {
            dirToNext = getDirToNextFence(cameFrom, cur);

            //if there was a corner in the fence...
            if(getOppositeDir(cameFrom) != dirToNext) {
                //add it to the polygon
                points.add(new BlockVector2D(cur.getBlockX(), cur.getBlockZ()));
                MCTowns.logDebug("Added a new point: " + "(" + cur.getBlockX() + "," + cur.getBlockZ() + ")");
            }


            switch (dirToNext) {
                case NORTH:
                    cur.add(0, 0, 1);
                    cameFrom = SOUTH;
                    break;
                case SOUTH:
                    cur.add(0, 0, -1);
                    cameFrom = NORTH;
                    break;
                case EAST:
                    cur.add(1, 0, 0);
                    cameFrom = WEST;
                    break;
                case WEST:
                    cur.add(-1, 0, 0);
                    cameFrom = EAST;
                    break;
                case NONE:
                    throw new IncompleteFenceException();
            }
            numFenceSegmentsTried++;
        } while (!cur.equals(l) && numFenceSegmentsTried < FENCE_SEGMENT_THRESHOLD);

        if(numFenceSegmentsTried >= FENCE_SEGMENT_THRESHOLD) {
            throw new InfiniteFenceLoopException();
        }


        return new ProtectedFenceRegion(id, points, 0, l.getWorld().getMaxHeight()-1);
    }

    private static final int getDirToNextFence(int cameFrom, Location l) {
        if (cameFrom != SOUTH) {
            if (l.clone().add(0, 0, -1).getBlock().getType() == Material.FENCE) {
                return SOUTH;
            }
        }

        if (cameFrom != NORTH) {
            if (l.clone().add(0, 0, 1).getBlock().getType() == Material.FENCE) {
                return NORTH;
            }
        }

        if (cameFrom != EAST) {
            if (l.clone().add(1, 0, 0).getBlock().getType() == Material.FENCE) {
                return EAST;
            }
        }

        if (cameFrom != WEST) {
            if (l.clone().add(-1, 0, 0).getBlock().getType() == Material.FENCE) {
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

    public static class InfiniteFenceLoopException extends Exception {
        public InfiniteFenceLoopException() {
            super("Either the fence was too long (>1000 fence segments) or the fence is not a valid configuration.");
        }
    }

    private static int getOppositeDir(int dir) {
        switch(dir) {
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            default:
                return -1;
        }
    }
}
