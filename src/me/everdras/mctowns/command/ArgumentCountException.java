/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command;

/**
 *
 * @author Joshua
 */
public class ArgumentCountException extends Exception {

    private int errorIndex;

    public ArgumentCountException(int index) {
        super("Insufficient number of arguments for the attempted command.");
        errorIndex = index;
    }

    public int getErrorIndex() {
        return errorIndex;
    }





}
