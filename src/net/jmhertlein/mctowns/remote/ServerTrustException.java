package net.jmhertlein.mctowns.remote;

import net.jmhertlein.mctowns.remote.client.ActionFailReason;

/**
 *
 * @author joshua
 */
public class ServerTrustException extends Exception {
    private ActionFailReason reason;
    public ServerTrustException(ActionFailReason reason) {
        super("Could not trust server.");
        this.reason = reason;
    }

    public ActionFailReason getReason() {
        return reason;
    }
    
}
