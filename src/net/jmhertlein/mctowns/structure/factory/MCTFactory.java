package net.jmhertlein.mctowns.structure.factory;

import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
public interface MCTFactory {
    public Town newTown(String townName, Player mayor);
    public Territory newTerritory(String territoryName, String worldName, String parentTownName);
    public Plot newPlot(String plotName, String worldName, String parentTownName, String parentTerritoryName);
}
