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
package cafe.josh.mctowns.util;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * A ProtectedFenceRegion is a WorldGuard region that can be constructed from a
 * fenced polygon of blocks in-game. There are some restrictions that are
 * detailed in the docs for "EasyRegion", mostly that they need to be closed
 * polygons where each fence block touches exactly two other fences.
 *
 * @author joshua
 */
public class ProtectedFenceRegion extends ProtectedPolygonalRegion {

    private static final int NORTH = 0, SOUTH = 1, EAST = 2, WEST = 3, NONE = -1;
    private static final int FENCE_SEGMENT_THRESHOLD = 1000;

    public ProtectedFenceRegion(String id, List<BlockVector2D> points, int minY, int maxY) {
        super(id, points, minY, maxY);
    }

    /**
     * Creates a new ProtectedFenceRegion with the given ID (must be formatted
     * manually to MCTowns spec) This is fairly "fire and forget", and handles
     * most exceptional circumstances.
     *
     * @param id The name of the region to useas a GUID
     * @param l any fence in the polygon
     *
     * @return the constructed PFR
     * @throws
     * ProtectedFenceRegion.IncompleteFenceException
     * if the fenced-in region is not completed
     * @throws
     * ProtectedFenceRegion.InfiniteFenceLoopException
     * if the fenced-in region is not properly formed (usually, where there
     * exists a fence that touches more than exactly two fences)
     */
    public static final ProtectedFenceRegion assembleSelectionFromFenceOrigin(String id, Location l) throws IncompleteFenceException, MalformedFenceRegionException {
        LinkedList<BlockVector2D> points = new LinkedList<>();

        Location cur;
        int dirToNext, cameFrom;

        cur = l.clone();
        cameFrom = NONE;
        int numFenceSegmentsTried = 0;
        do {
            dirToNext = getDirToNextFence(cameFrom, cur);

            //if there was a corner in the fence...
            if(getOppositeDir(cameFrom) != dirToNext) //add it to the polygon
            {
                points.add(new BlockVector2D(cur.getBlockX(), cur.getBlockZ()));
            }

            switch(dirToNext) {
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
        } while(!cur.equals(l) && numFenceSegmentsTried < FENCE_SEGMENT_THRESHOLD);

        if(numFenceSegmentsTried >= FENCE_SEGMENT_THRESHOLD) {
            throw new MalformedFenceRegionException();
        }

        return new ProtectedFenceRegion(id, points, 0, l.getWorld().getMaxHeight() - 1);
    }

    private static final int getDirToNextFence(int cameFrom, Location l) {
        if(cameFrom != SOUTH) {
            if(l.clone().add(0, 0, -1).getBlock().getType() == Material.FENCE) {
                return SOUTH;
            }
        }

        if(cameFrom != NORTH) {
            if(l.clone().add(0, 0, 1).getBlock().getType() == Material.FENCE) {
                return NORTH;
            }
        }

        if(cameFrom != EAST) {
            if(l.clone().add(1, 0, 0).getBlock().getType() == Material.FENCE) {
                return EAST;
            }
        }

        if(cameFrom != WEST) {
            if(l.clone().add(-1, 0, 0).getBlock().getType() == Material.FENCE) {
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

    /**
     * Thrown if a fence region is malformed such that it is not possible to
     * finish parsing it
     */
    public static class MalformedFenceRegionException extends Exception {

        public MalformedFenceRegionException() {
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
