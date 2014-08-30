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
    DEFAULT_TOWN("defaultTown", null),
    ECONOMY_ENABLED("economyEnabled", false),
    MAYORS_CAN_BUY_TERRITORIES("mayorsCanBuyTerritories", false),
    PRICE_PER_XZ_BLOCK("pricePerXZBlock", 0),
    MIN_NUM_PLAYERS_TO_BUY_TERRITORY("minNumPlayersToBuyTerritory", 3),
    ALLOW_TOWN_FRIENDLY_FIRE_MANAGEMENT("allowTownFriendlyFireManagement", true),
    QUICKSELECT_TOOL("quickSelectTool", "WOODEN_HOE"),
    LOG_COMMANDS("logCommands", true),
    PLAYERS_CAN_JOIN_MULTIPLE_TOWNS("playersCanJoinMultipleTowns", false),
    TERRITORY_XZ_SIZE_LIMIT("territoryXZSizeLimit", 800),
    DEBUG_MODE_ENABLED("debugModeEnabled", false);
    
    private final Object dflt;
    private final String key;
    
    private MCTConfig(String key, Object dflt) {
        this.dflt = dflt;
        this.key = key;
    }
    
    public void set(Object value) {
       MCTownsPlugin.getPlugin().getConfig().set(getKey(), value);
    }
    
    public Object getObject() {
        MCTownsPlugin p = MCTownsPlugin.getPlugin();
        return p == null ? dflt : MCTownsPlugin.getPlugin().getConfig().get(this.getKey(), dflt);
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
    
    public String getKey() {
        return key;
    }
}