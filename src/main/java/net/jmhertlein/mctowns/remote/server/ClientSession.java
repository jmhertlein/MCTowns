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
package net.jmhertlein.mctowns.remote.server;

import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.crypto.SecretKey;
import net.jmhertlein.mctowns.remote.auth.PublicIdentity;

/**
 *
 * @author joshua
 */
public class ClientSession {

    private final byte[] sessionID;
    private final PublicIdentity identity;
    private final SecretKey sessionKey;

    public ClientSession(int sessionID, PublicIdentity i, SecretKey sessionKey) {
        this.identity = i;
        this.sessionKey = sessionKey;
        this.sessionID = ByteBuffer.allocate(4).putInt(sessionID).array();
    }

    public PublicIdentity getIdentity() {
        return identity;
    }

    public SecretKey getSessionKey() {
        return sessionKey;
    }

    public byte[] getSessionID() {
        return Arrays.copyOf(sessionID, sessionID.length);
    }
}
