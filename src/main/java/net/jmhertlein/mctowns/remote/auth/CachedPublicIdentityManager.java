/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.remote.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author joshua
 */
public class CachedPublicIdentityManager {

    private final File cachedKeysDir;
    private final Set<PublicIdentity> loadedIdentities;

    /**
     * Creates a new manager
     *
     * @param cachedKeysDir the directory to use for the on-disk cache
     */
    public CachedPublicIdentityManager(File cachedKeysDir) {
        this.cachedKeysDir = cachedKeysDir;
        loadedIdentities = new HashSet<>();
    }

    /**
     *
     * @param identity
     * @return whether or not the key is on disk
     */
    public boolean publicIdentityIsCached(PublicIdentity identity) {
        //check to see if the key has already been loaded into RAM
        if (loadedIdentities.contains(identity)) {
            return true;
        }

        //it wasn't in RAM, so let's check on disk.
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(cachedKeysDir.toPath());) {
            for (Path p : dirStream) {
                if (!p.endsWith(".pub")) {
                    continue;
                }

                PublicIdentity cur = new PublicIdentity(p.toFile());
                loadedIdentities.add(cur);

                if (cur.equals(identity)) {
                    return true;
                }
            }
        } catch (IOException ex) {
            System.err.println("Error reading cached keys directory: " + cachedKeysDir.getAbsolutePath());
            Logger.getLogger(CachedPublicIdentityManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            System.err.println("Malformed YML file.");
            Logger.getLogger(CachedPublicIdentityManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        //wasn't in RAM or on disk.
        return false;
    }

    /**
     * Adds the public key to the on-disk cache.
     *
     * @param identity
     * @throws IOException thrown if there is an error writing the file to disk
     */
    public void cachePublicIdentity(PublicIdentity identity) throws IOException {
        loadedIdentities.add(identity);
        FileConfiguration f = new YamlConfiguration();
        identity.exportToConfiguration(f);
        f.save(new File(cachedKeysDir, identity.getFilename()));
    }

    /**
     * Completely deletes the identity from the on-disk cache.
     *
     * @param identity
     * @return true if the file was deleted successfully, false otherwise
     */
    public boolean deletePublicIdentity(PublicIdentity identity) {
        loadedIdentities.remove(identity);
        return new File(cachedKeysDir, identity.getFilename()).delete();
    }

    /**
     * Flushes any loaded keys from RAM. Does not delete the keys on disk.
     *
     * Useful if many keys were loaded but most are not frequently used.
     */
    public void flush() {
        loadedIdentities.clear();
    }

    /**
     * 
     * @return An unmodifiable set of all public identities present in the on-disk cache
     */
    public Set<PublicIdentity> getAllCachedIdentities() {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(cachedKeysDir.toPath());) {
            for (Path p : dirStream) {
                System.out.println("Checking path: " + p.toString());
                System.out.println(p.getFileName());
                if (!p.getFileName().toString().endsWith(".pub")) {
                    continue;
                }

                PublicIdentity cur = new PublicIdentity(p.toFile());
                loadedIdentities.add(cur);
            }
        } catch (IOException ex) {
            System.err.println("Error reading cached keys directory: " + cachedKeysDir.getAbsolutePath());
            Logger.getLogger(CachedPublicIdentityManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            System.err.println("Malformed YML file.");
            Logger.getLogger(CachedPublicIdentityManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Collections.unmodifiableSet(loadedIdentities);
    }

    /**
     * 
     * @param key
     * @return the public identity whose pubkey matches the specified key, or null if no such identity is found in the on-disk cache
     */
    public PublicIdentity getPublicIdentityByPublicKey(PublicKey key) {
        System.out.println("Checking cached identities...");
        for (PublicIdentity identity : this.getAllCachedIdentities()) {
            System.out.println("Checking identity file for: " + identity.getUsername());
            if (identity.getPubKey().equals(key)) {
                return identity;
            }
        }

        return null;
    }

}
