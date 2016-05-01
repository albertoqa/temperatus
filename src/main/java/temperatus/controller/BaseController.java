package temperatus.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.creation.NewIButtonController;
import temperatus.device.DeviceConnectedList;
import temperatus.lang.Lang;
import temperatus.listener.DeviceDetector;
import temperatus.listener.DeviceDetectorListener;
import temperatus.listener.DeviceDetectorSource;
import temperatus.model.pojo.Ibutton;
import temperatus.model.service.IbuttonService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This is the base controller of the application.
 * There will be always an instance of this class stored in the VistaNavigator class.
 * <p>
 * A StackPane (vistaHolder) is used to keep the actual view and it must have at least one element always.
 * <p>
 * Created by alberto on 17/1/16.
 */
@Controller
public class BaseController implements Initializable, AbstractController, DeviceDetectorListener {

    @FXML private StackPane vistaHolder;

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
    @FXML private ToggleButton nAuthor;

    @FXML private TitledPane accordionPane;
    @FXML private BorderPane parentPane;

    @Autowired IbuttonService ibuttonService;

    @Autowired DeviceDetectorSource deviceDetectorSource;
    @Autowired DeviceConnectedList deviceConnectedList;

    private ToggleGroup menuGroup = new ToggleGroup();  // only one menu option can be selected at a time

    private String actualBaseView = Constants.HOME;  // name of the currently selected view

    private static Logger logger = LoggerFactory.getLogger(BaseController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing base controller");

        deviceDetectorSource.addEventListener(this);
        deviceDetectorSource.addEventListener(deviceConnectedList);

        translate();    // Translate menu buttons

        menuGroup.getToggles().addAll(home, archive, devices, manage, configuration, about, nProject, nMission, nFormula, nGame, nSubject, nPosition, nAuthor);
        home.setSelected(true);
        setImages();    // Set icon images

        // A toggleButton must be selected always, this prevent to deselect a button
        menuGroup.selectedToggleProperty().addListener((ov, toggle, newToggle) -> {
            if (newToggle == null) {
                toggle.setSelected(true);
            }
        });

        VistaNavigator.setParentNode(this.parentPane);    // used to disable its elements when a modal window is opened
    }

    /***********************************
     * Menu
     **********************************/

    /**
     * Load images for menu icons
     */
    private void setImages() {
        if(Constants.NUMBER_OF_ICONS == Constants.ICONS.length && Constants.NUMBER_OF_ICONS == menuGroup.getToggles().size()) {
            ImageView[] images = new ImageView[Constants.NUMBER_OF_ICONS];
            for (int i = 0; i < images.length; i++) {
                ImageView imageView = new ImageView(Constants.ICONS[i]);
                imageView.setFitHeight(Constants.ICON_SIZE);
                imageView.setFitWidth(Constants.ICON_SIZE);
                images[i] = imageView;
            }

            about.setGraphic(images[0]);
            archive.setGraphic(images[1]);
            nAuthor.setGraphic(images[2]);
            configuration.setGraphic(images[3]);
            devices.setGraphic(images[4]);
            nFormula.setGraphic(images[5]);
            nGame.setGraphic(images[6]);
            home.setGraphic(images[7]);
            manage.setGraphic(images[8]);
            nMission.setGraphic(images[9]);
            nPosition.setGraphic(images[10]);
            nProject.setGraphic(images[11]);
            nSubject.setGraphic(images[12]);
        }
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
        VistaNavigator.openModal(Constants.CONFIG, language.get(Lang.CONFIGURATION));
    }

    @FXML
    private void goAbout() {
        VistaNavigator.loadVista(Constants.ABOUT);
        actualBaseView = Constants.ABOUT;
    }

