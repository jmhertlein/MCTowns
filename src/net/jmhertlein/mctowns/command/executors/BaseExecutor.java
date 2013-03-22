/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.command.executors;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.CipherOutputStream;
import net.jmhertlein.core.command.ECommand;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.townjoin.TownJoinManager;
import net.jmhertlein.mctowns.util.BugReport;
import net.jmhertlein.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        if (options.isLoggingCommands())
            MCTowns.logInfo("[Command]: Player: " + cs.getName() + " Command: " + new ECommand(string, strings));

        try {
            return executeCommand(cs, cmnd, string, strings);
        } catch (Exception e) {




            //tell the player what happened!
            cs.sendMessage(ChatColor.RED + "An internal error occurred while running the command. A bug report has been automatically sent to the developer.");
            return true;
        }


    }

    private void reportBug(Exception e) throws IOException, UnknownHostException {
        BugReport report = new BugReport(Bukkit.getServer(), e, options);

        Socket s = new Socket("services.jmhertlein.net", 9001);
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        oos.writeObject(report);
        oos.close();
        s.close();
    }

    public abstract boolean executeCommand(CommandSender cs, Command cmnd, String string, String[] strings);
}
