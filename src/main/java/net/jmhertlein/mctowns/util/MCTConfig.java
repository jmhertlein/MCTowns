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
    DEFAULT_TOWN,
    ECONOMY_ENABLED,
    MAYORS_CAN_BUY_TERRITORIES,
    PRICE_PER_XZ_BLOCK,
    MIN_NUM_PLAYERS_TO_BUY_TERRITORY,
    ALLOW_TOWN_FRIENDLY_FIRE_MANAGEMENT,
    QUICKSELECT_TOOL,
    LOG_COMMANDS,
    PLAYERS_CAN_JOIN_MULTIPLE_TOWNS,
    TERRITORY_XZ_SIZE_LIMIT;
    
    public void set(Object value) {
       MCTownsPlugin.getPlugin().getConfig().set(getKey(), value);
    }
    
    protected Object getObject() {
        return MCTownsPlugin.getPlugin().getConfig().get(this.getKey());
    }
    
    public int getInt() {
        return MCTownsPlugin.getPlugin().getConfig().getInt(this.getKey());
    }
    
    public String getString() {
        return MCTownsPlugin.getPlugin().getConfig().getString(this.getKey());
    }
    
    public boolean getBoolean() {
        return MCTownsPlugin.getPlugin().getConfig().getBoolean(this.getKey());
    }
    
    protected String getKey() {
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
        }
    }
    
    public static boolean validate() {
        for(MCTConfig c : MCTConfig.values()) {
            if(c.getObject() == null)
                return false;
        }       
        return true;
    }
}