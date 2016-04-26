package temperatus.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Validate license keys
 * <p>
 * Created by alberto on 26/4/16.
 */
public class KeyValidator {

    /**
     * Check if a mail-key tuple is valid or not
     * @param mail user mail
     * @param key key sent to the user
     * @return is the tuple valid?
     */
    public static boolean validate(String mail, String key) {
        try {
            // el programa tiene que incluir en su código la clave privada...
            String privatePassword = "mIcLaV3Pr1vAdAy893477-";    // FIXME que clave pongo? como protejo esto?
            String userKey = privatePassword + mail;

            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(StandardCharsets.UTF_8.encode(userKey));

            return key.equals(String.format("%032x", new BigInteger(1, md.digest())));
        } catch (NoSuchAlgorithmException ex) {
            // TODO show error on validation
            return false;
        }
    }

}
