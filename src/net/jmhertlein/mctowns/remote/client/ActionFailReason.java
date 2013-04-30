package net.jmhertlein.mctowns.remote.client;

/**
 *
 * @author joshua
 */
public enum ActionFailReason {
    SERVER_PUBLIC_KEY_MISMATCH,
    SERVER_FAILED_CLIENT_CHALLENGE,
    CLIENT_FAILED_SERVER_CHALLENGE,
    NO_FAILURE, 
    CONNECTION_REFUSED, UNKNOWN_HOST, FATAL_ERROR;
}
