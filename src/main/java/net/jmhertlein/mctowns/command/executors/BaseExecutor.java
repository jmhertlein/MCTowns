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
package net.jmhertlein.mctowns.command.executors;

import java.util.HashMap;
import net.jmhertlein.core.command.ECommand;
import net.jmhertlein.core.reporting.bugs.BugReportingCommandExecutor;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.townjoin.TownJoinManager;
import net.jmhertlein.mctowns.MCTowns;
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
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        return executeCommand(cs, cmnd, string, strings);
    }
   
    public boolean executeCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if (parent.getConfig().getBoolean("logCommands"))
            MCTowns.logInfo("[Command]: Player: " + cs.getName() + " Command: " + new ECommand(string, strings));

        return runCommand(cs, cmnd, string, strings);
    }

    public abstract boolean runCommand(CommandSender cs, Command cmnd, String string, String[] strings);
}
