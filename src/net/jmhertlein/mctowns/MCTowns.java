package net.jmhertlein.mctowns;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import net.jmhertlein.mctowns.remote.server.RemoteConnectionServer;
import net.jmhertlein.mctowns.townjoin.TownJoinManager;
import net.jmhertlein.mctowns.util.Config;
import net.jmhertlein.mctowns.util.metrics.Metrics;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class of the MCTowns plugin.
 *
 * @author joshua
 */
public class MCTowns extends JavaPlugin {

    public static final String TOWNS_SAVE_DIR_NAME = "saves",
            RSA_KEYS_DIR_NAME = "rsa_keys",
            AUTH_KEYS_DIR_NAME = "auth_keys",
            TEXT_CONFIG_FILE_NAME = "config.txt",
            META_TOWN_YAML_FILE_NAME = ".meta.yml";
    public static final Logger log = Logger.getLogger("Minecraft");
    private static final String MCT_TEXT_CONFIG_PATH = "plugins" + File.separator + "MCTowns" + File.separator + "config.txt";
    private static final boolean DEBUGGING = false;
    private static TownManager townManager;
    private TownJoinManager joinManager;
    private HashMap<String, ActiveSet> activeSets;
    private static WorldGuardPlugin wgp;
    private static Economy economy;
    private static Config options;
    private HashMap<Player, ActiveSet> potentialPlotBuyers;
    private boolean abortSave;
    private Set<File> dataDirs, configFiles;

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
        } else {
            logInfo("The save was aborted manually, so nothing was saved.");
        }

        log.info("[MCTowns]: MCTowns has been successfully disabled.");
        try {
            trimFiles();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MCTowns.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(MCTowns.class.getName()).log(Level.SEVERE, null, ex);
        }

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

        startRemoteServer();

        log.info("MCTowns is now fully loaded.");

    }

    private void checkFiles() {
        this.saveDefaultConfig();
        dataDirs = new HashSet<>();
        dataDirs.add(new File(this.getDataFolder(), RSA_KEYS_DIR_NAME));
        dataDirs.add(new File(this.getDataFolder(), TOWNS_SAVE_DIR_NAME));
        dataDirs.add(new File(this.getDataFolder(), AUTH_KEYS_DIR_NAME));

        for (File f : dataDirs)
            f.mkdirs();

        configFiles = new HashSet<>();
        configFiles.add(new File(this.getDataFolder(), TEXT_CONFIG_FILE_NAME));
        configFiles.add(new File(this.getDataFolder(),"config.yml"));
        

        for (File f : configFiles) {
            if (!f.exists())
                try {
                    f.createNewFile();
                    if (f.getName().equals(TEXT_CONFIG_FILE_NAME)) {
                        Config.resetConfigFileToDefault(f);
                        log.log(Level.INFO, "Created a default config file.");
                    }
                } catch (IOException ex) {
                    System.err.println("Error creating empty config file.");
                    Logger.getLogger(MCTowns.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }

    private void setupTownManager() {
        try {
            townManager = TownManager.readYAML(this.getDataFolder().getPath());
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
        saveDefaultConfig();
        options = new Config(MCT_TEXT_CONFIG_PATH);

        if (options.badConfig()) {
            logSevere(options.getFailReason());
        }
    }

    private void persistTownManager() {
        try {
            townManager.writeYAML(this.getDataFolder().getPath());
        } catch (IOException ex) {
            MCTowns.logSevere("Error saving town database: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
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

    private void trimFiles() throws FileNotFoundException, IOException, InvalidConfigurationException {
        File root = this.getDataFolder();
        File meta = new File(root, META_TOWN_YAML_FILE_NAME);
        FileConfiguration fileConfig = new YamlConfiguration();
        fileConfig.load(meta);

        List<String> towns = fileConfig.getStringList("towns"),
                regions = fileConfig.getStringList("regions");

        for (File f : root.listFiles()) {
            if (f.getName().equals(META_TOWN_YAML_FILE_NAME) || dataDirs.contains(f) || configFiles.contains(f))
                continue;

            String trunc = f.getName().substring(0, f.getName().lastIndexOf('.'));

            if (!(towns.contains(trunc) || regions.contains(trunc))) {
                f.delete();
            }
        }

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
        for (World w : this.getServer().getWorlds()) {
            getWgp().getRegionManager(w).save();
        }
    }

    public static boolean isDebugging() {
        return DEBUGGING;
    }

    private void startRemoteServer() {
        RemoteConnectionServer s;
        try {
            s = new RemoteConnectionServer(this, new File(this.getDataFolder(), "auth_keys"));
        } catch (IOException ex) {
            Logger.getLogger(MCTowns.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        s.start();
    }
}
