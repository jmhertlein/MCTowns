/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command.executors;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.HashMap;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.command.handlers.MCTHandler;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.townjoin.TownJoinManager;
import me.everdras.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Joshua
 */
public abstract class BaseExecutor implements CommandExecutor {
    protected MCTowns parent;
    protected TownManager townManager;
    protected TownJoinManager joinManager;
    protected HashMap<String, ActiveSet> activeSets;
    protected static WorldGuardPlugin wgp;
    protected static Economy economy;
    protected static Config options;
    protected HashMap<Player, ActiveSet> potentialPlotBuyers;

    public BaseExecutor(MCTowns parent, WorldGuardPlugin wgp, Economy economy, Config options, TownManager townManager, TownJoinManager joinManager, HashMap<String, ActiveSet> activeSets, HashMap<Player, ActiveSet> potentialPlotBuyers) {
        if(BaseExecutor.options == null)
            BaseExecutor.options = options;

        if(BaseExecutor.wgp == null)
            BaseExecutor.wgp = wgp;

        if(BaseExecutor.economy == null)
            BaseExecutor.economy = economy;

        this.townManager = townManager;
        this.joinManager = joinManager;
        this.activeSets = activeSets;
        this.potentialPlotBuyers = potentialPlotBuyers;
        this.parent = parent;
    }







}
