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
package cafe.josh.mctowns;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.logging.Level;
import cafe.josh.mctowns.util.MCTConfig;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author joshua
 */
public class MCTowns {
    private static WorldGuardPlugin wgp;

    public static String getConfigSummary() {
        return MCTownsPlugin.getPlugin().getConfig().saveToString();
    }

    public static void logInfo(String msg) {
        if(MCTownsPlugin.getPlugin() == null) {
            System.out.println(msg);
        } else {
            MCTownsPlugin.getPlugin().getLogger().log(Level.INFO, msg);
        }
    }

    public static void logWarning(String msg) {
        MCTownsPlugin.getPlugin().getLogger().log(Level.WARNING, msg);
    }

    public static void logSevere(String msg) {
        MCTownsPlugin.getPlugin().getLogger().log(Level.SEVERE, msg);
    }

    public static void logDebug(String msg) {
        if(MCTConfig.DEBUG_MODE_ENABLED.getBoolean()) {
            logInfo("[DEBUG]: " + msg);
        }
    }

    public static Economy getEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        return economyProvider != null ? economyProvider.getProvider() : null;
    }

    public static WorldGuardPlugin getWorldGuardPlugin() {
        if(wgp != null) {
            return wgp;
        }

        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if(p instanceof WorldGuardPlugin) {
            wgp = (WorldGuardPlugin) p;
        } else {
            wgp = null;
        }

        return wgp;
    }

    public static TownManager getTownManager() {
        return MCTownsPlugin.getPlugin().getTownManager();
    }

    public static void persistTownManager() {
        MCTownsPlugin.getPlugin().persistTownManager();
    }
}
