package net.jmhertlein.mctowns.util;

import java.io.*;
import java.math.BigDecimal;
import java.util.Scanner;
import org.bukkit.Material;

/**
 *
 * @author Joshua
 */
public class Config {

    private String path;
    private boolean economyEnabled;
    private boolean mayorsCanBuyTerritories;
    private BigDecimal pricePerXZBlock;
    private int minNumPlayersToBuyTerritory;
    private boolean allowTownFriendlyFireManagement;
    private Material qsTool;
    private boolean logCommands;
    private boolean playersCanJoinMultipleTowns;
    //true if config is tainted/bad/parse error, false otherwise.
    private boolean failBit;
    private String failReason;

    /**
     *
     * @param configFilePath
     */
    public Config(String configFilePath) {
        //init defaults
        economyEnabled = false;
        mayorsCanBuyTerritories = false;
        pricePerXZBlock = BigDecimal.ZERO;
        minNumPlayersToBuyTerritory = 3;
        allowTownFriendlyFireManagement = false;
        qsTool = Material.getMaterial(290);
        logCommands = false;

        failReason = "No fail detected.";
        File configPath = new File(configFilePath);

        if (!configPath.exists()) {
            failBit = true;
            failReason = "Config file not found.";
            return;
        }

        path = configPath.getPath();

        try {
            parseConfig();
        } catch (Exception ex) {
            failBit = true;
            failReason = "Generic exception in parsing config. (So helpful, amirite?) Message: " + ex.getMessage();
        }
    }

    private void parseConfig() throws FileNotFoundException {
        Scanner fileScan = new Scanner(new File(path));
        String curLine, curToken;
        Scanner lineScan;

        curLine = getNextLine(fileScan);

        while (curLine != null) {
            lineScan = new Scanner(curLine);
            lineScan.useDelimiter("=");
            curToken = lineScan.next().trim();

            switch (curToken) {
                case "allowTownFriendlyFireManagement":
                    curToken = lineScan.next().trim();
                    try {
                        allowTownFriendlyFireManagement = Boolean.parseBoolean(curToken);
                    } catch (Exception e) {
                        failBit = true;
                        failReason = "Error parsing token \"" + curToken + "\". Error message: " + e.getMessage();

                    }
                    break;

                case "economyEnabled":
                    curToken = lineScan.next().trim();
                    try {
                        economyEnabled = Boolean.parseBoolean(curToken);
                    } catch (Exception e) {
                        failBit = true;
                        failReason = "Error parsing token \"" + curToken + "\". Error message: " + e.getMessage();

                    }
                    break;

                case "mayorsCanBuyTerritories":
                    curToken = lineScan.next().trim();
                    try {
                        mayorsCanBuyTerritories = Boolean.parseBoolean(curToken);
                    } catch (Exception e) {
                        failBit = true;
                        failReason = "Error parsing token \"" + curToken + "\". Error message: " + e.getMessage();

                    }
                    break;

                case "minNumPlayersToBuyTerritory":
                    curToken = lineScan.next().trim();
                    try {
                        minNumPlayersToBuyTerritory = Integer.parseInt(curToken);

                    } catch (Exception e) {
                        failBit = true;
                        failReason = "Error parsing token \"" + curToken + "\". Error message: " + e.getMessage();

                    }
                    break;

                case "pricePerXZBlock":
                    curToken = lineScan.next().trim();
                    try {
                        pricePerXZBlock = new BigDecimal(curToken);
                    } catch (Exception e) {
                        failBit = true;
                        failReason = "Error parsing token \"" + curToken + "\". Error message: " + e.getMessage();

                    }
                    break;

                case "quickSelectTool":
                    curToken = lineScan.next().trim();
                    try {
                        qsTool = Material.getMaterial(Integer.parseInt(curToken));
                    } catch (Exception e) {
                        failBit = true;
                        failReason = "Error parsing token \"" + curToken + "\". Error message: " + e.getMessage();
                    }
                    break;
                    
                case "logCommands":
                    curToken = lineScan.next().trim();
                    try {
                        logCommands = Boolean.parseBoolean(curToken);
                    } catch (Exception e) {
                        failBit = true;
                        failReason = "Error parsing token \"" + curToken + "\". Error message: " + e.getMessage();
                    }
                    break;
                    
                case "playersCanJoinMultipleTowns":
                    curToken = lineScan.next().trim();
                    try {
                        playersCanJoinMultipleTowns = Boolean.parseBoolean(curToken);
                    } catch (Exception e) {
                        failBit = true;
                        failReason = "Error parsing token \"" + curToken + "\". Error message: " + e.getMessage();
                    }
                    break;

                default:
                    failBit = true;
                    failReason = "Unknown option \"" + curToken + "\".";
            }

            curLine = getNextLine(fileScan);
        }





    }

    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    public boolean mayorsCanBuyTerritories() {
        return mayorsCanBuyTerritories;
    }

    public BigDecimal getPricePerXZBlock() {
        return pricePerXZBlock;
    }

    public int getMinNumPlayersToBuyTerritory() {
        return minNumPlayersToBuyTerritory;
    }

    public boolean allowsTownFriendlyFireManagement() {
        return allowTownFriendlyFireManagement;
    }

    public Material getQsTool() {
        return qsTool;
    }

    public boolean isLoggingCommands() {
        return logCommands;
    }

    public boolean playersCanJoinMultipleTowns() {
        return playersCanJoinMultipleTowns;
    }
    
    

    
    
    /*
     * Returns the next uncommented, non-empty line in the file.
     */
    private String getNextLine(Scanner fileScan) {
        String cLine = null;

        while (fileScan.hasNext()) {
            cLine = fileScan.nextLine();
            if (!cLine.startsWith("#") && !cLine.isEmpty()) {
                return cLine;
            }
        }

        return null;
    }

    /**
     * Tests the validity of the config.
     *
     * @return true if the config is tainted/corrupted, false if it's useable.
     */
    public boolean badConfig() {
        return failBit;
    }

    /**
     * Gets the reason for which the config is not usable.
     *
     * @return the reason the config isn't usable
     */
    public String getFailReason() {
        return failReason;
    }

    /**
     * Attempts to replace the existing config file with the hardcoded default
     * copy.
     *
     * @throws IOException
     */
    public static void resetConfigFileToDefault(String path) throws IOException {
        FileOutputStream fos;
        PrintStream ps;

        File f = new File(path);
        if (!f.exists()) {
            f.createNewFile();
        }


        fos = new FileOutputStream(f);
        ps = new PrintStream(fos);







        ps.println("#This is an automatically generated default config file.");
        ps.println();
        ps.println("economyEnabled = false");
        ps.println("mayorsCanBuyTerritories = false");
        ps.println("pricePerXZBlock = 0");

        ps.println();

        ps.println("minNumPlayersToBuyTerritory = 3");
        ps.println();
        ps.println("allowTownFriendlyFireManagement = false");

        ps.println();
        ps.println("#Default is wooden hoe (ID 290)");
        ps.println("quickSelectTool = 290");
        
        ps.println();
        ps.println("#Log verbose information of each MCTowns command issued");
        ps.println("logCommands = false");
        
        ps.println();
        ps.println("#if set to true, players are allowed to join multiple towns");
        ps.println("playersCanJoinMultipleTowns = false");


        ps.close();
        fos.close();


    }
}
