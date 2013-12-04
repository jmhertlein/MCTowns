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
package net.jmhertlein.mctowns.util;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 *
 * @author Joshua
 */
public class WGUtils {

    /**
     * Returns the number of blocks that have unique positions in the xz-plane.
     * I.e. returns the number of blocks in a one-block-thick horizontal "slice" of the
     * region.
     *
     * @param reg the region
     *
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
