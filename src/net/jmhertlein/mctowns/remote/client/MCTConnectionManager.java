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
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private RSACipherInputStream cis;
    private RSACipherOutputStream cos;
    
    private PublicKey serverPubKey;

    public MCTConnectionManager(String hostname, int port, PrivateKey privateKey) {
        this.hostname = hostname;
        this.port = port;
        s = null;
        this.privateKey = privateKey;
        this.serverPubKey = null;
    }

    public void connect() throws IOException, UnknownHostException {
        s = new Socket(hostname, port);
    }

    public void disconnect() throws IOException {
        s.close();
    }

    public ObjectOutputStream getOutputStream() throws IOException {
        if (oos == null) {
            oos = new ObjectOutputStream(s.getOutputStream());
        }
        return oos;
    }

    public ObjectInputStream getInputStream() throws IOException {
        if (ois == null) {
            ois = new ObjectInputStream(s.getInputStream());
        }
        return ois;
    }

    public RSACipherInputStream getEncryptedInputStream() throws IOException, InvalidKeyException {
        if (cis == null)
            try {
                cis = new RSACipherInputStream(s.getInputStream(), privateKey);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
                Logger.getLogger(MCTConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        return cis;
    }

    public RSACipherOutputStream getEncryptedOutputStream(PublicKey serverPubKey) throws IOException, InvalidKeyException {
        if(cos == null)
        try {
            cos = new RSACipherOutputStream(s.getOutputStream(), serverPubKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(MCTConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cos;
    }

    public PublicKey getServerPubKey() {
        return serverPubKey;
    }
    
    
}
