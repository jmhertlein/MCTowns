package net.jmhertlein.mctowns.remote.auth;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import sun.misc.BASE64Encoder;

/**
 *
 * @author joshua
 */
public class Identity implements Serializable {
    private final String name;
    private final PublicKey pubKey;
    private final PrivateKey privateKey;

    public Identity(String name, PublicKey pubKey, PrivateKey privateKey) {
        this.name = name;
        this.pubKey = pubKey;
        this.privateKey = privateKey;
    }
    
    public Identity(String name, PublicKey pubKey) {
        this.name = name;
        this.pubKey = pubKey;
        this.privateKey = null;
    }

    public String getName() {
        return name;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    public String getPublicEncoded() {
        return new BASE64Encoder().encode(pubKey.getEncoded());
    }
    
    public String getPrivateEncoded() {
        return (privateKey == null) ? (null) : (new BASE64Encoder().encode(privateKey.getEncoded()));
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
