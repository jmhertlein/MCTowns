package net.jmhertlein.mctowns.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.mctowns.structure.unmodifiable.UnmodifiablePlot;
import net.jmhertlein.mctowns.structure.unmodifiable.UnmodifiableTerritory;
import net.jmhertlein.mctowns.structure.unmodifiable.UnmodifiableTown;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * MariaDB-based manger for all SQL statements
 *
 * @author joshua
 */
public class SQLManager {

    public static final int MARIADB_DEFAULT_PORT = 3306;
    private Connection c;
    private Map<SQLAction, PreparedStatement> statements;

    public SQLManager(String hostname, int port, String username, String password) {
        statements = new HashMap<>();
        try {
            c = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/test", username, password);
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            prepareStatements();
            System.out.println("Done preparing all statements.");
            System.out.println(statements.size());
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createTables() throws SQLException {
        c.createStatement().execute("CREATE TABLE Town ("
                + "townName VARCHAR(255),"
                + "mayorName VARCHAR(255),"
                + "defaultPlotPrice FLOAT,"
                + "friendlyFire BOOLEAN,"
                + "worldName VARCHAR(255),"
                + "motdColor VARCHAR(15),"
                + "motd TEXT,"
                + "spawnLoc TEXT,"
                + "buyablePlots BOOLEAN,"
                + "economyJoins BOOLEAN,"
                + "bankName VARCHAR(255),"
                + "PRIMARY KEY(townName)"
                + ")");

        c.createStatement().execute("CREATE TABLE Town2 ("
                + "townName VARCHAR(255),"
                + "assistantName VARCHAR(255),"
                + "PRIMARY KEY(townName, assistantName)"
                + ")");

        c.createStatement().execute("CREATE TABLE Town3 ("
                + "townName VARCHAR(255),"
                + "residentName VARCHAR(255),"
                + "PRIMARY KEY(townName, residentName)"
                + ")");

        c.createStatement().execute("CREATE TABLE Bank ("
                + "townName VARCHAR(255),"
                + "bankName VARCHAR(255),"
                + "townFunds FLOAT,"
                + "PRIMARY KEY(townName, bankName)"
                + ")");

        c.createStatement().execute("CREATE TABLE Bank2 ("
                + "townName VARCHAR(255),"
                + "bankName VARCHAR(255),"
                + "blockType VARCHAR(255),"
                + "amount INTEGER,"
                + "PRIMARY KEY(townName, bankName, blockType)"
                + ")");

        c.createStatement().execute("CREATE TABLE Territory  ("
                + "territoryName VARCHAR(255),"
                + "PRIMARY KEY(territoryName)"
                + ")");

        c.createStatement().execute("CREATE TABLE Plot ("
                + "plotName VARCHAR(255),"
                + "forSale BOOLEAN,"
                + "price FLOAT,"
                + "signLoc TEXT,"
                + "PRIMARY KEY(plotName)"
                + ")");

        c.createStatement().execute("CREATE TABLE Region ("
                + "regionName VARCHAR(255),"
                + "worldName VARCHAR(255),"
                + "PRIMARY KEY(regionName)"
                + ")");

        c.createStatement().execute("CREATE TABLE Contains ("
                + "territoryName VARCHAR(255),"
                + "plotName VARCHAR(255),"
                + "PRIMARY KEY(territoryName, plotName)"
                + ")");

        c.createStatement().execute("CREATE TABLE Owns ("
                + "townName VARCHAR(255),"
                + "territoryName VARCHAR(255),"
                + "PRIMARY KEY(townName,territoryName)"
                + ")");

        //add FK constraints

        c.createStatement().execute("ALTER TABLE Town2 ADD FOREIGN KEY (townName) REFERENCES Town(townName)");
        c.createStatement().execute("ALTER TABLE Town3 ADD FOREIGN KEY (townName) REFERENCES Town(townName)");
        c.createStatement().execute("ALTER TABLE Bank ADD FOREIGN KEY (townName) REFERENCES Town(townName)");
        c.createStatement().execute("ALTER TABLE Bank2 ADD FOREIGN KEY (townName) REFERENCES Town(townName)");
        c.createStatement().execute("ALTER TABLE Owns ADD FOREIGN KEY (townName) REFERENCES Town(townName)");

        c.createStatement().execute("ALTER TABLE Owns ADD FOREIGN KEY (territoryName) REFERENCES Territory(territoryName)");
        c.createStatement().execute("ALTER TABLE Contains ADD FOREIGN KEY (territoryName) REFERENCES Territory(territoryName)");

        c.createStatement().execute("ALTER TABLE Contains ADD FOREIGN KEY (plotName) REFERENCES Plot(plotName)");

        c.createStatement().execute("ALTER TABLE Territory ADD FOREIGN KEY (territoryName) REFERENCES Region(regionName)");
        c.createStatement().execute("ALTER TABLE Plot ADD FOREIGN KEY (plotName) REFERENCES Region(regionName)");
    }

    public void dropTables() throws SQLException {
        c.createStatement().execute("DROP TABLE Contains,Owns,Bank,Bank2,Territory,Plot,Region,Town3,Town2,Town");
    }

    public static void main(String[] args) {
        SQLManager sqlMan = new SQLManager("localhost", MARIADB_DEFAULT_PORT, "dbproject", "dbprojpass");
        try {
            System.out.println("Creating tables.");
            sqlMan.createTables();

            System.out.println("Modifying tables.");
            sqlMan.createTown("Anvil", "Josh", "world");
            sqlMan.createTown("Bruma", "Josh", "world");

            System.out.println("Num towns: " + sqlMan.getNumTowns());

            System.out.println("Dropping tables.");
            sqlMan.dropTables();
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void prepareStatements() throws SQLException {
        //update town
        PreparedStatement s = c.prepareStatement(
                "UPDATE Town "
                + "SET mayorName = ?, defaultPlotPrice = ?, friendlyFire = ?, motdColor = ?, motd = ?, spawnLoc = ?, buyablePlots = ?, economyJoins = ? "
                + "WHERE townName = ?");
        statements.put(SQLAction.UPDATE_TOWN, s);

        //update plot
        s = c.prepareStatement(
                "UPDATE Plot "
                + "SET forSale = ?, price = ?, signLoc = ? "
                + "WHERE regName = ?");
        statements.put(SQLAction.UPDATE_PLOT, s);

        //get parent town for territory
        s = c.prepareStatement(
                "SELECT townName "
                + "FROM Owns O "
                + "WHERE O.territoryName = ?");
        statements.put(SQLAction.GET_PARENT_TOWN_OF_TERRITORY, s);

        //get parent town of plot
        s = c.prepareStatement(
                "SELECT townName "
                + "FROM Owns O, Contains C "
                + "WHERE O.territoryName = C.territoryName AND C.plotName = ?");
        statements.put(SQLAction.GET_PARENT_TOWN_OF_PLOT, s);

        //get plots for town
        s = c.prepareStatement(
                "SELECT plotName "
                + "FROM Owns O, Contains C "
                + "WHERE O.territoryName = C.territoryName AND O.townName = ?");
        statements.put(SQLAction.GET_PLOTS_FOR_TOWN, s);

        //get parent territory of plot
        s = c.prepareStatement(
                "SELECT territoryName "
                + "FROM Contains C "
                + "WHERE plotName = ?");
        statements.put(SQLAction.GET_PARENT_TERRITORY_OF_PLOT, s);

        //get parent town of territory
        s = c.prepareStatement(
                "SELECT townName "
                + "FROM Owns "
                + "WHERE territoryName = ?");
        statements.put(SQLAction.GET_PARENT_TOWN_OF_TERRITORY, s);

        //get view of town
        s = c.prepareStatement("SELECT * FROM Town WHERE townName = ?");
        statements.put(SQLAction.VIEW_TOWN, s);

        //get view of plot
        s = c.prepareStatement("SELECT plotName,worldName,forSale,price,signLoc FROM Plot P, Region R WHERE P.plotName = ? AND R.regionName = ?");
        statements.put(SQLAction.VIEW_PLOT, s);

        //get view of territory
        s = c.prepareStatement("SELECT territoryName,worldName FROM Territory T, Region R WHERE T.territoryName = ? AND R.regionName = ?");
        statements.put(SQLAction.VIEW_TERRITORY, s);

        //count number of towns
        s = c.prepareStatement("SELECT COUNT(townName) FROM Town");
        statements.put(SQLAction.COUNT_TOWNS, s);

        //count num territories
        s = c.prepareStatement("SELECT COUNT(territoryName) FROM Territory");
        statements.put(SQLAction.COUNT_TERRITORIES, s);

        //count num plots
        s = c.prepareStatement("SELECT COUNT(plotName) FROM Plot");
        statements.put(SQLAction.COUNT_PLOTS, s);

        //get currency in town bank
        s = c.prepareStatement(
                "SELECT townFunds "
                + "FROM Town T, Bank B "
                + "WHERE T.townName = ? AND B.bankName = T.bankName");
        statements.put(SQLAction.GET_CURRENCY_IN_TOWN_BANK, s);

        //get num blocks of type in town bank
        s = c.prepareStatement(
                "SELECT amount "
                + "FROM Bank2 B, Town T "
                + "WHERE B.bankName = T.bankName AND T.townName = ?");
        statements.put(SQLAction.GET_BLOCKS_IN_TOWN_BANK, s);

        //get all residents in town
        s = c.prepareStatement("SELECT residentName FROM Town3 WHERE townName = ?");
        statements.put(SQLAction.GET_PLAYERS_IN_TOWN, s);

        //get all towns player is in
        s = c.prepareStatement("SELECT townName FROM Town3 WHERE residentName = ?");
        statements.put(SQLAction.GET_TOWNS_FOR_PLAYER, s);

        //get all territories in a town
        s = c.prepareStatement(
                "SELECT territoryName "
                + "FROM Owns "
                + "WHERE townName = ?");
        statements.put(SQLAction.GET_TERRITORIES_FOR_TOWN, s);

        //get plots for a territory
        s = c.prepareStatement("SELECT plotName "
                + "FROM Contains "
                + "WHERE territoryName = ?");
        statements.put(SQLAction.GET_PLOTS_FOR_TERRITORY, s);

        //delete a town
        s = c.prepareStatement("DELETE FROM Town WHERE townName = ?");
        statements.put(SQLAction.DELETE_TOWN, s);

        //delete a territory
        s = c.prepareStatement("DELETE FROM Territory WHERE territoryName = ?");
        statements.put(SQLAction.DELETE_TERRITORY, s);

        //delete a plot
        s = c.prepareStatement("DELETE FROM Plot WHERE plotName = ?");
        statements.put(SQLAction.DELETE_PLOT, s);

        //make a new territory
        s = c.prepareStatement("INSERT INTO Territory VALUES(?)");
        statements.put(SQLAction.CREATE_TERRITORY, s);
        s = c.prepareStatement("INSERT INTO Owns VALUES(?,?)");
        statements.put(SQLAction.CREATE_TERRITORY_OWNS, s);
        s = c.prepareStatement("INSERT INTO Region VALUES(?,?)");
        statements.put(SQLAction.CREATE_TERRITORY_REGION, s);

        //make a new plot
        s = c.prepareStatement("INSERT INTO Plot VALUES(?, false, 0, null)");
        statements.put(SQLAction.CREATE_PLOT, s);
        s = c.prepareStatement("INSERT INTO Contains VALUES(?,?)");
        statements.put(SQLAction.CREATE_PLOT_CONTAINS, s);
        s = c.prepareStatement("INSERT INTO Region VALUES(?,?)");
        statements.put(SQLAction.CREATE_PLOT_REGION, s);

        //make a new town
        s = c.prepareStatement("INSERT INTO Town VALUES(?, ?, 0, false, ?, \"CYAN\", \"Use /town motd <message> to change the town MOTD!\", NULL, false, false, \"Default Bank\")");
        statements.put(SQLAction.CREATE_TOWN, s);
        s = c.prepareStatement("INSERT INTO Bank VALUES(?, \"Default Bank\", 0)");
        statements.put(SQLAction.CREATE_TOWN_BANK, s);

        s = c.prepareStatement("INSERT INTO Town3 VALUES(?, ?)");
        statements.put(SQLAction.ADD_PLAYER_TO_TOWN, s);

        s = c.prepareStatement("INSERT INTO Town2 VALUES (?, ?)");
        statements.put(SQLAction.ADD_ASSISTANT_TO_TOWN, s);

        s = c.prepareStatement("DELETE FROM Town3 WHERE townName = ? AND residentName = ?");
        statements.put(SQLAction.REMOVE_PLAYER_FROM_TOWN, s);

        s = c.prepareStatement("DELETE FROM Town2 WHERE townName = ? AND assistantName = ?");
        statements.put(SQLAction.REMOVE_ASSISTANT_FROM_TOWN, s);

    }

    public boolean createTown(String townName, String mayorName, String worldName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.CREATE_TOWN);
            s.setString(1, townName);
            s.setString(2, mayorName);
            s.setString(3, worldName);
            s.execute();


            s = retrieveStatement(SQLAction.CREATE_TOWN_BANK);
            s.setString(1, townName);
            s.execute();

        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    public boolean createTerritory(String territoryName, String parentTownName, String worldName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.CREATE_TERRITORY);
            s.setString(1, territoryName);
            s.execute();

            s = retrieveStatement(SQLAction.CREATE_TERRITORY_OWNS);
            s.setString(1, parentTownName);
            s.setString(2, territoryName);
            s.execute();

            s = retrieveStatement(SQLAction.CREATE_TERRITORY_REGION);
            s.setString(1, territoryName);
            s.setString(2, worldName);
            s.execute();

        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    public boolean createPlot(String plotName, String parentTerritoryName, String worldName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.CREATE_PLOT);
            s.setString(1, plotName);
            s.execute();

            s = retrieveStatement(SQLAction.CREATE_PLOT_CONTAINS);
            s.setString(1, parentTerritoryName);
            s.setString(2, plotName);
            s.execute();

            s = retrieveStatement(SQLAction.CREATE_PLOT_REGION);
            s.setString(1, plotName);
            s.setString(2, worldName);
            s.execute();

        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    public boolean deleteTown(String townName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.DELETE_TOWN);

            s.setString(1, townName);
            s.execute();
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public boolean deleteTerritory(String regName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.DELETE_TERRITORY);

            s.setString(1, regName);
            s.execute();
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public boolean deletePlot(String regName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.DELETE_PLOT);

            s.setString(1, regName);
            s.execute();
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public UnmodifiableTown viewTown(String townName) {
        try {
            UnmodifiableTown ret;
            
            PreparedStatement s = retrieveStatement(SQLAction.VIEW_TOWN);
            s.setString(1, townName);
            ResultSet rs = s.executeQuery();
            if(!rs.next())
                return null;
            
            return new UnmodifiableTown(rs.getString("townName"), rs.getString("worldName"), rs.getString("motd"), ChatColor.valueOf(rs.getString("motdColor")), 
                    rs.getString("spawnLoc"), rs.getString("mayorName"), rs.getString("bankName"), rs.getBoolean("buyablePlots"), rs.getBoolean("economyJoins"), 
                    rs.getBigDecimal("defaultPlotPrice"), rs.getBoolean("friendlyFire"));
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public UnmodifiableTerritory viewTerritory(String territoryName) {
        try {
            UnmodifiableTerritory ret;
            
            PreparedStatement s = retrieveStatement(SQLAction.VIEW_TERRITORY);
            s.setString(1, territoryName);
            ResultSet rs = s.executeQuery();
            if(!rs.next())
                return null;
            
            return new UnmodifiableTerritory(territoryName, rs.getString("worldName"));
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public UnmodifiablePlot viewPlot(String plotName) {
        try {
            UnmodifiableTerritory ret;
            
            PreparedStatement s = retrieveStatement(SQLAction.VIEW_PLOT);
            s.setString(1, plotName);
            ResultSet rs = s.executeQuery();
            if(!rs.next())
                return null;
            
            return new UnmodifiablePlot(plotName, rs.getString("worldName"), rs.getString("signLoc"), rs.getBigDecimal("price"), rs.getBoolean("forSale"));
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public List<String> getTownResidents(String townName) {
        List<String> ret = new LinkedList<>();
        
        try {
            PreparedStatement s = retrieveStatement(SQLAction.GET_PLAYERS_IN_TOWN);
            
            s.setString(1, townName);
            ResultSet rs = s.executeQuery();
            
            while(rs.next()) {
                ret.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            ret.clear();
            return ret;
        }
        
        return ret;
        
        
    }

    public boolean updateTown(String townName, String mayorName, BigDecimal defaultPlotPrice, boolean friendlyFireAllowed, ChatColor motdColor, String motd, String spawnLoc, boolean buyablePlots, boolean economyJoins) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.UPDATE_TOWN);
            
            s.setString(1, townName);
            s.setString(2, mayorName);
            s.setBigDecimal(3, defaultPlotPrice);
            s.setBoolean(4, friendlyFireAllowed);
            s.setString(5, motdColor.name());
            s.setString(6, motd);
            s.setString(7, spawnLoc);
            s.setBoolean(8, buyablePlots);
            s.setBoolean(9, economyJoins);
            
            s.execute();
            return true;
            
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }

    public boolean updatePlot(String regName, boolean forSale, BigDecimal price, String signLoc) {
                try {
            PreparedStatement s = retrieveStatement(SQLAction.UPDATE_TOWN);
            
            s.setString(1, regName);
            s.setBoolean(2, forSale);
            s.setBigDecimal(3, price);
            s.setString(4, signLoc);
            
            s.execute();
            return true;
            
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public int getNumTowns() {
        PreparedStatement s = retrieveStatement(SQLAction.COUNT_TOWNS);

        try {
            ResultSet rs = s.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public int getNumTerritories() {
        PreparedStatement s = retrieveStatement(SQLAction.COUNT_TERRITORIES);

        try {
            ResultSet rs = s.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public int getNumPlots() {
        PreparedStatement s = retrieveStatement(SQLAction.COUNT_PLOTS);

        try {
            ResultSet rs = s.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public String getParentTownForTerritory(String territoryName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.GET_PARENT_TOWN_OF_TERRITORY);

            s.setString(1, territoryName);
            ResultSet rs = s.executeQuery();


            rs.next();
            return rs.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String getParentTerritoryOfPlot(String plotName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.GET_PARENT_TERRITORY_OF_PLOT);

            s.setString(1, plotName);
            ResultSet rs = s.executeQuery();


            rs.next();
            return rs.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public List<String> getTownsForPlayer(String playerName) {
        List<String> towns = new LinkedList<>();
        try {
            PreparedStatement s = retrieveStatement(SQLAction.GET_TOWNS_FOR_PLAYER);

            s.setString(1, playerName);
            ResultSet rs = s.executeQuery();


            while (rs.next()) {
                towns.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return towns;
    }

    public List<String> getTerritoriesForTown(String townName) {
        List<String> territs = new LinkedList<>();
        try {
            PreparedStatement s = retrieveStatement(SQLAction.GET_TERRITORIES_FOR_TOWN);

            s.setString(1, townName);
            ResultSet rs = s.executeQuery();


            while (rs.next()) {
                territs.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return territs;
    }

    public List<String> getPlotsForTerritory(String territoryName) {
        List<String> plots = new LinkedList<>();
        try {
            PreparedStatement s = retrieveStatement(SQLAction.GET_PLOTS_FOR_TERRITORY);

            s.setString(1, territoryName);
            ResultSet rs = s.executeQuery();


            while (rs.next()) {
                plots.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return plots;
    }

    public List<String> getPlotsForTown(String townName) {
        List<String> plots = new LinkedList<>();
        try {
            PreparedStatement s = retrieveStatement(SQLAction.GET_PLOTS_FOR_TOWN);

            s.setString(1, townName);
            ResultSet rs = s.executeQuery();


            while (rs.next()) {
                plots.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return plots;
    }

    public int getNumBlocksInTownBank(String townName, Material blockType) {
        PreparedStatement s = retrieveStatement(SQLAction.GET_BLOCKS_IN_TOWN_BANK);

        try {
            s.setString(1, townName);
            s.setString(2, blockType.name());
            ResultSet rs = s.executeQuery();

            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public int getCurrencyInTownBank(String townName) {
        PreparedStatement s = retrieveStatement(SQLAction.GET_CURRENCY_IN_TOWN_BANK);

        try {
            s.setString(1, townName);
            ResultSet rs = s.executeQuery();

            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public boolean addAssistantToTown(String townName, String assistantName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.ADD_ASSISTANT_TO_TOWN);

            s.setString(1, townName);
            s.setString(2, assistantName);
            s.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean addPlayerToTown(String townName, String playerName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.ADD_PLAYER_TO_TOWN);

            s.setString(1, townName);
            s.setString(2, playerName);
            s.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean removeAssistantFromTown(String townName, String assistantName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.REMOVE_ASSISTANT_FROM_TOWN);

            s.setString(1, townName);
            s.setString(2, assistantName);
            s.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean removePlayerFromTown(String townName, String playerName) {
        try {
            PreparedStatement s = retrieveStatement(SQLAction.REMOVE_PLAYER_FROM_TOWN);

            s.setString(1, townName);
            s.setString(2, playerName);
            s.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private PreparedStatement retrieveStatement(SQLAction action) {
        PreparedStatement s = statements.get(action);
        try {
            s.clearParameters();
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return s;
    }
}
