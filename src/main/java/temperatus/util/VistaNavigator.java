package temperatus.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.controller.AbstractController;
import temperatus.controller.BaseController;

/**
 * Utility class for controlling navigation between vistas.
 */
public class VistaNavigator {

    private VistaNavigator() {
    }

    private static Logger logger = LoggerFactory.getLogger(VistaNavigator.class.getName());

    public static final SpringFxmlLoader loader = new SpringFxmlLoader();

    public static final double MIN_HEIGHT = 680.0;
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
    /*  The main application layout  */

    public static BaseController baseController;

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

    private static Node parentNode;  // Used to disable when a modal window is opened

    public static void setParentNode(Node parentNode) {
        VistaNavigator.parentNode = parentNode;
    }

    public static Node getParentNode() {
        return parentNode;
    }

    private static Scene createModalScene(Parent root) {
        Scene scene = new Scene(root);
        scene.setFill(null);
        return scene;
    }

    private static Stage createModalStage(Scene scene, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        return stage;
    }

    public static <T> T openModal(String url, String title) {
        logger.info("Loading modal view: " + title);

        Parent root = loader.load(VistaNavigator.class.getResource(url));
        Scene scene = createModalScene(root);
        Stage stage = createModalStage(scene, title);
        Animation.fadeOutIn(null, root);
        if (parentNode != null) {
            parentNode.setDisable(true);
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
        parentNode.setDisable(false);
        baseController.selectBase();
    }

}

