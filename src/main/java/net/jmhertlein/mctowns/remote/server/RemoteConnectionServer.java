/*
 * Copyright (C) 2013 Joshua Michael Hertlein <jmhertlein@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import javax.crypto.SecretKey;
import net.jmhertlein.core.crypto.Keys;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.remote.auth.CachedPublicIdentityManager;
import net.jmhertlein.mctowns.remote.auth.PublicIdentity;
import net.jmhertlein.mctowns.remote.auth.permissions.PermissionContext;

/**
 *
 * @author joshua
 */
public class RemoteConnectionServer extends Thread {
    private static final String PROTOCOL_VERSION = "1";

    private final CachedPublicIdentityManager identityManager;
    private final ExecutorService threadPool;
    private boolean done;
    private final ServerSocket server;
    private PrivateKey privateKey;
    private PublicKey pubKey;
    private final Map<Integer, ClientSession> sessions;
    private final PermissionContext permissions;
    private final MCTownsPlugin p;
    
    private int nextSessionID;

    /**
     *
     * @param p
     * @param authorizedKeysDirectory
     * @throws IOException if there's an error listening on the port
     */
    public RemoteConnectionServer(MCTownsPlugin p, File authorizedKeysDirectory) throws IOException {
        identityManager = new CachedPublicIdentityManager(authorizedKeysDirectory);
        threadPool = Executors.newCachedThreadPool();
        done = false;
        server = new ServerSocket(p.getConfig().getInt("remoteAdminPort"));
        this.p = p;
        sessions = new ConcurrentHashMap<>();

        permissions = new PermissionContext(MCTowns.getRemoteConfig());
        nextSessionID = 0;

        loadServerKeys();
    }

    @Override
    public void run() {
        while (!done) {
            Socket client;
            try {
                client = server.accept();
            } catch (IOException ex) {
                MCTowns.logWarning("Error accepting client connection: " + ex);
                MCTowns.logWarning("Bad connection or server socket closing.");
                continue;
            }

            threadPool.submit(new HandleRemoteClientTask(p, identityManager, client, this));
        }
    }

    public synchronized void close() {
        done = true;
        try {
            server.close();
        } catch (IOException ignore) {
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
            }
        } catch (InterruptedException ignore) {
        }
    }

    private void loadServerKeys() throws IOException {
        File keysDir = MCTowns.getServerKeysDir();

        File pubKeyFile = new File(keysDir, "server.pub"),
                privKeyFile = new File(keysDir, "server.private");

        boolean regenKeys = false;

        if (!pubKeyFile.exists()) {
            MCTowns.logWarning("Error loading pubkey. Will generate new keypair.");
            regenKeys = true;
        }

        if (!privKeyFile.exists()) {
            MCTowns.logWarning("Error loading private key. Will generate new keypair.");
            regenKeys = true;
        }

        if (regenKeys) {
            int length = p.getConfig().getInt("remoteAdminKeyLength");
            MCTowns.logWarning("Generating new key pair of length " + length + ", this may take a moment.");
            KeyPair pair = Keys.newRSAKeyPair(length);
            MCTowns.logInfo("New key pair generated.");

            Keys.storeKey(pubKeyFile, pair.getPublic());
            Keys.storeKey(privKeyFile, pair.getPrivate());
            this.privateKey = pair.getPrivate();
            this.pubKey = pair.getPublic();
        } else {
            this.pubKey = Keys.loadPubKey(pubKeyFile);
            this.privateKey = Keys.loadPrivateKey(privKeyFile);
            MCTowns.logInfo("Keys loaded from disk.");
        }
    }

    public Map<Integer, ClientSession> getSessions() {
        return sessions;
    }

    public PermissionContext getPermissions() {
        return permissions;
    }
    
    public synchronized int addNewSession(PublicIdentity identity, SecretKey sessionKey) {
        sessions.put(nextSessionID, new ClientSession(nextSessionID, identity, sessionKey));
        nextSessionID++;
        return nextSessionID - 1;
    }
    
    public ClientSession getSession(int sessionID) {
        return sessions.get(sessionID);
    }
    
    public void kickClient(int sessionID) {
        //TODO: Need to 
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }
    
}
