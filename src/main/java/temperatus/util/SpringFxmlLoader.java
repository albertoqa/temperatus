package temperatus.util;

/**
 * Created by alberto on 17/1/16.
 */

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.net.URL;

public class SpringFxmlLoader {

    private static final ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringApplicationConfig.class);

    private Object controller = null;

    public <T> T load(URL url) {
        try  {
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(applicationContext::getBean);
            T r = loader.load();
            controller = loader.getController();
            return r;
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public <T> T getController() {
        return (T) controller;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}