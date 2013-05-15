package net.jmhertlein.mctowns.remote.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.jmhertlein.core.crypto.CryptoManager;
import net.jmhertlein.mctowns.MCTowns;
import org.bukkit.plugin.Plugin;


/**
 *
 * @author joshua
 */
public class RemoteConnectionServer extends Thread {
    
    private File authKeysDir;
    private ExecutorService threadPool;
    private boolean done;
    private ServerSocket server;
    private CryptoManager cMan;
    private PrivateKey privateKey;
    private PublicKey pubKey;
    private Map<Integer, ClientSession> sessionKeys;
    private MCTowns p;
    
    /**
     * 
     * @param authorizedKeysDirectory
     * @throws IOException if there's an error listening on the port
     */
    public RemoteConnectionServer(MCTowns p, File authorizedKeysDirectory) throws IOException {
        authKeysDir = authorizedKeysDirectory;
        threadPool = Executors.newCachedThreadPool();
        done = false;
        server = new ServerSocket(p.getConfig().getInt("remoteAdminPort"));
        cMan = new CryptoManager();
        this.p = p;
        sessionKeys = new ConcurrentHashMap<>();
        
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
            
            threadPool.submit(new HandleRemoteClientTask(p, privateKey, pubKey, authKeysDir, client, sessionKeys));
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

    private void loadServerKeys() throws IOException {
        File keysDir = new File(p.getDataFolder(), MCTowns.RSA_KEYS_DIR_NAME);
        
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
            int length = p.getConfig().getInt("remoteAdminKeyLength");
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
