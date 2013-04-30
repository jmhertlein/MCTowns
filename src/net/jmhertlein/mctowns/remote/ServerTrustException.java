package net.jmhertlein.mctowns.remote;

import net.jmhertlein.mctowns.remote.client.KeyExchangeFailReason;

/**
 *
 * @author joshua
 */
public class ServerTrustException extends Exception {
    private KeyExchangeFailReason reason;
    public ServerTrustException(KeyExchangeFailReason reason) {
        super("Could not trust server.");
        this.reason = reason;
    }

    public KeyExchangeFailReason getReason() {
        return reason;
    }
    
}
