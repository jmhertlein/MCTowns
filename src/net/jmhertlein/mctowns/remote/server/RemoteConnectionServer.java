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
import net.jmhertlein.core.crypto.Keys;
import net.jmhertlein.mctowns.MCTownsPlugin;
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
    private Keys cMan;
    private PrivateKey privateKey;
    private PublicKey pubKey;
    private Map<Integer, ClientSession> sessionKeys;
    private MCTownsPlugin p;
    
    /**
     * 
     * @param authorizedKeysDirectory
     * @throws IOException if there's an error listening on the port
     */
    public RemoteConnectionServer(MCTownsPlugin p, File authorizedKeysDirectory) throws IOException {
        authKeysDir = authorizedKeysDirectory;
        threadPool = Executors.newCachedThreadPool();
        done = false;
        server = new ServerSocket(p.getConfig().getInt("remoteAdminPort"));
        this.p = p;
        sessionKeys = new ConcurrentHashMap<>();
        
        loadServerKeys();
        
    }

    @Override
    public void run() {
        while(!done) {
            Socket client;
            try {
                client = server.accept();
            } catch (IOException ex) {
                System.err.println("Error accepting client connection: " + ex);
                System.err.println("Bad connection or server socket closing.");
                continue;
            }
            
            threadPool.submit(new HandleRemoteClientTask(p, privateKey, pubKey, authKeysDir, client, sessionKeys));
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
        File keysDir = new File(p.getDataFolder(), MCTownsPlugin.RSA_KEYS_DIR_NAME);
        
        File pubKeyFile = new File(keysDir, "server.pub"),
                privKeyFile = new File(keysDir, "server.private");
        
        boolean regenKeys = false;
        
        if(!pubKeyFile.exists()) {
            System.err.println("Error loading pubkey. Will generate new keypair.");
            regenKeys = true;
        }
        
        if(!privKeyFile.exists()) {
            System.err.println("Error loading private key. Will generate new keypair.");
            regenKeys = true;
        }
        
        if(regenKeys) {
            int length = p.getConfig().getInt("remoteAdminKeyLength");
            System.out.println("Generating new key pair of length " + length + ", this may take a moment.");
            KeyPair pair = Keys.newRSAKeyPair(length);
            System.out.println("New key pair generated.");
            
            Keys.storeKey(pubKeyFile, pair.getPublic());
            Keys.storeKey(privKeyFile, pair.getPrivate());
            this.privateKey = pair.getPrivate();
            this.pubKey = pair.getPublic();
        } else {
            System.out.println("Loading keys from disk.");
            this.pubKey = Keys.loadPubKey(pubKeyFile);
            this.privateKey = Keys.loadPrivateKey(privKeyFile);
            System.out.println("Keys loaded from disk.");
        }
        
        System.out.println("Done loading server keys.");
    }
}
