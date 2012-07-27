/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.townjoin;

import java.util.Objects;
import me.everdras.mctowns.structure.Town;
import org.bukkit.entity.Player;

/**
 *
 * @author Joshua
 */
public class TownJoinInfoPair {

    private String town, player;

    /**
     *
     * @param t
     * @param p
     */
    public TownJoinInfoPair(String t, String p) {
        town = t;
        player = p;
    }

    /**
     *
     * @param t
     * @param p
     */
    public TownJoinInfoPair(Town t, String p) {
        town = t.getTownName();
        player = p;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TownJoinInfoPair other = (TownJoinInfoPair) obj;
        if (!Objects.equals(this.town, other.town)) {
            return false;
        }
        if (!Objects.equals(this.player, other.player)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.town);
        hash = 29 * hash + Objects.hashCode(this.player);
        return hash;
    }

    /**
     * Makes a new town join info pair, to re
     *
     * @param town
     * @param player
     */
    public TownJoinInfoPair(Town town, Player player) {
        this.town = town.getTownName();
        this.player = player.getName();

    }

    /**
     *
     * @param other
     * @return
     */
    public boolean matches(TownJoinInfoPair other) {
        return (this.getPlayer().equals(other.getPlayer()) && this.getTown().equals(other.getTown()));
    }

    /**
     *
     * @return
     */
    public String getPlayer() {
        return player;
    }

    /**
     *
     * @return
     */
    public String getTown() {
        return town;
    }
}
