package net.jmhertlein.mctowns.remote.server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author joshua
 */
public class HandleRemoteClientTask implements Runnable {
    private static final int NUM_CHECK_BYTES = 500;
    private MCTServerProtocol protocol;

    /**
     *
     * @param cMan cryptomanager that will be shared with other tasks
     * @param privateKey private RSA key for the server
     * @param pubKey public RSA key for the server
     * @param authKeysDir Directory to hold pubkeys of authorized users
     * @param client the client socket this task will interface with
     */
    public HandleRemoteClientTask(Plugin p, PrivateKey privateKey, PublicKey pubKey, File authKeysDir, Socket client, Map<String,SecretKey> sessionKeys) {
        protocol = new MCTServerProtocol(p, client, privateKey, pubKey, authKeysDir, sessionKeys);
    }

    @Override
    public void run() {
        try {
            protocol.handleAction();
        } catch (IOException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
