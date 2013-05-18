package net.jmhertlein.mctowns.remote.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import net.jmhertlein.core.crypto.CryptoManager;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.remote.AuthenticationChallenge;
import net.jmhertlein.mctowns.remote.EncryptedSecretKey;
import net.jmhertlein.mctowns.remote.RemoteAction;
import net.jmhertlein.mctowns.remote.view.OverView;
import net.jmhertlein.mctowns.remote.view.PlayerView;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import sun.misc.BASE64Encoder;

/**
 *
 * @author joshua
 */
public class MCTServerProtocol {

    private static final int NUM_CHECK_BYTES = 50;
    private File authKeysDir;
    private CryptoManager cMan;
    private PublicKey serverPubKey;
    private PrivateKey serverPrivateKey;
    private Map<Integer, ClientSession> sessionKeys;
    private MCTowns p;
    private static volatile Integer nextSessionID = 0;
    
    private ClientSession clientSession;
    private String clientName;
    private Socket client;
    private RemoteAction action;

    public MCTServerProtocol(MCTowns p, Socket client, PrivateKey serverPrivateKey, PublicKey serverPublicKey, File authKeysDir, Map<Integer, ClientSession> sessionKeys) {
        this.authKeysDir = authKeysDir;
        this.serverPubKey = serverPublicKey;
        this.cMan = new CryptoManager();
        this.client = client;
        this.serverPrivateKey = serverPrivateKey;
        this.sessionKeys = sessionKeys;
        this.p = p;
    }

    private boolean doInitialKeyExchange() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        System.out.println("Opening object input stream.");
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

        System.out.println("Getting action.");
        action = (RemoteAction) ois.readObject();

        System.out.println("Getting username.");
        clientName = (String) ois.readObject();
        
        System.out.println("Loading client key from disk.");
        PublicKey clientKey = cMan.loadPubKey(new File(authKeysDir, clientName + ".pub"));


        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

        if (clientKey == null) {
            System.out.println("Didnt have public key for user. Exiting.");
            oos.writeObject(false);
            return false;
        } else {
            oos.writeObject(true);
        }

        //init ciphers
        Cipher outCipher = Cipher.getInstance("RSA");
        outCipher.init(Cipher.ENCRYPT_MODE, clientKey);

