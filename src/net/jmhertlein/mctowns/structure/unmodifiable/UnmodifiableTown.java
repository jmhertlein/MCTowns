/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.structure.unmodifiable;

import java.math.BigDecimal;
import java.util.HashSet;
import net.jmhertlein.core.location.Location;
import net.jmhertlein.mctowns.banking.BlockBank;
import org.bukkit.ChatColor;

/**
 *
 * @author joshua
 */
public class UnmodifiableTown {
    private final String townName;
    private final String worldName;
    private final String townMOTD;
    private final ChatColor motdColor;
    private final String townSpawn;
    private final String mayor;
    private final String bankName;
    private final boolean buyablePlots;
    private final boolean economyJoins;
    private final BigDecimal defaultPlotPrice;
    private final boolean friendlyFire;

    public UnmodifiableTown(String townName, String worldName, String townMOTD, ChatColor motdColor, String townSpawn, String mayor, String bankName, boolean buyablePlots, boolean economyJoins, BigDecimal defaultPlotPrice, boolean friendlyFire) {
        this.townName = townName;
        this.worldName = worldName;
        this.townMOTD = townMOTD;
        this.motdColor = motdColor;
        this.townSpawn = townSpawn;
        this.mayor = mayor;
        this.bankName = bankName;
        this.buyablePlots = buyablePlots;
        this.economyJoins = economyJoins;
        this.defaultPlotPrice = defaultPlotPrice;
        this.friendlyFire = friendlyFire;
    }

    public String getTownName() {
        return townName;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getTownMOTD() {
        return townMOTD;
    }

    public ChatColor getMotdColor() {
        return motdColor;
    }

    public String getTownSpawn() {
        return townSpawn;
    }

    public String getMayor() {
        return mayor;
    }

    public String getBankName() {
        return bankName;
    }

    public boolean isBuyablePlots() {
        return buyablePlots;
    }

    public boolean isEconomyJoins() {
        return economyJoins;
    }

    public BigDecimal getDefaultPlotPrice() {
        return defaultPlotPrice;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    @Override
    public String toString() {
        return "UnmodifiableTown{" + "townName=" + townName + ", worldName=" + worldName + ", townMOTD=" + townMOTD + ", motdColor=" + motdColor + ", townSpawn=" + townSpawn + ", mayor=" + mayor + ", bankName=" + bankName + ", buyablePlots=" + buyablePlots + ", economyJoins=" + economyJoins + ", defaultPlotPrice=" + defaultPlotPrice + ", friendlyFire=" + friendlyFire + '}';
    }
    
    

}
