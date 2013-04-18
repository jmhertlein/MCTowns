package net.jmhertlein.mctowns.structure.yaml;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import net.jmhertlein.core.location.Location;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.TownLevel;
import net.jmhertlein.mctowns.structure.factory.MCTFactory;
import net.jmhertlein.mctowns.structure.factory.YamlMCTFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_5_R2.block.CraftSign;

/**
 *
 * @author joshua
 */
public class YamlPlot extends YamlMCTRegion implements Plot {

    private static final long serialVersionUID = "PLOT".hashCode(); // DO NOT CHANGE

    private String parTerrName;
    private String parTownName;

    private boolean forSale;
    private BigDecimal price;
    private Location signLoc;

    /**
     * Creates a new plot with the specified properties.
     * @param name
     * @param worldName
     * @param parentTerritoryName
     * @param parentTownName
     */
    public YamlPlot(String name, String worldName, String parentTerritoryName, String parentTownName, File saveLocation) {
        super(name, worldName, saveLocation);

        parTerrName = parentTerritoryName;
        parTownName = parentTownName;
        
        //note to self- don't put calculateSignLoc() here because it needs the region to already
        //added to the region manager
    }

    /**
     * Empty constructor for de-serialization. Recommended: Don't use this.
     */
    public YamlPlot() {}

    /**
     *
     * @return
     */
    @Override
    public String getParentTerritoryName() {
        return parTerrName;
    }

    /**
     *
     * @return
     */
    @Override
    public String getParentTownName() {
        return parTownName;
    }

    /**
     *
     * @return the price of the Plot
     */
    @Override
    public BigDecimal getPrice() {
        return price;
    }

    /**
     *
     * @param price
     */
    @Override
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     *
     * @return the location of the plot's sale sign
     */
    @Override
    public Location getSignLoc() {
        return signLoc;
    }

    /**
     *
     * @return the shorter, reading-friendly name of the plot
     */
    @Override
    public String getTerseName() {
        String absName = name;

        while (absName.contains("_")) {
            absName = absName.substring(absName.indexOf('_') + 1);
        }

        return absName;

    }

    /**
     *
     * @param signLoc the new location for the plot's sale sign
     */
    @Override
    public void setSignLoc(Location signLoc) {
        this.signLoc = signLoc;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isForSale() {
        return forSale;
    }

    /**
     *
     * @param forSale
     */
    @Override
    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    /**
     *
     */
    @Override
    public void buildSign() {

        if (signLoc == null) {
            MCTowns.logSevere("The sign's location was null.");
        }

        org.bukkit.Location loc = Location.convertToBukkitLocation(Bukkit.getServer(), signLoc);

        loc.getBlock().setType(Material.SIGN_POST);



        CraftSign sign = (CraftSign) loc.getBlock().getState();

        sign.setLine(0, "[mct]");
        sign.setLine(1, "For sale!");
        sign.setLine(2, name);
        sign.setLine(3, "Price: " + price);

        sign.update();
    }

    /**
     *
     */
    @Override
    public void demolishSign() {
        Location.convertToBukkitLocation(Bukkit.getServer(), signLoc).getBlock().setType(Material.AIR);
    }

    /**
     * Tries to place the sign's location in the middle of the plot.
     */
    @Override
    public final void calculateSignLoc() {
        ProtectedRegion reg = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(name);
        Vector middle = reg.getMaximumPoint().add(reg.getMinimumPoint());
        middle = middle.divide(2);


        org.bukkit.Location loc = new org.bukkit.Location(wgp.getServer().getWorld(worldName), middle.getBlockX(), middle.getBlockY(), middle.getBlockZ());

        loc.setY(loc.getWorld().getHighestBlockYAt(loc));

        signLoc = Location.convertFromBukkitLocation(loc);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean signLocIsSet() {
        return signLoc != null;
    }

    /**
     *
     * @param f
     */
    @Override
    public void writeYAML(FileConfiguration f) {
        super.writeYAML(f);
        f.set("forSale", forSale);
        f.set("price", (price == null) ? "nil" : price.toString());
        f.set("signLoc", (signLoc == null) ? "nil" : signLoc.toList());
        f.set("parentTownName", parTownName);
        f.set("parentTerritoryName", parTerrName);
        f.set("type", TownLevel.PLOT.name());
    }

    /**
     *
     * @param f
     * @return
     */
    public static YamlPlot readYAML(FileConfiguration f, YamlMCTFactory factory) {
        YamlPlot p = new YamlPlot();

        p.name = f.getString("name");
        p.worldName = f.getString("worldName");
        p.parTerrName = f.getString("parentTerritoryName");
        p.parTownName = f.getString("parentTownName");
        p.forSale = f.getBoolean("forSale");

        if(f.getString("signLoc").equals("nil"))
            p.signLoc = null;
        else
            p.signLoc = Location.fromList(f.getStringList("signLoc"));

        if(f.getString("price").equals("nil"))
            p.price = null;
        else
            p.price = new BigDecimal(f.getString("price"));
        
        p.saveLocation = factory.getRegionSavePath(p.name);

        return p;
    }

    @Override
    public void save() throws IOException {
        FileConfiguration f = new YamlConfiguration();
        writeYAML(f);
        f.save(saveLocation);
    }
}
