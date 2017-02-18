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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.awt.geom.Line2D;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Joshua
 */
public class WGUtils {

    /**
     * Returns the number of blocks that have unique positions in the xz-plane.
     * I.e. returns the number of blocks in a one-block-thick horizontal "slice"
     * of the region.
     *
     * @param reg the region
     *
     * @return number of XZ-unique blocks
     */
    public static int getNumXZBlocksInRegion(ProtectedRegion reg) {
        BlockVector max, min;
        max = reg.getMaximumPoint();
        min = reg.getMinimumPoint();

        int xLength, zLength;
        xLength = max.getBlockX() - min.getBlockX();
        zLength = max.getBlockZ() - min.getBlockZ();
        xLength++;
        zLength++;

        return xLength * zLength;
    }

    /**
     * Compares all edges of two regions to see if any of them intersect
     *
     * @author sk89q
     * @license GNU GPLv3
     *
     * Copied from WorldGuard's source
     *
     * @param a
     * @param b
     *
     * @return whether any edges of a region intersect
     */
    public static boolean intersectsEdges(ProtectedRegion a, ProtectedRegion b) {
        List<BlockVector2D> pts1 = getPointsForRegionInCorrectOrder(a);
        List<BlockVector2D> pts2 = getPointsForRegionInCorrectOrder(b);
        BlockVector2D lastPt1 = pts1.get(pts1.size() - 1);
        BlockVector2D lastPt2 = pts2.get(pts2.size() - 1);
        for(BlockVector2D aPts1 : pts1) {
            for(BlockVector2D aPts2 : pts2) {

                Line2D line1 = new Line2D.Double(
                        lastPt1.getBlockX(),
                        lastPt1.getBlockZ(),
                        aPts1.getBlockX(),
                        aPts1.getBlockZ());

                if(line1.intersectsLine(
                        lastPt2.getBlockX(),
                        lastPt2.getBlockZ(),
                        aPts2.getBlockX(),
                        aPts2.getBlockZ())) {
                    return true;
                }
                lastPt2 = aPts2;
            }
            lastPt1 = aPts1;
        }
        return false;
    }

    /**
     * A workaround for an error in ProtectedPolygonalRegion
     *
     * @param r
     *
     * @return
     */
    public static List<BlockVector2D> getPointsForRegionInCorrectOrder(ProtectedRegion r) {
        if(r instanceof ProtectedCuboidRegion) {
            List<BlockVector2D> pts = new LinkedList<>();
            int x1 = r.getMinimumPoint().getBlockX();
            int x2 = r.getMaximumPoint().getBlockX();
            int z1 = r.getMinimumPoint().getBlockZ();
            int z2 = r.getMaximumPoint().getBlockZ();

            pts.add(new BlockVector2D(x1, z1));
            pts.add(new BlockVector2D(x2, z1));
            pts.add(new BlockVector2D(x2, z2));
            pts.add(new BlockVector2D(x1, z2));

            return pts;
        } else {
            return r.getPoints();
        }
    }
}
