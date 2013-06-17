package net.jmhertlein.mctowns.command.executors;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.HashMap;
import net.jmhertlein.core.command.ECommand;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.townjoin.TownJoinManager;
import net.jmhertlein.core.reporting.BugReportingCommandExecutor;
import net.jmhertlein.mctowns.MCTowns;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Joshua
 */
public abstract class BaseExecutor extends BugReportingCommandExecutor {


    protected MCTownsPlugin parent;
    protected TownManager townManager;
    protected TownJoinManager joinManager;
    protected HashMap<String, ActiveSet> activeSets;
    protected HashMap<Player, ActiveSet> potentialPlotBuyers;

    public BaseExecutor(MCTownsPlugin parent) {
        this.parent = parent;
        this.townManager = MCTowns.getTownManager();
        this.joinManager = parent.getJoinManager();
        this.activeSets = parent.getActiveSets();
        this.potentialPlotBuyers = parent.getPotentialPlotBuyers();
    }



    @Override
    public boolean executeCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if (parent.getConfig().getBoolean("logCommands"))
            MCTowns.logInfo("[Command]: Player: " + cs.getName() + " Command: " + new ECommand(string, strings));

        return runCommand(cs, cmnd, string, strings);
    }

    public abstract boolean runCommand(CommandSender cs, Command cmnd, String string, String[] strings);

    @Override
    protected String getEnvOptions() {
        return MCTowns.getConfigSummary();
    }

    @Override
    protected String getHostname() {
        return MCTowns.getBugReportHostname();
    }

    @Override
    protected int getPort() {
        return MCTowns.getBugReportPort();
    }

    @Override
    protected Plugin getPlugin() {
        return parent;
    }
}
