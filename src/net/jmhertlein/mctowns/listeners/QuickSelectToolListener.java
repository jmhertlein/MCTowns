package net.jmhertlein.mctowns.listeners;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.List;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.command.ActiveSet;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.structure.Plot;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author joshua
 */
public class QuickSelectToolListener implements Listener {

    public static Material SELECT_TOOL;
    private WorldGuardPlugin wgp;
    private MCTownsPlugin mctp;
    private TownManager townMan;

    public QuickSelectToolListener(WorldGuardPlugin wgp, MCTownsPlugin mctp) {
        this.wgp = wgp;
        this.mctp = mctp;
        this.townMan = MCTownsPlugin.getTownManager();
    }

    @EventHandler
    public void onToolUse(PlayerInteractEvent e) {
        if ((e.getPlayer().getItemInHand().getType().compareTo(SELECT_TOOL)) != 0) {
            return;
        }

        Player player = e.getPlayer();

        ActiveSet actives = mctp.getActiveSets().get(e.getPlayer().getName());

        if (actives == null) {
            actives = new ActiveSet();
            mctp.getActiveSets().put(player.getName(), actives);
            List<Town> towns = townMan.matchPlayerToTowns(player);
            actives.setActiveTown(towns.isEmpty() ? null : towns.get(0));
        }
        if(actives.getActiveTown() == null) {
            e.getPlayer().sendMessage(ChatColor.RED + "You need to set your active town first.");
            return;
        }

        Block b = e.getClickedBlock();

        if (b == null)
            return;

        Location spotClicked = b.getLocation();

        ApplicableRegionSet regs = wgp.getRegionManager(e.getPlayer().getWorld()).getApplicableRegions(spotClicked);

        Town town = actives.getActiveTown();
        
        if(town == null) {
            player.sendMessage(ChatColor.RED + "You need to set your active town first.");
            return;
        }
        
        Territory territ = null;
        for (ProtectedRegion pr : regs) {
            territ = townMan.getTerritory(pr.getId());
            if (territ != null && territ.getParentTown().equals(town.getTownName())) 
                break;
            else
                territ = null;
        }

        Plot plot = null;
        if (territ != null) {
            for (ProtectedRegion pr : regs) {
                plot = townMan.getPlot(pr.getId());
                if (plot != null && plot.getParentTerritoryName().equals(territ.getName())) 
                    break;
                else
                    plot = null;
            }
        }

        actives.setActiveTown(town);
        actives.setActiveTerritory(territ);
        actives.setActivePlot(plot);
        MCTownsPlugin.logDebug("Active set is now:" + actives);
        e.getPlayer().sendMessage(net.jmhertlein.core.chat.ChatUtil.INFO + "Active set is now: " + actives);
    }
}
