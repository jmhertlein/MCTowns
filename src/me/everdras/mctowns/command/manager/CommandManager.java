///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package me.everdras.mctowns.command.manager;
//
//import java.util.LinkedList;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandSender;
//
///**
// *
// * @author Joshua
// */
//public class CommandManager {
//    private CommandNode root;
//    private transient int stopIndex;
//    
//    
//    public CommandManager() {
//        root = new RootNode();
//    }
//    
//    public void executeCommand(CommandSender sender, Command cmd, String label, String[] args) {
//        //etc etc find right node, set curNode to equal it
//        String[] command = new String[args.length+1];
//        command[0] = label;
//        System.arraycopy(args, 0, command, 1, args.length);
//        
//        CommandNode node = findNode(command);
//        
//        if(node.executable) {
//            node.runCommand(sender, cmd, label, );
//        }
//        
//        
//    }
//    
//    public void addCommand(CommandNode newCommand) {
//        recurAddCommand(newCommand, root, 0);
//        
//    }
//    
//    private void recurAddCommand(CommandNode newCommand, CommandNode curNode, int tokenIndex) {
//        String token = newCommand.getIdentifierAt(tokenIndex);
//        //base case
//        if(token == null) {
//            curNode.addChild(newCommand);
//            return;
//        }
//        
//        LinkedList<CommandNode> children = curNode.getChildren();
//        CommandNode nextNode = null;
//        
//        for(CommandNode cn : children) {
//            if(cn.getIdentifier().equalsIgnoreCase(token)) {
//                nextNode = cn;
//            }
//        }
//        
//        if(nextNode == null) {
//            throw new RuntimeException("Discontinuous command tree.");
//        }
//        
//        recurAddCommand(newCommand, nextNode, tokenIndex+1);
//    }
//    
//    private CommandNode findNode(String[] command) {
//        CommandNode curNode = root;
//        CommandNode temp = null;
//        
//        for(int i = 0; i < command.length && curNode.hasChildren(); i++) {
//            temp = null;
//            for(CommandNode cn : curNode.getChildren()) {
//                if(command[i].equals(cn.getIdentifier()))
//                    temp = cn;
//            }
//            if(temp == null)
//                return curNode;
//            else
//                curNode = temp;
//            
//        }
//        
//        return curNode;
//    }
//    
//    
//    private class RootNode extends CommandNode {
//        public RootNode() {
//            super("/", "Usage: /mct, /town, /territory, /district, /plot", false, 0, 0);
//        }
//        
//    }
//    
//    
//    
//}
