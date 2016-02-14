package temperatus.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.log4j.Logger;
import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.button.ConnectedDevicesController;
import temperatus.listener.DaemonThreadFactory;
import temperatus.listener.DeviceDetector;
import temperatus.listener.DeviceDetectorListener;
import temperatus.listener.DeviceDetectorTask;
import temperatus.model.pojo.Ibutton;
import temperatus.model.service.IbuttonService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is the base controller of the application.
 * There will be always an instance of this class in the VistaNavigator class.
 * <p>
 * A StackPane (vistaHolder) is used to keep the actual view and it must have at least one element always.
 * <p>
 * Created by alberto on 17/1/16.
 */
@Controller
public class BaseController implements Initializable, AbstractController, DeviceDetectorListener {

    @FXML private StackPane vistaHolder;
    @FXML private Label clock;
    @FXML private ListView<String> menu;

    @Autowired IbuttonService ibuttonService;

    private final static String clockPattern = "HH:mm:ss";

    static Logger logger = Logger.getLogger(BaseController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing base controller");

        ConnectedDevicesController connectedDevicesController = VistaNavigator.preloadController(Constants.CONNECTED);

        //startDeviceListener();  // search for new connected devices

        Constants.deviceDetectorSource.addEventListener(this);
        Constants.deviceDetectorSource.addEventListener(connectedDevicesController);

        addMenuElements();
        menu.getSelectionModel().select(0);

        startClock();
    }

    /**
     * Set the menu elements for navigation in the language preferred
     */
    private void addMenuElements() {
        menu.getItems().add(language.get(Constants.LHOME));
        menu.getItems().add(language.get(Constants.ARCHIVE));
        menu.getItems().add(language.get(Constants.DEVICES));
        menu.getItems().add(language.get(Constants.LMANAGE));
        menu.getItems().add(language.get(Constants.CONFIGURATION));
    }

    /**
     * Starts the clock
     */
    private void startClock() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0),
                event -> clock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern(clockPattern)))),
                new KeyFrame(Duration.seconds(1)));

        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * @return the StackPane which holds the views (central)
     */
    public StackPane getVistaHolder() {
        return vistaHolder;
    }

    @FXML
    private void menuActions(MouseEvent event) {
        switch (menu.getSelectionModel().getSelectedIndex()) {
            case 0: {
                VistaNavigator.loadVista(Constants.HOME);
            }
            break;
            case 1: {
                VistaNavigator.loadVista(Constants.ARCHIVED);
            }
            break;
            case 2: {
                VistaNavigator.loadVista(Constants.CONNECTED);
            }
            break;
            case 3: {
                VistaNavigator.loadVista(Constants.MANAGE);
            }
            break;
            case 4: {
                VistaNavigator.openModal(Constants.CONFIG, language.get(Constants.CONFIGURATION));
            }
            break;
            default:
                break;
        }
    }

    /**
     * Change the selected element in the menu
     *
     * @param element
     */
    public void selectMenuElement(String element) {
        menu.getSelectionModel().select(language.get(element));
    }



    /***********************************
     *       View Operations
     **********************************/

    /**
     * Replaces the actual view for a new one
     *
     * @param node
     */
    public void setView(Node node) {
        if (vistaHolder.getChildren().size() > 0) {
            //avoid to set the same controller twice
            // remember to set the id of all fxml set in the baseView
            if (vistaHolder.getChildren().get(vistaHolder.getChildren().size() - 1).getId().equals(node.getId())) {
                return;
            }
            Animation.fadeOutIn(vistaHolder.getChildren().get(vistaHolder.getChildren().size() - 1), node);
        }
        vistaHolder.getChildren().setAll(node);
    }

    /**
     * Adds a new view to the stack
     *
     * @param node - new view to push to the stack
     */
    public void pushViewToStack(Node node) {
        if (vistaHolder.getChildren().size() > 0) {
            Animation.fadeOutIn(vistaHolder.getChildren().get(vistaHolder.getChildren().size() - 1), node);
        }
        vistaHolder.getChildren().add(node);
    }

    /**
     * Remove a view from the stack
     * Only if after pop stack is not empty
     */
    public void popViewFromStack() {
        if (vistaHolder.getChildren().size() >= 2) { // stack cannot be empty
            Animation.fadeOutIn(vistaHolder.getChildren().get(vistaHolder.getChildren().size() - 1), vistaHolder.getChildren().get(vistaHolder.getChildren().size() - 2));
        }
        vistaHolder.getChildren().remove(vistaHolder.getChildren().size() - 1);
    }

    @Override
    public void translate() {
        logger.debug("Nothing to translate");
    }



    /***********************************
     *       Device Detection
     **********************************/

    /**
     * Create a infinite task that search for all connected devices
     * If a new device is detected a notification is sent and all
     * classes which implement the DeviceDetectorListener are notified of
     * the event.
     * <p>
     * The task runs in a different thread and will stop when the program finish
     */
    private void startDeviceListener() {
        logger.info("Starting device detector task");

        DaemonThreadFactory daemonThreadFactory = new DaemonThreadFactory();
        DeviceDetectorTask task = new DeviceDetectorTask();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory);
        executor.scheduleAtFixedRate(task, Constants.DELAY, Constants.PERIOD, TimeUnit.SECONDS);
    }

    @Override
    public void arrival(DeviceDetector event) {
        logger.info("Listening event... device detected");

        boolean isNewButton = false;
        Ibutton ibutton = ibuttonService.getBySerial(event.getSerial());

        if (ibutton == null) {
            isNewButton = true;

            Platform.runLater(new Runnable() {
                public void run() {
                    VistaNavigator.openModal(Constants.NEW_IBUTTON, "");
                }
            });
        }

        if (!isNewButton) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Notifications.create().title("iButton detected").text("Serial: " + ibutton.getSerial()).show();
                }
            });
        }
    }

    @Override
    public void departure(DeviceDetector event) {

    }
}
