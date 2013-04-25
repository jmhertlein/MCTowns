package net.jmhertlein.mctowns.remote;

import java.io.Serializable;

/**
 *
 * @author joshua
 */
public class AuthenticationChallenge implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private byte[] challenge;
    
    public AuthenticationChallenge(byte[] challenge) {
        this.challenge = challenge;
    }
    
    public byte[] getChallenge() {
        return challenge;
    }
    
    public void setBytes(byte[] bytes) {
        challenge = bytes;
    }
}
