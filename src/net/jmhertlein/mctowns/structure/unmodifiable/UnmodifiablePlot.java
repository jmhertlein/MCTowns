/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.structure.unmodifiable;

import java.math.BigDecimal;

/**
 *
 * @author joshua
 */
public class UnmodifiablePlot {
    private final String plotName, worldName, signLoc;
    private final BigDecimal price;
    private final boolean forSale;

    public UnmodifiablePlot(String plotName, String worldName, String signLoc, BigDecimal price, boolean forSale) {
        this.plotName = plotName;
        this.worldName = worldName;
        this.signLoc = signLoc;
        this.price = price;
        this.forSale = forSale;
    }

    public String getPlotName() {
        return plotName;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getSignLoc() {
        return signLoc;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isForSale() {
        return forSale;
    }
    
}
