/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.remote.client;

import java.io.File;

/**
 *
 * @author joshua
 */
public class LocalClient {
    private static final File USER_STORAGE_DIR =
            new File(new File(System.getProperty("user.home")), ".mctRemoteAdminClient");
    
    
}
