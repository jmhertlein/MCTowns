/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.listeners;

import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.util.Config;
import org.bukkit.event.Listener;

/**
 * intended to stop PvP between townmates.
 *
 * @author Joshua
 */
public class MCTPvPListener implements Listener {

    private TownManager manager;
    private Config options;

    public MCTPvPListener(TownManager manager, Config options) {
        this.manager = manager;
        this.options = options;

    }

//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onEntityDamage(EntityDamageEvent event) {
//
//        if (!(event instanceof EntityDamageByEntityEvent)) {
//            return;
//        }
//
//        EntityDamageByEntityEvent pvpEvent = (EntityDamageByEntityEvent) event;
//
//        Player damager, damagee;
//
//        damager = (pvpEvent.getDamager() instanceof Player ? (Player) pvpEvent.getDamager() : null);
//
//        damagee = (pvpEvent.getEntity() instanceof Player ? (Player) pvpEvent.getEntity() : null);
//
//        //if it's not player versus player, return
//        if (damager == null || damagee == null) {
//            return;
//        }
//
//        Town damagerTown = manager.matchPlayerToTown(damager);
//        if (damagerTown.allowsFriendlyFire() && damagerTown.equals(manager.matchPlayerToTown(damagee))) {
//            event.setCancelled(true);
//            damager.sendMessage(ChatColor.RED + "That player is in your town! Don't attack him!");
//        }
//
//
//    }
}
