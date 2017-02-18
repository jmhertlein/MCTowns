/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cafe.josh.mctowns.util.test;

import cafe.josh.mctowns.util.MCTConfig;
import org.junit.Test;

/**
 *
 * @author joshua
 */
public class MCTConfigTest {
    @Test
    public void testBukkitlessQuery() {
        MCTConfig.DEBUG_MODE_ENABLED.getBoolean();
        MCTConfig.MAYORS_CAN_BUY_TERRITORIES.getBoolean();
        MCTConfig.LOG_COMMANDS.getBoolean();
        MCTConfig.QUICKSELECT_TOOL.getString();
        MCTConfig.DEFAULT_TOWN.getObject();
    }
}
