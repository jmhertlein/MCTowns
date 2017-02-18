/*
 * Copyright (C) 2013 Joshua Michael Hertlein <jmhertlein@gmail.com>
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
package cafe.josh.mctowns.listeners;

import java.util.Map;
import cafe.josh.mctowns.bank.DepositInventoryEntry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Joshua Michael Hertlein <jmhertlein@gmail.com>
 */
public class DepositBoxCloseListener implements Listener {
    private Map<String, DepositInventoryEntry> openDepositInventories;

    public DepositBoxCloseListener(Map<String, DepositInventoryEntry> openDepositInventories) {
        this.openDepositInventories = openDepositInventories;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        DepositInventoryEntry entry = openDepositInventories.remove(e.getPlayer().getName());
        if(entry == null) {
            return;
        }

        for(ItemStack i : e.getInventory()) {
            if(i == null || i.getType() == null || i.getType() == Material.AIR) {
                continue;
            }
            entry.getOpener().sendMessage(ChatColor.BLUE + "Depositing " + i.getAmount() + " of " + i.getType().toString());

            for(ItemStack overflow : entry.getTargetBank().getBankInventory().addItem(i).values()) {
                entry.getOpener().getInventory().addItem(overflow);
                entry.getOpener().sendMessage(ChatColor.AQUA + "The bank couldn't hold " + overflow.getAmount() + " of your " + overflow.getType().toString());
            }
        }

        entry.getOpener().sendMessage(ChatColor.GREEN + "Deposit complete.");
    }
}
