package temperatus.controller.creation;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.CheckListView;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Game;
import temperatus.model.pojo.Position;
import temperatus.model.service.FormulaService;
import temperatus.model.service.GameService;
import temperatus.model.service.PositionService;
import temperatus.util.Constants;
import temperatus.util.TextValidation;
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
 * View to create and update a game
 * <p>
 * Created by alberto on 26/1/16.
 */
@Controller
@Scope("prototype")
public class NewGameController extends AbstractCreationController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label observationsLabel;
    @FXML private Label numButtonsLabel;

    @FXML private TextField nameInput;
    @FXML private TextArea observationsInput;
    @FXML private TextField numButtonsInput;

    @FXML private StackPane imageStack;
    @FXML private ImageView imageView;
    @FXML private Canvas canvas;
    @FXML private TextField drawNumber;

    @FXML private Label defaultPositionsLabel;
    @FXML private Label defaultFormulasLabel;

    @FXML private CheckListView<Formula> formulasList;
    @FXML private CheckListView<Position> positionsList;

    @FXML private Button rightButton;
    @FXML private Button leftButton;
    @FXML private Button cleanButton;

    @Autowired GameService gameService;
    @Autowired PositionService positionService;
    @Autowired FormulaService formulaService;

    private Game game;

    private List<Image> images; // list of images shown
    private int selectedImage;  // index of the image currently selected
    private GraphicsContext gc; // graphic context to be able to draw circles and text over the image

    private static final int RADIUS = 4;    // size of the circle drawn when click over image
    private boolean drawn = false;          // detect if something was drawn over the current image

    private static final String FILE = "file:";
    private static final String PNG = ".png";
    private static final String FRONT_IMAGE = "/images/frontBody.png";
    private static final String BACK_IMAGE = "/images/backBody.png";
    private static final String LAT_IMAGE = "/images/lateralBody.png";
    private static final String LAT_IMAGER = "/images/lateralBodyR.png";
    private static final String DEFAULT_IMAGE = "/images/noimage.jpg";

    private static final String BACK = "back";
    private static final String FRONT = "front";
    private static final String LAT = "lateralL";
    private static final String LATR = "lateralR";

    private static final int MAX_DIGITS_FOR_NUM_BUTTONS = 3;

    private static Logger logger = LoggerFactory.getLogger(NewGameController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        images = new ArrayList<>();
        images.add(new Image(FRONT_IMAGE));
        images.add(new Image(BACK_IMAGE));
        images.add(new Image(LAT_IMAGE));
        images.add(new Image(LAT_IMAGER));

        imageView.setImage(images.get(0));
        selectedImage = 0;

        canvas.setOnMouseClicked(mouseHandler);
        gc = canvas.getGraphicsContext2D();
        drawNumber.setText("1");

        numButtonsInput.addEventFilter(KeyEvent.KEY_TYPED, TextValidation.numeric(MAX_DIGITS_FOR_NUM_BUTTONS));

        translate();

        getAllElements();
    }

    /**
     * Fetch all Positions and formulas from database and add it to the combo-boxes.
     * Use a different thread than the UI thread.
     */
    private void getAllElements() {
        Task<List<Position>> getPositionsTask = new Task<List<Position>>() {
            @Override
            public List<Position> call() throws Exception {
                return positionService.getAll();
            }
        };

        Task<List<Formula>> getFormulasTask = new Task<List<Formula>>() {
            @Override
            public List<Formula> call() throws Exception {
                return formulaService.getAll();
            }
        };

        getPositionsTask.setOnSucceeded(e -> positionsList.getItems().addAll(getPositionsTask.getValue()));
        getFormulasTask.setOnSucceeded(e -> formulasList.getItems().addAll(getFormulasTask.getValue()));

        // run the tasks using a thread from the thread pool:
        databaseExecutor.submit(getPositionsTask);
        databaseExecutor.submit(getFormulasTask);
    }


    /**
     * Handle mouse clicks over the images. Draw a small black circle on the position of the click
     * and draw the number from the input text
     */
    private EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
                gc.fillOval(mouseEvent.getX() - RADIUS, mouseEvent.getY() - RADIUS, RADIUS, RADIUS);    // draw circle
                gc.fillText(drawNumber.getText(), mouseEvent.getX() + RADIUS, mouseEvent.getY() + RADIUS);  // draw text
                drawNumber.setText(String.valueOf(Integer.valueOf(drawNumber.getText()) + 1));  // update text +1
                drawn = true;   // has been drawn to true to save snapshot
            }
        }
    };

    /**
     * When editing a game, pre-load its data
     *
     * @param game game to update/edit
     */
    public void setGameForUpdate(Game game) {
        saveButton.setText(language.get(Lang.UPDATE));
        this.game = game;
        nameInput.setText(game.getTitle());
        observationsInput.setText(game.getObservations());
        numButtonsInput.setText(String.valueOf(game.getNumButtons()));

        for (Formula formula : game.getFormulas()) {
            formulasList.getCheckModel().check(formula);    // check game's formulas
        }

        for (Position position : game.getPositions()) {
            positionsList.getCheckModel().check(position);    // check game's positions
        }

        List<temperatus.model.pojo.Image> imagesPaths = new ArrayList<>(game.getImages());
        images.clear();  // clear default images
        for (temperatus.model.pojo.Image image : imagesPaths) {
            try {
                Image im = new Image(FILE + image.getPath());
                if (!im.errorProperty().getValue()) {
                    images.add(im);
                } else {
                    images.add(new Image(DEFAULT_IMAGE));
                }
            } catch (Exception ex) {
                logger.error("Image file not found... " + ex.getMessage());
            }
        }

        if (images.size() > 0) {
            imageView.setImage(images.get(0));
        }
    }

    /**
     * Save or update a game to database
     */
    @Override
    @FXML
    protected void save() {
        try {
            logger.info("Saving game...");

            if (game == null) {
                game = new Game();
            }

            game.setTitle(nameInput.getText());
            game.setNumButtons(Integer.parseInt(numButtonsInput.getText()));
            game.setObservations(observationsInput.getText());

            game.getPositions().clear();
            game.getPositions().addAll(positionsList.getCheckModel().getCheckedItems()); // default positions

            game.getFormulas().clear();
            game.getFormulas().addAll(formulasList.getCheckModel().getCheckedItems());  // default formulas

            keepImage();
            game.getImages().clear();
            int index = 0;
            for (Image image : images) {                // save current images
                File file = saveImage(image, index);
                temperatus.model.pojo.Image im = new temperatus.model.pojo.Image();
                im.setGame(game);
                im.setPath(file.getAbsolutePath());
                game.getImages().add(im);
                index++;
            }

            gameService.saveOrUpdate(game);

            showAlertAndWait(Alert.AlertType.INFORMATION, language.get(Lang.SUCCESSFULLY_SAVED));

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
            showAlert(Alert.AlertType.ERROR, language.get(Lang.INVALID_NUMBER_BUTTONS));
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, language.get(Lang.DUPLICATE_ENTRY));
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, language.get(Lang.UNKNOWN_ERROR));
        }
    }

    /**
     * If image has been drawn then we have to keep it saved, otherwise don't save it.
     * When the image is saved (snapshot) the quality decreases.
     */
    private void keepImage() {
        if (drawn) {
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            WritableImage snapshot = imageStack.snapshot(snapshotParameters, null);
            images.set(selectedImage, snapshot);
            clearCanvas();
            drawn = false;
        }
    }

    /**
     * Change image shown to next image (iterate over all the images - round)
     */
    @FXML
    private void imageLeft() {
        keepImage();
        selectedImage = (selectedImage - 1) % images.size();
        if (selectedImage < 0) {
            selectedImage = images.size() - 1;
        }
        imageView.setImage(images.get(selectedImage));
    }

    /**
     * Change image shown to previous image (iterate - round)
     */
    @FXML
    private void imageRight() {
        keepImage();
        selectedImage = (selectedImage + 1) % images.size();
        imageView.setImage(images.get(selectedImage));
    }

    /**
     * Save image (png) to disk.
     *
     * @param image image to save
     * @param index index of the image -> front-back-lat
     * @return saved image file
     */
    private File saveImage(Image image, int index) {
        String fileName = nameInput.getText() + "$" + PNG;

        switch (index) {
            case 0:
                fileName = fileName.replace("$", FRONT);
                break;
            case 1:
                fileName = fileName.replace("$", BACK);
                break;
            case 2:
                fileName = fileName.replace("$", LAT);
                break;
            case 3:
                fileName = fileName.replace("$", LATR);
                break;
            default:
                break;
        }

        File outputFile = new File(Constants.IMAGES_PATH + fileName);
        outputFile.getParentFile().mkdir();
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            logger.error("Error saving image to disk... " + e.getMessage());
            showAlert(Alert.AlertType.WARNING, language.get(Lang.CANNOT_SAVE_IMAGE));
        }

        return outputFile;
    }

    /**
     * Clean all drawings from the current image, reset to default
     */
    @FXML
    private void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        switch (selectedImage) {
            case 0:
                imageView.setImage(new Image(FRONT_IMAGE));
                break;
            case 1:
                imageView.setImage(new Image(BACK_IMAGE));
                break;
            case 2:
                imageView.setImage(new Image(LAT_IMAGE));
                break;
            case 3:
                imageView.setImage(new Image(LAT_IMAGER));
                break;
            default:
                imageView.setImage(new Image(DEFAULT_IMAGE));
                break;
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Lang.NEW_GAME));
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
        nameLabel.setText(language.get(Lang.NAME_LABEL));
        observationsLabel.setText(language.get(Lang.OBSERVATIONS_LABEL));
        nameInput.setPromptText(language.get(Lang.NAME_PROMPT));
        observationsInput.setPromptText(language.get(Lang.OBSERVATIONS_PROMPT));
        numButtonsLabel.setText(language.get(Lang.NUMBER_OF_BUTTONS_LABEL));
        numButtonsInput.setPromptText(language.get(Lang.NUMBER_OF_BUTTONS_PROMPT));
        defaultFormulasLabel.setText(language.get(Lang.DEFAULT_FORMULAS_LABEL));
        defaultPositionsLabel.setText(language.get(Lang.DEFAULT_POSITIONS_LABEL));
        cleanButton.setText(language.get(Lang.CLEAN_BUTTON));
    }

}