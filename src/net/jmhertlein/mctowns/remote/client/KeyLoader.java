package net.jmhertlein.mctowns.remote.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.jmhertlein.core.crypto.CryptoManager;

/**
 *
 * @author joshua
 */
public class KeyLoader {

    private File rootDir;
    private Map<String, NamedKeyPair> loadedPairs;
    private CryptoManager cMan;

    public KeyLoader(File rootDirPath) {
        rootDir = rootDirPath;
        cMan = new CryptoManager();
        loadedPairs = new HashMap<>();

        for (File f : rootDir.listFiles()) {
            String pairName = f.getName();
            loadKey(pairName);
        }
    }

    public Collection<NamedKeyPair> getLoadedPairs() {
        return loadedPairs.values();
    }

    public NamedKeyPair getLoadedKey(String pairName) {
        return loadedPairs.get(pairName);
    }

    public final void loadKey(String pairName) {
        File keyDir = new File(rootDir, pairName);
        loadedPairs.put(pairName, new NamedKeyPair(
                pairName,
                cMan.loadPubKey(new File(keyDir, pairName + ".pub")),
                cMan.loadPrivateKey(new File(keyDir, pairName + ".private"))));
    }

    public void dropKey(String pairName) {
        loadedPairs.remove(pairName);
    }

    public boolean deleteKey(String pairName) {
        dropKey(pairName);
        File keyDir = new File(rootDir, pairName);
        return deleteDir(keyDir);
    }

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
}
