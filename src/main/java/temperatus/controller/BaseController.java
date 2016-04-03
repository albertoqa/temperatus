package temperatus.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @FXML private ToggleButton home;
    @FXML private ToggleButton archive;
    @FXML private ToggleButton devices;
    @FXML private ToggleButton manage;
    @FXML private ToggleButton configuration;
    @FXML private ToggleButton about;

    @FXML private ToggleButton nProject;
    @FXML private ToggleButton nMission;
    @FXML private ToggleButton nFormula;
    @FXML private ToggleButton nGame;
    @FXML private ToggleButton nSubject;
    @FXML private ToggleButton nPosition;

    @FXML private TitledPane accordionPane;

    @Autowired IbuttonService ibuttonService;
    @Autowired ConnectedDevicesController connectedDevicesController;   // scope = singleton
    @Autowired DeviceDetectorSource deviceDetectorSource;
    @Autowired DeviceOperationsManager deviceOperationsManager;

    private ToggleGroup menuGroup;

    private final static String clockPattern = "HH:mm:ss";
    private static String actualBaseView = Constants.HOME;

    static Logger logger = LoggerFactory.getLogger(BaseController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing base controller");

        deviceOperationsManager.init();

        deviceDetectorSource.addEventListener(this);
        deviceDetectorSource.addEventListener(connectedDevicesController);

        setImages();
        translate();

        menuGroup = new ToggleGroup();
        menuGroup.getToggles().addAll(home, archive, devices, manage, configuration, about, nProject, nMission, nFormula, nGame, nSubject, nPosition);
        home.setSelected(true);

        menuGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) {
                if (new_toggle == null) {
                    toggle.setSelected(true);
                }
            }
        });

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

    private void setImages() {

        ImageView homeI = new ImageView("/images/icons/home.png");
        homeI.setFitHeight(15);
        homeI.setFitWidth(15);
        home.setGraphic(homeI);

        ImageView archiveI = new ImageView("/images/icons/archive.png");
        archiveI.setFitHeight(15);
        archiveI.setFitWidth(15);
        archive.setGraphic(archiveI);

        ImageView deviceI = new ImageView("/images/icons/devices.png");
        deviceI.setFitHeight(15);
        deviceI.setFitWidth(15);
        devices.setGraphic(deviceI);

        ImageView manageI = new ImageView("/images/icons/manage.png");
        manageI.setFitHeight(15);
        manageI.setFitWidth(15);
        manage.setGraphic(manageI);

        ImageView confI = new ImageView("/images/icons/conf.png");
        confI.setFitHeight(15);
        confI.setFitWidth(15);
        configuration.setGraphic(confI);

        ImageView aboutI = new ImageView("/images/icons/about.png");
        aboutI.setFitHeight(15);
        aboutI.setFitWidth(15);
        about.setGraphic(aboutI);

    }

    @FXML
    private void goHome() {
        VistaNavigator.loadVista(Constants.HOME);
        actualBaseView = Constants.HOME;
    }

    @FXML
    private void goArchive() {
        VistaNavigator.loadVista(Constants.ARCHIVED);
        actualBaseView = Constants.ARCHIVED;
    }

    @FXML
    private void goDevices() {
        VistaNavigator.loadVista(Constants.CONNECTED);
        actualBaseView = Constants.CONNECTED;
    }

    @FXML
    private void goManage() {
        VistaNavigator.loadVista(Constants.MANAGE);
        actualBaseView = Constants.MANAGE;
    }

    @FXML
    private void goConfig() {
        VistaNavigator.openModal(Constants.CONFIG, language.get(Constants.CONFIGURATION));
    }

    @FXML
    private void goAbout() {
        VistaNavigator.loadVista(Constants.ABOUT);
        actualBaseView = Constants.ABOUT;
    }

    @FXML
    private void goNewProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, Constants.NEWPROJECT);
    }

    @FXML
    private void goNewMission() {
        VistaNavigator.loadVista(Constants.NEW_MISSION);
        actualBaseView = Constants.NEW_MISSION;
    }

    @FXML
    private void goNewGame() {
        VistaNavigator.openModal(Constants.NEW_GAME, Constants.NEWGAME);
    }

    @FXML
    private void goNewFormula() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, Constants.NEWPROJECT);
    }

    @FXML
    private void goNewSubject() {
        VistaNavigator.openModal(Constants.NEW_SUBJECT, Constants.NEWSUBJECT);
    }

    @FXML
    private void goNewPosition() {
        VistaNavigator.openModal(Constants.NEW_POSITION, Constants.NEWPOSITION);
    }

    public void selectBase() {
        selectMenuButton(actualBaseView);
    }

    public void setActualBaseView(String newBaseView) {
        actualBaseView = newBaseView;
    }

    public void selectMenuButton(String view) {
        switch (view) {
            case Constants.HOME: menuGroup.selectToggle(home);
                break;
            case Constants.ARCHIVED: menuGroup.selectToggle(archive);
                break;
            case Constants.CONNECTED: menuGroup.selectToggle(devices);
                break;
            case Constants.MANAGE: menuGroup.selectToggle(manage);
                break;
            case Constants.CONFIG: menuGroup.selectToggle(configuration);
                break;
            case Constants.ABOUT: menuGroup.selectToggle(about);
                break;
            case Constants.NEW_MISSION: menuGroup.selectToggle(nMission);
                break;
            default: break;
        }
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
        home.setText(language.get(Constants.LHOME));
        archive.setText(language.get(Constants.ARCHIVE));
        devices.setText(language.get(Constants.DEVICES));
        manage.setText(language.get(Constants.LMANAGE));
        configuration.setText(language.get(Constants.CONFIGURATION));
        about.setText(language.get(Constants.LABOUT));
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
