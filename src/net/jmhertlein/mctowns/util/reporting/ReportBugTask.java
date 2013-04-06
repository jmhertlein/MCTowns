package net.jmhertlein.mctowns.util.reporting;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author joshua
 */
public class ReportBugTask implements Runnable {
    private static final Logger log = Bukkit.getLogger();
    private Plugin p;
    private Exception e;
    private String options;
    private String hostname;
    private int port;

    public ReportBugTask(Plugin p, Exception e, String options, String hostname, int port) {
        this.e = e;
        this.options = options;
        this.hostname = hostname;
        this.port = port;
        this.p = p;
    }

    @Override
    public void run() {
        BugReport report = new BugReport(p, Bukkit.getServer(), e, options);
        
        try (Socket s = new Socket(hostname, port); ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());) {
            oos.writeObject(report);
        } catch (UnknownHostException ex) {
            log.log(Level.INFO, "Unable to report bug; DNS lookup failed.");
            log.log(Level.INFO, "Here is the error report, please submit a ticket containing it:");
            log.log(Level.INFO, "================================================================");
            log.log(Level.INFO, report.toString());
            log.log(Level.INFO, "================================================================");
        } catch (IOException ex) {
            log.log(Level.INFO, "Unable to report bug, generic failure. Remote bug server is probably not running.");
            log.log(Level.INFO, "Here is the error report, please submit a ticket containing it:");
            log.log(Level.INFO, "================================================================");
            log.log(Level.INFO, report.toString());
            log.log(Level.INFO, "================================================================");
        }
    }
}
