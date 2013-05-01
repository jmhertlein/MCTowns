/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.structure.unmodifiable;

/**
 *
 * @author joshua
 */
public class UnmodifiableTerritory {
    private final String territoryName, worldName;

    public UnmodifiableTerritory(String territoryName, String worldName) {
        this.territoryName = territoryName;
        this.worldName = worldName;
    }

    public String getTerritoryName() {
        return territoryName;
    }

    public String getWorldName() {
        return worldName;
    }
    
}
