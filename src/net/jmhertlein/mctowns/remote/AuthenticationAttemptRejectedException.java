package net.jmhertlein.mctowns.remote;

/**
 *
 * @author joshua
 */
public class AuthenticationAttemptRejectedException extends Exception {
    public AuthenticationAttemptRejectedException(String msg) {
        super(msg);
    }
}
