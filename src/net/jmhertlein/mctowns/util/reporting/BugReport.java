package net.jmhertlein.mctowns.util.reporting;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
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

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.message);
        hash = 61 * hash + Arrays.deepHashCode(this.stackTrace);
        hash = 61 * hash + Objects.hashCode(this.ip);
        hash = 61 * hash + Objects.hashCode(this.options);
        hash = 61 * hash + Objects.hashCode(this.bukkitVersion);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BugReport other = (BugReport) obj;
        if (!Objects.equals(this.message, other.message))
            return false;
        if (!Arrays.deepEquals(this.stackTrace, other.stackTrace))
            return false;
        if (!Objects.equals(this.ip, other.ip))
            return false;
        if (!Objects.equals(this.options, other.options))
            return false;
        if (!Objects.equals(this.bukkitVersion, other.bukkitVersion))
            return false;
        return true;
    }
    
    
    
    
    

}
