/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.everdras.mctowns.command.executors;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.HashMap;
import me.everdras.mctowns.MCTowns;
import me.everdras.mctowns.command.ActiveSet;
import me.everdras.mctowns.database.TownManager;
import me.everdras.mctowns.townjoin.TownJoinManager;
import me.everdras.mctowns.util.Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Joshua
 */
public class DistrictExecutor extends BaseExecutor {

    public DistrictExecutor(MCTowns parent, WorldGuardPlugin wgp, Economy economy, Config options, TownManager townManager, TownJoinManager joinManager, HashMap<String, ActiveSet> activeSets, HashMap<Player, ActiveSet> potentialPlotBuyers) {
        super(parent, wgp, economy, options, townManager, joinManager, activeSets, potentialPlotBuyers);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
