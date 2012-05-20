/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.listeners;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author joshua
 */
public class QuickSelectToolListener implements Listener {
    private static Material SELECT_TOOL;
    
    public void onToolUse(PlayerInteractEvent e) {
        if(! (e.getPlayer().getItemInHand().getType() == SELECT_TOOL) )
            return;
    }
}
