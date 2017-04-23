package cafe.josh.mctowns.util;

import org.bukkit.OfflinePlayer;

public class Players {
    public static boolean playedHasEverLoggedIn(OfflinePlayer p) {
        return p.getLastPlayed() != 0;
    }
}
