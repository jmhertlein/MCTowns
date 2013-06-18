/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        lastPlayed = p.getLastPlayed();
        towns = new ArrayList<>();
        isAssistant = new ArrayList<>();
        isMayor = new ArrayList<>();
        
        for(Town t : tMan.matchPlayerToTowns(playerName)) {
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
