package net.jmhertlein.mctowns.remote.auth;

/**
 *
 * @author joshua
 */
public class AuthenticationAttemptRejectedException extends Exception {
    public AuthenticationAttemptRejectedException(String msg) {
        super(msg);
    }
}
