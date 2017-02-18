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
package cafe.josh.mctowns.townjoin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import cafe.josh.mctowns.region.Town;

/**
 *
 * @author joshua
 */
public class TownJoinManager {

    /**
     * Key- Player name Value- Set of towns the player is currently invited to
     * join
     */
    private final Map<String, Set<Town>> joinInvitations;
    /**
     * Key- Town Value- Set of players who have requested membership to the town
     */
    private final Map<Town, Set<String>> joinRequests;

    public TownJoinManager() {
        joinInvitations = new ConcurrentHashMap<>();
        joinRequests = new ConcurrentHashMap<>();
    }

    public void invitePlayerToTown(final String playerName, final Town invitedTo) {
        Set<Town> towns = joinInvitations.get(playerName);

        if(towns == null) {
            joinInvitations.put(playerName, new HashSet<Town>() {
                {
                    this.add(invitedTo);
                }
            });
        } else {
            towns.add(invitedTo);
        }
    }

    public boolean invitationExists(final String playerName, final Town isInvitedTo) {
        Set<Town> towns = joinInvitations.get(playerName);

        return towns == null ? false : towns.contains(isInvitedTo);
    }

    public void addJoinRequest(final String playerName, final Town requestJoinTo) {
        Set<String> players = joinRequests.get(requestJoinTo);

        if(players == null) {
            joinRequests.put(requestJoinTo, new HashSet<String>() {
                {
                    this.add(playerName);
                }
            });
        } else {
            players.add(playerName);
        }
    }

    public boolean requestExists(final String playerName, final Town t) {
        Set<String> players = joinRequests.get(t);

        return players == null ? false : players.contains(playerName);
    }

    /**
     * Removes the player from the list of players who have requested membership
     * to the specified town
     *
     * @param t
     * @param playerName
     *
     * @return true if player was removed, false if player had not ever actually
     * requested membership
     */
    public boolean clearRequest(final String playerName, final Town t) {
        Set<String> playerNames = joinRequests.get(t);

        return playerNames == null ? false : playerNames.remove(playerName);
    }

    public void clearInvitationsForPlayer(final String playerName) {
        joinInvitations.remove(playerName);
    }

    /**
     *
     * @param playerName
     * @param t
     *
     * @return true if the invite was actually cleared, false if the player was
     * not ever actually invited
     */
    public boolean clearInvitationForPlayerFromTown(final String playerName, Town t) {
        Set<Town> towns = joinInvitations.get(playerName);
        return towns == null ? false : towns.remove(t);
    }

    public List<Town> getTownsPlayerIsInvitedTo(final String playerName) {
        ArrayList<Town> ret = new ArrayList<>();
        for(Entry<String, Set<Town>> e : joinInvitations.entrySet()) {
            if(e.getKey().equals(playerName)) {
                ret.addAll(e.getValue());
            }
        }

        return ret;
    }

    /**
     * Gets all current requests for the town.
     *
     * @param t
     *
     * @return A set of requests. Changes made to the set will be reflected in
     * later calls to this method
     */
    public Set<String> getPlayersRequestingMembershipToTown(final Town t) {
        Set<String> r = joinRequests.get(t);
        if(r == null) {
            r = new HashSet<>();
            joinRequests.put(t, r);
        }
        return r;
    }

    public Set<String> getIssuedInvitesForTown(final Town t) {
        HashSet<String> playersInvited = new HashSet<>();
        for(Entry<String, Set<Town>> e : joinInvitations.entrySet()) {
            if(e.getValue().contains(t)) {
                playersInvited.add(e.getKey());
            }
        }

        return playersInvited;
    }
}
