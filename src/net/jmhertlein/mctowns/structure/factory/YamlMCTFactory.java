package net.jmhertlein.mctowns.structure.factory;

import java.io.File;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.structure.MCTRegion;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.TownLevel;
import net.jmhertlein.mctowns.structure.yaml.YamlPlot;
import net.jmhertlein.mctowns.structure.yaml.YamlTerritory;
import net.jmhertlein.mctowns.structure.yaml.YamlTown;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public class YamlMCTFactory implements MCTFactory {

    private String rootSaveDirectory;

    public YamlMCTFactory(String rootSaveDirectory) {
        this.rootSaveDirectory = rootSaveDirectory;
    }

    @Override
    public Town newTown(String townName, Player mayor) {
        return new YamlTown(townName, mayor, getTownSavePath(townName));
    }

    @Override
    public Territory newTerritory(String territoryName, String worldName, String parentTownName) {
        territoryName = TownManager.formatRegionName(parentTownName, TownLevel.TERRITORY, worldName);
        return new YamlTerritory(territoryName, worldName, parentTownName, getRegionSavePath(territoryName));
    }

    @Override
    public Plot newPlot(String plotName, String worldName, String parentTownName, String parentTerritoryName) {
        plotName = TownManager.formatRegionName(parentTownName, TownLevel.PLOT, plotName);
        
        return new YamlPlot(plotName, worldName, parentTerritoryName, parentTownName, getRegionSavePath(plotName));
    }

    public File getRegionSavePath(String fullyQualifiedRegionName) {
        //f.save(new File(MCTowns.getMCTDataFolder() + File.separator + getName() + ".yml"));
        return new File(new File(rootSaveDirectory), fullyQualifiedRegionName + ".yml");
    }

    public File getTownSavePath(String townName) {
        return new File(new File(rootSaveDirectory), townName + ".yml");
    }
}
