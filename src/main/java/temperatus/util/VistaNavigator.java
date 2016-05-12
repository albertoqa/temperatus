package temperatus.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.controller.AbstractController;
import temperatus.controller.BaseController;

import java.util.Optional;

/**
 * Utility class for controlling navigation between vistas.
 */
public class VistaNavigator {

    private static AbstractController controller;   // controller of the view currently loaded on the main stack

    public static BaseController baseController;    // base controller of the application
    private static Stage mainStage;                 // main stage of the application

    private static Stage currentStage;              // if a modal stage is on screen this stage is pointing to it, else null
    private static Node parentNode;                 // Used to disable/blur when a modal window is opened

    public static final SpringFxmlLoader loader = new SpringFxmlLoader();

    public static final double MIN_HEIGHT = 620.0;  // minimum height allowed for the application
    public static final double MIN_WIDTH = 1000.0;  // minimum width allowed for the application

    private static Logger logger = LoggerFactory.getLogger(VistaNavigator.class.getName());

    // Don't allow to instantiate this class
    private VistaNavigator() {
    }

    //##################################################################//
    //                                                                  //
    //                      Abstract controller                         //
    //                                                                  //
    //  Should be set to the same controller loaded on the vistaHolder  //
    //  Used to reload some part of the view if needed                  //
    //                                                                  //
    //##################################################################//

    /**
     * Return the controller of the view currently loaded in the main stack
     *
     * @return controller of the view in the main stack
     */
    public static AbstractController getController() {
        return controller;
    }

    /**
     * Set the controller of the view currently loaded in the main stack
     *
     * @param controller controller of the view
     */
    public static void setController(AbstractController controller) {
        VistaNavigator.controller = controller;
        logger.debug("VistaNavigator: abstractController set to " + controller.getClass().getName());
    }

    //##################################################################//
    //                                                                  //
    //               Main Application Layout/Stage                      //
    //                                                                  //
    //##################################################################//

    /**
     * Set the base stage of the application
     *
     * @param mainStage stage of the main window
     */
    public static void setMainStage(Stage mainStage) {
        VistaNavigator.mainStage = mainStage;
    }

    /**
     * Get the stage of the main window
     *
     * @return main stage
     */
    public static Stage getMainStage() {
        return mainStage;
    }

    /**
     * Set the controller of the base fxml -> leftMenu, mainStack, deviceScanTask...
     *
     * @param baseController controller of the base fxml
     */
    public static void setBaseController(BaseController baseController) {
        VistaNavigator.baseController = baseController;
    }

    /**
     * Load a new vista in the main stack
     *
     * @param fxml url of the view to load
     * @param <T>  controller of the view
     * @return controller
     */
    public static <T> T loadVista(String fxml) {
        logger.info("Loading fxml: " + fxml);

        //avoid to set the same controller twice -- remember to set the id of all fxml set in the baseView
        if (baseController.getVistaHolder().getChildren().size() > 0 && baseController.getVistaHolder().getChildren().get(baseController.getVistaHolder().getChildren().size() - 1).getId().equals(fxml)) {
            return null;
        }

        Node node = loader.load(VistaNavigator.class.getResource(fxml));
        baseController.setView(node);
        return loader.getController();
    }

    /**
     * Push a new view to the main stack
     *
     * @param fxml location of the fxml to push
     * @param <T>  controller of the view pushed
     * @return controller
     */
    public static <T> T pushViewToStack(String fxml) {
        Node node = loader.load(VistaNavigator.class.getResource(fxml));
        baseController.pushViewToStack(node);
        return loader.getController();
    }

    /**
     * Pop a view from the main stack
     */
    public static void popViewFromStack() {
        baseController.popViewFromStack();
    }


    //##################################################################//
    //                                                                  //
    //                       Modal View Utils                           //
    //                                                                  //
    //##################################################################//

    /**
     * Set the stage of the modal window currently on screen
     *
     * @param currentStage stage currently on screen
     */
    public static void setCurrentStage(Stage currentStage) {
        VistaNavigator.currentStage = currentStage;
    }

    /**
     * Get the stage currently showed in the screen
     *
     * @return current stage
     */
    public static Stage getCurrentStage() {
        return currentStage;
    }

    /**
     * Set the parent node
     *
     * @param parentNode node
     */
    public static void setParentNode(Node parentNode) {
        VistaNavigator.parentNode = parentNode;
    }

    /**
     * Get the parent node of the currently showing stage
     *
     * @return parent node
     */
    public static Node getParentNode() {
        return parentNode;
    }

    /**
     * Create a new modal stage with the given scene and title
     *
     * @param scene scene contained in the stage
     * @param title title of the stage
     * @return stage created
     */
    private static Stage createModalStage(Scene scene, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);  // prevent the user to press on the application but outside of this modal
        stage.setResizable(false);
        stage.initOwner(mainStage);
        stage.getIcons().setAll(new Image(Constants.ICON_BAR));
        return stage;
    }

    /**
     * Open a modal window from the fxml given as the url and with the specified title
     *
     * @param url   location of the fxml to load
     * @param title title of the window
     * @param <T>   controller of the loaded window
     * @return controller
     */
    public static <T> T openModal(String url, String title) {
        logger.info("Loading modal view: " + title);

        Parent root = loader.load(VistaNavigator.class.getResource(url));
        Scene scene = new Scene(root);
        Stage stage = createModalStage(scene, title);
        currentStage = stage;       // modal window loaded, set the current stage to the new stage
        Animation.fadeOutIn(null, root);
        if (parentNode != null) {
            Animation.blurOut(parentNode);
        }

        // Add listener to the stage so if it is closed by the X button the onCloseModalAction is also called
        stage.setOnCloseRequest(event -> {
            logger.info("Closing stage");
            onCloseModalAction();
        });

        stage.show();
        return loader.getController();
    }

    /**
     * Close the modal window showing with the node given
     *
     * @param n node of the modal window to close
     */
    public static void closeModal(Node n) {
        Animation.fadeInOutClose(n);
        onCloseModalAction();
    }

    /**
     * When close a modal window remove blur effect from its parent, select the base view loaded from the
     * left menu and set the currentStage to null (no modal window currently on screen)
     */
    private static void onCloseModalAction() {
        currentStage = null;
        Animation.blurIn(parentNode);
        baseController.selectBase();
    }


    //##################################################################//
    //                                                                  //
    //                          Alert Utils                             //
    //                                                                  //
    //##################################################################//

    /**
     * Show a new alert window with the type and message given.
     *
     * @param type    type of the alert
     * @param message message to show
     */
    public static void showAlert(Alert.AlertType type, String message) {
        Alert alert = createAlert(type, message);
        alert.show();
    }

    /**
     * Show a new alert window with the type and message given and wait for user response
     *
     * @param type    type of the alert
     * @param message message to show
     */
    public static void showAlertAndWait(Alert.AlertType type, String message) {
        Alert alert = createAlert(type, message);
        alert.showAndWait();
    }

    /**
     * Show a confirmation alert and return true if user press OK, false otherwise.
     *
     * @param type    type of the alert to show
     * @param message message to show
     * @return confirmation or cancel?
     */
    public static boolean confirmationAlert(Alert.AlertType type, String message) {
        Alert alert = createAlert(type, message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && ButtonType.OK == result.get();
    }

    /**
     * Create a new alert with the given type and message
     *
     * @param type    alert type
     * @param message message to show
     * @return alert
     */
    private static Alert createAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        if (currentStage != null) {
            alert.initOwner(currentStage);
        } else if (mainStage != null) {
            alert.initOwner(mainStage);
        }
        return alert;
    }
}

