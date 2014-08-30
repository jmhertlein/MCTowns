/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jmhertlein.mctowns.util;

import net.jmhertlein.mctowns.MCTownsPlugin;

/**
 *
 * @author joshua
 */
public enum MCTConfig {
    DEFAULT_TOWN("defaultTown", false, null),
    ECONOMY_ENABLED("economyEnabled", true, false),
    MAYORS_CAN_BUY_TERRITORIES("mayorsCanBuyTerritories", true, false),
    PRICE_PER_XZ_BLOCK("pricePerXZBlock", true, 0),
    MIN_NUM_PLAYERS_TO_BUY_TERRITORY("minNumPlayersToBuyTerritory", true, 3),
    ALLOW_TOWN_FRIENDLY_FIRE_MANAGEMENT("allowTownFriendlyFireManagement", true, true),
    QUICKSELECT_TOOL("quickSelectTool", true, "WOODEN_HOE"),
    LOG_COMMANDS("logCommands", true, true),
    PLAYERS_CAN_JOIN_MULTIPLE_TOWNS("playersCanJoinMultipleTowns", true, false),
    TERRITORY_XZ_SIZE_LIMIT("territoryXZSizeLimit", true, 800),
    DEBUG_MODE_ENABLED("debugModeEnabled", false, false);
    
    private final boolean mandatory;
    private final Object dflt;
    private final String key;
    
    private MCTConfig(String key, boolean mandatory, Object dflt) {
        this.mandatory = mandatory;
        this.dflt = dflt;
        this.key = key;
    }
    
    public void set(Object value) {
       MCTownsPlugin.getPlugin().getConfig().set(getKey(), value);
    }
    
    protected Object getObject() {
        return MCTownsPlugin.getPlugin().getConfig().get(this.getKey(), dflt);
    }
    
    public int getInt() {
        MCTownsPlugin p = MCTownsPlugin.getPlugin();
        return p == null ? (int) dflt : p.getConfig().getInt(this.getKey(), (int) dflt);
    }
    
    public String getString() {
        MCTownsPlugin p = MCTownsPlugin.getPlugin();
        return p == null ? (String) dflt : p.getConfig().getString(this.getKey(), (String) dflt);
    }
    
    public boolean getBoolean() {
        MCTownsPlugin p = MCTownsPlugin.getPlugin();
        return p == null ? (boolean) dflt : p.getConfig().getBoolean(this.getKey(), (boolean) dflt);
    }

    public boolean isMandatory() {
        return mandatory;
    }
    
    public String getKey() {
        return key;
    }
    
    public static boolean validate() {
        for(MCTConfig c : MCTConfig.values()) 
            if(c.getObject() == null && c.isMandatory())
                return false;
        return true;
    }
}