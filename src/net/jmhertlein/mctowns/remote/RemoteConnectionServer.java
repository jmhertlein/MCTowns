package net.jmhertlein.mctowns.remote;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.core.crypto.CryptoManager;
import org.bukkit.plugin.Plugin;


/**
 *
 * @author joshua
 */
public class RemoteConnectionServer extends Thread {
    public static final int NUM_THREADS = 2, SERVER_PORT = 3333;
    
    private File authKeysDir;
    private ExecutorService threadPool;
    private boolean done;
    private ServerSocket server;
    private CryptoManager cMan;
    private PrivateKey privateKey;
    private PublicKey pubKey;
    private Plugin p;
    
    /**
     * 
     * @param authorizedKeysDirectory
     * @throws IOException if there's an error listening on the port
     */
    public RemoteConnectionServer(Plugin p, File authorizedKeysDirectory) throws IOException {
        authKeysDir = authorizedKeysDirectory;
        threadPool = Executors.newCachedThreadPool();
        done = false;
        server = new ServerSocket(SERVER_PORT);
        cMan = new CryptoManager();
        this.p = p;
        
        loadServerKeys();
        
    }

    @Override
    public void run() {
        while(!done) {
            Socket client;
            try {
                System.out.println("Listening on port.");
                client = server.accept();
                System.out.println("Got new client.");
            } catch (IOException ex) {
                System.err.println("Error accepting client connection: " + ex);
                System.err.println("Bad connection or server socket closing.");
                continue;
            }
            
            threadPool.submit(new HandleRemoteClientTask(cMan, privateKey, pubKey, authKeysDir, client));
            System.out.println("Submitted client to thread pool.");
        }
    }
    
    public synchronized void close() {
        done = true;
        try {
            server.close();
        } catch (IOException ignore) {}
        threadPool.shutdown();
        try {
            while(!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {}
        } catch (InterruptedException ignore) {}
        System.out.println("Closed cleanly.");
    }
    
    public static void main(String[] args) {
        RemoteConnectionServer s;
        
        try {
            s = new RemoteConnectionServer(null, new File("mct_auth_keys"));
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnectionServer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        System.out.println("Starting server...");
        s.start();
    }

    private void loadServerKeys() throws IOException {
        File keysDir = new File(p.getDataFolder(), "rsa_keys");
        keysDir.mkdirs();
        
        File pubKeyFile = new File(keysDir, "server.pub"),
                privKeyFile = new File(keysDir, "server.private");
        
        boolean regenKeys = false;
        
        if(!pubKeyFile.exists()) {
            System.err.println("Error loading pubkey. Will generate new keypair.");
            pubKeyFile.createNewFile();
            regenKeys = true;
        }
        
        if(!privKeyFile.exists()) {
            System.err.println("Error loading private key. Will generate new keypair.");
            privKeyFile.createNewFile();
            regenKeys = true;
        }
        
        if(regenKeys) {
            int length = p.getConfig().getInt("rsaKeyPairLength");
            System.out.println("Generating new key pair of length " + length + ", this may take a moment.");
            KeyPair pair = CryptoManager.newRSAKeyPair(length);
            System.out.println("New key pair generated.");
            
            cMan.storeKey(pubKeyFile, pair.getPublic());
            cMan.storeKey(privKeyFile, pair.getPrivate());
            this.privateKey = pair.getPrivate();
            this.pubKey = pair.getPublic();
        } else {
            System.out.println("Loading keys from disk.");
            this.pubKey = cMan.loadPubKey(pubKeyFile);
            this.privateKey = cMan.loadPrivateKey(privKeyFile);
            System.out.println("Keys loaded from disk.");
        }
        
        System.out.println("Done loading server keys.");
    }
    
    
}
