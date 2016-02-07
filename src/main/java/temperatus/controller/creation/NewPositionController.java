package temperatus.controller.creation;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Position;
import temperatus.model.service.PositionService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 27/1/16.
 */
@Controller
public class NewPositionController extends AbstractCreationController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label imageLabel;

    @FXML private TextField nameInput;
    @FXML private ImageView imageView;

    @FXML private Button selectImageButton;

    @Autowired PositionService positionService;

    static Logger logger = Logger.getLogger(NewProjectController.class.getName());

    private String imagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        // Set the default image to show -> no image picture
        Image image = new Image("/images/noimage.jpg");
        imageView.setImage(image);
    }

    @Override
    @FXML
    void save() {

        String name;

        try {
            logger.info("Saving project...");

            name = nameInput.getText();

            Position position = new Position(name, imagePath);
            positionService.save(position);

            Animation.fadeInOutClose(titledPane);
            if (VistaNavigator.getController() != null) {
                // Only necessary if base view needs to know about the new position creation
                VistaNavigator.getController().reload(position);
            }

            logger.info("Saved: " + position);

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

    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);

        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(image);
            imagePath = file.getAbsolutePath();
        } catch (IOException ex) {
            logger.info("Invalid image:" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        }

        // TODO save images to default folder
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.NEWPROJECT));
        nameInput.setPromptText(language.get(Constants.NAMEPROMPT));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        saveButton.setText(language.get(Constants.SAVE));
        cancelButton.setText(language.get(Constants.CANCEL));
        imageLabel.setText(language.get(Constants.IMAGELABEL));
        selectImageButton.setText(language.get(Constants.SELECTIMAGEBUTTON));
    }

}
