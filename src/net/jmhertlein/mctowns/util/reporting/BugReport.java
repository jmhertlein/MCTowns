package net.jmhertlein.mctowns.util.reporting;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.util.Config;
import org.bukkit.Server;

/**
 *
 * @author joshua
 */
public class BugReport implements Serializable {
    private static final long serialVersionUID = 1L;
    
    //CB stuff
    private String message;
    private StackTraceElement[] stackTrace;
    private String ip;
    private String options;
    private String bukkitVersion;
    private String mctVersion;
    
    //java stuff
    private String jreVendor;
    private String jreVersion;
    
    //OS stuff
    private String osName, osArch, osVersion;
    
    public BugReport(MCTowns plugin, Server s, Exception e, Config o) {
       ip = s.getIp();
       stackTrace = e.getStackTrace();
       message = e.getMessage();
       options = o.toString();
       bukkitVersion = s.getBukkitVersion();
       mctVersion = plugin.getDescription().getVersion();
       
       Properties p = System.getProperties();
       
       jreVendor = p.getProperty("java.vendor");
       jreVersion = p.getProperty("java.version");
       
       osArch = p.getProperty("os.arch");
       osVersion = p.getProperty("os.version");
       osName = p.getProperty("os.name");
       
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=====================Begin====================\n");
        sb.append("IP:");
        sb.append(ip);
        sb.append('\n');
        
        sb.append("CB Version:");
        sb.append(bukkitVersion);
        sb.append('\n');
        
        sb.append("MCTVersion:");
        sb.append(mctVersion);
        sb.append('\n');
        
        sb.append("Config:");
        sb.append(options.toString());
        sb.append('\n');
        
        sb.append("ErrMessage:");
        sb.append(message);
        sb.append('\n');
        
        sb.append("JRE Vendor:");
        sb.append(jreVendor);
        sb.append('\n');
        
        sb.append("JRE Version:");
        sb.append(jreVersion);
        sb.append('\n');
        
        sb.append("OS Name:");
        sb.append(osName);
        sb.append('\n');
        
        sb.append("OS Version:");
        sb.append(osVersion);
        sb.append('\n');
        
        sb.append("OS Arch:");
        sb.append(osArch);
        sb.append('\n');
        
        sb.append('\n');
        sb.append("Call Stack:\n");
        for(StackTraceElement e : stackTrace) {
            sb.append(e.toString());
            sb.append('\n');
        }
        sb.append('\n');
        
        sb.append("=====================End======================\n");
        
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
