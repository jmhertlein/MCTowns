package net.jmhertlein.mctowns.remote.client;

import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.jmhertlein.core.crypto.CryptoManager;

/**
 *
 * @author joshua
 */
public class KeyLoader {
    private File rootDir;
    private File serverKeysDir;
    private Map<String, NamedKeyPair> loadedPairs;
    private Map<String, PublicKey> cachedServerKeys;
    private CryptoManager cMan;

    public KeyLoader(File rootDirPath) {
        rootDir = rootDirPath;
        cMan = new CryptoManager();
        loadedPairs = new HashMap<>();
        cachedServerKeys = new HashMap<>();
        serverKeysDir = new File(rootDir, "servers");
        
        serverKeysDir.mkdirs();

        for (File f : rootDir.listFiles()) {
            if(f.equals(serverKeysDir))
                continue;
            String pairName = f.getName();
            loadKey(pairName);
        }
        
        for(File f : serverKeysDir.listFiles()) {
            String serverHostname = f.getName().substring(0, f.getName().lastIndexOf('.'));
            loadServerPublicKey(serverHostname);
        }
    }

    /**
     * Returns a collection of all loaded pairs
     * @return  a collection of NamedKeyPair objects
     */
    public Collection<NamedKeyPair> getLoadedPairs() {
        return loadedPairs.values();
    }

    public NamedKeyPair getLoadedKey(String pairName) {
        return loadedPairs.get(pairName);
    }

    /**
     * Loads a key from disk and caches it
     * @param pairName 
     */
    public final void loadKey(String pairName) {
        File keyDir = new File(rootDir, pairName);
        loadedPairs.put(pairName, new NamedKeyPair(
                pairName,
                cMan.loadPubKey(new File(keyDir, pairName + ".pub")),
                cMan.loadPrivateKey(new File(keyDir, pairName + ".private"))));
    }
    
    /**
     * Drops a cached key, but does not delete it from disk
     * @param pairName 
     */
    private void dropKey(String pairName) {
        loadedPairs.remove(pairName);
    }

    public boolean deleteKey(String pairName) {
        dropKey(pairName);
        File keyDir = new File(rootDir, pairName);
        return deleteDir(keyDir);
    }

    /**
     * Adds a new key pair to the cache and immediately saves it to disk
     * @param label
     * @param newPair 
     */
    public void persistAndLoadNewKeyPair(String label, KeyPair newPair) {
        loadedPairs.put(label, new NamedKeyPair(label, newPair.getPublic(), newPair.getPrivate()));
        File keyDir = new File(rootDir, label);

        cMan.storeKey(new File(keyDir, label + ".pub").getPath(), newPair.getPublic());
        cMan.storeKey(new File(keyDir, label + ".private").getPath(), newPair.getPrivate());
    }

    private boolean deleteDir(File f)  {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                if(!deleteDir(c))
                    return false;
        }
        if (!f.delete())
            return false;
        
        return true;
    }
    
    /**
     * Loads the pubkey for the specified server from disk
     * @param hostname
     * @return 
     */
    public final PublicKey loadServerPublicKey(String hostname) {
        File serverKeyFile = new File(serverKeysDir, hostname + ".pub");
        PublicKey ret = cMan.loadPubKey(serverKeyFile);
        cachedServerKeys.put(hostname, ret);
        
        return ret;
    }
    
    /**
     * Gets the loaded server key
     * @param hostname
     * @return the key if it's loaded, null if it's not loaded or no key for that server
     */
    public PublicKey getLoadedServerPublicKey(String hostname) {
        return cachedServerKeys.get(hostname);
    }
    
    /**
     * Add the key to the cache and immediately store it on disk
     * @param hostname
     * @param pubKey 
     */
    public void addAndPersistServerPublicKey(String hostname, PublicKey pubKey) {
        cachedServerKeys.put(hostname, pubKey);
        
        File serverFile = new File(serverKeysDir, hostname + ".pub");
        cMan.storeKey(serverFile.getPath(), pubKey);
    }
    
    /**
     * Returns a collection of servers for which keys are currently loaded
     * @return 
     */
    public Collection<String> getListOfCachedServers() {
        return Collections.unmodifiableCollection(cachedServerKeys.keySet());
    }

    public boolean keyExistsByName(String label) {
        return loadedPairs.containsKey(label);
    }
    
    public void deleteServerPublicKey(String hostname) {
        cachedServerKeys.remove(hostname);
        File keyFile = new File(serverKeysDir, hostname + ".pub");
        keyFile.delete();
    }
}
