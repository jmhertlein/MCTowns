/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jmhertlein.mctowns.util;

import java.util.NoSuchElementException;
import net.jmhertlein.mctowns.MCTownsPlugin;

/**
 *
 * @author joshua
 */
public enum MCTConfig {
    DEFAULT_TOWN(false, null),
    ECONOMY_ENABLED(true, false),
    MAYORS_CAN_BUY_TERRITORIES(true, false),
    PRICE_PER_XZ_BLOCK(true, 0),
    MIN_NUM_PLAYERS_TO_BUY_TERRITORY(true, 3),
    ALLOW_TOWN_FRIENDLY_FIRE_MANAGEMENT(true, true),
    QUICKSELECT_TOOL(true, "WOODEN_HOE"),
    LOG_COMMANDS(true, true),
    PLAYERS_CAN_JOIN_MULTIPLE_TOWNS(true, false),
    TERRITORY_XZ_SIZE_LIMIT(true, 800),
    DEBUG_MODE_ENABLED(false, false);
    
    private final boolean mandatory;
    private final Object dflt;
    
    private MCTConfig(boolean mandatory, Object dflt) {
        this.mandatory = mandatory;
        this.dflt = dflt;
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
        switch(this) {
            default:
                throw new NoSuchElementException("The enum value " + this.name() + "doesn't have a mapping to anything in config.yml!!!");
            case DEFAULT_TOWN:
                return "defaultTown";
            case ECONOMY_ENABLED:
                return "economyEnabled";
            case MAYORS_CAN_BUY_TERRITORIES:
                return "mayorsCanBuyTerritories";
            case PRICE_PER_XZ_BLOCK:
                return "pricePerXZBlock";
            case MIN_NUM_PLAYERS_TO_BUY_TERRITORY:
                return "minNumPlayersToBuyTerritory";
            case ALLOW_TOWN_FRIENDLY_FIRE_MANAGEMENT:
                return "allowTownFriendlyFireManagement";
            case QUICKSELECT_TOOL:
                return "quickSelectTool";
            case LOG_COMMANDS:
                return "logCommands";
            case PLAYERS_CAN_JOIN_MULTIPLE_TOWNS:
                return "playersCanJoinMultipleTowns";
            case TERRITORY_XZ_SIZE_LIMIT:
                return "territoryXZSizeLimit";
            case DEBUG_MODE_ENABLED:
                return "debugModeEnabled";
        }
    }
    
    public static boolean validate() {
        for(MCTConfig c : MCTConfig.values()) {
            if(c.getObject() == null && c.isMandatory())
                return false;
        }       
        return true;
    }
}