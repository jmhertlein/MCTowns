package net.jmhertlein.mctowns.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MariaDB-based manger for all SQL statements
 * @author joshua
 */
public class SQLManager {
    private Connection c;
    private Map<SQLAction, PreparedStatement> statements;
    
    public SQLManager(String hostname, int port, String username, String password) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            c = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/test", username, password);
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        prepareStatements();
    }
    
    private void prepareStatements() {
        
    }
    
    public void createTown(){}
    
    public void createTerritory(){}
    
    public void createPlot(){}
    
    public void deleteTown(){}
    
    public void deleteTerritory(){}
    
    public void deletePlot(){}
    
    public void updateTown(){}
    
    public void updateTerritory(){}
    
    public void updatePlot(){}
    
    public void getNumTowns(){}
    
    public void getNumTerritories(){}
    
    public void getNumPlots(){}
    
    public void getParentTownForTerritory(){}
    
    public void getParentTerritoryOfPlot(){}
    
    public void getTownsForPlayer(){}
    
    public void getTerritoriesForTown(){}
    
    public void getPlotsForTerritory(){}
    
    public void getNumBlocksInTownBank(){}
    
    public void getCurrencyInTownBank(){}
}
