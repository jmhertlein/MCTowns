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
package cafe.josh.mctowns.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author joshua
 */
public class UUIDs {

    /**
     * Converts a list of Strings to UUID's, using one of two strategies: 1.
     * Assume String is the toString() of a UUID, attempt to convert straight
     * back 2. If that fails, assume String is the name of a player: look up
     * their UUID and use that instead
     *
     * @param strings
     * @return
     */
    public static Set<UUID> stringsToIds(Collection<String> strings) {
        Set<UUID> ret = new HashSet<>();
        for(String s : strings) {
            ret.add(stringToId(s));
        }
        return ret;
    }

    /**
     * Converts a String to a UUID, using one of two strategies: 1. Assume
     * String is the toString() of a UUID, attempt to convert straight back 2.
     * If that fails, assume String is the name of a player: look up their UUID
     * and use that instead
     *
     * @param s
     * @return
     */
    public static UUID stringToId(String s) {
        UUID ret;
        try {
            ret = UUID.fromString(s);
        } catch(IllegalArgumentException iae) {
            ret = getUUIDForOfflinePlayer(Bukkit.getOfflinePlayer(s));
        }
        return ret;
    }

    public static Set<String> idsToStrings(Collection<UUID> ids) {
        Set<String> ret = new HashSet<>();
        for(UUID i : ids) {
            ret.add(i.toString());
        }
        return ret;
    }

    public static UUID getUUIDForOfflinePlayer(OfflinePlayer p) {
        return p.getUniqueId();
    }

    public static String getNameForUUID(UUID u) {
        return Bukkit.getOfflinePlayer(u).getName();
    }
}
