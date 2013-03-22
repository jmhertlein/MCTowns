package net.jmhertlein.mctowns.util.reporting;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.util.Config;
import org.bukkit.Bukkit;

/**
 *
 * @author joshua
 */
public class ReportBugTask implements Runnable {
    private Exception e;
    private Config options;

    public ReportBugTask(Exception e, Config o) {
        this.e = e;
        options = o;
    }

    @Override
    public void run() {
        BugReport report = new BugReport(Bukkit.getServer(), e, options);
        
        try (Socket s = new Socket(options.getBugReportHostname(), options.getPort()); ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());) {
            oos.writeObject(report);
        } catch (UnknownHostException ex) {
            MCTowns.logInfo("Unable to report bug; DNS lookup failed.");
        } catch (IOException ex) {
            MCTowns.logInfo("Unable to report bug, generic failure. Remote bug server is probably not running.");
        }
    }
}
