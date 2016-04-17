package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.model.pojo.Project;
import temperatus.model.service.ProjectService;
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
@Scope("prototype")
public class NewProjectController extends AbstractCreationController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label startDateLabel;
    @FXML private Label observationsLabel;

    @FXML private TextField nameInput;
    @FXML private DatePicker dateInput;
    @FXML private TextArea observationsInput;

    @Autowired ProjectService projectService;
    private Project project;
    private boolean isSave;

    static Logger logger = LoggerFactory.getLogger(NewProjectController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        project = null;
        isSave = true;
        dateInput.setValue(LocalDate.now());
        translate();
    }

    public void setProject(Project project) {
        saveButton.setText(language.get(Constants.UPDATE));
        this.project = project;
        nameInput.setText(project.getName());
        observationsInput.setText(project.getObservations());
        dateInput.setValue(LocalDate.now());
        isSave = false;
    }

    /**
     * Save a new project on the DB
     * User must input all necessary info (*Name, *StartDate, Observations {optional})
     */
    @Override
    @FXML
    void save() {
        String name;
        Date startDate;
        String observations;

        try {
            logger.info("Saving project...");

            name = nameInput.getText();
            observations = observationsInput.getText();
            startDate = Date.from(dateInput.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            if(project == null) {
                project = new Project();
            }

            project.setName(name);
            project.setObservations(observations);
            project.setDateIni(startDate);

            projectService.saveOrUpdate(project);

            VistaNavigator.closeModal(titledPane);
            if (VistaNavigator.getController() != null && isSave) {
                // Only necessary if base view needs to know about the new project creation
                VistaNavigator.getController().reload(project);
            }

            logger.info("Saved: " + project);

            //} catch (ControlledTemperatusException ex) {
            //    logger.warn("Exception while saving project: " + ex.getMessage());
            //    showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, "Duplicate entry");
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Unknown error.");
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.NEWPROJECT));
        nameInput.setPromptText(language.get(Constants.NAMEPROMPT));
        observationsInput.setPromptText(language.get(Constants.OBSERVATIONSPROMPT));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        observationsLabel.setText(language.get(Constants.OBSERVATIONSLABEL));
        startDateLabel.setText(language.get(Constants.STARTDATELABEL));
        saveButton.setText(language.get(Constants.SAVE));
        cancelButton.setText(language.get(Constants.CANCEL));
    }
}
