/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command;

import java.util.*;

/**
 *
 * @author Joshua
 */
public class MCTCommand {
    private ArrayList<String> args, flags;

    /**
     * Converts elements available in onCommand into an MCTCommand
     * @param label the first word after the slash
     * @param args all following words and args
     */
    public MCTCommand(String label, String[] args) {
        this.args = new ArrayList<>();
        this.flags = new ArrayList<>();

        this.args.add(label);

        for(String s : args) {
            if(s.startsWith("-"))
                this.flags.add(s);
            else
                this.args.add(s);
        }
    }

    /**
     * Parses the given command to make an MCTCommand that represents it
     * @param slashCommand
     */
    public MCTCommand(String slashCommand) {
        this.args = new ArrayList<>();
        this.flags = new ArrayList<>();

        slashCommand = slashCommand.substring(1); //take off the leading /

        Scanner scan = new Scanner(slashCommand);



        String s;
        while(scan.hasNext()) {
            s = scan.next();
            if(s.startsWith("-"))
                flags.add(s);
            else
                args.add(s);
        }
    }

    /**
     * Copies the passed args and flags into a new MCTCommand
     * @param args
     * @param flags
     */
    public MCTCommand(String[] args, String[] flags) {
        this.args = new ArrayList<>();
        this.flags = new ArrayList<>();

        this.args.addAll(Arrays.asList(args));
        this.flags.addAll(Arrays.asList(flags));
    }

    /**
     * Checks whether or not the flag is present in the command.
     * @param flag
     * @return
     */
    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    @Override
    public String toString() {
        String nu = "/";
        for(String s : args) {
            nu += s;
            nu += " ";
        }

        nu += "(";
        for(String s : flags) {
            nu += s;
            nu += ", ";
        }
        nu += ")";

        return nu;
    }

    public String getArgAtIndex(int i) {
        return args.get(i);
    }


}
