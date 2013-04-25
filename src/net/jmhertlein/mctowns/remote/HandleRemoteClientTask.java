package net.jmhertlein.mctowns.remote;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
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
public class HandleRemoteClientTask implements Runnable {
    private static final int NUM_CHECK_BYTES = 500;
    private Socket client;
    private File authKeysDir;
    private CryptoManager cMan;
    private PrivateKey privateKey;
    private PublicKey pubKey;
    
    private String clientName;

    public HandleRemoteClientTask(CryptoManager cMan, PrivateKey privateKey, PublicKey pubKey, File authKeysDir, Socket client) {
        this.client = client;
        this.authKeysDir = authKeysDir;
        this.cMan = cMan;
        this.privateKey = privateKey;
        this.pubKey = pubKey;
        clientName = null;
    }

    @Override
    public void run() {
        sendPubKey();
        receiveClientUserName();
        if(!authenticateClient()) {
            return;
        }
                
        
        
    }
    
    private boolean sendPubKey() {
        try(ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream())) {
            oos.writeObject(pubKey);
        } catch (IOException ex) {
            return false;
        }
        
        return true;
    }
    
    private boolean receiveClientUserName() {
        try(ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
            clientName = (String) ois.readObject();
        } catch (IOException ex) {
            System.err.println("Error getting client username.");
            return false;
        } catch (ClassNotFoundException ex) {
            System.err.println("Error converting client username to String.");
            return false;
        }
        
        return true;
    }
    
    private boolean authenticateClient() {
        //load the public key for their claimed username
        PublicKey clientKey = cMan.loadPubKey(new File(authKeysDir, clientName + ".pub").getPath());
        
        //now, all is encrypted from here on
        try(RSACipherOutputStream cos = new RSACipherOutputStream(client.getOutputStream(), clientKey);
                RSACipherInputStream cis = new RSACipherInputStream(client.getInputStream(), privateKey)) {
            byte[] checkBytes = new byte[NUM_CHECK_BYTES];
            new SecureRandom().nextBytes(checkBytes);
            
            //send user some random bytes
            cos.write(checkBytes);
            byte[] receivedBytes = new byte[NUM_CHECK_BYTES];
            //read their response
            cis.read(receivedBytes);
            
            boolean matches = true;
            for(int i = 0; i < checkBytes.length; i++) {
                if(receivedBytes[i] != checkBytes[i])
                    matches = false;
            }
            //if they weren't able to decrypt and send back the bytes, we're outta here.
            if(! matches)
                return false; //show's over, folks
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }

}
