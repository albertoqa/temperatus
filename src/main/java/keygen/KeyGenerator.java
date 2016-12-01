/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keygen;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author alberto
 */
public class KeyGenerator {

    /**
     * @param args the command line arguments
     *             arg[0] must be the mail of the user
     */
    public static void main(String[] args) {
        try {
            String privatePassword = "mIcLaV3Pr1vAdAy893477-";
            String key = privatePassword + args[0]; // private password + user mail
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(StandardCharsets.UTF_8.encode(key));

            // print key
            System.out.println("Your key is: " + String.format("%032x", new BigInteger(1, md.digest())));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(KeyGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
