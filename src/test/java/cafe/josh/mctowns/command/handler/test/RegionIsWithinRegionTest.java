/*
 * Copyright (C) 2014 Joshua M Hertlein
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
package cafe.josh.mctowns.command.handler.test;

import cafe.josh.mctowns.command.CommandHandler;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author joshua
 */
public class RegionIsWithinRegionTest {
    @Test
    public void testPentagonIsWithinPentagon() {
        List<BlockVector2D> interiorRegPoints, exteriorRegPoints;
        interiorRegPoints = new LinkedList<>();
        exteriorRegPoints = new LinkedList<>();

        interiorRegPoints.add(new BlockVector2D(-1, 1));
        interiorRegPoints.add(new BlockVector2D(1, 1));
        interiorRegPoints.add(new BlockVector2D(2, 0));
        interiorRegPoints.add(new BlockVector2D(1, -1));
        interiorRegPoints.add(new BlockVector2D(-1, -1));

        exteriorRegPoints.add(new BlockVector2D(-4, 2));
        exteriorRegPoints.add(new BlockVector2D(0, 4));
        exteriorRegPoints.add(new BlockVector2D(4, 2));
        exteriorRegPoints.add(new BlockVector2D(4, -4));
        exteriorRegPoints.add(new BlockVector2D(-5, -3));

        ProtectedPolygonalRegion interiorReg, exteriorReg;

        interiorReg = new ProtectedPolygonalRegion("interiorReg", interiorRegPoints, 0, 256);
        exteriorReg = new ProtectedPolygonalRegion("exterior", exteriorRegPoints, 0, 256);

        assertTrue(CommandHandler.regionIsWithinRegion(interiorReg, exteriorReg));
        assertFalse(CommandHandler.regionIsWithinRegion(exteriorReg, interiorReg));
    }

    @Test
    public void testPolygonIsNotWithinConcavePolygon() {
        List<BlockVector2D> interiorRegPoints, exteriorRegPoints;
        interiorRegPoints = new LinkedList<>();
        exteriorRegPoints = new LinkedList<>();

        exteriorRegPoints.add(new BlockVector2D(-5, 5));
        exteriorRegPoints.add(new BlockVector2D(0, 0));
        exteriorRegPoints.add(new BlockVector2D(5, 5));
        exteriorRegPoints.add(new BlockVector2D(5, -5));
        exteriorRegPoints.add(new BlockVector2D(-5, -5));

        interiorRegPoints.add(new BlockVector2D(-3, 1));
        interiorRegPoints.add(new BlockVector2D(4, 1));
        interiorRegPoints.add(new BlockVector2D(4, -3));
        interiorRegPoints.add(new BlockVector2D(-3, -3));

        ProtectedPolygonalRegion interiorReg, exteriorReg;

        interiorReg = new ProtectedPolygonalRegion("interiorReg", interiorRegPoints, 0, 256);
        exteriorReg = new ProtectedPolygonalRegion("exterior", exteriorRegPoints, 0, 256);

        assertFalse(CommandHandler.regionIsWithinRegion(interiorReg, exteriorReg));
        assertFalse(CommandHandler.regionIsWithinRegion(exteriorReg, interiorReg));
    }

    @Test
    public void testPolygonWithPointOutsideIsNotWithinPolygon() {
        List<BlockVector2D> interiorRegPoints, exteriorRegPoints;
        interiorRegPoints = new LinkedList<>();
        exteriorRegPoints = new LinkedList<>();

        exteriorRegPoints.add(new BlockVector2D(-5, 5));
        exteriorRegPoints.add(new BlockVector2D(5, 5));
        exteriorRegPoints.add(new BlockVector2D(5, -5));
        exteriorRegPoints.add(new BlockVector2D(-5, -5));

        interiorRegPoints.add(new BlockVector2D(-3, 1));
        interiorRegPoints.add(new BlockVector2D(0, 500));
        interiorRegPoints.add(new BlockVector2D(4, 1));
        interiorRegPoints.add(new BlockVector2D(4, -3));
        interiorRegPoints.add(new BlockVector2D(-3, -3));

        ProtectedPolygonalRegion interiorReg, exteriorReg;

        interiorReg = new ProtectedPolygonalRegion("interiorReg", interiorRegPoints, 0, 256);
        exteriorReg = new ProtectedPolygonalRegion("exterior", exteriorRegPoints, 0, 256);

        assertFalse(CommandHandler.regionIsWithinRegion(interiorReg, exteriorReg));
        assertFalse(CommandHandler.regionIsWithinRegion(exteriorReg, interiorReg));
    }

    @Test
    public void testCuboidIsWithinPolygon() {
        ProtectedRegion interior, exterior;

        interior = new ProtectedCuboidRegion("interior", new BlockVector(-1, 0, -1), new BlockVector(1, 256, 1));

        List<BlockVector2D> exteriorRegPoints = new LinkedList<>();
        exteriorRegPoints.add(new BlockVector2D(-5, 5));
        exteriorRegPoints.add(new BlockVector2D(5, 5));
        exteriorRegPoints.add(new BlockVector2D(5, -5));
        exteriorRegPoints.add(new BlockVector2D(-5, -5));
        exterior = new ProtectedPolygonalRegion("exterior", exteriorRegPoints, 0, 256);

        assertTrue(CommandHandler.regionIsWithinRegion(interior, exterior));
        assertFalse(CommandHandler.regionIsWithinRegion(exterior, interior));
    }

    @Test
    public void testCuboidIsWithinCuboid() {
        ProtectedRegion interior, exterior;

        interior = new ProtectedCuboidRegion("interior", new BlockVector(-1, 0, -1), new BlockVector(1, 256, 1));
        exterior = new ProtectedCuboidRegion("exterior", new BlockVector(-3, 0, -3), new BlockVector(3, 256, 3));

        assertTrue(CommandHandler.regionIsWithinRegion(interior, exterior));
        assertFalse(CommandHandler.regionIsWithinRegion(exterior, interior));
    }
}