        Cipher inCipher = Cipher.getInstance("RSA");
        inCipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);

        System.out.println("Sending our public key.");
        oos.writeObject(serverPubKey);

        Boolean clientAcceptsPublicKey = (Boolean) ois.readObject();

        if (!clientAcceptsPublicKey) {
            System.out.println("Client did not accept our public key- did not match their cached copy.");
            return false;
        }

        //send client auth challenge
        AuthenticationChallenge originalChallenge = new AuthenticationChallenge(NUM_CHECK_BYTES);
        oos.writeObject(originalChallenge.encrypt(outCipher));

        AuthenticationChallenge clientResponse = (AuthenticationChallenge) ois.readObject();
        clientResponse = clientResponse.decrypt(inCipher);

        if (clientResponse.equals(originalChallenge)) {
            oos.writeObject(true);
            System.out.println("Accepting client challenge response.");
        } else {
            oos.writeObject(false);
            System.out.println("Rejecting client challenge response.");
            return false;
        }

        AuthenticationChallenge clientChallenge = (AuthenticationChallenge) ois.readObject();
        oos.writeObject(clientChallenge.decrypt(inCipher).encrypt(outCipher));

        Boolean clientAcceptsUs = (Boolean) ois.readObject();

        if (!clientAcceptsUs) {
            System.out.println("Client did not accept us as server they wanted to connect to.");
            return false;
        } else {
            System.out.println("Client accepts us.");
        }

        System.out.println("Making new session key.");
        SecretKey newKey = CryptoManager.newAESKey(p.getConfig().getInt("remoteAdminSessionKeyLength"));
        System.out.println("Session key made.");

        System.out.println("Writing key...");
        oos.writeObject(new EncryptedSecretKey(newKey, outCipher));
        System.out.println("Wrote key.");
        
        BASE64Encoder e = new BASE64Encoder();
        System.out.println(e.encode(newKey.getEncoded()));
        

        int assignedSessionID = nextSessionID;
        nextSessionID++;

        sessionKeys.put(assignedSessionID, new ClientSession(assignedSessionID, clientName, newKey));
        System.out.println("Client assigned session id " + assignedSessionID);

        System.out.println("Writing session ID.");
        oos.writeObject(assignedSessionID);
        System.out.println("Wrote session ID");

        System.out.println("Done handling client.");
        
        client.close();
        return true;
    }

    public void handleAction() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        //receive session ID
        byte[] clientSessionIDBytes = new byte[4];
        client.getInputStream().read(clientSessionIDBytes);
        System.out.println("Read initial session ID.");
        int clientSessionID = ByteBuffer.wrap(clientSessionIDBytes).getInt();

        //if client indicates it does not have a session ID
        if (clientSessionID < 0) {
            if (!doInitialKeyExchange()) {
                System.err.println("User from " + client.getInetAddress() + " (Username: " + clientName + ")" + " tried to connect, but was not authorized.");
            }
            return;
        }

        clientSession = sessionKeys.get(clientSessionID);
        
        System.out.println("Client session key is null: " + (clientSession == null));
        
        Cipher inCipher = Cipher.getInstance("AES/CFB8/NoPadding"), outCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        IvParameterSpec iv = new IvParameterSpec(getKeyBytes(clientSession.getSessionKey()));
        inCipher.init(Cipher.DECRYPT_MODE, clientSession.getSessionKey(), iv);
        outCipher.init(Cipher.ENCRYPT_MODE, clientSession.getSessionKey(), iv);

        CipherOutputStream cos = new CipherOutputStream(client.getOutputStream(), outCipher);
        CipherInputStream cis = new CipherInputStream(client.getInputStream(), inCipher);
        
        ObjectOutputStream oos = new ObjectOutputStream(cos);
        ObjectInputStream ois = new ObjectInputStream(cis);
        
        action = (RemoteAction) ois.readObject();
        clientName = (String) ois.readObject();
        

        System.out.println("Handling action.");
        switch (action) {
            case GET_META_VIEW:
                sendMetaView(oos, ois);
                break;
            case GET_ALL_PLAYERS:
                sendAllPlayersList(oos, ois);
                break;
            case GET_VIEW_FOR_PLAYER:
                sendPlayerView(oos, ois);
                break;
            case GET_ALL_TOWNS:
                sendAllTowns(oos, ois);
                break;
        }

        client.close();
        System.out.println("Finished handling action.");
    }
    
    private void sendMetaView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        FileConfiguration f = p.getConfig();
        
        oos.writeObject(new OverView(f));
    }
    
    private byte[] getKeyBytes(SecretKey k) {
        byte[] key = k.getEncoded();
        byte[] keyBytes = new byte[16];
        System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, keyBytes.length));
        return keyBytes;
    }

    private void sendAllPlayersList(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        System.out.println("Creating list of all players ever played.");
        List<String> playerList = new LinkedList<>();
        
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            playerList.add(p.getName());
        }
        
        oos.writeObject(playerList);
        System.out.println("Sent list.");
    }

    private void sendPlayerView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String pName = (String) ois.readObject();
        PlayerView pView = new PlayerView(p.getServer(), p.getServer().getOfflinePlayer(pName), MCTowns.getTownManager());
        
        oos.writeObject(pView);
    }

    private void sendAllTowns(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        List<String> ret = new LinkedList<>();
        for(Town t : MCTowns.getTownManager().getTownsCollection()) {
            ret.add(t.getTownName());
        }
        
        oos.writeObject(ret);
    }
}
