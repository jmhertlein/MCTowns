package net.jmhertlein.mctowns;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import java.io.*;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.command.executors.MCTExecutor;
import net.jmhertlein.mctowns.command.executors.PlotExecutor;
import net.jmhertlein.mctowns.command.executors.TerritoryExecutor;
import net.jmhertlein.mctowns.command.executors.TownExecutor;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.listeners.MCTPlayerListener;
import net.jmhertlein.mctowns.listeners.MCTPvPListener;
import net.jmhertlein.mctowns.listeners.QuickSelectToolListener;
import net.jmhertlein.mctowns.permission.Perms;
import net.jmhertlein.mctowns.townjoin.TownJoinManager;
import net.jmhertlein.mctowns.util.Config;
import net.jmhertlein.mctowns.util.metrics.Metrics;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class of the MCTowns plugin.
 *
 * @author joshua
 */
public class MCTowns extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");
    private static final String MCT_DATA_FOLDER = "plugins" + File.separator + "MCTowns";
    private static final String TOWN_DATABASE_SAVE_PATH = MCT_DATA_FOLDER + File.separator + "MCTownsExternalTownDatabase.mct";
    private static final String BACKUP_TOWN_DATABASE_SAVE_PATH = MCT_DATA_FOLDER + File.separator + "MCTownsExternalTownDatabase.bak";
    private static final String MCT_TEXT_CONFIG_PATH = MCT_DATA_FOLDER + File.separator + "config.txt";
    private static final boolean DEBUGGING = true;
    private static TownManager townManager;
    private TownJoinManager joinManager;
    private HashMap<String, ActiveSet> activeSets;
    private static WorldGuardPlugin wgp;
    private static Economy economy;
    private static Config options;
    private HashMap<Player, ActiveSet> potentialPlotBuyers;
    private boolean abortSave;

    /**
     * Persist any data that needs to be persisted.
     */
    @Override
    public void onDisable() {
        try {
            saveWorldGuardWorlds();
        } catch (ProtectionDatabaseException ex) {
            logSevere("Error saving WG regions: " + ex.getLocalizedMessage());
        }
        
        if (!abortSave) {
            persistTownManager();
            persistTownManagerBackup();
        } else {
            logInfo("The save was aborted manually, so nothing was saved.");
        }

        log.info("[MCTowns]: MCTowns has been successfully disabled.");

        //release as much memory as I can, to make reloads suck less.
        townManager = null;
        joinManager = null;
        activeSets = null;
        economy = null;
        options = null;
        potentialPlotBuyers = null;
        wgp = null;
    }

    /**
     * Sets up files needed for persistence, registers listeners and
     * permissions, etc
     */
    @Override
    public void onEnable() {
        checkFiles();
        loadConfig();

        joinManager = new TownJoinManager();

        activeSets = new HashMap<>();

        if (options.isEconomyEnabled()) {
            potentialPlotBuyers = new HashMap<>();
        }

        Perms.registerPermNodes(getServer().getPluginManager());
        hookInDependencies();
        setupTownManager();
        regEventListeners();
        setCommandExecutors();

        abortSave = false;

        startMetricsCollection();

        log.info("MCTowns is now fully loaded.");

    }

    private void checkFiles() {
        ArrayDeque<File> files = new ArrayDeque<>();
        ArrayDeque<File> dirs = new ArrayDeque<>();

        //add dirs in descending path order
        dirs.add(new File(MCT_DATA_FOLDER));

        //add files
        files.add(new File(MCT_TEXT_CONFIG_PATH));

        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        for (File file : files) {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    if (file.getPath().equals(MCT_TEXT_CONFIG_PATH)) {
                        Config.resetConfigFileToDefault(MCT_TEXT_CONFIG_PATH);
                        log.log(Level.INFO, "Created a default config file.");
                    }
                } catch (IOException ex) {
                    log.log(Level.WARNING, "MCTowns: Unable to create necessary files. Will not save.");
                }
            }
        }
    }

    private void setupTownManager() {
        try {
            townManager = TownManager.readYAML(MCT_DATA_FOLDER);
        } catch (IOException | InvalidConfigurationException ex) {
            log.log(Level.WARNING, "MCTowns: Couldn't load the town database. Ignore if this is the first time the plugin has been run.");
            logInfo("If this was NOT expected, make sure you run the command /mct togglesave to make sure that you don't destroy your saves!");
            townManager = new TownManager();
        }
    }

    private void hookInDependencies() {
        try {
            wgp = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
        } catch (Exception e) {
            log.log(Level.SEVERE, "[MCTowns] Error occurred in hooking in to WorldGuard. Is both WorldGuard and WorldEdit installed?");
            log.log(Level.SEVERE, "[MCTowns] !!!!!NOTICE!!!!! MCTOWNS WILL NOW BE DISABLED.  !!!!!NOTICE!!!!!");
            this.getPluginLoader().disablePlugin(this);
        }

        if (options.isEconomyEnabled()) {
            try {
                boolean success = setupEconomy();
                if (!success) {
                    log.log(Level.SEVERE, "MCTowns: Unable to hook-in to Vault (1)!");
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "MCTowns: Unable to hook-in to Vault.");
            }
        }
    }

    private void regEventListeners() {
        MCTPlayerListener playerListener = new MCTPlayerListener(this);
        MCTPvPListener townPvPListener = new MCTPvPListener(townManager, options);
        QuickSelectToolListener qsToolListener = new QuickSelectToolListener(wgp, this);

        //configure the tool listener as per the config
        QuickSelectToolListener.SELECT_TOOL = options.getQsTool();

        if (options.allowsTownFriendlyFireManagement()) {
            getServer().getPluginManager().registerEvents(townPvPListener, this);
        }

        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(qsToolListener, this);
    }

    private void loadConfig() {
        options = new Config(MCT_TEXT_CONFIG_PATH);

        if (options.badConfig()) {
            logSevere(options.getFailReason());
        }
    }

    private void persistTownManager() {
        try {
            townManager.writeYAML(MCT_DATA_FOLDER);
        } catch (IOException ex) {
            MCTowns.logSevere("Error saving town database.");
        }
    }

    private void persistTownManagerBackup() {
        Calendar cal = Calendar.getInstance();
        String dateStamp = "(" + (cal.get(Calendar.MONTH) + 1) + cal.get(Calendar.DAY_OF_MONTH) + ")";

        File path = new File(BACKUP_TOWN_DATABASE_SAVE_PATH + dateStamp);

        if (!path.exists()) {
            try {
                path.createNewFile();
            } catch (Exception ignore) {
            }
        }

        logInfo("Backup saving as: " + path.getAbsolutePath());
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    private void setCommandExecutors() {
        getCommand("mct").setExecutor(new MCTExecutor(this));
        getCommand("town").setExecutor(new TownExecutor(this));
        getCommand("territory").setExecutor(new TerritoryExecutor(this));
        getCommand("plot").setExecutor(new PlotExecutor(this));
    }

    public static void logSevere(String msg) {
        log.log(Level.SEVERE, "[MCTowns]: " + msg);
    }

    public static void logInfo(String msg) {
        log.log(Level.INFO, "[MCTowns]: " + msg);
    }

    public static void logDebug(String msg) {
        if (DEBUGGING) {
            logInfo("[DEBUG]:" + msg);
        }
    }

    /**
     * Logged assertion. If the assertion passes, nothing happens. If it fails,
     * a warning is printed to the log.
     *
     * @param bool the boolean expression to assert
     * @param desc a description of the assertion to be printed to the log file.
     * i.e. where it is, what it is, what its failure implies, etc
     */
    public static void logAssert(boolean bool, String desc) {
        if (!bool) {
            logDebug("WARNING: ASSERTION FAILED: " + desc);
        }
    }

    public boolean willAbortSave() {
        return abortSave;
    }

    public void setAbortSave(boolean abortSave) {
        this.abortSave = abortSave;
    }

    public static Config getOptions() {
        return options;
    }

    public static TownManager getTownManager() {
        return townManager;
    }

    public TownJoinManager getJoinManager() {
        return joinManager;
    }

    public HashMap<String, ActiveSet> getActiveSets() {
        return activeSets;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public HashMap<Player, ActiveSet> getPotentialPlotBuyers() {
        return potentialPlotBuyers;
    }

    public static WorldGuardPlugin getWgp() {
        return wgp;
    }

    private void startMetricsCollection() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            logSevere("Unable to submit plugin information. Please let everdras@gmail.com know. Thanks!");
        }

        MCTowns.logDebug("Metrics reporting started.");
    }

    private void saveWorldGuardWorlds() throws ProtectionDatabaseException {
        for(World w : this.getServer().getWorlds()) {
            getWgp().getRegionManager(w).save();
        }
    }
}
