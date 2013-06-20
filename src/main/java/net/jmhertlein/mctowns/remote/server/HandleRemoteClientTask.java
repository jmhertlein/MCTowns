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
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.remote.auth.permissions.PermissionContext;

/**
 *
 * @author joshua
 */
public class HandleRemoteClientTask implements Runnable {

    private static final int NUM_CHECK_BYTES = 500;
    private MCTServerProtocol protocol;

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
            Map<Integer, ClientSession> sessionKeys,
            PermissionContext permissions) {
        protocol = new MCTServerProtocol(p, client, privateKey, pubKey, authKeysDir, sessionKeys, permissions);
    }

    @Override
    public void run() {
        try {
            protocol.handleAction();
        } catch (IOException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(HandleRemoteClientTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
