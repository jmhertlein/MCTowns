package net.jmhertlein.mctowns.remote.server;

import java.io.File;
import java.io.FilenameFilter;
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
import net.jmhertlein.mctowns.remote.auth.AuthenticationChallenge;
import net.jmhertlein.mctowns.remote.auth.EncryptedSecretKey;
import net.jmhertlein.mctowns.remote.RemoteAction;
import net.jmhertlein.mctowns.remote.auth.Identity;
import net.jmhertlein.mctowns.remote.view.OverView;
import net.jmhertlein.mctowns.remote.view.PlayerView;
import net.jmhertlein.mctowns.remote.view.PlotView;
import net.jmhertlein.mctowns.remote.view.TerritoryView;
import net.jmhertlein.mctowns.remote.view.TownView;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import sun.misc.BASE64Encoder;

/**
 *
 * @author joshua
 */
public class MCTServerProtocol {

    private static final int NUM_CHECK_BYTES = 50;
    private File authKeysDir;
    private PublicKey serverPubKey;
    private PrivateKey serverPrivateKey;
    private Map<Integer, ClientSession> sessionKeys;
    private MCTowns p;
    private static volatile Integer nextSessionID = 0;
    
    private ClientSession clientSession;
    private String clientName;
    private Socket client;
    private RemoteAction action;

    public MCTServerProtocol(MCTowns p, Socket client, PrivateKey serverPrivateKey, PublicKey serverPublicKey, File authKeysDir, Map<Integer, ClientSession> sessionKeys) {
        this.authKeysDir = authKeysDir;
        this.serverPubKey = serverPublicKey;
        this.client = client;
        this.serverPrivateKey = serverPrivateKey;
        this.sessionKeys = sessionKeys;
        this.p = p;
    }
    
    private boolean doInitialKeyExchange() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

        action = (RemoteAction) ois.readObject();

        clientName = (String) ois.readObject();
        
        System.out.println("Loading client key from disk.");
        PublicKey clientKey = Keys.loadPubKey(new File(authKeysDir, clientName + ".pub"));


        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

        if (clientKey == null) {
            System.out.println("Didnt have public key for user. Exiting.");
            oos.writeObject(false);
            return false;
        } else {
            oos.writeObject(true);
        }

        //init ciphers
        Cipher outCipher = Cipher.getInstance("RSA");
        outCipher.init(Cipher.ENCRYPT_MODE, clientKey);

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

        sessionKeys.put(assignedSessionID, new ClientSession(assignedSessionID, clientName, newKey));
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
        
        
        Cipher inCipher = Cipher.getInstance("AES/CFB8/NoPadding"), outCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        IvParameterSpec iv = new IvParameterSpec(getKeyBytes(clientSession.getSessionKey()));
        inCipher.init(Cipher.DECRYPT_MODE, clientSession.getSessionKey(), iv);
        outCipher.init(Cipher.ENCRYPT_MODE, clientSession.getSessionKey(), iv);

        CipherOutputStream cos = new CipherOutputStream(client.getOutputStream(), outCipher);
        CipherInputStream cis = new CipherInputStream(client.getInputStream(), inCipher);
        
        ObjectOutputStream oos = new ObjectOutputStream(cos);
        ObjectInputStream ois = new ObjectInputStream(cis);
        
        action = (RemoteAction) ois.readObject();
        clientName = (String) ois.readObject();
        
        p.getLogger().log(Level.INFO, "[RemoteAdmin]: {0} running action {1}", new Object[]{clientName, action.name()});

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
            case UPDATE_TOWN:
                updateTown(oos, ois);
                break;
            case UPDATE_PLOT:
                updatePlot(oos, ois);
                break;
                
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
        
        for(OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            playerList.add(offline.getName());
        }
        
