package temperatus.controller.manage.ampliate;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Game;
import temperatus.model.pojo.Image;
import temperatus.model.pojo.Position;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Show complete information of the selected game
 * <p>
 * Created by alberto on 29/2/16.
 */
@Controller
@Scope("prototype")
public class GameInfoController implements Initializable, AbstractController {

    @FXML private Button backButton;

    @FXML private ImageView imFront;
    @FXML private ImageView imLat;
    @FXML private ImageView imBack;
    @FXML private ImageView imLat2;

    @FXML private Label headerTitle;
    @FXML private Label nameLabel;
    @FXML private Label numberOfButtonsLabel;
    @FXML private Label numButtons;
    @FXML private Label defaultFormulasLabel;
    @FXML private Label defaultPositionsLabel;
    @FXML private Label positionsInfo;
    @FXML private Label formulasInfo;

    private Game game;

    private static final String BACK = "back";
    private static final String FRONT = "front";
    private static final String LAT_R = "lateral";
    private static final String LAT_L = "lateralL";

    private static final String COMMA = ", ";
    private static final String FILE = "file:";
    private static Logger logger = LoggerFactory.getLogger(GameInfoController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
    }

    /**
     * Set the game to show
     *
     * @param game game to show
     */
    public void setGame(Game game) {
        this.game = game;
        loadData();
        logger.debug("Game set..." + game);
    }

    /**
     * Load the data of the game on the view
     */
    private void loadData() {
        for (Image image : game.getImages()) {
            try {
                javafx.scene.image.Image im = new javafx.scene.image.Image(FILE + image.getPath());

                if (!im.errorProperty().getValue()) {
                    if (image.getPath().contains(FRONT)) {
                        imFront.setImage(im);
                    } else if (image.getPath().contains(LAT_R)) {
                        imLat.setImage(im);
                    } else {
                        imBack.setImage(im);
                    }
                }
            } catch (Exception ex) {
                logger.warn("Cannot load image...");
            }
        }

        nameLabel.setText(game.getTitle().toUpperCase());
        formulasInfo.setText("");
        positionsInfo.setText("");
        for (Formula formula : game.getFormulas()) {
            formulasInfo.setText(formulasInfo.getText() + formula.getName() + COMMA);
        }
        for (Position position : game.getPositions()) {
            positionsInfo.setText(positionsInfo.getText() + position.getPlace() + COMMA);
        }
    }

    /**
     * Go back to manage game view
     */
    @FXML
    private void back() {
        VistaNavigator.popViewFromStack();
    }

    @Override
    public void translate() {
        numberOfButtonsLabel.setText(language.get(Lang.NUMBUTTONSLABEL));
        defaultFormulasLabel.setText(language.get(Lang.DEFAULTFORMULASLABEL));
        defaultPositionsLabel.setText(language.get(Lang.DEFAULTPOSLABEL));
    }

}
