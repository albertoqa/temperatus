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
import temperatus.listener.DeviceDetector;
import temperatus.listener.DeviceDetectorListener;
import temperatus.listener.DeviceDetectorSource;
import temperatus.listener.DeviceOperationsManager;
import temperatus.model.pojo.Ibutton;
import temperatus.model.service.IbuttonService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

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
    @Autowired ConnectedDevicesController connectedDevicesController;   // scope = singleton
    @Autowired DeviceDetectorSource deviceDetectorSource;
    @Autowired DeviceOperationsManager deviceOperationsManager;

    private final static String clockPattern = "HH:mm:ss";

    static Logger logger = Logger.getLogger(BaseController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing base controller");

        deviceOperationsManager.init();

        deviceDetectorSource.addEventListener(this);
        deviceDetectorSource.addEventListener(connectedDevicesController);

        addMenuElements();
        menu.getSelectionModel().select(language.get(Constants.LHOME));

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

    /***********************************
     *             Menu
     **********************************/

    /**
     * Set the menu elements for navigation in the preferred language
     */
    private void addMenuElements() {
        menu.getItems().add(language.get(Constants.LHOME));
        menu.getItems().add(language.get(Constants.ARCHIVE));
        menu.getItems().add(language.get(Constants.DEVICES));
        menu.getItems().add(language.get(Constants.LMANAGE));
        menu.getItems().add(language.get(Constants.CONFIGURATION));
        menu.getItems().add(language.get(Constants.LABOUT));
    }

    /**
     * Menu controller, load view selected from the list
     * MenuList is as follows: HOME, ARCHIVED, CONNECTED, MANAGE, CONFIG, ABOUT
     *
     * @param event
     */
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
            case 5: {
                VistaNavigator.loadVista(Constants.ABOUT);
            }
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
     * @return the StackPane which holds the views (central)
     */
    public StackPane getVistaHolder() {
        return vistaHolder;
    }

    /**
     * Replaces the actual view for a new one
     *
     * @param node
     */
    public void setView(Node node) {
        logger.debug("Setting view in the root vistaHolder");

        if (vistaHolder.getChildren().size() > 0) {
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
        logger.debug("Pushing view into the stack vistaHolder");

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
        logger.debug("Poping view from the stack vistaHolder");

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
     * When a new device is detected the first thing to do is check if it is already saved in the db
     * that means: check if is the first time this device has been connected to this computer.
     * <p>
     * If it is the first time -> Allow the user to save the button to db and assign a default position to it
     * If it is not the first time (already in db) -> just show an alert to let the user know that is has been detected
     *
     * @param event
     */
    @Override
    public void arrival(DeviceDetector event) {
        logger.info("Listening event... device detected!");

        boolean isNewButton = false;
        Ibutton ibutton = ibuttonService.getBySerial(event.getSerial());    // Search for this serial on DB

        if (ibutton == null) {
            isNewButton = true;

            Platform.runLater(new Runnable() {
                public void run() {
                    VistaNavigator.openModal(Constants.NEW_IBUTTON, language.get(Constants.NEWBUTTONTITLE));
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
        logger.info("Listening event... device departed! Nothing to do here...");
    }
}
