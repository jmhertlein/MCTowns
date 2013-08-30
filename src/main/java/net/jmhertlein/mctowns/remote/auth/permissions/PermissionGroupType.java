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
package net.jmhertlein.mctowns.remote.auth.permissions;

/**
 *
 * @author joshua
 */
public enum PermissionGroupType {

    /**
     * A resident is unable to modify anything
     */
    RESIDENT,
    /**
     * A Mayor group lets members modify things they are a mayor of
     */
    MAYOR,
    /**
     * An admin group lets members modify anything, even if they are not mayor
     */
    ADMIN;
}
