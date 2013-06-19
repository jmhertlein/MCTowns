/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
