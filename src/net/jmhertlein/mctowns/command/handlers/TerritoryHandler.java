package net.jmhertlein.mctowns.command.handlers;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import static net.jmhertlein.core.chat.ChatUtil.*;
import net.jmhertlein.core.command.ECommand;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.database.TownManager;
import net.jmhertlein.mctowns.structure.MCTRegion;
import net.jmhertlein.mctowns.structure.Territory;
import net.jmhertlein.mctowns.structure.Town;
import net.jmhertlein.mctowns.structure.yaml.YamlMCTRegion;
import net.jmhertlein.mctowns.structure.yaml.YamlPlot;
import net.jmhertlein.mctowns.structure.yaml.YamlTerritory;
import net.jmhertlein.mctowns.structure.yaml.YamlTown;
import net.jmhertlein.mctowns.structure.TownLevel;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * @author Everdras
 */
public class TerritoryHandler extends CommandHandler {

    public TerritoryHandler(MCTowns parent) {
        super(parent);
    }

    public void addPlotToTerritory(String plotName) {
        if(localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }
        
        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        boolean autoActive = !cmd.hasFlag(ECommand.DISABLE_AUTOACTIVE);

        Town t = localSender.getActiveTown();

        if(t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }

        plotName = TownManager.formatRegionName(t, TownLevel.PLOT, plotName);

        World w = localSender.getPlayer().getWorld();
        String worldName = w.getName();

        Territory parTerr = localSender.getActiveTerritory();

        if (parTerr == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }



        ProtectedRegion region = getSelectedRegion(plotName);

        if (region == null) {
            localSender.sendMessage(ERR + "You need to make a WorldEdit selection first.");
            return;
        }

        if (! selectionIsWithinParent(region, localSender.getActiveTerritory())) {
            localSender.sendMessage(ERR + "Selection is not in territory!");
            return;
        }


        townManager.addPlot(plotName, w, region, t, parTerr);

        localSender.sendMessage(SUCC + "Plot added.");

        if (autoActive) {
            localSender.setActivePlot(townManager.getPlot(plotName));
            localSender.sendMessage(INFO + "Active plot set to newly created plot.");

        }

    }

    public void removePlotFromTerritory(String plotName) {
        if(localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }
        
        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Territory t = localSender.getActiveTerritory();

        if (t == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }

        if (! townManager.removePlot(plotName)) {
            localSender.sendMessage(ERR + "That plot doesn't exist. Make sure you're using the full name of the district (townname_district_districtshortname).");
            return;
        }

        localSender.sendMessage(SUCC + "Plot removed.");
    }

    public void addPlayerToTerritory(String playerName) {
        if(localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }
        
        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        Territory territ = localSender.getActiveTerritory();
        Player player = server.getPlayer(playerName);
        Town t = localSender.getActiveTown();

        if(player == null) {
            localSender.sendMessage(ChatColor.YELLOW + playerName + " is not online. Make sure you typed their name correctly!");
            
            if(!t.playerIsResident(playerName)) {
                localSender.sendMessage(ERR + "That player is not a member of the town.");
                return;
            }
        } else {
            if(!t.playerIsResident(player)) {
                localSender.sendMessage(ERR + "That player is not a member of the town.");
                return;
            }
        }

        if (territ == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }

        if (territ.addPlayer(playerName)) {
            localSender.sendMessage("Player added to territory.");
        } else {
            localSender.sendMessage(ERR + "That player is already in that territory.");
        }
    }

    public void removePlayerFromTerritory(String player) {
        if(localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }
        
        if (!localSender.hasMayoralPermissions()) {
            localSender.notifyInsufPermissions();
            return;
        }

        boolean recursive = cmd.hasFlag(ECommand.RECURSIVE);

        Territory territ = localSender.getActiveTerritory();

        if (territ == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }

        if (player == null) {
            localSender.sendMessage(ERR + "That player is not online.");
            return;
        }

        if (recursive) {
            if (!territ.removePlayer(player)) {
                localSender.sendMessage(ERR + "That player is not in this territory.");
                return;
            }


            YamlPlot p;
            for(MCTRegion reg : townManager.getRegionsCollection()) {
                if(reg instanceof YamlPlot) {
                    p = (YamlPlot) reg;
                    if(p.getParentTerritoryName().equals(territ.getName()))
                        p.removePlayer(player);
                }
            }

            localSender.sendMessage("Player removed from territory.");

        } else {
            if (!territ.removePlayer(player)) {
                localSender.sendMessage(ERR + "That player is not in this territory.");
                return;
            }
            localSender.sendMessage("Player removed from territory.");
        }
    }

    public void setActiveTerritory(String territName) {
        if(localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }
        
        Town t = localSender.getActiveTown();

        if (t == null) {
            localSender.notifyActiveTownNotSet();
            return;
        }



        Territory nuActive = townManager.getTerritory(territName);

        if (nuActive == null) {
            nuActive = townManager.getTerritory(TownManager.formatRegionName(t, TownLevel.TERRITORY, territName));
        }

        if (nuActive == null) {
            localSender.sendMessage(ERR + "The territory \"" + territName + "\" does not exist.");
            return;
        }

        if(! nuActive.getParentTown().equals(t.getTownName())) {
            localSender.sendMessage(ERR + "The territory \"" + territName + "\" does not exist in your town.");
            return;
        }

        localSender.setActiveTerritory(nuActive);
        localSender.sendMessage("Active territory set to " + nuActive.getName());
    }

    private void listPlots(int page) {
        if(localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }
        
        page--; //shift to 0-indexing

        if (page < 0) {
            localSender.sendMessage(ERR + "Invalid page.");
            return;
        }

        Territory t = localSender.getActiveTerritory();

        if (t == null) {
            localSender.notifyActiveTerritoryNotSet();
            return;
        }
        localSender.sendMessage(ChatColor.AQUA + "Existing plots (page " + page + "):");



        String[] plots = t.getPlotsCollection().toArray(new String[t.getPlotsCollection().size()]);

        for (int i = page * RESULTS_PER_PAGE; i < plots.length && i < page * RESULTS_PER_PAGE + RESULTS_PER_PAGE; i++) {
            localSender.sendMessage(ChatColor.YELLOW + plots[i]);
        }
    }

    public void listPlots(String s_page) {
        if(localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }
        
        int page;
        try {
            page = Integer.parseInt(s_page);
        } catch (NumberFormatException nfex) {
            localSender.sendMessage(ERR + "Error parsing integer argument. Found \"" + s_page + "\", expected integer.");
            return;
        }

        listPlots(page);
    }

    public void listPlots() {
        if(localSender.isConsole()) {
            localSender.notifyConsoleNotSupported();
            return;
        }
        
        listPlots(1);
    }
}
