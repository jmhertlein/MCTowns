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
package cafe.josh.mctowns.townjoin.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import cafe.josh.mctowns.townjoin.TownJoinManager;
import cafe.josh.mctowns.region.Town;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author joshua
 */
public class TownJoinManagerTest {
    private Town t;
    private TownJoinManager manager;

    @Before
    public void setupTest() {
        manager = new TownJoinManager();
        t = getMockTown("testTown1");
    }

    @After
    public void tearDownTest() {
        manager = null;
        t = null;
    }

    @Test
    public void testInvitation() {
        manager.invitePlayerToTown("Notch", t);
        assertTrue(manager.invitationExists("Notch", t));
    }

    @Test
    public void testRequest() {
        manager.addJoinRequest("Notch", t);
        assertTrue(manager.requestExists("Notch", t));
    }

    @Test
    public void testRequestRemoval() {
        testRequest();

        manager.clearRequest("Notch", t);
        assertFalse(manager.requestExists("Notch", t));
    }

    @Test
    public void testInvitationRemoval() {
        testInvitation();

        manager.clearInvitationForPlayerFromTown("Notch", t);
        assertFalse(manager.invitationExists("Notch", t));
    }

    @Test
    public void testBulkInvitationRemoval() {
        Town t2, t3;
        t2 = getMockTown("Town2");
        t3 = getMockTown("Town3");

        manager.invitePlayerToTown("Notch", t);
        manager.invitePlayerToTown("Notch", t2);
        manager.invitePlayerToTown("Notch", t3);

        assertTrue(manager.invitationExists("Notch", t));
        assertTrue(manager.invitationExists("Notch", t2));
        assertTrue(manager.invitationExists("Notch", t3));

        manager.clearInvitationsForPlayer("Notch");

        assertFalse(manager.invitationExists("Notch", t));
        assertFalse(manager.invitationExists("Notch", t2));
        assertFalse(manager.invitationExists("Notch", t3));
    }

    @Test
    public void testBulkRequestAdditionAndRemoval() {
        String[] names = new String[]{"Notch", "jeb", "RMS", "Linus"};

        for(String s : names) {
            manager.addJoinRequest(s, t);
        }
        for(String s : names) {
            assertTrue(manager.requestExists(s, t));
        }

        for(String s : names) {
            manager.clearRequest(s, t);
            assertFalse(manager.requestExists(s, t));
        }

        for(String s : names) {
            assertFalse(manager.requestExists(s, t));
        }
    }

    private static Town getMockTown(String name) {
        try {
            Town ret;
            Constructor<Town> constructor = Town.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            ret = constructor.newInstance();
            Field field = Town.class.getDeclaredField("townName");
            field.setAccessible(true);
            field.set(ret, name);
            return ret;
        } catch(NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
            Logger.getLogger(TownJoinManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("===============ERROR MAKING MOCK TOWN=======================");
            return null;
        }
    }
}
