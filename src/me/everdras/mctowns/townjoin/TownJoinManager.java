/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package me.everdras.mctowns.townjoin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import me.everdras.mctowns.structure.Town;

/**
 *
 * @author joshua
 */
public class TownJoinManager {
    private HashMap<String, Town> joinInvitations;
    private HashMap<Town, TreeMap<String, Boolean>> joinRequests;

    public TownJoinManager() {
        joinInvitations = new HashMap<>();
        joinRequests = new HashMap<>();
    }

    public void invitePlayerToTown(String playerName, Town invitedTo) {
        joinInvitations.put(playerName, invitedTo);
    }

    public boolean playerIsInvitedToTown(String playerName, Town isInvitedTo) {
        return joinInvitations.get(playerName).equals(isInvitedTo);
    }

    public void addPlayerRequestForTown(Town requestJoinTo, String playerName) {
        if(joinRequests.get(requestJoinTo) == null)
            joinRequests.put(requestJoinTo, new TreeMap<String, Boolean>());

        joinRequests.get(requestJoinTo).put(playerName, Boolean.TRUE);
    }

    public boolean townHasRequestFromPlayer(Town t, String playerName) {
        Boolean ret = joinRequests.get(t).get(playerName);
        return ret == null ? false : ret;
    }

    public boolean clearRequestForTownFromPlayer(Town t, String playerName) {
        return joinRequests.get(t).remove(playerName);
    }

    public void clearInvitationForPlayer(String playerName) {
        joinInvitations.remove(playerName);
    }

    public boolean clearInvitationForPlayerFromTown(String playerName, Town t) {
        if(! t.equals(joinInvitations.get(playerName)))
            return false;

        joinInvitations.remove(playerName);
        return true;
    }

    public Town getCurrentInviteForPlayer(String playerName) {
        return joinInvitations.get(playerName);
    }

    public String[] getCurrentRequestsForTown(Town t) {
        return (String[]) joinRequests.get(t).keySet().toArray();
    }

    public String[] getIssuedInvitesForTown(Town t) {
        LinkedList<String> ret = new LinkedList<>();
        for(Entry<String, Town> e : joinInvitations.entrySet()) {
            if(e.getValue().equals(t))
                ret.add(e.getValue().getTownName());
        }

        return ret.toArray(new String[ret.size()]);
    }
}
