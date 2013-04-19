package net.jmhertlein.mctowns.structure.yaml;

import java.io.File;
import java.io.IOException;
import net.jmhertlein.mctowns.structure.Territory;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.TownLevel;
import net.jmhertlein.mctowns.structure.factory.YamlMCTFactory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author joshua
 */
public class YamlTerritory extends YamlMCTRegion implements Territory{

    private static final long serialVersionUID = "TERRITORY".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;

    private String parTownName;
    private HashSet<String> plotNames;

    /**
     * Constructs a new territory
     *
     * @param name the desired name of the territory
     * @param worldName the name of the world in which the territory exists
     */
    public YamlTerritory(String name, String worldName, String parentTownName, File saveLocation) {
        super(name, worldName, saveLocation);
        plotNames = new HashSet<>();
        parTownName = parentTownName;
    }

    private YamlTerritory(){}

    /**
     * Adds a plot to the territory. Registering the WG region of the territory
     * needs to be done elsewhere.
     *
     * @param dist the plot to be added
     * @return false if the plot was not added because it is already added, true
     * otherwise
     */
    @Override
    public boolean addPlot(Plot plot) {
        if (plotNames.contains(plot.getName())) {
            return false;
        }

        plotNames.add(plot.getName());
        return true;
    }

    /**
     *
     * @return the plots owned by this territory
     */
    @Override
    public Collection<String> getPlotsCollection() {
        return (Collection<String>) plotNames.clone();
    }

    /**
     * Removes the plot from the territory
     *
     * @param plotName the name of the plot to be removed
     * @return if a plot was removed or not
     */
    @Override
    public boolean removePlot(String plotName) {
        return plotNames.remove(plotName);
    }

    @Override
    public String getParentTown() {
        return parTownName;
    }

    @Override
    public void writeYAML(FileConfiguration f) {
        super.writeYAML(f);
        f.set("town", parTownName);
        f.set("plots", getPlotNameList());
        f.set("type", TownLevel.TERRITORY.name());

    }

    public static YamlTerritory readYAML(FileConfiguration f, YamlMCTFactory factory) {
        YamlTerritory ret = new YamlTerritory();

        ret.name = f.getString("name");
        ret.worldName = f.getString("worldName");
        ret.parTownName = f.getString("town");

        ret.plotNames = new HashSet<>();
        ret.plotNames.addAll(f.getStringList("plots"));
        
        ret.saveLocation = factory.getRegionSavePath(ret.name);

        return ret;
    }

    private List<String> getPlotNameList() {
        LinkedList<String> ret = new LinkedList<>();

        for(String s : plotNames) {
            ret.add(s);
        }

        return ret;
    }

    @Override
    public void save() throws IOException {
        FileConfiguration f = new YamlConfiguration();
        writeYAML(f);
        f.save(saveLocation);
    }


}
