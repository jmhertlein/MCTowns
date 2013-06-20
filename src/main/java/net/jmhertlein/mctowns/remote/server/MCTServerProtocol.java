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
import java.io.FileNotFoundException;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import net.jmhertlein.core.crypto.Keys;
import net.jmhertlein.core.location.Location;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.permission.Perms;
import net.jmhertlein.mctowns.remote.auth.AuthenticationChallenge;
import net.jmhertlein.mctowns.remote.auth.EncryptedSecretKey;
import net.jmhertlein.mctowns.remote.RemoteAction;
import net.jmhertlein.mctowns.remote.auth.PublicIdentity;
import net.jmhertlein.mctowns.remote.auth.permissions.PermissionContext;
import net.jmhertlein.mctowns.remote.auth.permissions.PermissionGroup;
import net.jmhertlein.mctowns.remote.auth.permissions.PermissionGroupType;
import net.jmhertlein.mctowns.remote.view.OverView;
import net.jmhertlein.mctowns.remote.view.PlayerView;
import net.jmhertlein.mctowns.remote.view.PlotView;
import net.jmhertlein.mctowns.remote.view.TerritoryView;
import net.jmhertlein.mctowns.remote.view.TownView;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import sun.misc.BASE64Encoder;

/**
 *
 * @author joshua
 */
public class MCTServerProtocol {

    private static final String PROTOCOL_VERSION = "1";
    private static final int NUM_CHECK_BYTES = 50;
    private File authKeysDir;
    private PublicKey serverPubKey;
    private PrivateKey serverPrivateKey;
    private Map<Integer, ClientSession> sessionKeys;
    private MCTownsPlugin p;
    private PermissionContext permissions;
    private static volatile Integer nextSessionID = 0;
    private ClientSession clientSession;
    private String clientName;
    private Socket client;
    private RemoteAction action;
    private PermissionGroup applicableGroup;

    public static String getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    public MCTServerProtocol(MCTownsPlugin p,
            Socket client,
            PrivateKey serverPrivateKey,
            PublicKey serverPublicKey,
            File authKeysDir,
            Map<Integer, ClientSession> sessionKeys,
            PermissionContext permissions) {
        this.authKeysDir = authKeysDir;
        this.serverPubKey = serverPublicKey;
        this.client = client;
        this.serverPrivateKey = serverPrivateKey;
        this.sessionKeys = sessionKeys;
        this.p = p;
        this.permissions = permissions;
    }

    private boolean doInitialKeyExchange() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

        oos.writeObject(PROTOCOL_VERSION);

        action = (RemoteAction) ois.readObject();

        if (action == RemoteAction.ABORT_CONNECTION) {
            return false;
        }

        clientName = (String) ois.readObject();

        System.out.println("Loading client key from disk.");
        PublicIdentity identity = loadIdentityFromDisk();

        if (identity == null) {
            System.out.println("Didnt have public key for user. Exiting.");
            oos.writeObject(false);
            return false;
        } else {
            oos.writeObject(true);
        }

        //init ciphers
        Cipher outCipher = Cipher.getInstance("RSA");
        outCipher.init(Cipher.ENCRYPT_MODE, identity.getPubKey());