        oos.writeObject(playerList);
        System.out.println("Sent list.");
    }

    private void sendPlayerView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String pName = (String) ois.readObject();
        OfflinePlayer player = p.getServer().getOfflinePlayer(pName);
        if(player == null)
            oos.writeObject(null);
        else
            oos.writeObject(new PlayerView(p.getServer(), player, MCTowns.getTownManager()));
    }

    private void sendAllTowns(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        List<String> ret = new LinkedList<>();
        for(Town t : MCTowns.getTownManager().getTownsCollection()) {
            ret.add(t.getTownName());
        }
        
        oos.writeObject(ret);
    }
    
    private void sendTownView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String tName = (String) ois.readObject();
        Town t = MCTowns.getTownManager().getTown(tName);
        if(t == null)
            oos.writeObject(null);
        else
            oos.writeObject(new TownView(t));
    }

    private void addIdentity(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Identity i = (Identity) ois.readObject();
        
        Boolean result = Keys.storeKey(new File(authKeysDir, i.getName() + ".pub"), i.getPubKey());
        
        oos.writeObject(result);
    }

    private void sendIdentityList(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".pub");
            }
        };
        List<Identity> ret = new LinkedList<>();
        for(File f : authKeysDir.listFiles(filter)){
            ret.add(new Identity(Identity.trimFileName(f.getName()), Keys.loadPubKey(f)));
        }
        
        oos.writeObject(ret);
    }

    private void deleteIdentity(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String deleteMe = (String) ois.readObject();
        
        File identityFile = new File(authKeysDir, deleteMe + ".pub");
        
        if(identityFile.exists()) {
            oos.writeObject(identityFile.delete());
        } else {
            oos.writeObject(false);
        }
    }

    private void sendTerritoryList(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String townName = (String) ois.readObject();
        Town t = MCTowns.getTownManager().getTown(townName);
        
        if(t == null)
            oos.writeObject(new LinkedList<>());
        else
            oos.writeObject(new LinkedList<>(t.getTerritoriesCollection()));
    }

    private void sendTerritoryView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String territName = (String) ois.readObject();
        Territory t = MCTowns.getTownManager().getTerritory(territName);
        if(t == null)
            oos.writeObject(null);
        else
            oos.writeObject(new TerritoryView(t));
    }

    private void deleteTerritory(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String territName = (String) ois.readObject();
        Boolean result = MCTowns.getTownManager().removeTerritory(territName);
        oos.writeObject(result);
    }

    private void sendPlotView(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String plotName = (String) ois.readObject();
        Plot plot = MCTowns.getTownManager().getPlot(plotName);
        
        if(plot == null)
            oos.writeObject(null);
        else
            oos.writeObject(new PlotView(plot));
    }

    private void sendPlotList(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String territName = (String) ois.readObject();
        Territory t = MCTowns.getTownManager().getTerritory(territName);
        
        if(t == null)
            oos.writeObject(null);
        else
            oos.writeObject(new LinkedList<>(t.getPlotsCollection()));
    }

    private void deleteTown(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String townName = (String) ois.readObject();
        Boolean result = MCTowns.getTownManager().removeTown(townName);
        oos.writeObject(result);
    }

    private void createTown(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String townName = (String) ois.readObject();
        String mayorName = (String) ois.readObject();
        Location spawn = (Location) ois.readObject();
        
        Boolean result = MCTowns.getTownManager().addTown(townName, mayorName, spawn) == null ? false : true;
        
        oos.writeObject(result);
    }

    private void modifyPlotMembership(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Integer opMode = (Integer) ois.readObject();
        Integer membershipType = (Integer) ois.readObject();
        String playerName = (String) ois.readObject();
        String plotName = (String) ois.readObject();
        
        Plot plot = MCTowns.getTownManager().getPlot(plotName);
        
        if(plot == null) {
            oos.writeObject(false);
            return;
        }
        
        if(opMode == RemoteAction.ADD_PLAYER) {
            if(membershipType == RemoteAction.GUEST)
                plot.addGuest(playerName);
            else if(membershipType == RemoteAction.OWNER)
                plot.addPlayer(playerName);
        } else if(opMode == RemoteAction.DELETE_PLAYER) {
            plot.removePlayer(playerName);
        }
        
        oos.writeObject(true);
    }

    private void modifyTerritoryMembership(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Integer opMode = (Integer) ois.readObject();
        Integer membershipType = (Integer) ois.readObject();
        String playerName = (String) ois.readObject();
        String territoryName = (String) ois.readObject();
        
        Territory territ = MCTowns.getTownManager().getTerritory(territoryName);
        
        if(territ == null) {
            oos.writeObject(false);
            return;
        }
        
        if(opMode == RemoteAction.ADD_PLAYER) {
            if(membershipType == RemoteAction.GUEST)
                territ.addGuest(playerName);
            else if(membershipType == RemoteAction.OWNER)
                territ.addPlayer(playerName);
        } else if(opMode == RemoteAction.DELETE_PLAYER) {
            territ.removePlayer(playerName);
        }
        
        oos.writeObject(true);
    }

    private void modifyTownMembership(ObjectOutputStream oos, ObjectInputStream ois) throws ClassNotFoundException, IOException{
        Integer opMode = (Integer) ois.readObject();
        String playerName = (String) ois.readObject();
        String townName = (String) ois.readObject();
        
        Town town = MCTowns.getTownManager().getTown(townName);
        
        if(town == null) {
            oos.writeObject(false);
            return;
        }
        
        if(opMode == RemoteAction.ADD_PLAYER) {
            town.addPlayer(playerName);
        } else if(opMode == RemoteAction.DELETE_PLAYER) {
            town.removePlayer(playerName);
        }
        
        oos.writeObject(true);
    }

    private void modifyTownAssistants(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Integer opMode = (Integer) ois.readObject();
        String playerName = (String) ois.readObject();
        String townName = (String) ois.readObject();
        
        Town town = MCTowns.getTownManager().getTown(townName);
        
        if(town == null) {
            oos.writeObject(false);
            return;
        }
        
        if(opMode == RemoteAction.ADD_PLAYER) {
            town.addAssistant(playerName);
        } else if(opMode == RemoteAction.DELETE_PLAYER) {
            town.removeAssistant(playerName);
        }
        
        oos.writeObject(true);
    }

    private void updateTown(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        TownView view = (TownView) ois.readObject();
        
        Town t = MCTowns.getTownManager().getTown(view.getTownName());
        
        if(t == null) {
            oos.writeObject(false);
            return;
        }
        
        t.updateTown(view);
        
        oos.writeObject(true);
    }

    private void updatePlot(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        PlotView view = (PlotView) ois.readObject();
        
        Plot plot = MCTowns.getTownManager().getPlot(view.getPlotName());
        
        if(plot == null) {
            oos.writeObject(false);
            return;
        }
        
        plot.updatePlot(view);
        
        oos.writeObject(true);
    }
}
