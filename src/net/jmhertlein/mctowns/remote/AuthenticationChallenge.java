package net.jmhertlein.mctowns.remote;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 *
 * @author joshua
 */
public class AuthenticationChallenge implements Serializable {
    private byte[] challenge;
    public AuthenticationChallenge(int numBytes) {
        challenge = new byte[numBytes];
        new SecureRandom().nextBytes(challenge);
    }
    
    public void encrypt(Cipher c) throws IllegalBlockSizeException, BadPaddingException {
        challenge = c.doFinal(challenge);
    }
    
    public void decrypt(Cipher c) throws IllegalBlockSizeException, BadPaddingException {
        challenge = c.doFinal(challenge);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Arrays.hashCode(this.challenge);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AuthenticationChallenge other = (AuthenticationChallenge) obj;
        if (!Arrays.equals(this.challenge, other.challenge))
            return false;
        return true;
    }
    
    
}
