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
package cafe.josh.mctowns.townjoin;

/**
 *
 * @author Joshua
 */
public enum TownJoinMethod {
    INVITATION,
    ECONOMY;

    public static TownJoinMethod parseMethod(String s) throws TownJoinMethodFormatException {
        if(s.equalsIgnoreCase(INVITATION.toString())) {
            return INVITATION;
        } else if(s.equalsIgnoreCase(ECONOMY.toString())) {
            return ECONOMY;
        } else {
            throw new TownJoinMethodFormatException("Error: " + s + " was not a valid String representation of a TownJoinMethod.");
        }
    }

    @Override
    public String toString() {
        switch(this) {
            case INVITATION:
                return "invitation";
            case ECONOMY:
                return "economy";
            default:
                return null;
        }
    }
}
