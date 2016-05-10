package temperatus.controller.activation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Show a thanks note and let the user know that the application has been sucessfully activated
 * <p>
 * Created by alberto on 29/4/16.
 */
@Controller
public class ThanksController implements Initializable, AbstractController {

    @FXML private AnchorPane anchorPane;
    @FXML private Button continueButton;
    @FXML private Label featuresLabel;
    @FXML private Label successLabel;

    private boolean loadSplash;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
    }

    /**
     * Close this window and continue
     */
    @FXML
    private void cont() {
        if (loadSplash) {
            startApplication();
        } else {
            Stage currentStage = (Stage) anchorPane.getScene().getWindow();    // close current stage
            Animation.blurIn(VistaNavigator.getParentNode());
            currentStage.close();
        }
    }

    /**
     * Are we in the start of the application or the application has already started?
     *
     * @param loadSplash start the application or only close this window
     */
    void setLoadSplash(boolean loadSplash) {
        this.loadSplash = loadSplash;
    }

    /**
     * Close this stage and load the splash screen window
     */
    private void startApplication() {
        Stage currentStage = (Stage) anchorPane.getScene().getWindow();    // close current stage

        Stage stage = new Stage();
        Pane pane = VistaNavigator.loader.load(getClass().getResource(Constants.SPLASH));
        Scene scene = new Scene(pane);
        stage.initStyle(StageStyle.UNDECORATED); // remove borders
        stage.setScene(scene);
        stage.initOwner(currentStage);

        currentStage.close();
        stage.show();
    }

    @Override
    public void translate() {
        featuresLabel.setText(language.get(Lang.FEATURES_LABEL));
        successLabel.setText(language.get(Lang.SUCCESS_LABEL));
        continueButton.setText(language.get(Lang.CONTINUE));
    }
}
