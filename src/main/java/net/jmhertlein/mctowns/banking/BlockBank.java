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
package net.jmhertlein.mctowns.banking;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author joshua
 */
public class BlockBank {

    private static final long serialVersionUID = "TOWNBANK".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 1;
    private Map<Material, Integer> bank;
    private volatile BigDecimal townFunds;

    /**
     * Constructs a new empty block bank.
     */
    public BlockBank() {
        bank = new ConcurrentHashMap<>();
        townFunds = BigDecimal.ZERO;
    }

    /**
     * Deposits blocks into the block bank.
     *
     * @param blockType the type of block to deposit
     * @param quantity the number of blocks to deposit
     * @return true if the blocks were deposited, false if they were not due to
     * any reason.
     */
    public boolean depositBlocks(Material blockType, int quantity) {

        if (quantity <= 0) {
            return false;
        }

        if (bank.containsKey(blockType)) {
            bank.put(blockType, quantity + bank.get(blockType));
        } else {
            bank.put(blockType, quantity);
        }

        return true;
    }

    /**
     * Attempts to subtract quantity blocks of type corresponding to dataValue
     * from the bank. The withdrawal either completes successfully (EXACTLY
     * 'quantity' blocks were withdrawn) and true is returned, or NOTHING
     * happens and false is returned.
     *
     * @param blockType the type of block to withdraw
     * @param quantity the number of blocks to withdraw
     * @return true if the full number of blocks were withdrawn, false otherwise
     */
    public boolean withdrawBlocks(Material blockType, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (bank.containsKey(blockType)) {
            if (bank.get(blockType) - quantity < 0) {
                return false;
            }

            bank.put(blockType, bank.get(blockType) - quantity);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks how many blocks of a certain data value are in the bank
     *
     * @param blockType the data value of the block to be queried
     * @return the number of blocks in the bank for the given data value
     */
    public int queryBlocks(Material blockType) {
        if (bank.containsKey(blockType)) {
            return bank.get(blockType).intValue();
        }
        return -1;
    }

    public boolean depositCurrency(BigDecimal amt) {
        if (amt.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        townFunds = townFunds.add(amt);
        return true;

    }

    public BigDecimal withdrawCurrency(BigDecimal amt) {
        BigDecimal result;

        result = townFunds.subtract(amt);

        if (result.compareTo(BigDecimal.ZERO) < 0) {
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

    public void writeYAML(FileConfiguration f) {
        f.set("bank.townFunds", townFunds.toString());

        List<String> l = new LinkedList<>();

        for (Entry<Material, Integer> e : bank.entrySet()) {
            l.add(e.getKey().name() + "|" + e.getValue().toString());
        }

        f.set("bank.contents", l);
    }

    public static BlockBank readYAML(FileConfiguration f) {
        BlockBank bank = new BlockBank();

        bank.bank = new TreeMap<>();
        String[] temp;

        for (String s : f.getStringList("bank.contents")) {
            temp = s.split("[|]");
            bank.bank.put(Material.valueOf(temp[0]), Integer.parseInt(temp[1].trim()));
        }

        bank.townFunds = new BigDecimal(f.getString("bank.townFunds"));

        return bank;
    }
}
