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
package net.jmhertlein.mctowns;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.math.BigDecimal;
import java.util.logging.Level;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.util.ClockSource;
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
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("economy.economyEnabled");
    }

    public static boolean mayorsCanBuyTerritories() {
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("economy.mayorsCanBuyTerritories");
    }

    public static BigDecimal getTerritoryPricePerColumn() {
        return new BigDecimal(MCTownsPlugin.getPlugin().getConfig().getString("economy.pricePerXZBlock"));
    }

    public static int getMinNumPlayersToBuyTerritory() {
        return MCTownsPlugin.getPlugin().getConfig().getInt("economy.minNumPlayersToBuyTerritory");
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
        return MCTownsPlugin.getPlugin().getConfig().getString("bugReporting.hostname", "services.jmhertlein.net");
    }

    public static int getBugReportPort() {
        return MCTownsPlugin.getPlugin().getConfig().getInt("bugReporting.port", 9001);
    }

    public static String getConfigSummary() {
        return MCTownsPlugin.getPlugin().getConfig().saveToString();
    }

    public static void logInfo(String msg) {
        if(MCTownsPlugin.getPlugin() == null)
            System.out.println(msg);
        else
            MCTownsPlugin.getPlugin().getLogger().log(Level.INFO, msg);
    }

    public static void logWarning(String msg) {
        MCTownsPlugin.getPlugin().getLogger().log(Level.WARNING, msg);
    }

    public static void logSevere(String msg) {
        MCTownsPlugin.getPlugin().getLogger().log(Level.SEVERE, msg);
    }
    
    public static void logDebug(String msg) {
        if(MCTowns.getDebugModeEnabled())
            logInfo("[DEBUG]: " + msg);
    }

    public static Economy getEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        return economyProvider != null ? economyProvider.getProvider() : null;
    }

    public static WorldGuardPlugin getWorldGuardPlugin() {
        if (wgp != null)
            return wgp;

        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (p instanceof WorldGuardPlugin)
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
        MCTownsPlugin.getPlugin().getConfig().set("economy.economyEnabled", value);
    }

    public static void setMayorsCanBuyTerritories(boolean value) {
        MCTownsPlugin.getPlugin().getConfig().set("economy.mayorsCanBuyTerritories", value);
    }

    public static void setTerritoryPricePerColumn(BigDecimal val) {
        MCTownsPlugin.getPlugin().getConfig().set("economy.pricePerXZBlock", val.toPlainString());
    }

    public static void setMinNumPlayersToBuyTerritory(int n) {
        MCTownsPlugin.getPlugin().getConfig().set("economy.minNumPlayersToBuyTerritory", n);
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
        MCTownsPlugin.getPlugin().getConfig().set("bugReporting.hostName", s);
    }

    public static void setBugReportPort(int port) {
        MCTownsPlugin.getPlugin().getConfig().set("bugReporting.port", port);
    }

    public static void persistTownManager() {
        MCTownsPlugin.getPlugin().persistTownManager();
    }

    public static String getDefaultTown() {
        return MCTownsPlugin.getPlugin().getConfig().getString("defaultTown");
    }

    public static void setDefaultTown(String townName) {
        MCTownsPlugin.getPlugin().getConfig().set("defaultTown", townName);
    }

    public static boolean getDebugModeEnabled() {
        if(MCTownsPlugin.getPlugin() == null)
            return true;
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("debugModeEnabled", false);
    }

    public static boolean getTaxesEnabled() {
        return MCTownsPlugin.getPlugin().getConfig().getBoolean("economy.taxes.taxesEnabled", false);
    }

    public static void setTaxesEnabled(boolean value) {
        MCTownsPlugin.getPlugin().getConfig().set("economy.taxes.taxesEnabled", value);
    }

    public static double getMaxTaxAmount() {
        return MCTownsPlugin.getPlugin().getConfig().getDouble("economy.taxes.maxTaxAmount", 100);
    }

    public static void setMaxTaxAmount(double value) {
        MCTownsPlugin.getPlugin().getConfig().set("economy.taxes.maxTaxAmount", value);
    }

    public static int getMinTaxDelayInTicks() {
        return MCTownsPlugin.getPlugin().getConfig().getInt("economy.taxes.minTaxDelayInTicks", 1728000);
    }

    public static void setMinTaxDelayInTicks(int value) {
        MCTownsPlugin.getPlugin().getConfig().set("economy.taxes.minTaxDelayInTicks", value);
    }

    public static ClockSource getTaxClockSource() {
        return ClockSource.valueOf(MCTownsPlugin.getPlugin().getConfig().getString("advanced.taxClockSource", "minecraft").toUpperCase());
    }

    public static void setTaxClockSource(ClockSource value) {
        MCTownsPlugin.getPlugin().getConfig().set("advanced.taxClockSource", value.name().toLowerCase());
    }

    public static int getMaxTaxDelayInMinutes() {
        return MCTownsPlugin.getPlugin().getConfig().getInt("advanced.minTaxDelayInMinutes", 1440);
    }

    public static void setMaxTaxDelayInMinutes(int value) {
        MCTownsPlugin.getPlugin().getConfig().set("advanced.minTaxDelayInMinutes", value);
    }
}
