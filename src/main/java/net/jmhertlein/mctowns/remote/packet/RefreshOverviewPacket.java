/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.remote.packet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.remote.RemoteAction;
import net.jmhertlein.mctowns.remote.server.ClientSession;
import net.jmhertlein.mctowns.remote.server.RemoteConnectionServer;
import net.jmhertlein.mctowns.remote.view.OverView;

/**
 *
 * @author joshua
 */
public class RefreshOverviewPacket implements ClientPacket {

    @Override
    public void onServerReceive(ClientSession client, RemoteConnectionServer server, MCTownsPlugin plugin) {
        OverView v = new OverView(plugin.getConfig());
        try {
            client.getConnection().writeObject(1, v);
        } catch (IOException ex) {
            Logger.getLogger(RefreshOverviewPacket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public RemoteAction getAction() {
        return RemoteAction.GET_OVER_VIEW;
    }
}
