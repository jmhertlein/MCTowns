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
package net.jmhertlein.mctowns.remote.auth;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import net.jmhertlein.core.crypto.Keys;

/**
 *
 * @author joshua
 */
public class Identity extends PublicIdentity implements Serializable {

    private final PrivateKey privateKey;

    public Identity(String name, PublicKey pubKey, PrivateKey privateKey) {
        super(name, pubKey);
        this.privateKey = privateKey;
    }

    public Identity(String name, PublicKey pubKey) {
        super(name, pubKey);
        this.privateKey = null;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getPublicEncoded() {
        return Keys.getBASE64ForKey(this.getPubKey());
    }

    public String getPrivateEncoded() {
        return Keys.getBASE64ForKey(privateKey);
    }

    /**
     * Trims the trailing ".pub" off of an Identity file's name
     *
     * @param s
     * @return
     */
    public static String trimFileName(String s) {
        return s.substring(0, s.lastIndexOf(".pub"));
    }
}
