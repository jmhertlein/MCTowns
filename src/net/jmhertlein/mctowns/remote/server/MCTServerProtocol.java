package net.jmhertlein.mctowns.remote.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import net.jmhertlein.core.crypto.CryptoManager;
import net.jmhertlein.mctowns.remote.AuthenticationChallenge;
import net.jmhertlein.mctowns.remote.EncryptedSecretKey;
import net.jmhertlein.mctowns.remote.RemoteAction;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author joshua
 */
public class MCTServerProtocol {
    private static final int NUM_CHECK_BYTES = 500;
    private Socket client;
    private File authKeysDir;
    private CryptoManager cMan;
    private PublicKey serverPubKey;
    private String clientName;
    private PrivateKey serverPrivateKey;
    private Map<String, SecretKey> sessionKeys;
    private Plugin p;

    public MCTServerProtocol(Plugin p, Socket client, PrivateKey serverPrivateKey, PublicKey serverPublicKey, File authKeysDir, Map<String, SecretKey> sessionKeys) {
        this.authKeysDir = authKeysDir;
        this.serverPubKey = serverPublicKey;
        this.cMan = new CryptoManager();
        this.client = client;
        this.serverPrivateKey = serverPrivateKey;
        this.sessionKeys = sessionKeys;
        this.p = p;
    }

    private boolean exchangeKeys(String username, ObjectInputStream ois) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        System.out.println("Loading client key from disk.");
        PublicKey clientKey = cMan.loadPubKey(new File(authKeysDir, username + ".pub"));

        if (clientKey == null) {
            System.out.println("Didnt have public key for user. Exiting.");
            return false;
        }
        Cipher outCipher = Cipher.getInstance("RSA");
        outCipher.init(Cipher.ENCRYPT_MODE, clientKey);

        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        System.out.println("Sending our public key.");
        oos.writeObject(serverPubKey);
        
        //send client auth challenge
        AuthenticationChallenge originalChallenge = new AuthenticationChallenge(NUM_CHECK_BYTES);
        originalChallenge.encrypt(outCipher);
        oos.writeObject(originalChallenge);
        
        AuthenticationChallenge clientResponse = (AuthenticationChallenge) ois.readObject();
        
        if(clientResponse.equals(originalChallenge))
            oos.writeObject(true);
        else {
            oos.writeObject(false);
            return false;
        }
        
        AuthenticationChallenge clientChallenge = (AuthenticationChallenge) ois.readObject();
        Cipher inCipher = Cipher.getInstance("RSA");
        inCipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);
        clientChallenge.decrypt(inCipher);
        
        oos.writeObject(clientChallenge);
        
        Boolean clientAcceptsUs = (Boolean) ois.readObject();
        
        if(!clientAcceptsUs) {
            System.out.println("Client did not accept us as server they wanted to connect to.");
            return false;
        }
        

        System.out.println("Making new session key.");
        SecretKey newKey = CryptoManager.newAESKey(p.getConfig().getInt("remoteAdminSessionKeyLength"));
        System.out.println("Session key made.");




        System.out.println("Writing key...");
        oos.writeObject(new EncryptedSecretKey(newKey, outCipher));
        System.out.println("Wrote key.");
        
        sessionKeys.put(username, newKey);

        System.out.println("Done handling client.");
        return true;
    }

    public void handleAction() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        System.out.println("Opening input stream.");
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

        System.out.println("Getting action.");
        RemoteAction clientAction = (RemoteAction) ois.readObject();
        System.out.println("Getting username.");
        String username = (String) ois.readObject();

        System.out.println("Handling action.");
        switch (clientAction) {
            case KEY_EXCHANGE:
                if (!exchangeKeys(username, ois))
                    System.err.println("Username " + username + " tried to connect, but was not authorized.");
                break;
        }

        client.close();
        System.out.println("Finished handling action.");
    }
}
