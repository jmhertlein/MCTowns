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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import net.jmhertlein.core.crypto.Keys;
import net.jmhertlein.core.io.ChanneledConnectionManager;
import net.jmhertlein.core.io.PacketReceiveListener;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.remote.auth.CachedPublicIdentityManager;
import net.jmhertlein.mctowns.remote.auth.EncryptedSecretKey;
import net.jmhertlein.mctowns.remote.auth.PublicIdentity;
import net.jmhertlein.mctowns.remote.packet.ClientPacket;

/**
 *
 * @author joshua
 */
public class HandleRemoteClientTask implements Runnable {
    private final MCTownsPlugin p;
    private final PrivateKey serverPrivateKey;
    private final PublicKey serverPubKey;
    private final CachedPublicIdentityManager identityManager;
    private final Socket client;
    private final RemoteConnectionServer server;

    /**
     *
     * @param p
     * @param identityManager
     * @param client the client socket this task will interface with
     * @param server
     */
    public HandleRemoteClientTask(MCTownsPlugin p,
            CachedPublicIdentityManager identityManager,
            Socket client,
            RemoteConnectionServer server) {
        this.p = p;
        
        this.server = server;
        this.serverPrivateKey = server.getPrivateKey();
        this.serverPubKey = server.getPubKey();
        this.identityManager = identityManager;
        this.client = client;
    }

    @Override
    public void run() {
        int sessionID = readSessionID();
        System.out.println("Client with id " + sessionID + " is connecting.");
        
        if (sessionID < 0) {
            try {
                doInitialKeyExchange(new ObjectOutputStream(client.getOutputStream()), new ObjectInputStream(client.getInputStream()));
            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                System.err.println("Error authenticating client.");
                Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Done");
        } else {
            ClientSession session = server.getSession(sessionID);
            
            System.out.println("Opening encrypted streams...");
            ChanneledConnectionManager conMan = openChanneledSecureConnection(session.getSessionKey(), client);
            System.out.println("Opened.");

            conMan.addPacketReceiveListener(new PacketReceiveListener() {
                @Override
                public boolean onPacketReceive(Object data, int channel) {
                    if (channel == 0) {
                        ((ClientPacket) data).onServerReceive(server, p);
                        return false;
                    }

                    return true;
                }
            });
            System.out.println("Added listener for client. Exiting.");
        }
    }

    private boolean doInitialKeyExchange(ObjectOutputStream os, ObjectInputStream is) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //write the protocol version we're going to use
        System.out.println("Writing version...");
        os.writeUTF(MCTServerProtocol.getProtocolVersion());
        os.flush();
        System.out.println("Reading response to version...");
        //check to see if they're aborting connecting due to protocol version mismatch
        if (!is.readBoolean()) {
            return false;
        }

        //receive the client's pubkey
        System.out.println("Reading client key");
        PublicKey receivedClientKey = (PublicKey) is.readObject();
        System.out.println("Read client key.");

        //see if the client's pubkey is in our cache and is matched to a name
        System.out.println("Checking if key is in cache...");
        PublicIdentity clientIdentity = identityManager.getPublicIdentityByPublicKey(receivedClientKey);
        System.out.println("Key cache checking done.");

        //if we have the identity, tell them we're proceeding
        if (clientIdentity != null) {
            System.out.println("Had key, okay.");
            os.writeBoolean(true);
        } //else, tell them we're stopping because they're not authorized
        else {
            System.out.println("No key, no-go.");
            os.writeBoolean(false);
        }
        //os.flush();

        //Now that we have their pubkey, we can prepare for authentication challenges.
        Cipher outCipher = Cipher.getInstance("RSA");
        outCipher.init(Cipher.ENCRYPT_MODE, clientIdentity.getPubKey());

        Cipher inCipher = Cipher.getInstance("RSA");
        inCipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);

        //send them our pubkey
        System.out.println("Writing our pubkey");
        os.writeObject(serverPubKey);
        System.out.println("Our key written");

        //see if they accept our pubkey
        //if they do, we're good
        //else, abort connecting
        System.out.println("Checking if client accepts our key");
        if (!is.readBoolean()) {
            System.out.println("They didn't like it, quitting.");
            return false;
        } else {
            System.out.println("They were okay with it, proceeding.");
        }

        //we're skipping the stupid 'authentication challenges' things because there's no point
        //prepare the session key
        SecretKey newKey = Keys.newAESKey(MCTowns.getRemoteAdminSessionKeyLength());

        //write it for them
        System.out.println("Writing session key");
        os.writeObject(new EncryptedSecretKey(newKey, outCipher));
        System.out.println("Session key written");

        //no need to give them a session ID this time
        //also no need to cache their session key more globally
        int newSessionID = server.addNewSession(clientIdentity, newKey);

        os.writeInt(newSessionID);
        os.flush();
        client.close();

        //write details of their login to log
        MCTowns.logInfo(String.format("%s logged in via the Remote Admin Client (%s)", clientIdentity.getUsername(), client.getInetAddress().toString()));
        System.out.println("All done.");
        return true;
    }

    private static ChanneledConnectionManager openChanneledSecureConnection(SecretKey sessionKey, Socket client) {
        try {
            ObjectOutputStream toos = Keys.getEncryptedObjectOutputStream(sessionKey, client.getOutputStream());
            toos.flush();
            ObjectInputStream tois = Keys.getEncryptedObjectInputStream(sessionKey, client.getInputStream());
            return new ChanneledConnectionManager(toos, tois);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IOException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private int readSessionID() {
        byte[] clientSessionIDBytes = new byte[4];
        try {
            client.getInputStream().read(clientSessionIDBytes);
        } catch (IOException ex) {
            return -2;
        }
        return ByteBuffer.wrap(clientSessionIDBytes).getInt();
    }
}
