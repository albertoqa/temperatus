package temperatus.controller;

import com.dalsemi.onewire.container.OneWireSensor;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.creation.NewIButtonController;
import temperatus.device.DeviceConnectedList;
import temperatus.device.DeviceOperationsManager;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.listener.DeviceDetector;
import temperatus.listener.DeviceDetectorListener;
import temperatus.listener.DeviceDetectorSource;
import temperatus.model.pojo.Author;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.utils.AutoCompleteComboBoxListener;
import temperatus.model.service.AuthorService;
import temperatus.model.service.IbuttonService;
import temperatus.util.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.scene.layout.AnchorPane.setTopAnchor;

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
    @FXML private HBox userPane;

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

    @FXML private Label userLabel;
    @FXML private Label historyLabel;

    @FXML private ComboBox<Author> userBox;
    @FXML private Button startButton;
    @FXML private Button newUserButton;
    @FXML private TextField userInput;

    @Autowired IbuttonService ibuttonService;

    @Autowired DeviceOperationsManager deviceOperationsManager;
    private static boolean isFirstTime = true;  // check if is the first user logging after the application was opened

    @Autowired DeviceDetectorSource deviceDetectorSource;
    @Autowired DeviceConnectedList deviceConnectedList;
    @Autowired AuthorService authorService;

    private ObservableList<Author> authors;
    private final BooleanProperty showBottomPane = new SimpleBooleanProperty(this, "showBottomPane", true);
    private DoubleProperty bottomPaneLocation = new SimpleDoubleProperty(this, "bottomPaneLocation");

    private ToggleGroup menuGroup = new ToggleGroup();  // only one menu option can be selected at a time

    private String actualBaseView = Constants.HOME;  // name of the currently selected view
    private int newIbuttonStageCount = 0;   // count of how many new ibutton screens are open at a time

    private static Logger logger = LoggerFactory.getLogger(BaseController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing base controller");
        userLabel.setText(Constants.EMPTY);

        deviceDetectorSource.addEventListener(this);
        deviceDetectorSource.addEventListener(deviceConnectedList);

        translate();    // Translate menu buttons
        VistaNavigator.setParentNode(this.parentPane);    // used to disable its elements when a modal window is opened

        menuGroup.getToggles().addAll(home, archive, devices, manage, configuration, about, nProject, nMission, nFormula, nGame, nSubject, nPosition, nAuthor);
        home.setSelected(true);
        setImages();    // Set icon images

        // A toggleButton must be selected always, this prevent to deselect a button
        menuGroup.selectedToggleProperty().addListener((ov, toggle, newToggle) -> {
            if (newToggle == null) {
                toggle.setSelected(true);
            }
        });

        // when the user label is pressed the User pane is shown
        userLabel.setOnMouseClicked(event -> {
            Animation.blurOut(VistaNavigator.getParentNode());
            setShowBottomPane(true);
            parentPane.setDisable(true);
        });

        // when the history label is pressed the history view is pushed to the stack and shown
        historyLabel.setOnMouseClicked(event -> VistaNavigator.pushViewToStack(Constants.HISTORY));

        /**
         * Control the animation of the user pane
         * On first start(BaseController is only loaded once at application start) the user pane is shown
         * and the base controller and home view are disabled because the user is required to select an user.
         */
        authors = FXCollections.observableArrayList();

        showBottomPaneProperty().addListener((observable, oldValue, newValue) -> animateBottomPane());
        bottomPaneLocation.addListener((observable, oldValue, newValue) -> updateBottomPaneAnchors());
        new AutoCompleteComboBoxListener<>(userBox);
        getAllUsers();

        Animation.blurOut(VistaNavigator.getParentNode());
        parentPane.setDisable(true);
        userBox.setItems(authors);
    }

    //##################################################################//
    //                                                                  //
    //                         User Control                             //
    //                                                                  //
    //##################################################################//

    /**
     * Fetch all Authors from database and add it to the box.
     * Use a different thread than the UI thread.
     */
    private void getAllUsers() {
        Task<List<Author>> getAuthorsTask = new Task<List<Author>>() {
            @Override
            public List<Author> call() throws Exception {
                return authorService.getAll();
            }
        };

        // on task completion add all authors to the combo-box - if a user was selected, pre-select it
        getAuthorsTask.setOnSucceeded(e -> {
            authors.setAll(getAuthorsTask.getValue());
            if (User.getUser() != null) {
                userBox.getSelectionModel().select(User.getUser());
            }
        });

        // run the task using a thread from the thread pool:
        databaseExecutor.submit(getAuthorsTask);
    }

    /**
     * Change view elements to allow to insert a new user or select users
     */
    @FXML
    private void newUser() {
        if (userBox.isVisible()) {
            newUserButton.setText(language.get(Lang.BACK_BUTTON));
            userBox.setVisible(false);
            userInput.setVisible(true);
        } else {
            newUserButton.setText(language.get(Lang.NEW_USER_BUTTON));
            userBox.setVisible(true);
            userInput.setVisible(false);
        }
    }

    /**
     * Set the user currently using the application and close the user pane
     */
    @FXML
    private void setUser() {
        if (userBox.isVisible()) {  // already existent user
            Author selected = userBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                User.setUser(selected);
                setShowBottomPane(false);
                Animation.blurIn(VistaNavigator.getParentNode());
                parentPane.setDisable(false);
                startDeviceScanTask();
            } else {
                showAlert(Alert.AlertType.INFORMATION, language.get(Lang.MUST_SELECT_USER));
            }
        } else {    // new user
            Author author = new Author();
            author.setName(userInput.getText());
            try {
                authorService.saveOrUpdate(author);
                User.setUser(author);
                userBox.getItems().add(author);
                userBox.getSelectionModel().select(author);
                newUser();
                setShowBottomPane(false);
                Animation.blurIn(VistaNavigator.getParentNode());
                parentPane.setDisable(false);
                startDeviceScanTask();
            } catch (ControlledTemperatusException e) {
                logger.error("BaseController: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, e.getMessage());
            } catch (ConstraintViolationException ex) {
                logger.warn("Duplicate entry");
                showAlert(Alert.AlertType.ERROR, language.get(Lang.DUPLICATE_ENTRY));
            } catch (Exception ex) {
                logger.warn("Unknown exception" + ex.getMessage());
                showAlert(Alert.AlertType.ERROR, language.get(Lang.UNKNOWN_ERROR));
            }
        }
    }

    /**
     * Check if the user is logged for the fist time since the application was opened and if so, start the devices scan
     * task. This prevents that modal windows asking for alias for detected devices prompt over the user logging pane.
     */
    private void startDeviceScanTask() {
        if (isFirstTime) {
            logger.info("Starting devices scan task");
            deviceOperationsManager.init(); // start executors (and device scan task)
            isFirstTime = false;
        }
    }

    /**
     * Update user pane location on show/hide animation
     */
    private void updateBottomPaneAnchors() {
        setTopAnchor(userPane, getBottomPaneLocation());
    }

    /*
    * Starts the animation for the bottom pane.
    */
    private void animateBottomPane() {
        if (isShowBottomPane()) {
            slideBottomPane(0);
        } else {
            slideBottomPane(-userPane.prefHeight(-1));
        }
    }

    /**
     * @return is the pane currently on screen?
     */
    private boolean isShowBottomPane() {
        return showBottomPane.get();
    }

    /**
     * @param showBottom set visibility of user pane
     */
    private void setShowBottomPane(boolean showBottom) {
        showBottomPane.set(showBottom);
    }

    /**
     * Returns the property used to control the visibility of the bottom panel.
     * When the value of this property changes to false then the bottom panel
     * will slide out to the left).
     *
     * @return the property used to control the bottom panel
     */
    private BooleanProperty showBottomPaneProperty() {
        return showBottomPane;
    }

    /**
     * @return current location of the user pane
     */
    private double getBottomPaneLocation() {
        return bottomPaneLocation.get();
    }

    /**
     * Slide animation for user pane
     *
     * @param toY location to translate user pane
     */
    private void slideBottomPane(double toY) {
        KeyValue keyValue = new KeyValue(bottomPaneLocation, toY);
        Double animationDuration = 300.0;
        KeyFrame keyFrame = new KeyFrame(Duration.millis(animationDuration), keyValue);
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
    }

    /**
     * Reload Author on edit/create
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Author) {
            if (!authors.contains(object)) {
                authors.add((Author) object);
            }
            logger.debug("Author reloaded: " + object);
        }
    }

    /**
     * Remove author from the combo-box if is removed from the database.
     *
     * @param author author to remove
     */
    public void removeAuthor(Author author) {
        if (userBox.getSelectionModel().getSelectedItem().equals(author)) {
            userBox.getSelectionModel().clearSelection();
            authors.remove(author);
            userBox.setItems(authors);
            Animation.blurOut(VistaNavigator.getParentNode());
            setShowBottomPane(true);
            parentPane.setDisable(true);
        } else {
            authors.remove(author);
            userBox.setItems(authors);
        }
    }

    //##################################################################//
    //                                                                  //
    //                         Menu Control                             //
    //                                                                  //
    //##################################################################//

    /**
     * Load images for menu icons
     */
    private void setImages() {
        if (Constants.NUMBER_OF_ICONS == Constants.ICONS.length && Constants.NUMBER_OF_ICONS == menuGroup.getToggles().size()) {
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

    /**
     * Load home view
     */
    @FXML
    private void goHome() {
        VistaNavigator.loadVista(Constants.HOME);
        actualBaseView = Constants.HOME;
    }

    /**
     * Load Archived view
     */
    @FXML
    private void goArchive() {
        VistaNavigator.loadVista(Constants.ARCHIVED);
        actualBaseView = Constants.ARCHIVED;
    }

    /**
     * Load Devices view
     */
    @FXML
    private void goDevices() {
        VistaNavigator.loadVista(Constants.CONNECTED);
        actualBaseView = Constants.CONNECTED;
    }

    /**
     * Load Manage viw
     */
    @FXML
    private void goManage() {
        VistaNavigator.loadVista(Constants.MANAGE);
        actualBaseView = Constants.MANAGE;
    }

    /**
     * Load Configuration modal view
     */
    @FXML
    private void goConfig() {
        VistaNavigator.openModal(Constants.CONFIG, Constants.EMPTY);
    }

    /**
     * Load About view
     */
    @FXML
    private void goAbout() {
        VistaNavigator.openModal(Constants.ABOUT, Constants.EMPTY);
    }

    /**
     * Load New Project modal view
     */
    @FXML
    private void goNewProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, Constants.EMPTY);
    }

    /**
     * Load New Mission view
     */
    @FXML
    private void goNewMission() {
        VistaNavigator.loadVista(Constants.NEW_MISSION);
        actualBaseView = Constants.NEW_MISSION;
    }

    /**
     * Load New Game modal view
     */
    @FXML
    private void goNewGame() {
        VistaNavigator.openModal(Constants.NEW_GAME, Constants.EMPTY);
    }

    /**
     * Load New Formula modal view
     */
    @FXML
    private void goNewFormula() {
        VistaNavigator.openModal(Constants.NEW_FORMULA, Constants.EMPTY);
    }

    /**
     * Load New Subject modal view
     */
    @FXML
    private void goNewSubject() {
        VistaNavigator.openModal(Constants.NEW_SUBJECT, Constants.EMPTY);
    }

    /**
     * Load New Position modal view
     */
    @FXML
    private void goNewPosition() {
        VistaNavigator.openModal(Constants.NEW_POSITION, Constants.EMPTY);
    }

    /**
     * Load New Author modal view
     */
    @FXML
    private void goNewAuthor() {
        VistaNavigator.openModal(Constants.NEW_AUTHOR, Constants.EMPTY);
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

    //##################################################################//
    //                                                                  //
    //                       View Operations                            //
    //                                                                  //
    //##################################################################//

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
        historyLabel.setText(language.get(Lang.HISTORY_LABEL));
        newUserButton.setText(language.get(Lang.NEW_USER_BUTTON));
        startButton.setText(language.get(Lang.CONTINUE));
        userInput.setPromptText(language.get(Lang.NEW_USER_PROMPT));
        userBox.setPromptText(language.get(Lang.SELECT_USER_PROMPT));
    }

    /**
     * Set the name of the user currently logged
     *
     * @param name name of the user
     */
    public void setUserName(String name) {
        userLabel.setText(language.get(Lang.USER) + "  " + name);
    }

    /**
     * Show on screen a window for save a new device connected
     */
    private void showNewIbuttonView(DeviceDetector event) {
        SpringFxmlLoader loader = new SpringFxmlLoader();
        Parent root = loader.load(VistaNavigator.class.getResource(Constants.NEW_IBUTTON));

        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.setTitle(Constants.EMPTY);
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.getIcons().setAll(new javafx.scene.image.Image(Constants.ICON_BAR));
        stage.initOwner(VistaNavigator.getMainStage());

        // if userPane is showing: blur user pane
        if (isShowBottomPane()) {
            if (newIbuttonStageCount == 0) {
                Animation.blurOut(userPane);
                stage.setOnCloseRequest(e -> {
                    logger.info("Closing stage");
                    Animation.fadeOutClose(root);
                    Animation.blurIn(userPane);
                    newIbuttonStageCount--;
                });
            } else {
                stage.setOnCloseRequest(e -> {
                    logger.info("Closing stage");
                    Animation.fadeOutClose(root);
                    newIbuttonStageCount--;
                });
            }
            newIbuttonStageCount++;
        }

        // if another modal window is open: show this over the modal window
        else if (VistaNavigator.getCurrentStage() != null) {
            stage.initOwner(VistaNavigator.getCurrentStage());
            stage.setOnCloseRequest(e -> {
                logger.info("Closing stage");
                Animation.fadeOutClose(root);
            });
        }

        // if only the base window is open: blur the base controller
        // if more than one ibutton connected at the same time and new
        else {
            if (newIbuttonStageCount == 0) {
                Animation.blurOut(parentPane);
                stage.setOnCloseRequest(e -> {
                    logger.info("Closing stage");
                    Animation.fadeInOutClose(root);
                    Animation.blurIn(parentPane);
                    newIbuttonStageCount--;
                });
            } else {
                stage.setOnCloseRequest(e -> {
                    logger.info("Closing stage");
                    Animation.fadeOutClose(root);
                    newIbuttonStageCount--;
                });
            }
            newIbuttonStageCount++;
        }

        // common
        ((NewIButtonController) loader.getController()).setData(event.getSerial(), event.getContainer().getName(), !(event.getContainer() instanceof OneWireSensor));
        stage.show();
    }

    //##################################################################//
    //                                                                  //
    //                       Device Detection                           //
    //                                                                  //
    //##################################################################//

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
            Platform.runLater(() -> showNewIbuttonView(event));
        } else {
            Platform.runLater(() -> Notifications.create().title(language.get(Lang.IBUTTONDETECTED)).text("Serial:  " + ibutton.getSerial()).show());
        }
    }

    /**
     * Nothing to do with departure events
     *
     * @param event info of the device departing
     */
    @Override
    public void departure(DeviceDetector event) {
        logger.info("Listening event... device departed! Nothing to do here...");
    }
}
