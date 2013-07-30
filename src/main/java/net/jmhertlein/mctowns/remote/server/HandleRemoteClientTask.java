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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import net.jmhertlein.core.crypto.Keys;
import net.jmhertlein.core.io.ChanneledConnectionManager;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.remote.auth.permissions.PermissionContext;

/**
 *
 * @author joshua
 */
public class HandleRemoteClientTask implements Runnable {

    private static final int NUM_CHECK_BYTES = 500;
    private MCTownsPlugin p;
    private PrivateKey serverPrivateKey;
    private PublicKey serverPubKey, clientPubKey;
    private SecretKey sessionKey;
    private File authKeysDir;
    private Socket client;
    private PermissionContext permissions;
    private ChanneledConnectionManager connection;

    /**
     *
     * @param cMan cryptomanager that will be shared with other tasks
     * @param privateKey private RSA key for the server
     * @param pubKey public RSA key for the server
     * @param authKeysDir Directory to hold pubkeys of authorized users
     * @param client the client socket this task will interface with
     */
    public HandleRemoteClientTask(MCTownsPlugin p,
            PrivateKey privateKey,
            PublicKey pubKey,
            File authKeysDir,
            Socket client,
            PermissionContext permissions) {
        this.p = p;
        this.serverPrivateKey = privateKey;
        this.serverPubKey = pubKey;
        this.authKeysDir = authKeysDir;
        this.client = client;
        this.permissions = permissions;
    }

    @Override
    public void run() {
    }

    private boolean doInitialKeyExchange(OutputStream os, InputStream is) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //write the protocol version we're going to use

        //check to see if they're aborting connecting due to protocol version mismatch

        //receive the client's pubkey

        //see if the client's pubkey is in our cache and is matched to a name

        //if we have the identity, tell them we're proceeding
        //else, tell them we're stopping because they're not authorized

        //Now that we have their pubkey, we can prepare for authentication challenges.
        Cipher outCipher = Cipher.getInstance("RSA");
        //outCipher.init(Cipher.ENCRYPT_MODE, identity.getPubKey());

        Cipher inCipher = Cipher.getInstance("RSA");
        //inCipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);

        //send them our pubkey

        //see if they accept our pubkey

        //if they do, we're good
        //else, abort connecting

        //we're skipping the stupid 'authentication challenges' things because there's no point

        //prepare the session key
        SecretKey newKey = Keys.newAESKey(MCTowns.getRemoteAdminSessionKeyLength());
        
        //write it for them
        //oos.writeObject(new EncryptedSecretKey(newKey, outCipher));

        //no need to give them a session ID this time
        //also no need to cache their session key more globally
        //sessionKeys.put(assignedSessionID, new ClientSession(assignedSessionID, identity, newKey));

        //write details of their login to log
        //MCTowns.logInfo(String.format("%s logged in via the Remote Admin Client (%s)", clientName, client.getInetAddress().toString()));
        return true;
    }
    
    private ChanneledConnectionManager openChanneledSecureConnection() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return new ChanneledConnectionManager(
                Keys.getEncryptedObjectOutputStream(sessionKey, client.getOutputStream()), 
                Keys.getEncryptedObjectInputStream(sessionKey, client.getInputStream())
                );
    }
}
