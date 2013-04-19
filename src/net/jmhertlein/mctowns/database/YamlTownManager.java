package net.jmhertlein.mctowns.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import net.jmhertlein.mctowns.structure.MCTRegion;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.yaml.YamlMCTRegion;
import net.jmhertlein.mctowns.structure.yaml.YamlPlot;
import net.jmhertlein.mctowns.structure.yaml.YamlTerritory;
import net.jmhertlein.mctowns.structure.yaml.YamlTown;
import net.jmhertlein.mctowns.structure.TownLevel;
import net.jmhertlein.mctowns.structure.factory.MCTFactory;
import net.jmhertlein.mctowns.structure.factory.YamlMCTFactory;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author joshua
 */
public class YamlTownManager extends TownManager {

    /**
     * Constructs a new, empty town manager.
     */
    public YamlTownManager(MCTFactory factory) {
        super(factory);
        towns = new HashMap<>();
        regions = new HashMap<>();
    }

    /**
     *
     * @param rootDirPath
     * @throws IOException
     */
    public void writeYAML(String rootDirPath) throws IOException {
        FileConfiguration f;

        f = new YamlConfiguration();
        List<String> l = new LinkedList<>();

        l.addAll(towns.keySet());
        f.set("towns", l);

        l = new LinkedList<>();

        l.addAll(regions.keySet());
        f.set("regions", l);

        f.save(new File(rootDirPath + File.separator + ".meta.yml"));

        //now save the regions
        for(Town t : towns.values()) {
            t.save();
        }

        for(MCTRegion reg : regions.values()) {
            reg.save();
        }
    }

    /**
     *
     * @param rootDirPath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws InvalidConfigurationException
     */
    public static YamlTownManager readYAML(String rootDirPath, YamlMCTFactory factory) throws FileNotFoundException, IOException, InvalidConfigurationException {
        File rootDir = new File(rootDirPath);
        FileConfiguration metaF, f;

        YamlTownManager ret = new YamlTownManager(factory);

        metaF = new YamlConfiguration();
        metaF.load(rootDirPath + File.separator + ".meta.yml");

        for(String s : metaF.getStringList("towns")) {
            f = new YamlConfiguration();
            f.load(rootDirPath + File.separator + s + ".yml");
            ret.towns.put(s, YamlTown.readYAML(f, factory));
        }

        for(String s : metaF.getStringList("regions")) {
            f = new YamlConfiguration();
            f.load(rootDirPath + File.separator + s + ".yml");

            if(TownLevel.parseTownLevel(f.getString("type")) == TownLevel.PLOT)
                ret.regions.put(s, YamlPlot.readYAML(f, factory));
            else
                ret.regions.put(s, YamlTerritory.readYAML(f, factory));
        }

        return ret;


    }

    @Override
    public void save() throws IOException {
        
    }
    
    
}
