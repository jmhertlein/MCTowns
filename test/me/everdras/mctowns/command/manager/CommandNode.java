/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command.manager;

import java.util.LinkedList;
import java.util.Scanner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Joshua
 */
public class CommandNode {

    private LinkedList<CommandNode> children;
    protected String[] ancestry;
    protected String identifier, argsHelp;
    protected boolean executable;
    protected int reqArgs, optArgs;

    public CommandNode(String fullSlashCommand, String argsHelp, boolean isExec, int argsReq, int argsOpt) {
        children = new LinkedList<CommandNode>();

        if (fullSlashCommand.startsWith("/")) {
            fullSlashCommand = fullSlashCommand.substring(1);
        }

        Scanner scan = new Scanner(fullSlashCommand);
        scan.useDelimiter(" ");

        LinkedList<String> temp = new LinkedList<String>();

        while (scan.hasNext()) {
            temp.add(scan.next());
        }

        identifier = temp.pop();
        ancestry = new String[temp.size()];
        ancestry = temp.toArray(ancestry);
        executable = isExec;

        this.argsHelp = argsHelp;

        reqArgs = argsReq;
        optArgs = argsOpt;
    }

    public void runCommand(CommandSender sender, Command cmd, String label, String[] args) {
        throw new RuntimeException("runCommand was invoked because node was marked executable, but runCommand was not overidden");
    }

    public LinkedList<CommandNode> getChildren() {
        return children;
    }

    public int getNumChildren() {
        return children.size();
    }

    public boolean hasChildren() {
        return children.size() != 0;
    }

    public String getIdentifierAt(int index) {
        if (index < ancestry.length) {
            return ancestry[index];
        }
//        else if(index == ancestry.length)
//            return identifier;
        else {
            return null;
        }
    }

    public String[] getAncestry() {
        return ancestry;
    }

    public String getArgsHelp() {
        return argsHelp;
    }

    public boolean isExecutable() {
        return executable;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getOptArgs() {
        return optArgs;
    }

    public int getReqArgs() {
        return reqArgs;
    }

    /**
     * Adds the passed command as a child of this command node.
     * Will not add multiple children of same identifier.
     * @param child the child node to be added
     */
    public void addChild(CommandNode child) {
        if (!children.contains(child)) {
            children.add(child);
        }
    }

    /**
     * Compares the nodes. Returns true if they have the same identifier,
     * false otherwise.
     * @param other the node to compare to
     * @return
     */
    public boolean equals(CommandNode other) {
        if (identifier.equals(other.identifier)) {
            for (int i = 0; i < ancestry.length; i++) {
                try {
                    if (!ancestry[i].equals(other.ancestry[i])) {
                        return false;
                    }
                } catch(ArrayIndexOutOfBoundsException oobe) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }
}
