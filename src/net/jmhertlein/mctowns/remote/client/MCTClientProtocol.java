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
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import net.jmhertlein.mctowns.remote.AuthenticationAttemptRejectedException;
import net.jmhertlein.mctowns.remote.AuthenticationChallenge;
import net.jmhertlein.mctowns.remote.EncryptedSecretKey;
import net.jmhertlein.mctowns.remote.RemoteAction;
import net.jmhertlein.mctowns.remote.ServerTrustException;

/**
 *
 * @author joshua
 */
public class MCTClientProtocol {
    public static final int CHALLENGE_LENGTH = 50;
    private String hostname;
    private int port;
    private String username;
    
    private Socket server;
    
    private PublicKey clientPubkey;
    private PrivateKey clientPrivKey;
    
    private PublicKey serverPubKey;
    private SecretKey sessionKey;
    
    private KeyLoader keyLoader;

    public MCTClientProtocol(String hostname, int port, KeyLoader k, String username, PublicKey clientPubkey, PrivateKey clientPrivKey) {
        this.hostname = hostname;
        this.port = port;
        this.clientPubkey = clientPubkey;
        this.clientPrivKey = clientPrivKey;
        this.username = username;
        this.keyLoader = k;
    }
    
    private void connect() throws UnknownHostException, IOException {
        server = new Socket(hostname, port);
    }
    
    private void exchangeKeys(RemoteAction action, ObjectOutputStream oos) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, AuthenticationAttemptRejectedException, ServerTrustException  {
        Cipher inCipher = Cipher.getInstance("RSA");
        inCipher.init(Cipher.DECRYPT_MODE, clientPrivKey);
        
        ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
        
        Boolean usernameAccepted = (Boolean) ois.readObject();
        
        if(!usernameAccepted) {
            throw new AuthenticationAttemptRejectedException("Server rejected username.");
        }
        System.out.println("Receiving server pubkey.");
        serverPubKey = (PublicKey) ois.readObject();
        
        PublicKey cachedServerPubKey = keyLoader.getLoadedServerPublicKey(hostname);
        if(cachedServerPubKey == null) {
            keyLoader.addAndPersistServerPublicKey(hostname, serverPubKey);
            oos.writeObject(true);
        } else if(cachedServerPubKey.equals(serverPubKey)) {
            oos.writeObject(true);
        } else {
            oos.writeObject(false);
            throw new ServerTrustException(ActionFailReason.SERVER_PUBLIC_KEY_MISMATCH);
        }
        
        Cipher outCipher = Cipher.getInstance("RSA");
        outCipher.init(Cipher.ENCRYPT_MODE, serverPubKey);
        
        //server sends encrypted challenge
        AuthenticationChallenge serverChallenge = (AuthenticationChallenge) ois.readObject();
        try {
            //we decrypt it
            //send it back
            oos.writeObject(serverChallenge.decrypt(inCipher).encrypt(outCipher));
        } catch (BadPaddingException | IllegalBlockSizeException ex) {
            System.out.println("Error decrypting, we probably don't have the right private key.");
            oos.writeObject(new AuthenticationChallenge(1).encrypt(outCipher));
        }
        
        //check their approval
        Boolean serverApproval = (Boolean) ois.readObject();
        
        if(!serverApproval) {
            System.out.println("Server did not accept our authentication attempt..");
            throw new AuthenticationAttemptRejectedException("Failed server's authentication challenge.");
        } else
            System.out.println("Server accepted our challenge response.");
        
        //make our challenge
        AuthenticationChallenge ourChallenge = new AuthenticationChallenge(CHALLENGE_LENGTH);
        
        //encrypt it with their pubkey
        //send it
        oos.writeObject(ourChallenge.encrypt(outCipher));
        
        //receive their response
        AuthenticationChallenge serverResponse = (AuthenticationChallenge) ois.readObject();
        
        //send our approval... or lack thereof
        if(serverResponse.decrypt(inCipher).equals(ourChallenge)) {
            oos.writeObject(true);
            System.out.println("Accepting server challenge.");
        } else {
            System.out.println("Server failed our trust test.");
            oos.writeObject(false);
            throw new ServerTrustException(ActionFailReason.SERVER_FAILED_CLIENT_CHALLENGE);
        }
        
        System.out.println("Reading session key.");
        EncryptedSecretKey encryptedSessionKey = (EncryptedSecretKey) ois.readObject();
        
        sessionKey = new SecretKeySpec(inCipher.doFinal(encryptedSessionKey.getEncoded()), "AES");
        System.out.println("Read session key.");
        
    }
    
    public void submitAction(RemoteAction action) throws UnknownHostException, IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, AuthenticationAttemptRejectedException, ServerTrustException {
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
