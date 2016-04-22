package temperatus.controller.creation;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Position;
import temperatus.model.service.FormulaService;
import temperatus.model.service.PositionService;
import temperatus.util.VistaNavigator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * View to create and save/update a position
 * <p>
 * Created by alberto on 27/1/16.
 */
@Controller
@Scope("prototype")
public class NewPositionController extends AbstractCreationController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label imageLabel;

    @FXML private TextField nameInput;
    @FXML private ImageView imageView;

    @FXML private Button selectImageButton;

    @Autowired PositionService positionService;
    @Autowired FormulaService formulaService;

    private Position position;

    private static Logger logger = LoggerFactory.getLogger(NewPositionController.class.getName());

    private static final String DEFAULT_IMAGE = "/images/noimage.jpg";  // Set the default image to show -> no image picture
    private String imagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        position = null;
        translate();

        Image image = new Image(DEFAULT_IMAGE); // show default image
        imageView.setImage(image);
        imagePath = DEFAULT_IMAGE;
    }

    /**
     * When editing a position, pre-load its data
     *
     * @param position position to update/edit
     */
    public void setPositionForUpdate(Position position) {
        saveButton.setText(language.get(Lang.UPDATE));
        this.position = position;
        nameInput.setText(position.getPlace());
        imagePath = position.getPicture();

        try {
            imageView.setImage(new Image(imagePath));
        } catch (Exception ex) {
            logger.error("Cannot find image for position...");
            imageView.setImage(new Image(DEFAULT_IMAGE));
        }
    }

    /**
     * Save or update a position to database
     */
    @Override
    @FXML
    void save() {
        try {
            logger.info("Saving position...");

            String name = nameInput.getText();
            boolean isUsed = false;     // check if this position has been used in any formula saved to the database

            if (!isValidName(name)) {
                throw new ControlledTemperatusException(language.get(Lang.INVALID_NAME_FORMULA));
            } else if (position != null && isUsedInAFormula(position.getPlace())) {
                // warn user of position used in a formula... formula will be useless if delete position
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Lang.REMOVE_FORMULA_CONFIRMATION));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                    isUsed = true;
                }
            }

            if (!isUsed) {  // not used or user doesn't care so delete it
                if (position == null) {
                    position = new Position();
                }

                position.setPlace(name);
                position.setPicture(imagePath);
                positionService.saveOrUpdate(position);

                VistaNavigator.closeModal(titledPane);
                if (VistaNavigator.getController() != null) {
                    // Only necessary if base view needs to know about the new position creation
                    VistaNavigator.getController().reload(position);
                }

                logger.info("Saved: " + position);
            }

        } catch (ControlledTemperatusException ex) {
            logger.warn("Exception while saving position: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, language.get(Lang.DUPLICATE_ENTRY));
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, language.get(Lang.UNKNOWN_ERROR));
        }
    }

    /**
     * Check if this position was used in any formula...
     *
     * @param place position to look for
     * @return is used in a formula?
     */
    private boolean isUsedInAFormula(String place) {
        boolean isUsed = false;
        for (Formula formula : formulaService.getAll()) {   // search in all formulas of the database
            if (formula.getOperation().contains(place)) {
                isUsed = true;
                break;
            }
        }
        return isUsed;
    }

    /**
     * Check if actual name is valid.. cannot contain operators used in formulas
     *
     * @param name name to check
     * @return is name valid?
     */
    private boolean isValidName(String name) {
        return !(name.contains("+") || name.contains("-") || name.contains("*") || name.contains("/") || name.contains("(") || name.contains(")"));
    }

    /**
     * Open a fileChooser to allow the user to select a image from the computer
     * Only allow images in jpg and png.
     */
    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                imageView.setImage(image);
                imagePath = file.getAbsolutePath();
            } catch (IOException ex) {
                logger.warn("Invalid image:" + ex.getMessage());
                showAlert(Alert.AlertType.ERROR, ex.getMessage());
            }
        }

        // TODO save images to default folder
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Lang.NEWPOSITION));
        nameInput.setPromptText(language.get(Lang.NAMEPROMPT));
        nameLabel.setText(language.get(Lang.NAMELABEL));
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
        imageLabel.setText(language.get(Lang.IMAGELABEL));
        selectImageButton.setText(language.get(Lang.SELECTIMAGEBUTTON));
    }

}
