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
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author joshua
 */
public class OverView implements Serializable {

    private final Boolean economyEnabled,
            mayorsCanBuyTerritories,
            allowTownFriendlyFireManagement,
            logCommands,
            playerCanJoinMultipleTowns;
    private final Float pricePerXZBlock;
    private final Integer minNumPlayersToBuyTerritory,
            bugReportPort,
            remoteAdminPort,
            quickSelectTool,
            remoteAdminKeyLength,
            remoteAdminSessionKeyLength;
    private final String bugReportHostname;

    public OverView(Boolean economyEnabled, Boolean mayorsCanBuyTerritories, Boolean allowTownFriendlyFireManagement, Boolean logCommands, Boolean playerCanJoinMultipleTowns,
            Float pricePerXZBlock, Integer minNumPlayersToBuyTerritory, Integer quickSelectTool,
            String bugReportHostname, Integer bugReportPort,
            Integer remoteAdminPort, Integer remoteAdminKeyLength, Integer remoteAdminSessionKeyLength) {
        this.economyEnabled = economyEnabled;
        this.mayorsCanBuyTerritories = mayorsCanBuyTerritories;
        this.allowTownFriendlyFireManagement = allowTownFriendlyFireManagement;
        this.logCommands = logCommands;
        this.playerCanJoinMultipleTowns = playerCanJoinMultipleTowns;
        this.pricePerXZBlock = pricePerXZBlock;
        this.minNumPlayersToBuyTerritory = minNumPlayersToBuyTerritory;
        this.bugReportPort = bugReportPort;
        this.remoteAdminPort = remoteAdminPort;
        this.quickSelectTool = quickSelectTool;
        this.remoteAdminKeyLength = remoteAdminKeyLength;
        this.remoteAdminSessionKeyLength = remoteAdminSessionKeyLength;
        this.bugReportHostname = bugReportHostname;
    }

    public OverView(FileConfiguration f) {
        this.economyEnabled = f.getBoolean("economyEnabled");
        this.allowTownFriendlyFireManagement = f.getBoolean("allowTownFriendlyFireManagement");
        this.bugReportHostname = f.getString("bugReportHostname");
        this.bugReportPort = f.getInt("bugReportPort");
        this.logCommands = f.getBoolean("logCommands");
        this.mayorsCanBuyTerritories = f.getBoolean("mayorsCanBuyTerritories");
        this.pricePerXZBlock = new Float(f.getDouble("pricePerXZBlock"));
        this.quickSelectTool = f.getInt("quickSelectTool");
        this.playerCanJoinMultipleTowns = f.getBoolean("playersCanJoinMultipleTowns");
        this.minNumPlayersToBuyTerritory = f.getInt("minNumPlayersToBuyTerritory");
        this.remoteAdminKeyLength = f.getInt("remoteAdminKeyLength");
        this.remoteAdminPort = f.getInt("remoteAdminPort");
        this.remoteAdminSessionKeyLength = f.getInt("remoteAdminSessionKeyLength");
    }

    public Boolean getEconomyEnabled() {
        return economyEnabled;
    }

    public Boolean getMayorsCanBuyTerritories() {
        return mayorsCanBuyTerritories;
    }

    public Boolean getAllowTownFriendlyFireManagement() {
        return allowTownFriendlyFireManagement;
    }

    public Boolean getLogCommands() {
        return logCommands;
    }

    public Boolean getPlayerCanJoinMultipleTowns() {
        return playerCanJoinMultipleTowns;
    }

    public Float getPricePerXZBlock() {
        return pricePerXZBlock;
    }

    public Integer getMinNumPlayersToBuyTerritory() {
        return minNumPlayersToBuyTerritory;
    }

    public Integer getBugReportPort() {
        return bugReportPort;
    }

    public Integer getRemoteAdminPort() {
        return remoteAdminPort;
    }

    public Integer getQuickSelectTool() {
        return quickSelectTool;
    }

    public Integer getRemoteAdminKeyLength() {
        return remoteAdminKeyLength;
    }

    public Integer getRemoteAdminSessionKeyLength() {
        return remoteAdminSessionKeyLength;
    }

    public String getBugReportHostname() {
        return bugReportHostname;
    }

    public void applyUpdates() {
        MCTowns.setBugReportHostname(bugReportHostname);
        MCTowns.setBugReportPort(bugReportPort);
        MCTowns.setEconomyIsEnabled(economyEnabled);
        MCTowns.setIsLoggingCommands(logCommands);
        MCTowns.setMayorsCanBuyTerritories(mayorsCanBuyTerritories);
        MCTowns.setMinNumPlayersToBuyTerritory(minNumPlayersToBuyTerritory);
        MCTowns.setPlayersCanJoinMultipleTowns(playerCanJoinMultipleTowns);
        MCTowns.setQuickSelectTool(Material.getMaterial(quickSelectTool));
        MCTowns.setRemoteAdminKeyLength(remoteAdminKeyLength);
        MCTowns.setRemoteAdminPort(remoteAdminPort);
        MCTowns.setRemoteAdminSessionKeyLength(remoteAdminSessionKeyLength);
        MCTowns.setTerritoryPricePerColumn(new BigDecimal(pricePerXZBlock));
        MCTownsPlugin.getPlugin().saveConfig();
    }
}
