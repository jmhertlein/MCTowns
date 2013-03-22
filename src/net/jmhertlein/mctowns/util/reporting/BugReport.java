package net.jmhertlein.mctowns.util.reporting;

import java.io.Serializable;
import net.jmhertlein.mctowns.util.Config;
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
    private String options;
    private String bukkitVersion;
    
    public BugReport(Server s, Exception e, Config o) {
       ip = s.getIp();
       stackTrace = e.getStackTrace();
       message = e.getMessage();
       options = o.toString();
       bukkitVersion = s.getBukkitVersion();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IP:");
        sb.append(ip);
        sb.append('\n');
        
        sb.append("CB Version:");
        sb.append(bukkitVersion);
        sb.append('\n');
        
        sb.append("Config:");
        sb.append(options.toString());
        sb.append('\n');
        
        sb.append("ErrMessage:");
        sb.append(message);
        sb.append('\n');
        
        sb.append("Call Stack:\n");
        for(StackTraceElement e : stackTrace) {
            sb.append(e.toString());
            sb.append('\n');
        }
        sb.append('\n');
        
        sb.append("End\n");
        
        return sb.toString();
        
    }
    

}
