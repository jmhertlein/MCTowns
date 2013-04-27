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
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
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
    private PublicKey clientKey;

    /**
     * 
     * @param cMan cryptomanager that will be shared with other tasks
     * @param privateKey private RSA key for the server
     * @param pubKey public RSA key for the server
     * @param authKeysDir Directory to hold pubkeys of authorized users
     * @param client the client socket this task will interface with
     */
    public HandleRemoteClientTask(CryptoManager cMan, PrivateKey privateKey, PublicKey pubKey, File authKeysDir, Socket client) {
        this.client = client;
        this.authKeysDir = authKeysDir;
        this.cMan = cMan;
        this.privateKey = privateKey;
        this.pubKey = pubKey;
        clientName = null;
        clientKey = null;
    }

    @Override
    public void run() {
        System.out.println("Hanling client.");
        
        System.out.println("Sending pubkey...");
        sendPubKey();
        System.out.println("Pubkey sent.");
        
        System.out.println("Waiting for client username...");
        receiveClientUserName();
        System.out.println("Got client username.");
        
        if(!authenticateClient()) {
            try { //tell the user we are rejecting their auth response
                new ObjectOutputStream(client.getOutputStream()).writeObject(false);
            } catch (IOException ex) {}
            return;
        }
        
        try { //tell the user we have accepted their authentication response
            new ObjectOutputStream(client.getOutputStream()).writeObject(true);
        } catch (IOException ex) { 
            System.err.println("Error communicating with " + client.getInetAddress() + ", err: " + ex); 
        }
        
        receiveCommands();
        
        
                
        
        
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
        clientKey = cMan.loadPubKey(new File(authKeysDir, clientName + ".pub").getPath());
        
        //now, all is encrypted from here on
        try(RSACipherOutputStream cos = new RSACipherOutputStream(client.getOutputStream(), clientKey);
                RSACipherInputStream cis = new RSACipherInputStream(client.getInputStream(), privateKey);
                ObjectOutputStream unencryptedOos = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(cis)) { //init two encrypted streams, an encrypted object input stream, and an unencrypted object output stream
            
            byte[] originalBytes = new byte[NUM_CHECK_BYTES];
            new SecureRandom().nextBytes(originalBytes);
            
            
            
            AuthenticationChallenge challenge = new AuthenticationChallenge(cos.getCipher().doFinal(originalBytes));
            
            //send user the challenge
            unencryptedOos.writeObject(challenge);
            //read their response
            byte[] responseBytes = ((AuthenticationChallenge) ois.readObject()).getChallenge();
            
            if(responseBytes.length != originalBytes.length)
                return false;
            
            for(int i = 0; i < responseBytes.length; i++) {
                if(responseBytes[i] != originalBytes[i])
                    return false;
            }
            return true;
            
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException | ClassNotFoundException | IllegalBlockSizeException | BadPaddingException ex) {
            System.err.println("Error communicating with " + client.getInetAddress() + ", err: " + ex);
        }
        
        return false;
    }

    private void receiveCommands() {
        boolean done = false;
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new RSACipherOutputStream(client.getOutputStream(), pubKey));
                ObjectInputStream ois = new ObjectInputStream(new RSACipherInputStream(client.getInputStream(), privateKey))){ 
            while(!done) {
                RemoteAction action = (RemoteAction) ois.readObject();
                System.out.println("ACTION RECEIVED:" + action.name()); //dummy line in place of actually doing the input...
                
                if(action == RemoteAction.TERMINATE_CONNECTION)
                    done = true;
            }
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            System.err.println("Error communicating with " + client.getInetAddress() + ", err: " + ex);
        } catch (ClassNotFoundException ex) {
            System.err.println(ex);
        } 
    }

}
