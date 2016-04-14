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
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Position;
import temperatus.model.service.FormulaService;
import temperatus.model.service.PositionService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
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

    static Logger logger = LoggerFactory.getLogger(NewProjectController.class.getName());

    private String imagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        position = null;
        translate();

        // Set the default image to show -> no image picture
        imagePath = "/images/noimage.jpg";
        Image image = new Image(imagePath);
        imageView.setImage(image);
    }

    public void setPositionForUpdate(Position position) {
        saveButton.setText(language.get(Constants.UPDATE));
        this.position = position;
        nameInput.setText(position.getPlace());
        imagePath = position.getPicture();
        // TODO show image
    }

    @Override
    @FXML
    void save() {

        String name;
        boolean isUsed = false;

        try {
            logger.info("Saving position...");

            name = nameInput.getText();

            if(!isValidName(name)) {
                throw new ControlledTemperatusException("Name cannot contain any of the following symbols: + - * / ( )");
            } else if(position != null && isUsedInAFormula(position.getPlace())) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "This position is used in some Formula, if you edit it the formula will stop working. Do you want to continue?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.CANCEL) {
                    isUsed = true;
                }
            }

            if(!isUsed) {
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
            showAlert(Alert.AlertType.ERROR, "Duplicate entry");
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Unknown error.");
        }
    }

    private boolean isUsedInAFormula(String place) {
        boolean isUsed = false;
        List<Formula> formulas = formulaService.getAll();

        for(Formula formula: formulas) {
            if(formula.getOperation().contains(place)) {
                isUsed = true;
                break;
            }
        }

        return isUsed;
    }

    private boolean isValidName(String name) {
        boolean isValid = true;

        if(name.contains("+") || name.contains("-") || name.contains("*") || name.contains("/") || name.contains("(") || name.contains(")")) {
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                imageView.setImage(image);
                imagePath = file.getAbsolutePath();
            } catch (IOException ex) {
                logger.info("Invalid image:" + ex.getMessage());
                showAlert(Alert.AlertType.ERROR, ex.getMessage());
            }
        }

        // TODO save images to default folder
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.NEWPOSITION));
        nameInput.setPromptText(language.get(Constants.NAMEPROMPT));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        saveButton.setText(language.get(Constants.SAVE));
        cancelButton.setText(language.get(Constants.CANCEL));
        imageLabel.setText(language.get(Constants.IMAGELABEL));
        selectImageButton.setText(language.get(Constants.SELECTIMAGEBUTTON));
    }

}
