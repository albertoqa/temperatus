package temperatus.lang;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import temperatus.util.Constants;

import java.util.*;

/**
 * Created by alberto on 20/12/15.
 */
public class Language {

    private static final Language instance = new Language();
    private final List<String> languages = Arrays.asList("Spanish", "English");
    private final List<String> locales = Arrays.asList("es_ES", "en_US");
    private ResourceBundle resourceBundle;
    private Locale locale;

    private Language() {
        loadLanguage();
    }

    public final void loadLanguage() {
        try {
            String[] language = Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN).split("_");
            locale = new Locale(language[0], language[1]);
        } catch (Exception ex) {
            locale = new Locale("en", "US");
        }
        resourceBundle = ResourceBundle.getBundle("languages/language", locale);
    }

    public String get(String message) {
        try {
            return resourceBundle.getString(message);
        } catch (MissingResourceException ex) {
            return "?????";
        }
    }

    public ObservableList<String> getLanguagesList() {
        ObservableList<String> idiomas = FXCollections.observableList(languages);
        return idiomas;
    }

    public String languageToLocale(String language) {
        return locales.get(languages.indexOf(language));
    }

    public String localeToLanguage(String locale) {
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