    @FXML
    private void goNewProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, Lang.NEW_PROJECT);
    }

    @FXML
    private void goNewMission() {
        VistaNavigator.loadVista(Constants.NEW_MISSION);
        actualBaseView = Constants.NEW_MISSION;
    }

    @FXML
    private void goNewGame() {
        VistaNavigator.openModal(Constants.NEW_GAME, Lang.NEW_GAME);
    }

    @FXML
    private void goNewFormula() {
        VistaNavigator.openModal(Constants.NEW_FORMULA, Lang.NEW_FORMULA_BUTTON);
    }

    @FXML
    private void goNewSubject() {
        VistaNavigator.openModal(Constants.NEW_SUBJECT, Lang.NEW_SUBJECT_BUTTON);
    }

    @FXML
    private void goNewPosition() {
        VistaNavigator.openModal(Constants.NEW_POSITION, Lang.NEW_POSITION);
    }

    @FXML
    private void goNewAuthor() {
        VistaNavigator.openModal(Constants.NEW_AUTHOR, Lang.NEW_AUTHOR_BUTTON);
    }


    /**
     * Select the toggleButton matching the current view
     */
    public void selectBase() {
        selectMenuButton(actualBaseView);
    }

    /**
     * Set the name of the current view
     *
     * @param newBaseView name of the view currently on screen
     */
    public void setActualBaseView(String newBaseView) {
        actualBaseView = newBaseView;
    }

    /**
     * Select the corresponding toggleButton
     *
     * @param view name of the current view
     */
    public void selectMenuButton(String view) {
        switch (view) {
            case Constants.HOME:
                menuGroup.selectToggle(home);
                break;
            case Constants.ARCHIVED:
                menuGroup.selectToggle(archive);
                break;
            case Constants.CONNECTED:
                menuGroup.selectToggle(devices);
                break;
            case Constants.MANAGE:
                menuGroup.selectToggle(manage);
                break;
            case Constants.CONFIG:
                menuGroup.selectToggle(configuration);
                break;
            case Constants.ABOUT:
                menuGroup.selectToggle(about);
                break;
            case Constants.NEW_MISSION:
                menuGroup.selectToggle(nMission);
                accordionPane.setExpanded(true);
                break;
            default:
                break;
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
     * @param node new view to show
     */
    public void setView(Node node) {
        logger.info("Setting view in the root vistaHolder");

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
        logger.info("Pushing view into the stack vistaHolder");

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
        logger.info("Pop view from the stack vistaHolder");

        if (vistaHolder.getChildren().size() >= 2) { // stack cannot be empty
            Animation.fadeOutIn(vistaHolder.getChildren().get(vistaHolder.getChildren().size() - 1), vistaHolder.getChildren().get(vistaHolder.getChildren().size() - 2));
        }
        vistaHolder.getChildren().remove(vistaHolder.getChildren().size() - 1);
    }

    @Override
    public void translate() {
        home.setText(language.get(Lang.LHOME));
        archive.setText(language.get(Lang.ARCHIVE));
        devices.setText(language.get(Lang.DEVICES));
        manage.setText(language.get(Lang.LMANAGE));
        configuration.setText(language.get(Lang.CONFIGURATION));
        about.setText(language.get(Lang.LABOUT));
        nPosition.setText(language.get(Lang.NPOSITION));
        nProject.setText(language.get(Lang.NPROJECT));
        nMission.setText(language.get(Lang.NMISSION));
        nFormula.setText(language.get(Lang.NFORMULA));
        nGame.setText(language.get(Lang.NGAME));
        nSubject.setText(language.get(Lang.NSUBJECT));
        nAuthor.setText(language.get(Lang.NAUTHOR));
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
     * @param event info of the device connected
     */
    @Override
    public void arrival(DeviceDetector event) {
        logger.info("Listening event... device detected!");

        Ibutton ibutton = ibuttonService.getBySerial(event.getSerial());    // Search for this serial on DB

        if (ibutton == null) {
            Platform.runLater(() -> {
                NewIButtonController newIButtonController = VistaNavigator.openModal(Constants.NEW_IBUTTON, language.get(Lang.NEWBUTTONTITLE));
                newIButtonController.setData(event.getSerial(), event.getContainer().getName());
            });
        } else {
            Platform.runLater(() -> Notifications.create().title(language.get(Lang.IBUTTONDETECTED)).text("Serial:  " + ibutton.getSerial()).show());
        }
    }

    @Override
    public void departure(DeviceDetector event) {
        logger.info("Listening event... device departed! Nothing to do here...");
    }
}
