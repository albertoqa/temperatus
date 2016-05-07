package temperatus.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.controller.AbstractController;
import temperatus.controller.BaseController;

/**
 * Utility class for controlling navigation between vistas.
 */
public class VistaNavigator {

    // Don't allow to instantiate this class
    private VistaNavigator() {}

    private static Logger logger = LoggerFactory.getLogger(VistaNavigator.class.getName());

    public static final SpringFxmlLoader loader = new SpringFxmlLoader();

    public static final double MIN_HEIGHT = 620.0;
    public static final double MIN_WIDTH = 1000.0;

    ////////////////////////////////////////////////////////////////////////////
    /**
     * Abstract controller
     * Should be set to the same controller loaded on the vistaHolder
     * Used to reload some part of the view if needed
     */

    private static AbstractController controller;

    public static AbstractController getController() {
        return controller;
    }

    public static void setController(AbstractController controller) {
        VistaNavigator.controller = controller;
        logger.debug("VistaNavigator: abstractController set to " + controller.getClass().getName());
    }

    ////////////////////////////////////////////////////////////////////////////
    /*  The main application layout/stage  */

    public static BaseController baseController;
    private static Stage mainStage;

    public static void setMainStage(Stage mainStage) {
        VistaNavigator.mainStage = mainStage;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setBaseController(BaseController baseController) {
        VistaNavigator.baseController = baseController;
    }

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

    public static <T> T pushViewToStack(String fxml) {
        Node node = loader.load(VistaNavigator.class.getResource(fxml));
        baseController.pushViewToStack(node);
        return loader.getController();
    }

    public static void popViewFromStack() {
        baseController.popViewFromStack();
    }


    ////////////////////////////////////////////////////////////////////////////
    /*  Modal Views Utils  */

    private static Stage currentStage;

    public static void setCurrentStage(Stage currentStage) {
        VistaNavigator.currentStage = currentStage;
    }

    private static Node parentNode;  // Used to disable when a modal window is opened

    public static void setParentNode(Node parentNode) {
        VistaNavigator.parentNode = parentNode;
    }

    public static Node getParentNode() {
        return parentNode;
    }

    private static Stage createModalStage(Scene scene, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.initOwner(mainStage);
        return stage;
    }

    public static <T> T openModal(String url, String title) {
        logger.info("Loading modal view: " + title);

        Parent root = loader.load(VistaNavigator.class.getResource(url));
        Scene scene = new Scene(root);
        Stage stage = createModalStage(scene, title);
        currentStage = stage;
        Animation.fadeOutIn(null, root);
        if (parentNode != null) {
            Animation.blurOut(parentNode);
        }

        stage.setOnCloseRequest(event -> {
            logger.info("Closing stage");
            onCloseModalAction();
        });

        stage.show();
        return loader.getController();
    }

    public static void closeModal(Node n) {
        Animation.fadeInOutClose(n);
        onCloseModalAction();
    }

    private static void onCloseModalAction() {
        currentStage = null;
        //parentNode.setDisable(false);
        Animation.blurIn(parentNode);
        baseController.selectBase();
    }


    ////////////////////////////////////////////////////////////////////////////
    /*  Alert Utils  */

    public static void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        if(currentStage != null) {
            alert.initOwner(currentStage);
        } else if(mainStage != null) {
            alert.initOwner(mainStage);
        }
        alert.show();
    }

}

