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
package net.jmhertlein.mctowns.remote.packet;

import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.remote.RemoteAction;
import net.jmhertlein.mctowns.remote.server.ClientSession;
import net.jmhertlein.mctowns.remote.server.RemoteConnectionServer;

/**
 *
 * @author Joshua Michael Hertlein <jmhertlein@gmail.com>
 */
public interface ClientPacket {
    /**
     * Run server-side when the packet is received
     * @param client the ClientSession of the client who sent the packet
     * @param server the remote connection server object
     * @param plugin the MCTowns plugin object
     */
    public void onServerReceive(ClientSession client, RemoteConnectionServer server, MCTownsPlugin plugin);
    
    /**
     * 
     * @return the RemoteAction associated with the packet being sent
     */
    public RemoteAction getAction();
}
