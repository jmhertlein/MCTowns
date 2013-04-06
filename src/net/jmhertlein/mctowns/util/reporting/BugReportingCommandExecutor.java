package net.jmhertlein.mctowns.util.reporting;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author joshua
 */
public abstract class BugReportingCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        try {
            return executeCommand(cs, cmnd, string, strings);
        } catch (Exception e) {
            reportBug(getPlugin(), e, getEnvOptions());

            //tell the player what happened!
            cs.sendMessage(ChatColor.RED + "An internal error occurred while running the command. A bug report has been automatically sent.");
            return true;
        }


    }

    private void reportBug(Plugin p, Exception e, String envOptions) {
        new Thread(new ReportBugTask(p, e, envOptions, getHostname(), getPort())).start();
    }

    public abstract boolean executeCommand(CommandSender cs, Command cmnd, String string, String[] strings);
    
    protected abstract String getEnvOptions();
    
    protected abstract String getHostname();
    
    protected abstract int getPort();
    
    protected abstract Plugin getPlugin();

}
