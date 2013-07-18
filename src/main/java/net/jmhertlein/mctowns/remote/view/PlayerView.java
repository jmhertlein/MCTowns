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
package net.jmhertlein.mctowns.remote.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

/**
 *
 * @author joshua
 */
public class PlayerView implements Serializable {

    private final String playerName;
    private final boolean banned;
    private final long firstPlayed, lastPlayed;
    private final List<String> towns;
    private final List<Boolean> isMayor, isAssistant;

    public PlayerView(Server s, OfflinePlayer p, TownManager tMan) {
        playerName = p.getName();
        banned = p.isBanned();
        firstPlayed = p.getFirstPlayed();
        towns = new ArrayList<>();
        isAssistant = new ArrayList<>();
        isMayor = new ArrayList<>();

        lastPlayed = p.isOnline() ? -1 : p.getLastPlayed();

        for (Town t : tMan.matchPlayerToTowns(playerName)) {
            towns.add(t.getTownName());
            isMayor.add(t.playerIsMayor(playerName));
            isAssistant.add(t.playerIsAssistant(playerName));
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isBanned() {
        return banned;
    }

    public long getFirstPlayed() {
        return firstPlayed;
    }

    public long getLastPlayed() {
        return lastPlayed;
    }

    public List<String> getTowns() {
        return towns;
    }

    public List<Boolean> getIsMayor() {
        return isMayor;
    }

    public List<Boolean> getIsAssistant() {
        return isAssistant;
    }
}
