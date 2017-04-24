/*
 * Copyright (C) 2014 Joshua Michael Hertlein <jmhertlein@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cafe.josh.mctowns.util;

import cafe.josh.mctowns.MCTownsPlugin;

/**
 *
 * @author joshua
 */
public enum MCTConfig {
    WG_VER_REGEX("wgRequirement", ".*"),
    DEFAULT_TOWN("defaultTown", null),
    ECONOMY_ENABLED("economyEnabled", false),
    MAYORS_CAN_BUY_TERRITORIES("mayorsCanBuyTerritories", false),
    PRICE_PER_XZ_BLOCK("pricePerXZBlock", 0),
    MIN_NUM_PLAYERS_TO_BUY_TERRITORY("minNumPlayersToBuyTerritory", 3),
    ALLOW_TOWN_FRIENDLY_FIRE_MANAGEMENT("allowTownFriendlyFireManagement", true),
    QUICKSELECT_TOOL("quickSelectTool", "WOOD_HOE"),
    LOG_COMMANDS("logCommands", true),
    PLAYERS_CAN_JOIN_MULTIPLE_TOWNS("playersCanJoinMultipleTowns", false),
    TERRITORY_XZ_SIZE_LIMIT("territoryXZSizeLimit", 800),
    DEBUG_MODE_ENABLED("debugModeEnabled", false),
    CURRENCY_INPUT_PATTERN("currencyInputPattern", "^\\d{1,10}(\\.\\d{1,10})?$");

    private static final MCTownsPlugin p = MCTownsPlugin.getPlugin();

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
        return p == null ? dflt : p.getConfig().get(this.getKey(), dflt);
    }

    public int getInt() {
        return p == null ? (int) dflt : p.getConfig().getInt(this.getKey(), (int) dflt);
    }

    public String getString() {
        String defString = dflt == null ? null : dflt.toString();
        return p == null ? defString : p.getConfig().getString(this.getKey(), defString);
    }

    public boolean getBoolean() {
        return p == null ? (boolean) dflt : p.getConfig().getBoolean(this.getKey(), (boolean) dflt);
    }

    public String getKey() {
        return key;
    }
}
