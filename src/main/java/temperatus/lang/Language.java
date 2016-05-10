package temperatus.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.util.Constants;

import java.util.*;

/**
 * Languages supported: English, Spanish
 * <p>
 * Created by alberto on 20/12/15.
 */
public class Language {

    private static final Language instance = new Language();    // singleton, only one language instance

    private final List<String> languages = Arrays.asList("Spanish", "English");     // language supported
    private final List<String> locales = Arrays.asList("es_ES", "en_US");

    private ResourceBundle resourceBundle;  // resourceBundle with the strings in each language

    private static Logger logger = LoggerFactory.getLogger(Language.class.getName());

    /**
     * Load the language preferred for the user
     */
    private Language() {
        loadLanguage();
    }

    /**
     * Load the preferred language. If no preferred language set or not found English selected by default.
     */
    private void loadLanguage() {
        Locale locale;
        try {
            String[] language = Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN).split(Constants.UNDERSCORE);
            locale = new Locale(language[0], language[1]);
        } catch (Exception ex) {
            logger.error("Error loading language");
            locale = new Locale("en", "US");    // If any error, load English
        }
        resourceBundle = ResourceBundle.getBundle("languages/language", locale);
    }

    /**
     * Get a string for a given variable from the selected language resource bundle.
     *
     * @param message name of the string to get
     * @return string in the selected language
     */
    public String get(String message) {
        try {
            return resourceBundle.getString(message);
        } catch (MissingResourceException ex) {
            logger.warn("String [ " + message + " ] not found in resources");
            return "??????";
        }
    }

    /**
     * Convert a locale to the language related
     *
     * @param locale locale related to the language
     * @return name of the language related to the locale
     */
    private String localeToLanguage(String locale) {
        return languages.get(locales.indexOf(locale));
    }

    /**
     * Get the preferred language from the system preferences
     *
     * @return preferred locale
     */
    public String getLanguage() {
        String language = Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN);
        return localeToLanguage(language);
    }

    /**
     * Reload the language preferred
     */
    public void reloadLanguage() {
        loadLanguage();
    }

    /**
     * Return the instance of Language (singleton)
     *
     * @return language instance
     */
    public static Language getInstance() {
        return instance;
    }

}
