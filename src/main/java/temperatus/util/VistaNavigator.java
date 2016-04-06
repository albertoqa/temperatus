package temperatus.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
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

    static Logger logger = LoggerFactory.getLogger(VistaNavigator.class.getName());

    public static final SpringFxmlLoader loader = new SpringFxmlLoader();

    public static final double MIN_HEIGHT = 800.0;
    public static final double MIN_WIDTH = 1200.0;

    ////////////////////////////////////////////////////////////////////////////
    /**
     * Abstract controller
     * Should be set to the same controller loaded on the vistaHolder
     * Used to reload some part of the view if needed
     */

    public static AbstractController controller;

    public static AbstractController getController() {
        return controller;
    }

    public static void setController(AbstractController controller) {
        logger.debug("VistaNavigator: abstractController set to " + controller.getClass().getName());

        VistaNavigator.controller = controller;
    }


    ////////////////////////////////////////////////////////////////////////////
    /*  The main application layout  */

    public static BaseController baseController;

    public static void setBaseController(BaseController baseController) {
        VistaNavigator.baseController = baseController;
    }

    public static <T> T loadVista(String fxml) {
        if (baseController.getVistaHolder().getChildren().size() > 0) {
            //avoid to set the same controller twice -- remember to set the id of all fxml set in the baseView
            if (baseController.getVistaHolder().getChildren().get(baseController.getVistaHolder().getChildren().size() - 1).getId().equals(fxml)) {
                return null;
            }
        }
        Node node = (Node) loader.load(VistaNavigator.class.getResource(fxml));
        baseController.setView(node);
        return loader.getController();
    }

    public static <T> T pushViewToStack(String fxml) {
        Node node = (Node) loader.load(VistaNavigator.class.getResource(fxml));
        baseController.pushViewToStack(node);
        return loader.getController();
    }

    public static void popViewFromStack() {
        baseController.popViewFromStack();
    }


    ////////////////////////////////////////////////////////////////////////////
    /*  Vista Utils  */

    public static <T> T preloadController(String url) {
        loader.load(VistaNavigator.class.getResource(url));
        return loader.getController();
    }

    public static Scene createModalScene(Parent root) {
        Scene scene = new Scene(root);
        scene.setFill(null);
        return scene;
    }

    public static Stage createModalStage(Scene scene, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        return stage;
    }

    public static <T> T openModal(String url, String title, AnchorPane pane) {
        Parent root = loader.load(VistaNavigator.class.getResource(url));
        Scene scene = createModalScene(root);
        Stage stage = createModalStage(scene, title);
        Animation.fadeOutIn(null, root);
        if(pane != null) {
            pane.setDisable(true);
        }
        stage.show();
        return loader.getController();
    }

    public static void closeModal(Node n, Node p) {
        Animation.fadeInOutClose(n);
        p.setDisable(false);
    }

    public static <T> T setViewInStackPane(StackPane stackPane, String fxml) {
        Node node = (Node) loader.load(VistaNavigator.class.getResource(fxml));

        if (stackPane.getChildren().size() > 0) {
            Animation.fadeOutIn(stackPane.getChildren().get(0), node);
        }
        stackPane.getChildren().setAll(node);
        return loader.getController();
    }

    public static <T> T loadViewInTab(Tab tab, String fxml) {
        Node node = (Node) loader.load(VistaNavigator.class.getResource(fxml));
        tab.setContent(node);
        return loader.getController();
    }

}

