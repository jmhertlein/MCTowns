/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.structure;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import me.everdras.mctowns.MCTowns;

/**
 *
 * @author joshua
 */
public class District extends MCTownsRegion implements Externalizable {

    private static final long serialVersionUID = "DISTRICT".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;
    private HashMap<String, Plot> plots;

    public District() {
    }

    /**
     * Creates a new district.
     * @param name the name of the new district
     * @param worldName the name of the world in which the district exists
     */
    public District(String name, String worldName) {
        super(name, worldName);
        plots = new HashMap<>();
    }

    /**
     * Returns the plot whose name is name
     * @param name the name of the plot
     * @return the plot by the name name
     */
    public Plot getPlot(String name) {
        return plots.get(name);
    }

    /**
     *
     * @deprecated
     * @return a hashmap of the plots owned
     */
    public HashMap<String, Plot> getPlots() {
        return plots;
    }

    /**
     *
     * @return the plots owned by the district
     */
    public Collection<Plot> getPlotsCollection() {
        return plots.values();
    }

    /**
     *  Adds the plot to the district. Registering the WG region of the plot will need to be done elsewhere.
     * @param p the plot to be added
     * @return false if the plot was not added because it already is added, false otherwise
     */
    public boolean addPlot(Plot p) {
        if (plots.containsKey(p.getName())) {
            return false;
        }

        plots.put(p.getName(), p);
        return true;
    }

    /**
     * Removes the plot. Unregistering the plot's WG region will need to be done elsewhere.
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

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        int ver = in.readInt();

        if (ver == 0) {
            //============Beginning of original variables for version 0=========
            plots = (HashMap<String, Plot>) in.readObject();
            //============End of original variables for version 0===============
        }
        else {
            MCTowns.log.log(Level.SEVERE, "MCTowns: Unsupported version (version " + ver + ") of District.");
        }
    }
}
