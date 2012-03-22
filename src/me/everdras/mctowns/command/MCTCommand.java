/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author Joshua
 */
public class MCTCommand {
    public static final String DISABLE_AUTOACTIVE = "-na", RECURSIVE = "-r", ADMIN = "-admin";
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

    public String getArgAtIndex(final int i) throws ArgumentCountException {


        if(i >= args.size())
            throw new ArgumentCountException(i);

        return args.get(i);
    }

    public boolean hasArgAtIndex(int i) {
        return args.size() > i;
    }

    public String get(int i) throws ArgumentCountException {
        return getArgAtIndex(i);
    }

    /**
     * Assumes that the command is trying to flag a region (i.e. this.get(0).equals(some town level), get(1).equals("flag"), get(3).equals(some flag name))
     * and turns arguments at indices in the rage [3, end) into a string array and returns it.
     * @return arguments for the specified flag
     */
    public String[] getFlagArguments() {
        String[] flagArgs = new String[args.size()-3];

        for(int i = 3; i < args.size(); i++)
            flagArgs[i] = args.get(i);

        return flagArgs;
    }


}
