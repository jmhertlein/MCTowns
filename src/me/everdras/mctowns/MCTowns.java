package me.everdras.mctowns;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.io.*;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.command.handlers.CommandHandler;
import me.everdras.mctowns.command.MCTCommand;
import me.everdras.mctowns.command.executors.MCTExecutor;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.listeners.MCTPlayerListener;
import me.everdras.mctowns.listeners.MCTPvPListener;
import me.everdras.mctowns.permission.Perms;
import me.everdras.mctowns.structure.TownLevel;
import me.everdras.mctowns.townjoin.TownJoinManager;
import me.everdras.mctowns.townjoin.TownJoinMethod;
import me.everdras.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class of the MCTowns plugin.
 *
 * @author joshua
 */
public class MCTowns extends JavaPlugin {

    /**
     * The logger for Minecraft.
     */
    public static final Logger log = Logger.getLogger("Minecraft");
    private static final String MCT_DATA_FOLDER = "plugins" + File.separator + "MCTowns";
    private static final String TOWN_DATABASE_SAVE_PATH = MCT_DATA_FOLDER + File.separator + "MCTownsExternalTownDatabase.mct";
    private static final String BACKUP_TOWN_DATABASE_SAVE_PATH = MCT_DATA_FOLDER + File.separator + "MCTownsExternalTownDatabase.bak";
    private static final String MCT_TEXT_CONFIG_PATH = MCT_DATA_FOLDER + File.separator + "config.txt";
    private TownManager townManager;
    private TownJoinManager joinManager;
    private HashMap<String, ActiveSet> activeSets;
    private static WorldGuardPlugin wgp;
    private static Economy economy;
    private static Config options;
    private HashMap<Player, ActiveSet> potentialPlotBuyers;

    /**
     * Persist any data that needs to be persisted.
     */
    @Override
    public void onDisable() {
        serializeTownManager();
        serializeBackup();
        log.info("[MCTowns]: MCTowns has been successfully disabled.");

        //release as much memory as I can, to make reloads suck less.
        townManager = null;
        joinManager = null;
        activeSets = null;
        economy = null;
        options = null;
        potentialPlotBuyers = null;
    }

    /**
     * Sets up files needed for persistence, registers listeners and
     * permissions, etc
     */
    @Override
    public void onEnable() {
        log.log(Level.INFO, "MCTowns is now setting up...");

        checkFiles();
        loadConfig();

        setupTownManager();

        joinManager = new TownJoinManager(townManager);

        activeSets = new HashMap<>();

        if (options.isEconomyEnabled()) {
            potentialPlotBuyers = new HashMap<>();
        }

        Perms.registerPermNodes(getServer().getPluginManager());

        hookInDependencies();

        regEventListeners();

        setCommandExecutors();

        log.info("MCTowns is now fully loaded.");

    }


    private void checkFiles() {


        ArrayDeque<File> files = new ArrayDeque<>();
        ArrayDeque<File> dirs = new ArrayDeque<>();

        //add dirs in descending path order
        dirs.add(new File(MCT_DATA_FOLDER));

        //add files
        files.add(new File(TOWN_DATABASE_SAVE_PATH));
        files.add(new File(MCT_TEXT_CONFIG_PATH));
        files.add(new File(BACKUP_TOWN_DATABASE_SAVE_PATH));

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
        File path = new File(TOWN_DATABASE_SAVE_PATH);

        FileInputStream fis;
        ObjectInputStream ois;

        try {
            fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);

            townManager = new TownManager();
            townManager.readExternal(ois);

            ois.close();
            fis.close();

        } catch (Exception e) {
            log.log(Level.WARNING, "MCTowns: Couldn't load the town database. Ignore if this is the first time the plugin has been run.");
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
        MCTPlayerListener playerListener = new MCTPlayerListener(townManager, joinManager, options, economy, potentialPlotBuyers);
        MCTPvPListener townPvPListener = new MCTPvPListener(townManager, options);

        if (options.allowsTownFriendlyFireManagement()) {
            getServer().getPluginManager().registerEvents(townPvPListener, this);
        }

        getServer().getPluginManager().registerEvents(playerListener, this);

        //TODO: Fix this, see ticket no. 13
        //getServer().getPluginManager().registerEvent(Type.PLAYER_RESPAWN, respawnListener, Priority.Monitor, this);
    }

    private void loadConfig() {
        options = new Config(MCT_TEXT_CONFIG_PATH);

        if (options.badConfig()) {
            logSevere(options.getFailReason());
            logSevere("Couldn't load the config! DISABLING MCTOWNS.");
            this.getPluginLoader().disablePlugin(this);
        }
    }

    private void serializeTownManager() {
        File path = new File(TOWN_DATABASE_SAVE_PATH);

        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);

            townManager.writeExternal(oos);

            oos.close();
            fos.close();

        } catch (IOException e) {
            log.log(Level.WARNING, "MCTowns: Error saving the town database.");


        }




    }

    private void serializeBackup() {

        File path = new File(BACKUP_TOWN_DATABASE_SAVE_PATH);

        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);

            townManager.writeExternal(oos);

            oos.close();
            fos.close();

        } catch (IOException e) {
            log.log(Level.WARNING, "Error saving the town database backup.");
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



        getCommand("mct").setExecutor(new MCTExecutor(this, wgp, economy, options, townManager, joinManager, activeSets, potentialPlotBuyers));
    }


    public static void logSevere(String msg) {
        log.log(Level.SEVERE, "[MCTowns]: " + msg);
    }

    public static void logInfo(String msg) {
        log.log(Level.INFO, "[MCTowns]: " + msg);
    }
}
