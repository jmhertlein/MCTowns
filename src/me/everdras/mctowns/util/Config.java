package me.everdras.mctowns.util;

import java.io.*;
import java.math.BigDecimal;
import java.util.Scanner;

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
     * Gets the reason for which the config is not useable.
     *
     * @return the reason the config isn't useable
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
        if(!f.exists()) {
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


        ps.close();
        fos.close();


    }
}
