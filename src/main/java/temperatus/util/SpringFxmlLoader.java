package temperatus.util;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.net.URL;

/**
 * Override JavaFX loader to load Spring Beans for Controllers
 * <p>
 * Created by alberto on 17/1/16.
 */
class SpringFxmlLoader {

    private static final ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringApplicationConfig.class);

    private Object controller = null;

    public <T> T load(URL url) {
        try {
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(applicationContext::getBean);
            T r = loader.load();
            controller = loader.getController();
            return r;
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    /**
     * Get the controller of the last loaded view
     *
     * @param <T> type of controller
     * @return last loaded view's controller
     */
    public <T> T getController() {
        return (T) controller;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}