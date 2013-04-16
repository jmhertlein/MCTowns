package net.jmhertlein.mctowns.townjoin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import net.jmhertlein.mctowns.structure.yaml.YamlTown;

/**
 *
 * @author joshua
 */
public class TownJoinManager {

    /**
     * Key- Player name Value- Set of towns the player is currently invited to
     * join
     */
    private HashMap<String, Set<YamlTown>> joinInvitations;
    /**
     * Key- Town Value- Set of players who have requested membership to the town
     */
    private HashMap<YamlTown, Set<String>> joinRequests;

    public TownJoinManager() {
        joinInvitations = new HashMap<>();
        joinRequests = new HashMap<>();
    }

    public void invitePlayerToTown(final String playerName, final YamlTown invitedTo) {
        Set<YamlTown> towns = joinInvitations.get(playerName);

        if (towns == null)
            joinInvitations.put(playerName, new HashSet<YamlTown>() {
                {
                    this.add(invitedTo);
                }
            });
        else
            towns.add(invitedTo);

    }

    public boolean playerIsInvitedToTown(String playerName, YamlTown isInvitedTo) {
        Set<YamlTown> towns = joinInvitations.get(playerName);

        return towns == null ? false : towns.contains(isInvitedTo);
    }

    public void addPlayerRequestForTown(final YamlTown requestJoinTo, final String playerName) {
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

    public boolean townHasRequestFromPlayer(YamlTown t, String playerName) {
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
    public boolean clearRequestForTownFromPlayer(YamlTown t, String playerName) {
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
    public boolean clearInvitationForPlayerFromTown(String playerName, YamlTown t) {
        Set<YamlTown> towns = joinInvitations.get(playerName);
        return towns == null ? false : towns.remove(t);
    }

    public List<YamlTown> getTownsPlayerIsInvitedTo(String playerName) {
        ArrayList<YamlTown> ret = new ArrayList<>();
        for (Entry<String, Set<YamlTown>> e : joinInvitations.entrySet()) {
            if (e.getKey().equals(playerName))
                ret.addAll(e.getValue());
        }

        return ret;
    }

    /**
     * Gets all current requests for the town.
     *
     * @param t
     * @return A set of requests. Changes made to the set will be reflected in later calls to this method
     */
    public Set<String> getPlayersRequestingMembershipToTown(YamlTown t) {
        Set<String> r = joinRequests.get(t);
        if(r == null) {
            r = new HashSet<>();
            joinRequests.put(t, r);
        }
        return r;
    }

    public Set<String> getIssuedInvitesForTown(YamlTown t) {
        HashSet<String> playersInvited = new HashSet<>();
        for (Entry<String, Set<YamlTown>> e : joinInvitations.entrySet()) {
            if (e.getValue().contains(t)) {
                playersInvited.add(e.getKey());
            }
        }

        return playersInvited;
    }
}
