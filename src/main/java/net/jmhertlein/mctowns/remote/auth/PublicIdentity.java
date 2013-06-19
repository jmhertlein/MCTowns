/*
 * Copyright (C) 2013 joshua
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
package net.jmhertlein.mctowns.remote.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.Objects;
import net.jmhertlein.core.crypto.Keys;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


/**
 *
 * @author joshua
 */
public class PublicIdentity implements Serializable {
    private String username, permissionGroup;
    private PublicKey pubKey;
    
    public PublicIdentity(File f) throws FileNotFoundException, IOException, InvalidConfigurationException {
        YamlConfiguration c = new YamlConfiguration();
        c.load(f);
        loadFromFileConfiguration(c);
    }
    
    public PublicIdentity(FileConfiguration f) {
        loadFromFileConfiguration(f);
    }
    
    public PublicIdentity(String username, PublicKey pubKey) {
        this.username = username;
        this.pubKey = pubKey;
    }
    
    private void loadFromFileConfiguration(FileConfiguration f) {
        username = f.getString("username");
        pubKey = Keys.getPublicKeyFromBASE64X509Encoded(f.getString("pubKey"));
        permissionGroup = (f.getString("group") == null ? "default" : f.getString("group"));
    }

    public String getUsername() {
        return username;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public String getPermissionGroup() {
        return permissionGroup;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.username);
        hash = 83 * hash + Objects.hashCode(this.pubKey);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PublicIdentity other = (PublicIdentity) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.pubKey, other.pubKey)) {
            return false;
        }
        return true;
    }
    
    public void exportToConfiguration(FileConfiguration f) {
        f.set("username", username);
        f.set("pubKey", Keys.getBASE64ForPublicKey(pubKey));
        f.set("group", permissionGroup);
    }
}
