package net.jmhertlein.mctowns.remote;

/**
 *
 * @author joshua
 */
public class ServerTrustException extends Exception {
    private ActionStatus reason;
    public ServerTrustException(ActionStatus reason) {
        super("Could not trust server.");
        this.reason = reason;
    }

    public ActionStatus getReason() {
        return reason;
    }
    
}
