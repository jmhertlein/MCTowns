package net.jmhertlein.mctowns.remote;

/**
 *
 * @author joshua
 */
public enum ActionStatus {
    SERVER_PUBLIC_KEY_MISMATCH,
    SERVER_FAILED_CLIENT_CHALLENGE,
    CLIENT_FAILED_SERVER_CHALLENGE,
    NO_FAILURE, 
    CONNECTION_REFUSED, 
    UNKNOWN_HOST,
    CONNECTION_INTERRUPTED,
    UNHANDLED_ERROR;
}
