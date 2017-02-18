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

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cafe.josh.mctowns.bank.DepositInventoryEntry;
import cafe.josh.mctowns.listeners.DepositBoxCloseListener;
import cafe.josh.mctowns.listeners.MCTPlayerListener;
import cafe.josh.mctowns.listeners.QuickSelectToolListener;
import cafe.josh.mctowns.permission.Perms;
import cafe.josh.mctowns.townjoin.TownJoinManager;
import cafe.josh.mctowns.upgrade.ResourceUpgradePaths;
import cafe.josh.reflective.TreeCommandExecutor;
import cafe.josh.reflective.TreeTabCompleter;
import cafe.josh.mctowns.command.ActiveSet;
import cafe.josh.mctowns.command.MCTHandler;
import cafe.josh.mctowns.command.PlotHandler;
import cafe.josh.mctowns.command.TerritoryHandler;
import cafe.josh.mctowns.command.TownHandler;
import cafe.josh.mctowns.util.MCTConfig;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

/**
 * The main class of the MCTowns plugin.
 *
 * @author Joshua Michael Hertlein <jmhertlein@gmail.com>
 */
public class MCTownsPlugin extends JavaPlugin {

    private static MCTownsPlugin singleton;
    private File savesDir, configFile,
            metaFile;
    private static TownManager townManager;
    private TownJoinManager joinManager;
    private HashMap<String, ActiveSet> activeSets;
    private HashMap<Player, ActiveSet> potentialPlotBuyers;
    private Map<String, DepositInventoryEntry> openDepositInventories;
    private boolean abortSave;
    private Set<File> dataDirs, configFiles;
    private TreeCommandExecutor commands;

