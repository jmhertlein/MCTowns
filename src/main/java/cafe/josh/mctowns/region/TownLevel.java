/*
 * Copyright (C) 2013 Joshua Michael Hertlein <jmhertlein@gmail.com>
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
package cafe.josh.mctowns.region;

/**
 * A simple enum to hold what sort of town-y thing something is.
 *
 * @author Joshua
 */
public enum TownLevel {

    TOWN,
    TERRITORY,
    PLOT;
    public static final String TERRITORY_INFIX = "_territ_";
    public static final String PLOT_INFIX = "_plot_";

    @Override
    public String toString() {
        switch(this) {
            case TOWN:
                return "TOWN";
            case TERRITORY:
                return "TERRITORY";
            case PLOT:
                return "PLOT";
            default:
                return "y u break me? ;_;";
        }
    }

    public static final TownLevel parseTownLevel(String s) {
        s = s.toUpperCase();

        switch(s) {
            case "TOWN":
                return TOWN;
            case "TERRITORY":
                return TERRITORY;
            case "PLOT":
                return PLOT;
            default:
                throw new TownLevelFormatException(s);
        }
    }

    public static class TownLevelFormatException extends RuntimeException {

        public TownLevelFormatException(String badToken) {
            super("Error: " + badToken + " is not a town level.");
        }
    }
}
