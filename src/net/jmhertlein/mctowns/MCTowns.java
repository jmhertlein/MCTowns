/*
 * Copyright (C) 2013 joshua
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
package net.jmhertlein.mctowns;

import java.math.BigDecimal;
import java.util.logging.Level;
import org.bukkit.Material;

/**
 *
 * @author joshua
 */
public class MCTowns {
    public static boolean economyIsEnabled() {
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("economyEnabled");
    }
    
    public static boolean mayorsCanBuyTerritories() {
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("mayorsCanBuyTerritories");
    }
    
    public static BigDecimal getTerritoryPricePerColumn() {
        return new BigDecimal(MCTownsPlugin.getPlugin().getConfig().getInt("pricePerXZBlock"));
    }
    
    public static int getMinNumPlayersToBuyTerritory() {
        return MCTownsPlugin.getPlugin().getConfig().getInt("minNumPlayersToBuyTerritory");
    }
    
    public static Material getQuickSelectTool() {
        return Material.getMaterial(MCTownsPlugin.getPlugin().getConfig().getInt("quickSelectTool"));
    }
    
    public static boolean isLoggingCommands() {
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("logCommands");
    }
    
    public static boolean playersCanJoinMultipleTowns() {
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("playersCanJoinMultipleTowns");
    }
    
    public static String getBugReportHostname() {
        return MCTownsPlugin.getPlugin().getConfig().getString("bugReportHostName");
    }
    
    public static int getBugReportPort() {
        return MCTownsPlugin.getPlugin().getConfig().getInt("bugReportPort");
    }
    
    public static boolean remoteAdminServerIsEnabled() {
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("remoteAdminServerEnabled");
    }
    
    public static int getRemoteAdminKeyLength() {
        return MCTownsPlugin.getPlugin().getConfig().getInt("remoteAdminKeyLength");
    }
    
    public static int getRemoteAdminSessionKeyLength() {
        return MCTownsPlugin.getPlugin().getConfig().getInt("remoteAdminSessionKeyLength");
    }
    
    public static int getRemoteAdminPort() {
        return MCTownsPlugin.getPlugin().getConfig().getInt("remoteAdminPort");
    }

    public static String getConfigSummary() {
        //TODO: implement this
        return "";
    }
    
    public static void logInfo(String msg) {
        MCTownsPlugin.getPlugin().getLogger().log(Level.INFO, msg);
    }
    
    public static void logWarning(String msg) {
        MCTownsPlugin.getPlugin().getLogger().log(Level.WARNING, msg);
    }
    
    public static void logSevere(String msg) {
        MCTownsPlugin.getPlugin().getLogger().log(Level.SEVERE, msg);
    }
}
