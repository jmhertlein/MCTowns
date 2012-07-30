/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.structure;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import net.jmhertlein.mctowns.MCTowns;

/**
 *
 * @author joshua
 */
public class Territory extends MCTownsRegion implements Externalizable {

    private static final long serialVersionUID = "TERRITORY".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;
    private HashMap<String, Plot> plots;

    /**
     * Required constructor for Externalization
     */
    public Territory() {
    }

    /**
     * Constructs a new territory
     *
     * @param name the desired name of the territory
     * @param worldName the name of the world in which the territory exists
     */
    public Territory(String name, String worldName) {
        super(name, worldName);
        plots = new HashMap<>();
    }

    /**
     * Adds a plot to the territory. Registering the WG region of the territory
     * needs to be done elsewhere.
     *
     * @param dist the plot to be added
     * @return false if the plot was not added because it is already added, true
     * otherwise
     */
    public boolean addPlot(Plot plot) {
        if (plots.containsKey(plot.getName())) {
            return false;
        }

        plots.put(plot.getName(), plot);
        return true;


    }

    /**
     * Returns the plot whose name is name
     *
     * @param name the name of the plot to be returned
     * @return the plot whose name is name
     */
    public Plot getPlot(String name) {
        return plots.get(name);
    }

    /**
     *
     * @return the plots owned by this territory
     */
    public Collection<Plot> getPlotsCollection() {
        return plots.values();
    }

    /**
     * Removes the plot from the territory
     *
     * @param plotName the name of the plot to be removed
     * @return the removed plot
     */
    public Plot removePlot(String plotName) {
        return plots.remove(plotName);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeInt(VERSION);
        out.writeObject(plots);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        int ver = in.readInt();

        if (ver == 0) {
            //============Beginning of original variables for version 0=========
            plots = (HashMap<String, Plot>) in.readObject();
            //============End of original variables for version 0===============
        } else {
            MCTowns.log.log(Level.SEVERE, "MCTowns: Unsupported version (version " + ver + ") of Territory.");
        }
    }
}
