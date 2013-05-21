package net.jmhertlein.mctowns.remote.auth;

import java.io.Serializable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

/**
 *
 * @author joshua
 */
public class EncryptedSecretKey implements Serializable {
    private byte[] encoded;
    
    public EncryptedSecretKey(SecretKey key, Cipher c) throws IllegalBlockSizeException, BadPaddingException {
        encoded = c.doFinal(key.getEncoded());
    }

    public byte[] getEncoded() {
        return encoded;
    }
    
    
}
