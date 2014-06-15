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

package net.jmhertlein.mctowns.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author joshua
 */
public class UUIDs {
    public static Set<UUID> stringsToIds(List<String> strings) {
        Set<UUID> ret = new HashSet<>();
        for(String s : strings)
            ret.add(UUID.fromString(s));
        return ret;
    }
    public static Set<String> idsToStrings(Set<UUID> ids) {
        Set<String> ret = new HashSet<>();
        for(UUID i : ids)
            ret.add(i.toString());
        return ret;
    }

    public static UUID getUUIDForOfflinePlayer(OfflinePlayer p) {
        return p.getUniqueId();
    }

    public static String getNameForUUID(UUID u) {
        return Bukkit.getPlayer(u).getName();
    }
}
