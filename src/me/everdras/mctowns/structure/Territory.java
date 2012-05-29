/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.structure;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import me.everdras.mctowns.MCTowns;

/**
 *
 * @author joshua
 */
public class Territory extends MCTownsRegion implements Externalizable {

    private static final long serialVersionUID = "TERRITORY".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 0;

    private HashMap<String, District> districts;


    /**
     * Required constructor for Externalization
     */
    public Territory() {}
    
    
    /**
     * Constructs a new territory
     * @param name the desired name of the territory
     * @param worldName the name of the world in which the territory exists
     */
    public Territory(String name, String worldName) {
        super(name, worldName);
        districts = new HashMap<String, District>();
    }

    /**
     * Adds a district to the territory. Registering the WG region of the territory needs to be done elsewhere.
     * @param dist the district to be added
     * @return false if the district was not added because it is already added, true otherwise
     */
    public boolean addDistrict(District dist) {
        if (districts.containsKey(dist.getName())) {
            return false;
        }

        districts.put(dist.getName(), dist);
        return true;


    }

    /**
     * Returns the district whose name is name
     * @param name the name of the district to be returned
     * @return the district whose name is name
     */
    public District getDistrict(String name) {
        return districts.get(name);
    }

    /**
     * @deprecated
     * @return
     */
    public HashMap<String, District> getDistricts() {
        return districts;
    }

    /**
     *
     * @return the districts owned by this territory
     */
    public Collection<District> getDistrictsCollection() {
        return districts.values();
    }

    /**
     * Removed the district from the territory
     * @param distName the name of the district to be removed
     * @return the removed district
     */
    public District removeDistrict(String distName) {
        return districts.remove(distName);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeInt(VERSION);
        out.writeObject(districts);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        int ver = in.readInt();

        if(ver == 0) {
            //============Beginning of original variables for version 0=========
            districts = (HashMap<String, District>) in.readObject();
            //============End of original variables for version 0===============
        }
        else {
            MCTowns.log.log(Level.SEVERE, "MCTowns: Unsupported version (version " + ver + ") of Territory.");
        }
    }


}
