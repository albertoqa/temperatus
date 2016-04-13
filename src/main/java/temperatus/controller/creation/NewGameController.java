package temperatus.controller.creation;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.ListSelectionView;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Game;
import temperatus.model.pojo.Position;
import temperatus.model.service.FormulaService;
import temperatus.model.service.GameService;
import temperatus.model.service.PositionService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 26/1/16.
 */
@Controller
@Scope("prototype")
public class NewGameController extends AbstractCreationController implements Initializable {

    @FXML Label nameLabel;
    @FXML Label observationsLabel;
    @FXML Label numButtonsLabel;

    @FXML TextField nameInput;
    @FXML TextArea observationsInput;
    @FXML TextField numButtonsInput;

    @FXML StackPane imageStack;
    @FXML ImageView imageView;
    @FXML Canvas canvas;
    @FXML TextField drawNumber;

    @FXML ListSelectionView<Position> positionsSelector;
    @FXML CheckListView<Formula> formulasList;

    @Autowired GameService gameService;
    @Autowired PositionService positionService;
    @Autowired FormulaService formulaService;

    private Game game;

    private List<Image> images;
    private int selectedImage;
    private GraphicsContext gc;

    private final int radius = 4;
    private boolean drawed = false;

    private final String frontImage = "frontBody";
    private final String backImage = "backBody";

    static Logger logger = LoggerFactory.getLogger(NewGameController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        images = new ArrayList<>();
        images.add(new Image("/images/frontBody.png"));
        images.add(new Image("/images/backBody.png"));

        imageView.setImage(images.get(0));
        selectedImage = 0;

        canvas.setOnMouseClicked(mouseHandler);
        gc = canvas.getGraphicsContext2D();

        positionsSelector.getSourceItems().addAll(positionService.getAll());
        formulasList.getItems().addAll(formulaService.getAll());

        translate();
    }

    private EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
                gc.fillOval(mouseEvent.getX()-radius, mouseEvent.getY()-radius, radius, radius);
                gc.fillText(drawNumber.getText(), mouseEvent.getX(), mouseEvent.getX());
                drawed = true;
            }
        }
    };

    public void setGameForUpdate(Game game) {
        saveButton.setText(language.get(Constants.UPDATE));
        this.game = game;
        nameInput.setText(game.getTitle());
        observationsInput.setText(game.getObservations());
        numButtonsInput.setText(String.valueOf(game.getNumButtons()));

        for(Formula formula: game.getFormulas()) {
            formulasList.getCheckModel().check(formula);
        }

        positionsSelector.getTargetItems().addAll(game.getPositions());
        positionsSelector.getSourceItems().removeAll(game.getPositions());

        List<temperatus.model.pojo.Image> imagesPaths = new ArrayList<>(game.getImages());
        images.clear();
        for(temperatus.model.pojo.Image image: imagesPaths) {
            javafx.scene.image.Image im = new javafx.scene.image.Image("file:" + image.getPath());
            images.add(im);
        }
        imageView.setImage(images.get(0));
    }

    @Override
    @FXML
    protected void save() {
        keepImage();

        String name;
        String observations;
        Integer numButtons;

        try {
            logger.info("Saving game...");

            name = nameInput.getText();
            observations = observationsInput.getText();
            numButtons = Integer.parseInt(numButtonsInput.getText());

            if(game == null) {
                game = new Game();
            }

            game.setTitle(name);
            game.setNumButtons(numButtons);
            game.setObservations(observations);

            List<Position> defaultPositions = positionsSelector.getTargetItems();
            game.getPositions().addAll(defaultPositions);

            List<Formula> defaultFormulas = formulasList.getCheckModel().getCheckedItems();
            game.getFormulas().addAll(defaultFormulas);

            game.getImages().clear();
            int index = 0;
            for(Image image: images) {
                File file = saveImage(image, index);
                temperatus.model.pojo.Image im = new temperatus.model.pojo.Image();
                im.setGame(game);
                im.setPath(file.getAbsolutePath());
                game.getImages().add(im);
                index++;
            }

            gameService.saveOrUpdate(game);

            VistaNavigator.closeModal(titledPane);
            if (VistaNavigator.getController() != null) {
                VistaNavigator.getController().reload(game);
            }

            logger.info("Saved" + game);

        } catch (ControlledTemperatusException ex) {
            logger.warn("Exception: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (NumberFormatException ex) {
            logger.warn("Invalid input for number of buttons");
            showAlert(Alert.AlertType.ERROR, "Invalid number of buttons.");
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, "Duplicate Game.");
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Unknown error.");
        }
    }

    private void keepImage() {
        if(drawed) {
            WritableImage snapshot = imageStack.snapshot(new SnapshotParameters(), null);
            images.set(selectedImage, snapshot);
            clearCanvas();
            drawed = false;
        }
    }

    @FXML
    private void imageLeft() throws IOException {
        keepImage();
        selectedImage = (selectedImage - 1) % images.size();
        if (selectedImage < 0) {
            selectedImage = -selectedImage;
        }
        imageView.setImage(images.get(selectedImage));
    }

    @FXML
    private void imageRight() {
        keepImage();
        selectedImage = (selectedImage + 1) % images.size();
        imageView.setImage(images.get(selectedImage));
    }

    private File saveImage(Image image, int index) {
        String pathToSave = "/Users/alberto/Desktop/";    // TODO change to application directory
        String fileName = nameInput.getText() + index + ".png";

        File outputFile = new File(pathToSave + fileName);
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputFile;
    }

    @FXML
    private void clearCanvas() {
        // TODO change to reload image
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.NEWGAME));
        saveButton.setText(language.get(Constants.SAVE));
        cancelButton.setText(language.get(Constants.CANCEL));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        observationsLabel.setText(language.get(Constants.OBSERVATIONSLABEL));
        nameInput.setPromptText(language.get(Constants.NAMEPROMPT));
        observationsInput.setPromptText(language.get(Constants.OBSERVATIONSPROMPT));
        numButtonsLabel.setText(language.get(Constants.NUMBUTTONSLABEL));
        numButtonsInput.setPromptText(language.get(Constants.NUMBUTTONSPROMPT));
    }

}
