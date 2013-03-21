package net.jmhertlein.mctowns.util;

import java.io.Serializable;
import org.bukkit.Server;

/**
 *
 * @author joshua
 */
public class BugReport implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String message;
    private StackTraceElement[] stackTrace;
    private String ip;
    private Config options;
    private String bukkitVersion;
    
    public BugReport(Server s, Exception e, Config o) {
       ip = s.getIp();
       stackTrace = e.getStackTrace();
       message = e.getMessage();
       options = o;
       bukkitVersion = s.getBukkitVersion();
    }
    

}
