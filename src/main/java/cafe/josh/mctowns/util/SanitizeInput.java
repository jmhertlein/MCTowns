/*
 * Copyright (C) 2017 Toby Austin Bennett <toby.a.bennnett@gmail.com>
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
package cafe.josh.mctowns.util;

import cafe.josh.mctowns.command.MCTLocalSender;
import java.math.BigDecimal;
import java.lang.Long;
import static net.jmhertlein.core.chat.ChatUtil.ERR;

/**
 *
 * @author toby
 */
public class SanitizeInput {
    
    public static BigDecimal SanitizeBigDec(String toSanitize, MCTLocalSender sender) {
       
       try{
           
        long inputToLong = Long.parseLong(toSanitize);
        BigDecimal longToBigDec = new BigDecimal(inputToLong);
        
        return longToBigDec;
        
       } catch ( NumberFormatException e) {
        sender.sendMessage(ERR + "Error parsing quantity \"" + toSanitize + "\" : " + e.getMessage());
        System.out.println(e.getMessage());
        return null;
       }
        
    }
    
    public static BigDecimal SanitizeBigDec(String toSanitize) {
       
       try{
           
        long inputToLong = Long.parseLong(toSanitize);
        BigDecimal longToBigDec = new BigDecimal(inputToLong);
        
        return longToBigDec;
        
       } catch ( NumberFormatException e) {
        System.out.println("Error parsing quantity \"" + toSanitize + "\" : " + e.getMessage());
        return null;
       }
        
    }

}