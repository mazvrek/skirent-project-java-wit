package pl.skirent.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n {
    private static I18n instance;
    private ResourceBundle bundle;
    private Locale currentLocale;

    private I18n() {
        currentLocale = new Locale("pl");
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    public static I18n getInstance() {
        if (instance == null) {
            instance = new I18n();
        }
        return instance;
    }

    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        this.bundle = ResourceBundle.getBundle("messages", locale);
    }

    public Locale getLocale() {
        return currentLocale;
    }
}
