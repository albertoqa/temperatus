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
public class KeyValidator {

    /**
     * @param args the command line arguments
     *             args[0] must be the mail of the user
     *             args[1] must be the key to check
     */
    public static void main(String[] args) {
        try {
            String privatePassword = "mIcLaV3Pr1vAdAy893477-";  // private password
            String key = privatePassword + args[0];     // key formed by private password and user mail
            String expectedValue = args[1];             // key to check if valid

            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(StandardCharsets.UTF_8.encode(key));

            // check if valid key
            if (expectedValue.equals(String.format("%032x", new BigInteger(1, md.digest())))) {
                System.out.println("Valid key");
            } else {
                System.out.println("Invalid key");
            }

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(KeyValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
