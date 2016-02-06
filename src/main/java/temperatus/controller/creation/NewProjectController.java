package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Language;
import temperatus.model.pojo.Project;
import temperatus.model.service.ProjectService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * View to create and save a new project
 * <p>
 * Created by alberto on 19/1/16.
 */
@Controller
public class NewProjectController extends AbstractCreationController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label startDateLabel;
    @FXML private Label observationsLabel;

    @FXML private TextField nameInput;
    @FXML private DatePicker dateInput;
    @FXML private TextArea observationsInput;

    @Autowired ProjectService projectService;

    private final Language language = Language.getInstance();
    static Logger logger = Logger.getLogger(NewProjectController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateInput.setValue(LocalDate.now());
        translate();
    }

    /**
     * Save a new project on the DB
     * User must input all necessary info (*Name, *StartDate, Observations {optional})
     */
    @Override
    @FXML
    void save(){
        String name;
        Date startDate;
        String observations;

        try {
            logger.info("Saving project...");

            name = nameInput.getText();
            observations = observationsInput.getText();
            startDate = Date.from(dateInput.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            Project project = new Project(name, startDate, observations);
            projectService.save(project);

            Animation.fadeInOutClose(titledPane);
            if (VistaNavigator.getController() != null) {
                // Only necessary if base view needs to know about the new project creation
                VistaNavigator.getController().reload(project);
            }

            logger.info("Saved: " + project);

        } catch (ControlledTemperatusException ex){
            logger.warn("Exception while saving project: " + ex.getMessage());
            // TODO show alert
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            // TODO show alert
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            // TODO show alert
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.NEWPROJECT));
        nameInput.setPromptText(language.get(Constants.NAMEPROMP));
        observationsInput.setPromptText(language.get(Constants.OBSERVATIONSPROMP));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        observationsLabel.setText(language.get(Constants.OBSERVATIONSLABEL));
        startDateLabel.setText(language.get(Constants.STARTDATELABEL));
        saveButton.setText(language.get(Constants.SAVE));
        cancelButton.setText(language.get(Constants.CANCEL));
    }
}
