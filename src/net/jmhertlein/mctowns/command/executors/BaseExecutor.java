/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.command.executors;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.HashMap;
import net.jmhertlein.core.command.ECommand;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.townjoin.TownJoinManager;
import net.jmhertlein.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Joshua
 */
public abstract class BaseExecutor implements CommandExecutor {

    protected MCTowns parent;
    protected TownManager townManager;
    protected TownJoinManager joinManager;
    protected HashMap<String, ActiveSet> activeSets;
    protected static WorldGuardPlugin wgp = MCTowns.getWgp();
    protected static Economy economy = MCTowns.getEconomy();
    protected static Config options = MCTowns.getOptions();
    protected HashMap<Player, ActiveSet> potentialPlotBuyers;

    public BaseExecutor(MCTowns parent) {
        this.parent = parent;
        this.townManager = parent.getTownManager();
        this.joinManager = parent.getJoinManager();
        this.activeSets = parent.getActiveSets();
        this.potentialPlotBuyers = parent.getPotentialPlotBuyers();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(options.isLoggingCommands())
            MCTowns.logInfo("[Command]: Player: " + cs.getName() + " Command: " + new ECommand(string, strings));
        return true;
    }


}
