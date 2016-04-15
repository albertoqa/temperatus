package temperatus.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.util.Constants;

import java.util.*;

/**
 * Created by alberto on 20/12/15.
 */
public class Language {

    private final String UNDERSCORE = "_";  // Split by underscore

    private static final Language instance = new Language();
    private final List<String> languages = Arrays.asList("Spanish", "English");
    private final List<String> locales = Arrays.asList("es_ES", "en_US");
    private ResourceBundle resourceBundle;

    private static Logger logger = LoggerFactory.getLogger(Language.class.getName());

    private Language() {
        loadLanguage();
    }

    private void loadLanguage() {
        Locale locale;
        try {
            String[] language = Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN).split(UNDERSCORE);
            locale = new Locale(language[0], language[1]);
        } catch (Exception ex) {
            logger.error("Error loading language");

            locale = new Locale("en", "US");    // If any error, load English
        }
        resourceBundle = ResourceBundle.getBundle("languages/language", locale);
    }

    public String get(String message) {
        try {
            return resourceBundle.getString(message);
        } catch (MissingResourceException ex) {
            logger.warn("String not found in resources");
            return "??????";
        }
    }

    private String localeToLanguage(String locale) {
        return languages.get(locales.indexOf(locale));
    }

    public String getLanguage() {
        String language = Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN);
        return localeToLanguage(language);
    }

    public static Language getInstance() {
        return instance;
    }

}
