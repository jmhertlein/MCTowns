package me.everdras.mctowns.util;

import java.io.*;
import java.util.Scanner;

/**
 *
 * @author Joshua
 */
public class Config {

    private String path;
    private boolean economyEnabled;
    private boolean mayorsCanBuyTerritories;
    private float pricePerXZBlock;
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
            failReason = "Generic exception in parsing config.";
        }
    }

    private void parseConfig() throws FileNotFoundException {
        Scanner fileScan = new Scanner(new File(path));

        String curLine = getNextLine(fileScan);

        if (curLine == null) {
            failBit = true;
            failReason = "Reached end of config while parsing.";
            return;
        }
        Scanner lineScan = new Scanner(curLine);

        if (!lineScan.hasNextBoolean()) {
            failBit = true;
            failReason = "Error on token: \"" + lineScan + "\".";
            return;
        }
        economyEnabled = lineScan.nextBoolean();


        //===================================================================

        curLine = getNextLine(fileScan);

        if (curLine == null) {
            failBit = true;
            failReason = "Reached end of config while parsing.";
            return;
        }
        lineScan = new Scanner(curLine);


        if (!lineScan.hasNextBoolean()) {
            failBit = true;
            failReason = "Error on token: \"" + lineScan + "\".";
            return;
        }
        mayorsCanBuyTerritories = lineScan.nextBoolean();

        //=================================================================

        curLine = getNextLine(fileScan);

        if (curLine == null) {
            failBit = true;
            failReason = "Reached end of config while parsing.";
            return;
        }
        lineScan = new Scanner(curLine);


        if (!lineScan.hasNextFloat()) {
            failBit = true;
            failReason = "Error on token: \"" + lineScan + "\".";
            return;
        }
        pricePerXZBlock = lineScan.nextFloat();
        pricePerXZBlock /= (16 * 16);

        //==================================================================

        curLine = getNextLine(fileScan);

        if (curLine == null) {
            failBit = true;
            failReason = "Reached end of config while parsing.";
            return;
        }
        lineScan = new Scanner(curLine);


        if (!lineScan.hasNextInt()) {
            failBit = true;
            failReason = "Error on token: \"" + lineScan + "\".";
            return;
        }

        minNumPlayersToBuyTerritory = lineScan.nextInt();

        //==================================================================

        curLine = getNextLine(fileScan);

        if (curLine == null) {
            failBit = true;
            failReason = "Reached end of config while parsing.";
            return;
        }
        lineScan = new Scanner(curLine);


        if (!lineScan.hasNextBoolean()) {
            failBit = true;
            failReason = "Error on token: \"" + lineScan + "\".";
            return;
        }

        allowTownFriendlyFireManagement = lineScan.nextBoolean();

        //==================================================================




    }

    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    public boolean mayorsCanBuyTerritories() {
        return mayorsCanBuyTerritories;
    }

    public float getPricePerXZBlock() {
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
     * @return true if the config is tainted/corrupted, false if it's useable.
     */
    public boolean badConfig() {
        return failBit;
    }

    /**
     * Gets the reason for which the config is not useable.
     * @return the reason the config isn't useabe
     */
    public String getFailReason() {
        return failReason;
    }

    /**
     * Attempts to replace the existing config file with the hardcoded default copy.
     * @throws IOException
     */
    public static void resetConfigFileToDefault(String path) throws IOException {
        FileOutputStream fos;
        PrintStream ps;


        fos = new FileOutputStream(new File(path));
        ps = new PrintStream(fos);

        ps.println("#This is an automatically generated default config file.");
        ps.println("#Please do NOT alter the order of the config. The only things");
        ps.println("#that should be uncommented are the actual values to read in.");
        ps.println();
        ps.println("#Economy is enabled:");
        ps.println("false");
        ps.println("#Mayors can buy territories:");
        ps.println("false");
        ps.println("#Price per chunk");
        ps.println("100");

        ps.println();

        ps.println("#Number of players that a town needs to have for a mayor to be able to add territories");
        ps.println("0");
        ps.println();
        ps.println("#Mayors can decide whether or not their towns allow friendly fire. (false causes MCTowns to not interfere with PvP at all.)");
        ps.println("false");


        ps.close();
        fos.close();


    }
}
