/*
 * Copyright (C) 2014 Joshua M Hertlein
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jmhertlein.mctowns.task;

import java.math.BigDecimal;
import net.jmhertlein.mctowns.MCTowns;
import net.jmhertlein.mctowns.MCTownsPlugin;
import net.jmhertlein.mctowns.structure.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author joshua
 */
public class TaxCollectionTask extends BukkitRunnable {
    private final MCTownsPlugin p;

    public TaxCollectionTask(MCTownsPlugin p) {
        this.p = p;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis(), last = MCTowns.getLastTaxTimestamp();

        now /= 1000;
        now /= 60;
        last /= 1000;
        now /= 60;

        if ((now - last) < MCTowns.getTaxDelayMinutes())
            return;

        for (Town t : p.getTownManager().getTownsCollection()) {
            if (!t.collectsTaxes())
                continue;

            for (String s : t.getResidentNames()) {
                BigDecimal amount;
                switch (t.getTaxStrategy()) {
                    case FLAT_AMOUNT:
                        amount = t.getFlatTaxAmount();
                        break;
                    case PERCENTAGE:
                        amount = new BigDecimal(MCTowns.getEconomy().getBalance(s)).multiply(new BigDecimal(t.getPercentTaxAmount()));
                        break;
                    default:
                        throw new RuntimeException("Error: Unknown tax strategy: " + t.getTaxStrategy().name());
                }
                if (!chargePlayer(s, amount))
                    handlePlayerInDefault(s, t, amount);
                else
                    handlePlayerPaidSuccessfully(s, t, amount);
            }
        }
    }

    private static boolean chargePlayer(String s, BigDecimal amount) {
        final BigDecimal balance = new BigDecimal(MCTowns.getEconomy().getBalance(s));
        if (balance.compareTo(amount) < 0)
            return false;
        else {
            MCTowns.getEconomy().withdrawPlayer(s, amount.doubleValue());
            return true;
        }

    }

    private static void handlePlayerInDefault(String s, Town t, BigDecimal amount) {
        Player player = Bukkit.getPlayerExact(s),
                mayor = Bukkit.getPlayerExact(t.getMayor());

        if(mayor != null) {
            mayor.sendMessage(ChatColor.RED + s + " was unable to pay taxes in " + t.getTownName());
        }

        if(player != null) {
            player.sendMessage(ChatColor.RED + "You were unable to pay the tax of " + amount + " in " + t.getTownName() + "!");
        }
    }

    private static void handlePlayerPaidSuccessfully(String s, Town t, BigDecimal amount) {
        Player player = Bukkit.getPlayerExact(s);

        if(player != null) {
            player.sendMessage(ChatColor.RED + "You paid the tax of " + amount + " in " + t.getTownName() + "!");
        }
    }

}
