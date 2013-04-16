/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.structure;

import java.math.BigDecimal;
import net.jmhertlein.core.location.Location;

/**
 *
 * @author joshua
 */
public interface Plot {

    /**
     *
     */
    void buildSign();

    /**
     * Tries to place the sign's location in the middle of the plot.
     */
    void calculateSignLoc();

    /**
     *
     */
    void demolishSign();

    /**
     *
     * @return
     */
    String getParentTerritoryName();

    /**
     *
     * @return
     */
    String getParentTownName();

    /**
     *
     * @return the price of the Plot
     */
    BigDecimal getPrice();

    /**
     *
     * @return the location of the plot's sale sign
     */
    Location getSignLoc();

    /**
     *
     * @return the shorter, reading-friendly name of the plot
     */
    String getTerseName();

    /**
     *
     * @return
     */
    boolean isForSale();

    /**
     *
     * @param forSale
     */
    void setForSale(boolean forSale);

    /**
     *
     * @param price
     */
    void setPrice(BigDecimal price);

    /**
     *
     * @param signLoc the new location for the plot's sale sign
     */
    void setSignLoc(Location signLoc);

    /**
     *
     * @return
     */
    boolean signLocIsSet();
    
}
