/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.townjoin;

import java.util.LinkedList;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.structure.Town;
import org.bukkit.entity.Player;

/**
 *
 * @author Joshua
 */
public class TownJoinManager {

    private LinkedList<TownJoinInfoPair> invites, requests;
    private TownManager manager;

    /**
     * Creates a new manager with empty lists for invites and requests
     *
     * @param manager The town manager, used to check to make sure towns exist
     * and players aren't being re-added
     */
    public TownJoinManager(TownManager manager) {
        invites = new LinkedList<>();
        requests = new LinkedList<>();
        this.manager = manager;
    }

    /**
     * Checks to see if, for the given invite, there is a request already
     * submitted under the same player and town name. If a match is found, the
     * request it matched to will be removed.
     *
     * @param invite the invite for whom a match will be checked
     * @return true if the match was found and the request was discarded, false
     * otherwise
     */
    public boolean matchInviteToRequestAndDiscard(TownJoinInfoPair invite) {
        TownJoinInfoPair removeMe = null;

        for (TownJoinInfoPair req : requests) {
            if (invite.matches(req)) {
                removeMe = req;
                break;
            }
        }

        if (removeMe != null) {
            requests.remove(removeMe);
            return true;
        }

        return false;
    }

    /**
     * Checks to see if, for the given request, there is an invite already
     * submitted under the same player and town name. If a match is found, the
     * invite it matched to will be removed.
     *
     * @param request the request for whom a match will be checked
     * @return true if the match was found and the invite was discarded, false
     * otherwise
     */
    public boolean matchRequestToInivteAndDiscard(TownJoinInfoPair request) {
        TownJoinInfoPair removeMe = null;

        for (TownJoinInfoPair invite : invites) {
            if (invite.matches(request)) {
                removeMe = invite;
                break;
            }
        }

        if (removeMe != null) {
            invites.remove(removeMe);
            return true;
        }


        return false;
    }

    /**
     * Adds an invitation (inviting a player to join a town) to the database if
     * and only if the player did not already
     *
     * @param pair
     * @return true if invite was added, false if player is already added
     */
    public boolean submitInvitation(TownJoinInfoPair pair) {

        if (!invites.contains(pair) && !manager.getTown(pair.getTown()).playerIsResident(pair.getPlayer())) {
            invites.add(pair);
            return true;
        }
        return false;

    }

    /**
     * Adds a request to the list of requests
     *
     * @param pair
     * @return true if request was added, false if player is already added
     */
    public boolean submitRequest(TownJoinInfoPair pair) {

        if (requests.contains(pair) || manager.getTown(pair.getTown()).playerIsResident(pair.getPlayer())) {
            return false;
        }

        requests.add(pair);
        return true;
    }

    /**
     * Returns all the current pending invites for all towns
     *
     * @return the list of invites
     */
    public LinkedList<TownJoinInfoPair> getInvites() {
        return invites;
    }

    /**
     * Returns all the current pending requests for all towns
     *
     * @return the list of requests
     */
    public LinkedList<TownJoinInfoPair> getRequests() {
        return requests;
    }

    /**
     * Returns a list of pending requests for the town
     *
     * @param t the specified town
     * @return the list of pending requests
     */
    public LinkedList<TownJoinInfoPair> getPendingRequestsForTown(Town t) {
        LinkedList<TownJoinInfoPair> requestsForTown = new LinkedList<>();

        for (TownJoinInfoPair tjip : requests) {
            if (tjip.getTown().equals(t.getTownName())) {
                requestsForTown.add(tjip);
            }
        }

        return requestsForTown;
    }

    /**
     * Returns a list of pending invites for the town
     *
     * @param t the specified town
     * @return the list of pending invites
     */
    public LinkedList<TownJoinInfoPair> getPendingInvitesForTown(Town t) {
        LinkedList<TownJoinInfoPair> invitesForTown = new LinkedList<>();

        for (TownJoinInfoPair tjip : invites) {
            if (tjip.getTown().equals(t.getTownName())) {
                invitesForTown.add(tjip);
            }
        }

        return invitesForTown;
    }

    /**
     * Removes the matching (.equals()) req from the list of requests
     *
     * @param t The town the req is for
     * @param p The player the req is for
     * @return if the list was modified as a result of this call
     */
    public boolean removeRequest(Town t, Player p) {
        return requests.remove(new TownJoinInfoPair(t, p));
    }

    /**
     *
     * @param t
     * @param exactPlayerName
     * @return
     */
    public boolean removeRequest(Town t, String exactPlayerName) {
        return requests.remove(new TownJoinInfoPair(t, exactPlayerName));
    }

    /**
     * Removes the matching (.equals()) req from the list of requests
     *
     * @param removeMe the request to remove
     * @return if the list was modified as a result of this call
     */
    public boolean removeRequest(TownJoinInfoPair removeMe) {
        return requests.remove(removeMe);
    }

    /**
     * Removes the matching (.equals()) invite form the list of invites
     *
     * @param t the town the inv is for
     * @param p the player the inv is for
     * @return if the list was modified as a result of this call
     */
    public boolean removeInvitation(Town t, Player p) {
        return invites.remove(new TownJoinInfoPair(t, p));
    }

    /**
     * Removes the matching (.equals()) invite form the list of invites
     *
     * @param removeMe the invitation to remove
     * @return if the list was modified as a result of this call
     */
    public boolean removeInvitation(TownJoinInfoPair removeMe) {
        return invites.remove(removeMe);
    }

    /**
     * Removes the matching (.equals()) invite form the list of invites
     *
     * @param t
     * @param playerName
     * @return if the list was modified as a result of this call
     */
    public boolean removeInvitation(Town t, String playerName) {
        return invites.remove(new TownJoinInfoPair(t.getTownName(), playerName));
    }

    /**
     * Returns a list of all the invites that are pending for player p
     *
     * @param p The player to get pending invites for
     * @return the list of pending invites for player p
     */
    public LinkedList<TownJoinInfoPair> getInvitesForPlayer(Player p) {
        LinkedList<TownJoinInfoPair> invsForPlayer = new LinkedList<>();

        for (TownJoinInfoPair tjip : invites) {
            if (tjip.getPlayer().equals(p.getName())) {
                invsForPlayer.add(tjip);
            }
        }

        return invsForPlayer;
    }

    /**
     * Returns a list of all the requests that are pending for player p
     *
     * @param p The player to get pending requests for
     * @return the list of pending requests for player p
     */
    public LinkedList<TownJoinInfoPair> getRequestsForPlayer(Player p) {
        LinkedList<TownJoinInfoPair> reqsForPlayer = new LinkedList<>();

        for (TownJoinInfoPair tjip : requests) {
            if (tjip.getPlayer().equals(p.getName())) {
                reqsForPlayer.add(tjip);
            }
        }

        return reqsForPlayer;
    }
}