    /**
     * Persist any data that needs to be persisted.
     */
    @Override
    public void onDisable() {
        if(this.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            return;
        }

        try {
            saveWorldGuardWorlds();
        } catch(Exception ex) {
            MCTowns.logSevere("Error saving WG regions: " + ex.getLocalizedMessage());
        }

        if(!abortSave) {
            persistTownManager();
        } else {
            MCTowns.logInfo("The save was aborted manually, so nothing was saved.");
        }

        MCTowns.logInfo("[MCTowns]: MCTowns has been successfully disabled.");
        try {
            trimFiles();
        } catch(FileNotFoundException ex) {
            Logger.getLogger(MCTownsPlugin.class.getName()).log(Level.SEVERE, null, ex);
        } catch(IOException | InvalidConfigurationException ex) {
            Logger.getLogger(MCTownsPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

        //release as much memory as I can, to make reloads suck less.
        townManager = null;
        joinManager = null;
        activeSets = null;
        potentialPlotBuyers = null;
    }

    /**
     * Sets up files needed for persistence, registers listeners and
     * permissions, etc
     */
    @Override
    public void onEnable() {
        singleton = this;
        setupFiles();

        if(!hookInDependencies()) {
            return;
        }

        joinManager = new TownJoinManager();
        activeSets = new HashMap<>();
        openDepositInventories = new HashMap<>();
        if(MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            potentialPlotBuyers = new HashMap<>();
        }

        Perms.registerPermNodes(getServer().getPluginManager());
        setupTownManager();
        regEventListeners();
        setCommandExecutors();

        abortSave = false;

        startPeriodicSaveTask();
        startMetricsReporting();

        MCTowns.logInfo("MCTowns is now fully loaded.");
    }

    private void setupFiles() {
        saveDefaultConfig();
        savesDir = new File(this.getDataFolder(), "saves");

        configFile = new File(this.getDataFolder(), "config.yml");
        metaFile = new File(savesDir, ".meta.yml");

        dataDirs = new HashSet<>();
        dataDirs.add(savesDir);

        for(File f : dataDirs) {
            f.mkdirs();
        }

        configFiles = new HashSet<>();
        configFiles.add(configFile);
        configFiles.add(metaFile);

        try {
            if(!metaFile.exists()) {
                metaFile.createNewFile();
            }
        } catch(IOException ex) {
            MCTowns.logSevere("Error creating essential config file: " + ex.getMessage());
        }

        ResourceUpgradePaths.upgradeResources(this.getDataFolder(), this);
    }

    private void setupTownManager() {
        try {
            townManager = TownManager.readYAML(savesDir.getAbsolutePath());
        } catch(IOException | InvalidConfigurationException ex) {
            MCTowns.logWarning("MCTowns: Couldn't load the town database. Ignore if this is the first time the plugin has been run.");
            townManager = new TownManager();
        }
    }

    private boolean hookInDependencies() {
        Plugin wgp = this.getServer().getPluginManager().getPlugin("WorldGuard");
        if(wgp == null) {
            MCTowns.logSevere("========================================================");
            MCTowns.logSevere(" _   _  ____ _______ _____ _____ ______");
            MCTowns.logSevere("| \\ | |/ __ \\__   __|_   _/ ____|  ____|");
            MCTowns.logSevere("|  \\| | |  | | | |    | || |    | |__   ");
            MCTowns.logSevere("| . ` | |  | | | |    | || |    |  __|  ");
            MCTowns.logSevere("| |\\  | |__| | | |   _| || |____| |____ ");
            MCTowns.logSevere("|_| \\_|\\____/  |_|  |_____\\_____|______|");
            MCTowns.logSevere("");
            MCTowns.logSevere("You're missing the WorldGuard plugin. This is a required dependency. See this wiki page: https://github.com/jmhertlein/MCTowns/wiki/Download-Methods#dependencies");
            MCTowns.logSevere("");
            MCTowns.logSevere("=========================================================");
            this.getPluginLoader().disablePlugin(this);
            return false;
        }

        String wgVersion = wgp.getDescription().getVersion();
        MCTowns.logInfo("Hooked WorldGuard version " + wgVersion);

        /*
         * Crappy version detection but it's not like Bukkit or WG actually give us a format spec to
         * work with, so might as well try Version check failing warns only, though, since this is
         * bound to produce false positives.
         */
        if(!wgVersion.matches(MCTConfig.WG_VER_REGEX.getString())) {
            MCTowns.logWarning("======== WG VERSION WARNING ==========");
            MCTowns.logWarning("Your WorldGuard version might be unsupported!");
            MCTowns.logWarning("======== WG VERSION WARNING ==========");
        }

        if(MCTConfig.ECONOMY_ENABLED.getBoolean()) {
            try {
                boolean success = setupEconomy();
                if(!success) {
                    MCTowns.logSevere("MCTowns: Unable to hook-in to Vault (1)!");
                }
            } catch(Exception e) {
                MCTowns.logSevere("MCTowns: Unable to hook-in to Vault.");
                return false;
            }
        }

        return true;
    }

    private void regEventListeners() {
        MCTPlayerListener playerListener = new MCTPlayerListener(this);
        QuickSelectToolListener qsToolListener = new QuickSelectToolListener(MCTowns.getWorldGuardPlugin(), this);

        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(qsToolListener, this);
        getServer().getPluginManager().registerEvents(new DepositBoxCloseListener(openDepositInventories), this);
    }

    public void persistTownManager() {
        try {
            townManager.writeYAML(savesDir.getAbsolutePath());
        } catch(IOException ex) {
            MCTowns.logSevere("Error saving town database: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        } catch(NullPointerException npe) {
            MCTowns.logSevere("Error saving town database - database did not load correctly!");
        }
    }

    private boolean setupEconomy() {
        Economy economy = null;
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if(economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    private void setCommandExecutors() {
        TreeCommandExecutor tree = new TreeCommandExecutor();
        tree.add(new TownHandler(this));
        tree.add(new TerritoryHandler(this));
        tree.add(new PlotHandler(this));
        tree.add(new MCTHandler(this));

        TreeTabCompleter completer = new TreeTabCompleter(tree);
        PluginCommand[] cmnds = new PluginCommand[]{getCommand("mct"), getCommand("town"), getCommand("territory"), getCommand("plot")};
        for(PluginCommand c : cmnds) {
            c.setExecutor(tree);
            c.setTabCompleter(completer);
        }

        getCommand("mct").setExecutor(tree);
        getCommand("town").setExecutor(tree);
        getCommand("territory").setExecutor(tree);
        getCommand("plot").setExecutor(tree);

        this.commands = tree;
    }

    private void trimFiles() throws FileNotFoundException, IOException, InvalidConfigurationException {
        File root = savesDir;
        FileConfiguration fileConfig = new YamlConfiguration();
        fileConfig.load(metaFile);

        List<String> towns = fileConfig.getStringList("towns"),
                regions = fileConfig.getStringList("regions");

        for(File f : root.listFiles()) {
            //not really necessary since nothing but town files are in saves now, but... better safe.
            if(dataDirs.contains(f) || configFiles.contains(f)) {
                continue;
            }

            //snips off the ".yml" from the end of the files, so they'll match their region or town names again
            String regionName = f.getName().substring(0, f.getName().lastIndexOf('.'));

            if(!(towns.contains(regionName) || regions.contains(regionName))) {
                f.delete();
            }
        }
    }

    public boolean willAbortSave() {
        return abortSave;
    }

    public void setAbortSave(boolean abortSave) {
        this.abortSave = abortSave;
    }

    public TownManager getTownManager() {
        return townManager;
    }

    public TownJoinManager getJoinManager() {
        return joinManager;
    }

    public HashMap<String, ActiveSet> getActiveSets() {
        return activeSets;
    }

    public HashMap<Player, ActiveSet> getPotentialPlotBuyers() {
        return potentialPlotBuyers;
    }

    public Map<String, DepositInventoryEntry> getOpenDepositInventories() {
        return openDepositInventories;
    }

    private void saveWorldGuardWorlds() throws Exception {
        for(World w : this.getServer().getWorlds()) {
            MCTowns.getWorldGuardPlugin().getRegionManager(w).save();
        }
    }

    public static MCTownsPlugin getPlugin() {
        return singleton;
    }

    private void startPeriodicSaveTask() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                //MCTowns.logInfo("Saving...");
                MCTowns.persistTownManager();
                //MCTowns.logInfo("Saved.");
            }
        };

        //5 * 60 * 20 = 5 minutes worth of ticks
        //i.e. save every 5 mins
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, run, 5 * 60 * 20, 5 * 60 * 20);
    }

    public File getSavesDir() {
        return savesDir;
    }

    public Set<File> getConfigFiles() {
        return configFiles;
    }

    public Set<File> getDataDirs() {
        return dataDirs;
    }

    private void startMetricsReporting() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch(IOException ex) {
            MCTowns.logWarning("Error: Unable to start metrics reporting.");
        }
    }

    public TreeCommandExecutor getCommandExecutor() {
        return commands;
    }
}
