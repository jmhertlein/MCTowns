/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.listeners;

import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.structure.Town;
import me.everdras.mctowns.util.Config;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;

/**
 * EntityListener subclass intended to stop PvP between townmates.
 *
 * @author Joshua
 */
public class MCTPvPListener extends EntityListener {

    private TownManager manager;
    private Config options;

    public MCTPvPListener(TownManager manager, Config options) {
        super();

        this.manager = manager;
        this.options = options;

    }


    @Override
    public void onEntityDamage(EntityDamageEvent event) {

        if(!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent pvpEvent = (EntityDamageByEntityEvent) event;

        Player damager, damagee;

        damager = (pvpEvent.getDamager() instanceof Player ? (Player) pvpEvent.getDamager() : null);

        damagee = (pvpEvent.getEntity() instanceof Player ? (Player) pvpEvent.getEntity() : null);

        //if it's not player versus player, return
        if(damager == null || damagee == null) {
            return;
        }

        Town damagerTown = manager.matchPlayerToTown(damager);
        if(damagerTown.allowsFriendlyFire() && damagerTown.equals(manager.matchPlayerToTown(damagee))) {
            event.setCancelled(true);
            damager.sendMessage(ChatColor.RED + "That player is in your town! Don't attack him!");
        }


    }

}
