/*
 * Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of IPMDecisionsDSSService.
 * IPMDecisionsDSSService is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * IPMDecisionsDSSService is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with IPMDecisionsDSSService.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.ipmdecisions.dssservice.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @copyright 2021 <a href="http://www.nibio.no/">NIBIO</a>
 * @author  Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MD5Encrypter {
    
    
    /**
     * 
     * @param original The original string
     * @param salt The salt to add in order to stop rainbow tables from guessing your string
     * @return 
     */
    public static String getMD5HexString(String original, String salt)
    {
        return getMD5HexString(original + salt);
    }
    
    /**
     * @param original the string to be encrypted
     * @return the MD5 sum 
     */
    public static String getMD5HexString (String original) {
        String password = original;
        String MD5HexString = "";
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
        
            MD5HexString =  getHexString(md.digest());
        }
        catch (NoSuchAlgorithmException nsae) {nsae.printStackTrace();}
        
        return MD5HexString;
    }
    
    /**
     * Converts the MD5 byte array to a hexadecimal string
     * @param bytes
     * @return 
     */
    private static String getHexString(byte[] bytes)
    {
        StringBuffer hexString = new StringBuffer();
        for(int i=0;i<bytes.length; i++)
        {
            appendHexPair(bytes[i],hexString);
        }
        
        return hexString.toString();
    }
    
    
    /**
     *Appends a hexadecimal representation of a particular char value
     * to a string buffer. That is, two hexadecimal digits are appended
     * to the string.
     * @param	b			a byte whose hex representation is to be obtained
     * @param	hexString	the string to append the hex digits to
     */
    private static StringBuffer appendHexPair(byte b, StringBuffer hexString)
    {
		char	highNibble = kHexChars[(b & 0xF0) >> 4];
		char	lowNibble = kHexChars[b & 0x0F];

		hexString.append(highNibble);
		hexString.append(lowNibble);
                
                return hexString;
    }
    	
   
     private static final char kHexChars[] =
    	{ '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };
}
