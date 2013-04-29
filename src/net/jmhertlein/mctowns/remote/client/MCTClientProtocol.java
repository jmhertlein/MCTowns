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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import net.jmhertlein.mctowns.remote.EncryptedSecretKey;
import net.jmhertlein.mctowns.remote.RemoteAction;

/**
 *
 * @author joshua
 */
public class MCTClientProtocol {
    private String hostname;
    private int port;
    private String username;
    
    private Socket server;
    
    private PublicKey clientPubkey;
    private PrivateKey clientPrivKey;
    
    private PublicKey serverPubKey;
    private SecretKey sessionKey;

    public MCTClientProtocol(String hostname, int port, String username, PublicKey clientPubkey, PrivateKey clientPrivKey) {
        this.hostname = hostname;
        this.port = port;
        this.clientPubkey = clientPubkey;
        this.clientPrivKey = clientPrivKey;
        this.username = username;
    }
    
    private void connect() throws UnknownHostException, IOException {
        server = new Socket(hostname, port);
    }
    
    private void exchangeKeys(RemoteAction action, ObjectOutputStream oos) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher inCipher = Cipher.getInstance("RSA");
        inCipher.init(Cipher.DECRYPT_MODE, clientPrivKey);
        
        ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
        
        System.out.println("Receiving server pubkey.");
        serverPubKey = (PublicKey) ois.readObject();
        
        //server sends encrypted challenge
        
        //we decrypt it
        
        //send it back
        
        //check their approval
        
        //make our challenge
        
        //encrypt it with their pubkey
        
        //send it
        
        //receive their response
        
        //send our approval... or lack thereof
        
        System.out.println("Reading session key.");
        EncryptedSecretKey encryptedSessionKey = (EncryptedSecretKey) ois.readObject();
        
        sessionKey = new SecretKeySpec(inCipher.doFinal(encryptedSessionKey.getEncoded()), "AES");
        System.out.println("Read session key.");
        
    }
    
    public void submitAction(RemoteAction action) throws UnknownHostException, IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        connect();
        ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());
        oos.writeObject(action);
        oos.writeObject(username);
        switch(action) {
            case KEY_EXCHANGE:
                exchangeKeys(action, oos);
                break;
        }
        
        server.close();
    }
}
