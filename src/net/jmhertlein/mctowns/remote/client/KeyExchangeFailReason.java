package net.jmhertlein.mctowns.remote.client;

/**
 *
 * @author joshua
 */
public enum KeyExchangeFailReason {
    SERVER_PUBLIC_KEY_MISMATCH,
    SERVER_FAILED_CLIENT_CHALLENGE,
    CLIENT_FAILED_SERVER_CHALLENGE,
    NO_FAILURE;
}
