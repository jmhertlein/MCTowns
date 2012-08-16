/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.structure;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import net.jmhertlein.core.location.Location;
import net.jmhertlein.mctowns.MCTowns;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.block.CraftSign;

/**
 *
 * @author joshua
 */
public class Plot extends MCTownsRegion {

    private static final long serialVersionUID = "PLOT".hashCode(); // DO NOT CHANGE

    /*
     *
     */
    private static final int VERSION = 0;

    private String parTerrName;
    private String parTownName;

    private boolean forSale;
    private BigDecimal price;
    private Location signLoc;

    public Plot(String name, String worldName, String parentTerritoryName, String parentTownName) {
        super(name, worldName);

        parTerrName = parentTerritoryName;
        parTownName = parentTownName;

        //calculateSignLoc(wgp);
    }

    public Plot() {}

    public String getParentTerritoryName() {
        return parTerrName;
    }

    public String getParentTownName() {
        return parTownName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Location getSignLoc() {
        return signLoc;
    }

    public String getAbstractName() {
        String absName = name;

        while (absName.contains("_")) {
            absName = absName.substring(absName.indexOf('_') + 1);
        }

        return absName;

    }

    public void setSignLoc(Location signLoc) {
        this.signLoc = signLoc;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public void buildSign(Server s) {

        if (signLoc == null) {
            MCTowns.logSevere("The sign's location was null.");
        }

        org.bukkit.Location loc = Location.convertToBukkitLocation(s, signLoc);

        loc.getBlock().setType(Material.SIGN_POST);



        CraftSign sign = (CraftSign) loc.getBlock().getState();

        sign.setLine(0, "[mct]");
        sign.setLine(1, "For sale!");
        sign.setLine(2, name);
        sign.setLine(3, "Price: " + price);

        sign.update();
    }

    public void demolishSign(Server s) {
        Location.convertToBukkitLocation(s, signLoc).getBlock().setType(Material.AIR);
    }

    public void calculateSignLoc(WorldGuardPlugin wgp) {
        ProtectedRegion reg = wgp.getRegionManager(wgp.getServer().getWorld(worldName)).getRegion(name);
        Vector middle = reg.getMaximumPoint().add(reg.getMinimumPoint());
        middle = middle.divide(2);


        org.bukkit.Location loc = new org.bukkit.Location(wgp.getServer().getWorld(worldName), middle.getBlockX(), middle.getBlockY(), middle.getBlockZ());

        loc.setY(loc.getWorld().getHighestBlockYAt(loc));

        signLoc = Location.convertFromBukkitLocation(loc);
    }

    public boolean signLocIsSet() {
        return signLoc != null;
    }

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

    public static Plot readYAML(FileConfiguration f) {
        Plot p = new Plot();

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

        return p;
    }
}
