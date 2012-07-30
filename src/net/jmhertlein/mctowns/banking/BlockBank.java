/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.mctowns.banking;

import java.io.*;
import java.math.BigDecimal;
import java.util.TreeMap;
import java.util.logging.Level;
import net.jmhertlein.mctowns.MCTowns;

/**
 *
 * @author joshua
 */
public class BlockBank implements Externalizable {

    private static final long serialVersionUID = "TOWNBANK".hashCode(); // DO NOT CHANGE
    private static final int VERSION = 1;
    private TreeMap<Integer, Integer> bank;
    private BigDecimal townFunds;

    /**
     * Constructs a new empty block bank.
     */
    public BlockBank() {
        bank = new TreeMap<>();
        townFunds = BigDecimal.ZERO;
    }

    /**
     * Deposits blocks into the block bank.
     *
     * @param dataValue the type of block to deposit
     * @param quantity the number of blocks to deposit
     * @return true if the blocks were deposited, false if they were not due to
     * any reason.
     */
    public boolean depositBlocks(int dataValue, int quantity) {

        if (quantity <= 0) {
            return false;
        }

        if (bank.containsKey(dataValue)) {
            bank.put(dataValue, quantity + bank.get(dataValue));
        } else {
            bank.put(dataValue, quantity);
        }

        return true;
    }

    /**
     * Attempts to subtract quantity blocks of type corresponding to dataValue
     * from the bank. The withdrawl either completes successfully (EXACTLY
     * 'quantity' blocks were withdrawn) and true is returned, or NOTHING
     * happens and false is returned.
     *
     * @param dataValue the type of block to withdraw
     * @param quantity the number of blocks to withdraw
     * @return true if the full number of blocks were withdrawn, false otherwise
     */
    public boolean withdrawBlocks(int dataValue, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        if (bank.containsKey(dataValue)) {
            if (bank.get(dataValue) - quantity < 0) {
                return false;
            }

            bank.put(dataValue, bank.get(dataValue) - quantity);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks how many blocks of a certain data value are in the bank
     *
     * @param dataValue the data value of the block to be queried
     * @return the number of blocks in the bank for the given data value
     */
    public int queryBlocks(int dataValue) {
        if (bank.containsKey(dataValue)) {
            return bank.get(dataValue).intValue();
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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);

        out.writeObject(bank);
        out.writeObject(townFunds);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        int ver = in.readInt();

        if (ver == 0) {
            //============Beginning of original variables for version 0=========
            bank = (TreeMap<Integer, Integer>) in.readObject();
            //============End of original variables for version 0===============
            townFunds = BigDecimal.ZERO;
        } else if (ver == 1) {
            //============Beginning of original variables for version 1=========
            bank = (TreeMap<Integer, Integer>) in.readObject();
            townFunds = (BigDecimal) in.readObject();
            //============End of original variables for version 1===============

        } else {
            MCTowns.log.log(Level.SEVERE, "MCTowns: Unsupported version (version " + ver + ") of BlockBank.");
        }
    }
}
