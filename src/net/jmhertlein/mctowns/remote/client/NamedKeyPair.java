package net.jmhertlein.mctowns.remote.client;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 *
 * @author joshua
 */
public class NamedKeyPair {
    private final String name;
    private final PublicKey pubKey;
    private final PrivateKey privateKey;

    public NamedKeyPair(String name, PublicKey pubKey, PrivateKey privateKey) {
        this.name = name;
        this.pubKey = pubKey;
        this.privateKey = privateKey;
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
    
    
}