        Cipher inCipher = Cipher.getInstance("RSA");
        inCipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);

        System.out.println("Sending our public key.");
        oos.writeObject(serverPubKey);

        Boolean clientAcceptsPublicKey = (Boolean) ois.readObject();

        if (!clientAcceptsPublicKey) {
            System.out.println("Client did not accept our public key- did not match their cached copy.");
            return false;
        }

        //send client auth challenge
        AuthenticationChallenge originalChallenge = new AuthenticationChallenge(NUM_CHECK_BYTES);
        oos.writeObject(originalChallenge.encrypt(outCipher));

        AuthenticationChallenge clientResponse = (AuthenticationChallenge) ois.readObject();
        clientResponse = clientResponse.decrypt(inCipher);

        if (clientResponse.equals(originalChallenge)) {
            oos.writeObject(true);
        } else {
            oos.writeObject(false);
            System.out.println("Rejecting client challenge response.");
            return false;
        }

        AuthenticationChallenge clientChallenge = (AuthenticationChallenge) ois.readObject();
        oos.writeObject(clientChallenge.decrypt(inCipher).encrypt(outCipher));

        Boolean clientAcceptsUs = (Boolean) ois.readObject();

        if (!clientAcceptsUs) {
            System.out.println("Client did not accept us as server they wanted to connect to.");
            return false;
        }

        SecretKey newKey = Keys.newAESKey(p.getConfig().getInt("remoteAdminSessionKeyLength"));

        oos.writeObject(new EncryptedSecretKey(newKey, outCipher));

        BASE64Encoder e = new BASE64Encoder();
        System.out.println(e.encode(newKey.getEncoded()));


        int assignedSessionID = nextSessionID;
        nextSessionID++;

        sessionKeys.put(assignedSessionID, new ClientSession(assignedSessionID, identity, newKey));
        System.out.println("Client assigned session id " + assignedSessionID);

        oos.writeObject(assignedSessionID);

        client.close();
        return true;
    }

    public void handleAction() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        //receive session ID
        byte[] clientSessionIDBytes = new byte[4];
        client.getInputStream().read(clientSessionIDBytes);
        int clientSessionID = ByteBuffer.wrap(clientSessionIDBytes).getInt();

        //if client indicates it does not have a session ID
        if (clientSessionID < 0) {
            if (!doInitialKeyExchange()) {
                System.err.println("User from " + client.getInetAddress() + " (Username: " + clientName + ")" + " tried to connect, but was not authorized.");
            }
            return;
        }

        clientSession = sessionKeys.get(clientSessionID);

        clientName = clientSession.getIdentity().getUsername();


        Cipher inCipher = Cipher.getInstance("AES/CFB8/NoPadding"), outCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        IvParameterSpec iv = new IvParameterSpec(getKeyBytes(clientSession.getSessionKey()));
        inCipher.init(Cipher.DECRYPT_MODE, clientSession.getSessionKey(), iv);
        outCipher.init(Cipher.ENCRYPT_MODE, clientSession.getSessionKey(), iv);

        CipherOutputStream cos = new CipherOutputStream(client.getOutputStream(), outCipher);
        CipherInputStream cis = new CipherInputStream(client.getInputStream(), inCipher);

        ObjectOutputStream oos = new ObjectOutputStream(cos);
        ObjectInputStream ois = new ObjectInputStream(cis);

        action = (RemoteAction) ois.readObject();
        applicableGroup = permissions.getGroups().get(clientSession.getIdentity().getPermissionGroup());

        p.getLogger().log(Level.INFO, "[RemoteAdmin]: {0} running action {1}", new Object[]{clientName, action.name()});

        if (permissions.userHasPermission(clientSession.getIdentity(), action)) {
            oos.writeObject(true);
            executeAction(oos, ois);
        } else {
            oos.writeObject(false);
        }

        client.close();
    }

    private void sendMetaView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        FileConfiguration f = p.getConfig();

        oos.writeObject(new OverView(f));
    }

    private byte[] getKeyBytes(SecretKey k) {
        byte[] key = k.getEncoded();
        byte[] keyBytes = new byte[16];
        System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, keyBytes.length));
        return keyBytes;
    }

    private void sendAllPlayersList(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        System.out.println("Creating list of all players ever played.");
        List<String> playerList = new LinkedList<>();

        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            playerList.add(offline.getName());
        }

        oos.writeObject(playerList);
        System.out.println("Sent list.");
    }

    private void sendPlayerView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String pName = (String) ois.readObject();
        OfflinePlayer player = p.getServer().getOfflinePlayer(pName);
        if (player == null) {
            oos.writeObject(null);
        } else {
            oos.writeObject(new PlayerView(p.getServer(), player, MCTowns.getTownManager()));
        }
    }

    private void sendAllTowns(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        List<String> ret = new LinkedList<>();
        for (Town t : MCTowns.getTownManager().getTownsCollection()) {
            ret.add(t.getTownName());
        }

        oos.writeObject(ret);
    }

    private void sendTownView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String tName = (String) ois.readObject();
        Town t = MCTowns.getTownManager().getTown(tName);
        if (t == null) {
            oos.writeObject(null);
        } else {
            oos.writeObject(new TownView(t));
        }
    }

    private void addIdentity(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        PublicIdentity i = (PublicIdentity) ois.readObject();

        Boolean result;

        FileConfiguration f = new YamlConfiguration();
        i.exportToConfiguration(f);

        f.save(i.getUsername() + ".pub");

        oos.writeObject(true);
    }

    private void sendIdentityList(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        List<PublicIdentity> ret = new LinkedList<>();
        for (File f : authKeysDir.listFiles()) {
            if (!f.getName().endsWith(".pub")) {
                continue;
            }
            PublicIdentity i;
            try {
                i = new PublicIdentity(f);
            } catch (FileNotFoundException | InvalidConfigurationException ex) {
                MCTowns.logSevere(String.format("Error parsing identity \"%s\": %s", f.getName(), ex.getLocalizedMessage()));
                continue;
            }

            ret.add(i);
        }

        oos.writeObject(ret);
    }

    private void deleteIdentity(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String deleteMe = (String) ois.readObject();

        File identityFile = new File(authKeysDir, deleteMe + ".pub");

        if (identityFile.exists()) {
            oos.writeObject(identityFile.delete());
        } else {
            oos.writeObject(false);
        }
    }

    private void sendTerritoryList(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String townName = (String) ois.readObject();
        Town t = MCTowns.getTownManager().getTown(townName);

        if (t == null) {
            oos.writeObject(new LinkedList<>());
        } else {
            oos.writeObject(new LinkedList<>(t.getTerritoriesCollection()));
        }
    }

    private void sendTerritoryView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String territName = (String) ois.readObject();
        Territory t = MCTowns.getTownManager().getTerritory(territName);
        if (t == null) {
            oos.writeObject(null);
        } else {
            oos.writeObject(new TerritoryView(t));
        }
    }

    private void deleteTerritory(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        final String territName = (String) ois.readObject();
        Future<Boolean> result;

        Callable<Boolean> c = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                Town parentTown = MCTowns.getTownManager().getTown(MCTowns.getTownManager().getTerritory(territName).getParentTown());
                if ((applicableGroup.getType() == PermissionGroupType.MAYOR && parentTown.playerIsMayor(clientName)) || applicableGroup.getType() == PermissionGroupType.ADMIN) {
                    return MCTowns.getTownManager().removeTerritory(territName);
                } else {
                    return false;
                }
            }
        };

        result = Bukkit.getScheduler().callSyncMethod(p, c);
        try {
            oos.writeObject(result.get());
        } catch (InterruptedException | ExecutionException ex) {
            oos.writeObject(false);
        }
    }

    private void sendPlotView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String plotName = (String) ois.readObject();
        Plot plot = MCTowns.getTownManager().getPlot(plotName);

        if (plot == null) {
            oos.writeObject(null);
        } else {
            oos.writeObject(new PlotView(plot));
        }
    }

    private void sendPlotList(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String territName = (String) ois.readObject();
        Territory t = MCTowns.getTownManager().getTerritory(territName);

        if (t == null) {
            oos.writeObject(null);
        } else {
            oos.writeObject(new LinkedList<>(t.getPlotsCollection()));
        }
    }

    private void deleteTown(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        final String townName = (String) ois.readObject();
        Future<Boolean> result;

        Callable<Boolean> c = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                Town t = MCTowns.getTownManager().getTown(townName);
                if ((t.playerIsMayor(clientName)
                        && applicableGroup.getType() == PermissionGroupType.MAYOR
                        && Bukkit.getPlayerExact(clientName).hasPermission(Perms.REMOVE_TOWN.toString()))
                        || applicableGroup.getType() == PermissionGroupType.ADMIN) {
                    return MCTowns.getTownManager().removeTown(townName);
                } else {
                    return false;
                }
            }
        };

        result = Bukkit.getScheduler().callSyncMethod(p, c);
        try {
            oos.writeObject(result.get());
        } catch (InterruptedException | ExecutionException ex) {
            oos.writeObject(false);
        }
    }

    private void createTown(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String townName = (String) ois.readObject();
        String mayorName = (String) ois.readObject();
        Location spawn = (Location) ois.readObject();

        if (Bukkit.getServer().getWorld(spawn.getWorld()) == null
                || Bukkit.getServer().getOfflinePlayer(mayorName) == null) {
            oos.writeObject(false);
            return;
        }
        org.bukkit.Location bukkitSpawn = Location.convertToBukkitLocation(Bukkit.getServer(), spawn);

        while (bukkitSpawn.getY() + 1 < bukkitSpawn.getWorld().getMaxHeight() && bukkitSpawn.getBlock().getType() != Material.AIR) {
            bukkitSpawn.setY(bukkitSpawn.getBlockY() + 1);
        }

        Boolean result = MCTowns.getTownManager().addTown(townName, mayorName, spawn) == null ? false : true;

        oos.writeObject(result);
    }

    private void modifyPlotMembership(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        final Integer opMode = (Integer) ois.readObject();
        final Integer membershipType = (Integer) ois.readObject();
        final String playerName = (String) ois.readObject();
        String plotName = (String) ois.readObject();

        final Plot plot = MCTowns.getTownManager().getPlot(plotName);

        if (plot == null) {
            oos.writeObject(false);
            return;
        }

        Callable<Boolean> c = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                Town t = MCTowns.getTownManager().getTown(plot.getParentTownName());
                if (!((t.playerIsMayor(clientName) && applicableGroup.getType() == PermissionGroupType.MAYOR)
                        || applicableGroup.getType() == PermissionGroupType.ADMIN)) {
                    return false;
                }


                if (opMode.intValue() == RemoteAction.MODE_ADD_PLAYER) {
                    if (membershipType == RemoteAction.GUEST) {
                        return plot.addGuest(playerName);
                    } else if (membershipType == RemoteAction.OWNER) {
                        return plot.addPlayer(playerName);
                    }
                } else if (opMode.intValue() == RemoteAction.MODE_DELETE_PLAYER) {
                    return plot.removePlayer(playerName);
                }

                return null;
            }
        };

        Future<Boolean> result = Bukkit.getScheduler().callSyncMethod(p, c);
        try {
            oos.writeObject(result.get());
        } catch (InterruptedException | ExecutionException ex) {
            oos.writeObject(false);
        }

    }

    private void modifyTerritoryMembership(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        final Integer opMode = (Integer) ois.readObject();
        final Integer membershipType = (Integer) ois.readObject();
        final String playerName = (String) ois.readObject();
        String territoryName = (String) ois.readObject();

        final Territory territ = MCTowns.getTownManager().getTerritory(territoryName);

        if (territ == null) {
            oos.writeObject(false);
            return;
        }

        Callable<Boolean> c = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                Town t = MCTowns.getTownManager().getTown(territ.getParentTown());
                if (!((t.playerIsMayor(clientName) && applicableGroup.getType() == PermissionGroupType.MAYOR)
                        || applicableGroup.getType() == PermissionGroupType.ADMIN)) {
                    return false;
                }
                if (opMode.intValue() == RemoteAction.MODE_ADD_PLAYER) {
                    if (membershipType == RemoteAction.GUEST) {
                        return territ.addGuest(playerName);
                    } else if (membershipType == RemoteAction.OWNER) {
                        return territ.addPlayer(playerName);
                    }
                } else if (opMode.intValue() == RemoteAction.MODE_DELETE_PLAYER) {
                    return territ.removePlayer(playerName);
                }

                return null;
            }
        };

        Future<Boolean> result = Bukkit.getScheduler().callSyncMethod(p, c);
        try {
            oos.writeObject(result.get());
        } catch (InterruptedException | ExecutionException ex) {
            oos.writeObject(false);
        }
    }

    private void modifyTownMembership(ObjectOutputStream oos, ObjectInputStream ois) throws ClassNotFoundException, IOException {
        Integer opMode = (Integer) ois.readObject();
        String playerName = (String) ois.readObject();
        String townName = (String) ois.readObject();

        if (Bukkit.getServer().getOfflinePlayer(playerName) == null) {
            oos.writeObject(false);
            return;
        }

        Town town = MCTowns.getTownManager().getTown(townName);

        if (town == null) {
            oos.writeObject(false);
            return;
        }

        if (!((town.playerIsMayor(clientName) && applicableGroup.getType() == PermissionGroupType.MAYOR)
                || applicableGroup.getType() == PermissionGroupType.ADMIN)) {
            oos.writeObject(false);
            return;
        }

        Boolean result = null;

        if (opMode.intValue() == RemoteAction.MODE_ADD_PLAYER) {
            result = town.addPlayer(playerName);
        } else if (opMode.intValue() == RemoteAction.MODE_DELETE_PLAYER) {
            town.removePlayer(playerName);
            result = true;
        }

        oos.writeObject(result);
    }

    private void modifyTownAssistants(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Integer opMode = (Integer) ois.readObject();
        String playerName = (String) ois.readObject();
        String townName = (String) ois.readObject();

        Town town = MCTowns.getTownManager().getTown(townName);

        if (town == null) {
            oos.writeObject(false);
            return;
        }

        if (!((town.playerIsMayor(clientName) && applicableGroup.getType() == PermissionGroupType.MAYOR)
                || applicableGroup.getType() == PermissionGroupType.ADMIN)) {
            oos.writeObject(false);
            return;
        }

        Boolean result = null;
        if (opMode.intValue() == RemoteAction.MODE_ADD_PLAYER) {
            result = town.addAssistant(playerName);
        } else if (opMode.intValue() == RemoteAction.MODE_DELETE_PLAYER) {
            result = town.removeAssistant(playerName);
        }

        oos.writeObject(result);
    }

    private void updateTown(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        TownView view = (TownView) ois.readObject();

        Town t = MCTowns.getTownManager().getTown(view.getTownName());

        if (t == null) {
            oos.writeObject(false);
            return;
        }

        if (!((t.playerIsMayor(clientName) && applicableGroup.getType() == PermissionGroupType.MAYOR)
                || applicableGroup.getType() == PermissionGroupType.ADMIN)) {
            oos.writeObject(false);
            return;
        }

        t.updateTown(view);

        oos.writeObject(true);
    }

    private void updatePlot(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        PlotView view = (PlotView) ois.readObject();

        Plot plot = MCTowns.getTownManager().getPlot(view.getPlotName());

        if (plot == null) {
            oos.writeObject(false);
            return;
        }

        Town town = MCTowns.getTownManager().getTown(plot.getParentTownName());

        if (!((town.playerIsMayor(clientName) && applicableGroup.getType() == PermissionGroupType.MAYOR)
                || applicableGroup.getType() == PermissionGroupType.ADMIN)) {
            oos.writeObject(false);
            return;
        }

        plot.updatePlot(view);

        oos.writeObject(true);
    }

    private void deletePlot(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        final String plotName = (String) ois.readObject();
        Future<Boolean> result;

        Callable<Boolean> c = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                Town t = MCTowns.getTownManager().getTown(MCTowns.getTownManager().getPlot(plotName).getParentTownName());

                if ((t.playerIsMayor(clientName) && applicableGroup.getType() == PermissionGroupType.MAYOR)
                        || applicableGroup.getType() == PermissionGroupType.ADMIN) {
                    return MCTowns.getTownManager().removePlot(plotName);
                } else {
                    return false;
                }
            }
        };

        result = Bukkit.getScheduler().callSyncMethod(p, c);
        try {
            oos.writeObject(result.get());
        } catch (InterruptedException | ExecutionException ex) {
            oos.writeObject(false);
        }
    }

    private void updateConfig(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        final OverView v = (OverView) ois.readObject();

        Future<Boolean> result;

        Callable<Boolean> c = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                v.applyUpdates();
                return true;
            }
        };

        result = Bukkit.getScheduler().callSyncMethod(p, c);
        try {
            oos.writeObject(result.get());
        } catch (InterruptedException | ExecutionException ex) {
            oos.writeObject(false);
        }
    }

    private PublicIdentity loadIdentityFromDisk() throws IOException {
        for (File f : authKeysDir.listFiles()) {
            if (!f.getName().endsWith(".pub")) {
                continue;
            }

            PublicIdentity i;
            try {
                i = new PublicIdentity(f);
            } catch (FileNotFoundException | InvalidConfigurationException ex) {
                MCTowns.logWarning(String.format("Error parsing identity file \"%s\": %s", f.getName(), ex.getLocalizedMessage()));
                continue;
            }

            if (i.getUsername().equals(clientName)) {
                return i;
            }
        }

        return null;
    }

    private void executeAction(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        switch (action) {
            case GET_META_VIEW:
                sendMetaView(oos, ois);
                break;
            case GET_PLAYER_LIST:
                sendAllPlayersList(oos, ois);
                break;
            case GET_VIEW_FOR_PLAYER:
                sendPlayerView(oos, ois);
                break;
            case GET_TOWN_LIST:
                sendAllTowns(oos, ois);
                break;
            case GET_TOWN_VIEW:
                sendTownView(oos, ois);
                break;
            case ADD_IDENTITY:
                addIdentity(oos, ois);
                break;
            case GET_IDENTITY_LIST:
                sendIdentityList(oos, ois);
                break;
            case DELETE_IDENTITY:
                deleteIdentity(oos, ois);
                break;
            case GET_TERRITORY_LIST:
                sendTerritoryList(oos, ois);
                break;
            case GET_TERRITORY_VIEW:
                sendTerritoryView(oos, ois);
                break;
            case DELETE_TERRITORY:
                deleteTerritory(oos, ois);
                break;
            case GET_PLOT_VIEW:
                sendPlotView(oos, ois);
                break;
            case GET_PLOTS_LIST:
                sendPlotList(oos, ois);
                break;
            case DELETE_TOWN:
                deleteTown(oos, ois);
                break;
            case CREATE_TOWN:
                createTown(oos, ois);
                break;
            case MODIFY_PLOT_MEMBERSHIP:
                modifyPlotMembership(oos, ois);
                break;
            case MODIFY_TERRITORY_MEMBERSHIP:
                modifyTerritoryMembership(oos, ois);
                break;
            case MODIFY_TOWN_MEMBERSHIP:
                modifyTownMembership(oos, ois);
                break;
            case MODIFY_TOWN_ASSISTANTS:
                modifyTownAssistants(oos, ois);
                break;
            case UPDATE_TOWN:
                updateTown(oos, ois);
                break;
            case UPDATE_PLOT:
                updatePlot(oos, ois);
                break;
            case DELETE_PLOT:
                deletePlot(oos, ois);
                break;
            case UPDATE_CONFIG:
                updateConfig(oos, ois);
                break;

        }
    }
}
