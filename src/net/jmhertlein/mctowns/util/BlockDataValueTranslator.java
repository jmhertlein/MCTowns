/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.util;

import org.bukkit.Material;

/**
 * Provides translator functionalities for the names and IDs of blocks.
 *
 * @author joshua
 */
public class BlockDataValueTranslator {

    /**
     *
     * @param blockName The block whose ID is being requested
     * @return the block's ID, or -1 if it doesn't exist
     */
    public static int getBlockID(String blockName) {
        if (Material.matchMaterial(blockName) != null) {
            return Material.matchMaterial(blockName).getId();
        }

        return -1;

    }

    /**
     *
     * @param blockID - the ID of the block whose name is being requested
     * @return the block's name, or null if block doesn't exist
     */
    public static String getBlockName(int blockID) {
        if (Material.getMaterial(blockID) != null) {
            return Material.getMaterial(blockID).name();
        }
        return null;

    }

    /**
     *
     * @param name the name of the block whose existence is being checked
     * @return true if it exists, false otherwise
     */
    public static boolean blockExists(String name) {
        return Material.matchMaterial(name) != null;
    }

    /**
     *
     * @param id the id of the block whose existence is being checked.
     * @return true if the block exists, false otherwise.
     */
    public static boolean blockExists(int id) {
        return Material.getMaterial(id) != null;
    }
}
