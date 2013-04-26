package net.jmhertlein.mctowns.remote.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.NoSuchPaddingException;
import net.jmhertlein.core.crypto.CryptoManager;
import net.jmhertlein.core.crypto.RSACipherInputStream;
import net.jmhertlein.core.crypto.RSACipherOutputStream;

/**
 *
 * @author joshua
 */
public class MCTConnectionManager {
    private String hostname;
    private int port;
    
    private Socket s;
    private PrivateKey privateKey;
    private PublicKey pubKey;

    public MCTConnectionManager(String hostname, int port, PublicKey pubKey, PrivateKey privateKey) {
        this.hostname = hostname;
        this.port = port;
        s = null;
        this.privateKey = privateKey;
        this.pubKey = pubKey;
    }
    
    public void connect() throws IOException, UnknownHostException {
        s = new Socket(hostname, port);
    }
    
    public void disconnect() throws IOException {
        s.close();
    }
    
    public ObjectOutputStream getOutputStream() throws IOException {
        return new ObjectOutputStream(s.getOutputStream());
    }
    
    public ObjectInputStream getInputStream() throws IOException {
        return new ObjectInputStream(s.getInputStream());
    }
    
    public RSACipherInputStream getEncryptedInputStream() throws IOException, InvalidKeyException {
        try {
            return new RSACipherInputStream(s.getInputStream(), privateKey);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MCTConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(MCTConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public RSACipherOutputStream getEncryptedOutputStream() throws IOException, InvalidKeyException {
        try {
            return new RSACipherOutputStream(s.getOutputStream(), pubKey);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MCTConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(MCTConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
