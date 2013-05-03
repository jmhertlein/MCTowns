/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.remote.server;

import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.crypto.SecretKey;

/**
 *
 * @author joshua
 */
public class ClientSession {
    private final byte[] sessionID;
    private final String username;
    private final SecretKey sessionKey;

    public ClientSession(int sessionID, String username, SecretKey sessionKey) {
        this.username = username;
        this.sessionKey = sessionKey;
        this.sessionID = ByteBuffer.allocate(4).putInt(sessionID).array();
    }


    public String getUsername() {
        return username;
    }

    public SecretKey getSessionKey() {
        return sessionKey;
    }

    public byte[] getSessionID() {
        return Arrays.copyOf(sessionID, sessionID.length);
    }
    
}
