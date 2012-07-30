/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.util;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 *
 * @author Joshua
 */
public class WGUtils {

    /**
     * Returns the number of blocks that have unique positions on the X and Z
     * axes. I.e. returns the number of blocks in one horizontal "slice" of the
     * region.
     *
     * @param reg the region
     * @return number of XZ-unique blocks
     */
    public static long getNumXZBlocksInRegion(ProtectedRegion reg) {

        BlockVector max, min;
        max = reg.getMaximumPoint();
        min = reg.getMinimumPoint();

        long xLength, zLength;

        xLength = max.getBlockX() - min.getBlockX();
        zLength = max.getBlockZ() - min.getBlockZ();
        xLength++;
        zLength++;


        return xLength * zLength;
    }
}
