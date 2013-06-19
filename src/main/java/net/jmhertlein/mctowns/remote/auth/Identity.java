package net.jmhertlein.mctowns.remote.auth;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.apache.commons.codec.binary.Base64;

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
        return Base64.encodeBase64String(this.getPubKey().getEncoded());
    }
    
    public String getPrivateEncoded() {
        return  Base64.encodeBase64String(privateKey.getEncoded());
    }
    
    /**
     * Trims the trailing ".pub" off of an Identity file's name
     * @param s
     * @return 
     */
    public static String trimFileName(String s) {
        return s.substring(0, s.lastIndexOf(".pub"));
    }
}
