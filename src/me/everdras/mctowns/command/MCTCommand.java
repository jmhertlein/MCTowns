/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command;

/**
 *
 * @author Joshua
 */
public class MCTCommand {
    private String[] args, flags;

    /**
     * Converts elements available in onCommand into an MCTCommand
     * @param label the first word after the slash
     * @param args all following words and args
     */
    public MCTCommand(String label, String[] args) {

    }

    /**
     * Parses the given command to make an MCTCommand that represents it
     * @param slashCommand
     */
    public MCTCommand(String slashCommand) {

    }

    /**
     * Copies the passed args and flags into a new MCTCommand
     * @param args
     * @param flags
     */
    public MCTCommand(String[] args, String[] flags) {

    }
}
