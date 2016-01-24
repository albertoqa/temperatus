package temperatus.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import temperatus.controller.BaseController;

/**
 * Utility class for controlling navigation between vistas.
 */
public class VistaNavigator {

    /**
     * The main application layout controller.
     */
    public static BaseController baseController;

    public static void setBaseController(BaseController baseController) {
        VistaNavigator.baseController = baseController;
    }

    public static void loadVista(String fxml) {
        SpringFxmlLoader loader = new SpringFxmlLoader();
        baseController.setView((Node) loader.load(VistaNavigator.class.getResource(fxml)));
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

    public static <T> T openModal(String url, String title) {
        SpringFxmlLoader loader = new SpringFxmlLoader();
        Parent root = loader.load(VistaNavigator.class.getResource(url));
        Scene scene = createModalScene(root);
        Stage stage = createModalStage(scene, title);
        Animation.fadeOutIn(null, root);
        stage.show();
        return loader.getController();
    }

    public static <T> T setViewInStackPane(StackPane stackPane, String fxml) {
        SpringFxmlLoader loader = new SpringFxmlLoader();
        Node node = (Node) loader.load(VistaNavigator.class.getResource(fxml));

        if (stackPane.getChildren().size() > 0) {
            Animation.fadeOutIn(stackPane.getChildren().get(0), node);
        }
        stackPane.getChildren().setAll(node);
        return loader.getController();
    }
}

