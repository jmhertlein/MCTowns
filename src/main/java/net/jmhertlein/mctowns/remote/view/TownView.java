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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import net.jmhertlein.core.location.Location;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 *
 * @author joshua
 */
public class TownView implements Serializable {

    private final String townName, motd, motdColor;
    private final Location spawnLoc;
    private final boolean friendlyFire, economyJoins, buyablePlots;
    private final BigDecimal defaultPlotPrice;
    private final Collection<String> territories, residents, assistants;
    private final String mayorName;

    public TownView(String townName, String motd, String motdColor, Location spawnLoc, boolean friendlyFire, boolean economyJoins, boolean buyablePlots, BigDecimal defaultPlotPrice, String mayorName) {
        this.townName = townName;
        this.motd = motd;
        this.motdColor = motdColor;
        this.spawnLoc = spawnLoc;
        this.friendlyFire = friendlyFire;
        this.economyJoins = economyJoins;
        this.buyablePlots = buyablePlots;
        this.defaultPlotPrice = defaultPlotPrice;
        this.mayorName = mayorName;

        territories = null;
        residents = null;
        assistants = null;
    }

    public TownView(Town t) {
        townName = t.getTownName();
        motdColor = t.getMotdColor().name();
        motd = t.getTownMOTD();

        spawnLoc = Location.convertFromBukkitLocation(t.getSpawn(Bukkit.getServer()));

        friendlyFire = t.allowsFriendlyFire();
        buyablePlots = t.usesBuyablePlots();
        economyJoins = t.usesEconomyJoins();

        territories = t.getTerritoriesCollection();
        residents = Arrays.asList(t.getResidentNames());
        assistants = t.getAssistantNames();
        mayorName = t.getMayor();

        defaultPlotPrice = t.getDefaultPlotPrice();
    }

    public String getTownName() {
        return townName;
    }

    public String getMotd() {
        return motd;
    }

    public ChatColor getMotdColor() {
        return ChatColor.valueOf(motdColor);
    }

    public Location getSpawnLoc() {
        return spawnLoc;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public boolean isEconomyJoins() {
        return economyJoins;
    }

    public boolean isBuyablePlots() {
        return buyablePlots;
    }

    public BigDecimal getDefaultPlotPrice() {
        return defaultPlotPrice;
    }

    public Collection<String> getTerritories() {
        return territories;
    }

    public Collection<String> getResidents() {
        return residents;
    }

    public Collection<String> getAssistants() {
        return assistants;
    }

    public String getMayorName() {
        return mayorName;
    }
}
