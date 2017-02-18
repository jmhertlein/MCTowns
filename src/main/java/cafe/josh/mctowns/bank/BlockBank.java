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
package cafe.josh.mctowns.bank;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author joshua
 */
public class BlockBank {
    private final Map<String, DepositInventoryEntry> openDepositInventories;

    private final Inventory bankInventory;
    private volatile BigDecimal townFunds;

    /**
     * Constructs a new empty block bank.
     *
     * @param openInventories
     */
    public BlockBank(Map<String, DepositInventoryEntry> openInventories) {
        bankInventory = Bukkit.getServer().createInventory(null, 9 * 6, "Town Bank");
        bankInventory.setMaxStackSize(500);
        openDepositInventories = openInventories;
        townFunds = BigDecimal.ZERO;
    }

    public Inventory getBankInventory() {
        return bankInventory;
    }

    public boolean depositCurrency(BigDecimal amt) {
        if(amt.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        townFunds = townFunds.add(amt);
        return true;

    }

    public BigDecimal withdrawCurrency(BigDecimal amt) {
        BigDecimal result;

        result = townFunds.subtract(amt);

        if(result.compareTo(BigDecimal.ZERO) < 0) {
            amt = amt.add(result);
        }

        townFunds = townFunds.subtract(amt);

        return amt;
    }

    public BigDecimal getCurrencyBalance() {
        return townFunds;
    }

    public boolean hasCurrencyAmount(BigDecimal amt) {
        return townFunds.compareTo(amt) >= 0;
    }

    public Inventory getNewDepositBox(Player p) {
        Inventory i = Bukkit.createInventory(p, 9 * 4, "Town Bank Deposit Box");
        openDepositInventories.put(p.getName(), new DepositInventoryEntry(p, this));
        return i;
    }

    public void writeYAML(FileConfiguration f) {
        f.set("bank.townFunds", townFunds.toString());

        List<ItemStack> existantContents = new LinkedList<>();
        for(ItemStack i : bankInventory.getContents()) {
            if(i != null) {
                existantContents.add(i);
            }
        }

        f.set("bank.contents", existantContents);
    }

    @SuppressWarnings("unchecked")
    public static BlockBank readYAML(FileConfiguration f, Map<String, DepositInventoryEntry> open) {
        BlockBank bank = new BlockBank(open);

        bank.townFunds = new BigDecimal(f.getString("bank.townFunds"));

        for(ItemStack i : (List<ItemStack>) f.getList("bank.contents")) {
            if(i != null) {
                bank.bankInventory.addItem(i);
            }
        }

        return bank;
    }
}
