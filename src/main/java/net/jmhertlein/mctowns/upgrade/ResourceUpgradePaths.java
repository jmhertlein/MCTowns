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
package net.jmhertlein.mctowns.upgrade;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.database.TownManager;
import org.bukkit.configuration.InvalidConfigurationException;

/**
 *
 * @author Joshua Michael Hertlein <jmhertlein@gmail.com>
 */
public abstract class ResourceUpgradePaths {
    public static void upgradeResources(File rootDir, MCTownsPlugin p) {
        String installedVersion = p.getConfig().getString("installedVersion");
        
        //pre-2.2.0 didn't have this, so if it comes back null, let's assume we're on 2.1.0
        if(installedVersion == null) {
            installedVersion = "2.1.0";
        }
        
        switch(installedVersion) {
            case "2.1.0":
                upgradeFrom210To220(rootDir, p);
            default:
                p.getLogger().info("Resources are up to date.");
                break;
        }
    }

    private static void upgradeFrom210To220(File rootDir, MCTownsPlugin p) {
        p.getLogger().warning("Beginning resource migration from v2.1.0 to v2.2.0");
        
        TownManager tempManager;
        try {
            tempManager = TownManager.readYAML(rootDir);
        } catch (IOException | InvalidConfigurationException ex) {
            p.getLogger().severe("ERROR MIGRATING RESOURCES:");
            Logger.getLogger(ResourceUpgradePaths.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        try {
            tempManager.writeYAML(p.getSavesDir().getAbsolutePath());
        } catch (IOException ex) {
            p.getLogger().severe("ERROR MIGRATING RESOURCES:");
            Logger.getLogger(ResourceUpgradePaths.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for(File f : rootDir.listFiles()) {
            if(!p.getConfigFiles().contains(f)) {
                f.delete();
            }
        }
        
        p.getLogger().warning("Completed resource migration from v2.1.0 to v2.2.0");
    }
}
