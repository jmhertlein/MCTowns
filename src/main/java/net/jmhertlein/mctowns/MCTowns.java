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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.io.File;
import java.math.BigDecimal;
import java.util.logging.Level;
import net.jmhertlein.mctowns.database.TownManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author joshua
 */
public class MCTowns {
    private static WorldGuardPlugin wgp;
    
    public static boolean economyIsEnabled() {
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("economyEnabled");
    }
    
    public static boolean mayorsCanBuyTerritories() {
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("mayorsCanBuyTerritories");
    }
    
    public static BigDecimal getTerritoryPricePerColumn() {
        return new BigDecimal(MCTownsPlugin.getPlugin().getConfig().getString("pricePerXZBlock"));
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
    
    public static Economy getEconomy() {
        RegisteredServiceProvider<Economy> 
                economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        
        return economyProvider != null ? economyProvider.getProvider() : null;
    }
    
    public static WorldGuardPlugin getWorldGuardPlugin() {
        if(wgp != null)
            return wgp;
        
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if(p instanceof WorldGuardPlugin)
            wgp = (WorldGuardPlugin) p;
        else
            wgp = null;
        
        return wgp;
    }
    
    public static TownManager getTownManager() {
        return MCTownsPlugin.getPlugin().getTownManager();
    }
    
    public static FileConfiguration getConfig() {
        return MCTownsPlugin.getPlugin().getConfig();
    }
    
    public static void setEconomyIsEnabled(boolean value) {
        MCTownsPlugin.getPlugin().getConfig().set("economyEnabled", value);
    }
    
    public static void setMayorsCanBuyTerritories(boolean value) {
        MCTownsPlugin.getPlugin().getConfig().set("mayorsCanBuyTerritories", value);
    }
    
    public static void setTerritoryPricePerColumn(BigDecimal val) {
        MCTownsPlugin.getPlugin().getConfig().set("pricePerXZBlock", val.toPlainString());
    }
    
    public static void setMinNumPlayersToBuyTerritory(int n) {
        MCTownsPlugin.getPlugin().getConfig().set("minNumPlayersToBuyTerritory", n);
    }
    
    public static void setQuickSelectTool(Material m) {
        MCTownsPlugin.getPlugin().getConfig().set("quickSelectTool", m.getId());
    }
    
    public static void setIsLoggingCommands(boolean value) {
        MCTownsPlugin.getPlugin().getConfig().set("logCommands", value);
    }
    
    public static void setPlayersCanJoinMultipleTowns(boolean value) {
        MCTownsPlugin.getPlugin().getConfig().set("playersCanJoinMultipleTowns", value);
    }
    
    public static void setBugReportHostname(String s) {
        MCTownsPlugin.getPlugin().getConfig().set("bugReportHostName", s);
    }
    
    public static void setBugReportPort(int port) {
        MCTownsPlugin.getPlugin().getConfig().set("bugReportPort", port);
    }
    
    public static void setRemoteAdminServerIsEnabled(boolean value) {
        MCTownsPlugin.getPlugin().getConfig().set("remoteAdminServerEnabled", value);
    }
    
    public static void setRemoteAdminKeyLength(int ln) {
        MCTownsPlugin.getPlugin().getConfig().set("remoteAdminKeyLength", ln);
    }
    
    public static void setRemoteAdminSessionKeyLength(int ln) {
        MCTownsPlugin.getPlugin().getConfig().set("remoteAdminSessionKeyLength", ln);
    }
    
    public static void setRemoteAdminPort(int port) {
        MCTownsPlugin.getPlugin().getConfig().set("remoteAdminPort", port);
    }
    
    public static File getServerKeysDir() {
        return MCTownsPlugin.getPlugin().getServerKeysDir();
    }
    
    public static File getAuthKeysDir() {
        return MCTownsPlugin.getPlugin().getAuthKeysDir();
    }
}
