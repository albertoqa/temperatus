package temperatus.util;

import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.lang.Lang;
import temperatus.lang.Language;

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

    private static final String KEY_FORMAT = "%032x";
    private static final String ALGORITHM = "SHA";      // algorithm used

    private static final String PRIVATE_PASSWORD = "mIcLaV3Pr1vAdAy893477-";    // the master key has to be included with the source code for encrypt-decrypt

    private static Logger logger = LoggerFactory.getLogger(KeyValidator.class.getName());

    /**
     * Check if a mail-key tuple is valid or not
     *
     * @param mail user mail
     * @param key  key sent to the user
     * @return is the tuple valid?
     */
    public static boolean validate(String mail, String key) {
        try {
            String userKey = PRIVATE_PASSWORD + mail;
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(StandardCharsets.UTF_8.encode(userKey));

            return key.equals(String.format(KEY_FORMAT, new BigInteger(1, md.digest())));
        } catch (NoSuchAlgorithmException ex) {
            logger.error("Error validating credentials... " + ex.getMessage());
            VistaNavigator.showAlert(Alert.AlertType.ERROR, Language.getInstance().get(Lang.ERROR_VALIDATING_CREDENTIALS));
            return false;
        }
    }

}
