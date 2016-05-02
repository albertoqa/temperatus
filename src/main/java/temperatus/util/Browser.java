package temperatus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;

/**
 * Open the default web browser and load a url
 * <p>
 * Created by alberto on 1/5/16.
 */
public class Browser {

    private static Logger logger = LoggerFactory.getLogger(Browser.class.getName());

    /**
     * Open a web-page on the default browser
     *
     * @param uri page to open
     */
    public static void openWebPage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                logger.error("Error opening default browser... " + e.getMessage());
            }
        }
    }

}