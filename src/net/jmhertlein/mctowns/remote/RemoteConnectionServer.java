package net.jmhertlein.mctowns.remote;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
        threadPool = Executors.newFixedThreadPool(NUM_THREADS);
        done = false;
        server = new ServerSocket(SERVER_PORT);
        cMan = new CryptoManager();
        this.p = p;
        
        privateKey = cMan.loadPrivateKey(p.getConfig().getString("serverPrivateKey"));
        
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
            
            threadPool.submit(new HandleRemoteClientTask(cMan, authKeysDir, client));
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
    
    
}
