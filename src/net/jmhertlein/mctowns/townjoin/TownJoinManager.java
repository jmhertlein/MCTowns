/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.townjoin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import net.jmhertlein.mctowns.structure.Town;

/**
 *
 * @author joshua
 */
public class TownJoinManager {

    /**
     * Key- Player name Value- Set of towns the player is currently invited to
     * join
     */
    private HashMap<String, Set<Town>> joinInvitations;
    /**
     * Key- Town Value- Set of players who have requested membership to the town
     */
    private HashMap<Town, Set<String>> joinRequests;

    public TownJoinManager() {
        joinInvitations = new HashMap<>();
        joinRequests = new HashMap<>();
    }

    public void invitePlayerToTown(final String playerName, final Town invitedTo) {
        Set<Town> towns = joinInvitations.get(playerName);

        if (towns == null)
            joinInvitations.put(playerName, new HashSet<Town>() {
                {
                    this.add(invitedTo);
                }
            });
        else
            towns.add(invitedTo);

    }

    public boolean playerIsInvitedToTown(String playerName, Town isInvitedTo) {
        Set<Town> towns = joinInvitations.get(playerName);

        return towns == null ? false : towns.contains(isInvitedTo);
    }

    public void addPlayerRequestForTown(final Town requestJoinTo, final String playerName) {
        Set<String> players = joinRequests.get(requestJoinTo);

        if (players == null)
            joinRequests.put(requestJoinTo, new HashSet<String>() {
                {
                    this.add(playerName);
                }
            });
        else
            players.add(playerName);
    }

    public boolean townHasRequestFromPlayer(Town t, String playerName) {
        Set<String> players = joinRequests.get(t);

        return players == null ? false : players.contains(playerName);
    }

    /**
     * Removes the player from the list of players who have requested membership
     * to the specified town
     *
     * @param t
     * @param playerName
     * @return true if player was removed, false if player had not ever actually
     * requested membership
     */
    public boolean clearRequestForTownFromPlayer(Town t, String playerName) {
        Set<String> playerNames = joinRequests.get(t);

        return playerNames == null ? false : playerNames.remove(playerName);
    }

    public void clearInvitationsForPlayer(String playerName) {
        joinInvitations.put(playerName, null);
    }

    /**
     *
     * @param playerName
     * @param t
     * @return true if the invite was actually cleared, false if the player was
     * not ever actually invited
     */
    public boolean clearInvitationForPlayerFromTown(String playerName, Town t) {
        Set<Town> towns = joinInvitations.get(playerName);
        return towns == null ? false : towns.remove(t);
    }

    public List<Town> getTownsPlayerIsInvitedTo(String playerName) {
        ArrayList<Town> ret = new ArrayList<>();
        for (Entry<String, Set<Town>> e : joinInvitations.entrySet()) {
            if (e.getKey().equals(playerName))
                ret.addAll(e.getValue());
        }

        return ret;
    }

    /**
     * Gets all current requests for the town.
     *
     * @param t
     * @return an array of requests, or an empty array if there are none
     */
    public Set<String> getPlayersRequestingMembershipToTown(Town t) {
        return joinRequests.get(t);
    }

    public Set<String> getIssuedInvitesForTown(Town t) {
        HashSet<String> playersInvited = new HashSet<>();
        for (Entry<String, Set<Town>> e : joinInvitations.entrySet()) {
            if (e.getValue().contains(t)) {
                playersInvited.add(e.getKey());
            }
        }

        return playersInvited;
    }
}
