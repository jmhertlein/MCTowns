package net.jmhertlein.mctowns.remote.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.jmhertlein.mctowns.remote.AuthenticationAttemptRejectedException;
import net.jmhertlein.mctowns.remote.AuthenticationChallenge;
import net.jmhertlein.mctowns.remote.EncryptedSecretKey;
import net.jmhertlein.mctowns.remote.RemoteAction;
import net.jmhertlein.mctowns.remote.ServerTrustException;
import net.jmhertlein.mctowns.remote.view.OverView;
import sun.misc.BASE64Encoder;

/**
 *
 * @author joshua
 */
public class MCTClientProtocol {

    public static final int CHALLENGE_LENGTH = 50;
    private final String hostname;
    private final int port;
    private final String username;
    private Socket server;
    private final PublicKey clientPubkey;
    private final PrivateKey clientPrivKey;
    private PublicKey serverPubKey;
    private SecretKey sessionKey;
    private byte[] sessionID;
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
        System.out.println("Connecting.");
        server = new Socket(hostname, port);
    }

    private void disconnect() throws IOException {
        System.out.println("Disconnecting.");
        server.close();
        server = null;
    }

    public void doInitialKeyExchange() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, AuthenticationAttemptRejectedException, ServerTrustException {
        connect();

        //send -1 to indicate we do not have a session ID
        sendSessionID(server.getOutputStream(), integerToBytes(-1));

        //write our intended action and username
        ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());
        sendInitialData(oos, RemoteAction.KEY_EXCHANGE, username);

        Cipher inCipher = Cipher.getInstance("RSA");
        inCipher.init(Cipher.DECRYPT_MODE, clientPrivKey);

        ObjectInputStream ois = new ObjectInputStream(server.getInputStream());

        //
        Boolean serverHadPubkeyForOurUsername = (Boolean) ois.readObject();

        if (!serverHadPubkeyForOurUsername) {
            throw new AuthenticationAttemptRejectedException("Server rejected username.");
        }
        System.out.println("Receiving server pubkey.");
        serverPubKey = (PublicKey) ois.readObject();

        PublicKey cachedServerPubKey = keyLoader.getLoadedServerPublicKey(hostname);
        if (cachedServerPubKey == null) {
            keyLoader.addAndPersistServerPublicKey(hostname, serverPubKey);
            oos.writeObject(true);
        } else if (cachedServerPubKey.equals(serverPubKey)) {
            oos.writeObject(true);
        } else {
            oos.writeObject(false);
            throw new ServerTrustException(ActionStatus.SERVER_PUBLIC_KEY_MISMATCH);
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

        if (!serverApproval) {
            System.out.println("Server did not accept our authentication attempt..");
            throw new AuthenticationAttemptRejectedException("Failed server's authentication challenge.");
        } else {
            System.out.println("Server accepted our challenge response.");
        }

        //make our challenge
        AuthenticationChallenge ourChallenge = new AuthenticationChallenge(CHALLENGE_LENGTH);

        //encrypt it with their pubkey
        //send it
        oos.writeObject(ourChallenge.encrypt(outCipher));

        //receive their response
        AuthenticationChallenge serverResponse = (AuthenticationChallenge) ois.readObject();

        //send our approval... or lack thereof
        if (serverResponse.decrypt(inCipher).equals(ourChallenge)) {
            oos.writeObject(true);
            System.out.println("Accepting server challenge.");
        } else {
            System.out.println("Server failed our trust test.");
            oos.writeObject(false);
            throw new ServerTrustException(ActionStatus.SERVER_FAILED_CLIENT_CHALLENGE);
        }

        System.out.println("Reading session key.");
        EncryptedSecretKey encryptedSessionKey = (EncryptedSecretKey) ois.readObject();

        sessionKey = new SecretKeySpec(inCipher.doFinal(encryptedSessionKey.getEncoded()), "AES");
        System.out.println("Read session key.");

        BASE64Encoder e = new BASE64Encoder();
        System.out.println(e.encode(sessionKey.getEncoded()));

        Integer id = (Integer) ois.readObject();

        System.out.println("Reading session ID");
        sessionID = integerToBytes(id);
        System.out.println("Reading session ID");

        disconnect();
    }

    private byte[] integerToBytes(Integer i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private void sendSessionID(OutputStream os, byte[] sID) throws IOException {
        os.write(sID);
    }

    private void sendInitialData(ObjectOutputStream oos, RemoteAction a, String uName) throws IOException {
        oos.writeObject(a);
        oos.writeObject(uName);
    }

    public OverView getOverView() throws IOException {
        System.out.println("Getting overview.");

        System.out.println("Connecting.");
        connect();
        sendSessionID(server.getOutputStream(), sessionID);
        System.out.println("Connected.");

        System.out.println("Initializing streams....");
        ObjectOutputStream oos = getEncryptedOutputStream();
        ObjectInputStream ois = getEncryptedInputStream();
        System.out.println("Streams initialized");

        System.out.println("Sending initial data");
        sendInitialData(oos, RemoteAction.GET_META_VIEW, username);
        System.out.println("Data sent.");

        System.out.println("Reading overview");
        OverView ret;
        try {
            ret = (OverView) ois.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MCTClientProtocol.class.getName()).log(Level.SEVERE, null, ex);
            ret = null;
        }
        System.out.println("Read, done");

        disconnect();
        return ret;
    }

    private ObjectOutputStream getEncryptedOutputStream() throws IOException {
        try {
            Cipher c = Cipher.getInstance("AES/CFB8/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, sessionKey, new IvParameterSpec(getKeyBytes(sessionKey)));
            return new ObjectOutputStream(new CipherOutputStream(server.getOutputStream(), c));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return null;
        }
    }

    private ObjectInputStream getEncryptedInputStream() throws IOException {
        try {
            Cipher c = Cipher.getInstance("AES/CFB8/NoPadding");
            c.init(Cipher.DECRYPT_MODE, sessionKey, new IvParameterSpec(getKeyBytes(sessionKey)));
            return new ObjectInputStream(new CipherInputStream(server.getInputStream(), c));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return null;
        }
    }

    private byte[] getKeyBytes(SecretKey k) {
        byte[] key = k.getEncoded();
        byte[] keyBytes = new byte[16];
        System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, keyBytes.length));
        return keyBytes;
    }
}
