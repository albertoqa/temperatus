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
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Project;
import temperatus.model.service.ProjectService;
import temperatus.util.DateUtils;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * View to create/update and save a new project
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
    private boolean isNew;  // if project is not new (update) then don't reload

    private static Logger logger = LoggerFactory.getLogger(NewProjectController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        project = null;
        isNew = true;
        translate();

        dateInput.setValue(LocalDate.now());    // default date to now
    }

    /**
     * Update an existing project
     *
     * @param project project to update
     */
    public void setProject(Project project) {
        saveButton.setText(language.get(Lang.UPDATE));
        this.project = project;
        nameInput.setText(project.getName());
        observationsInput.setText(project.getObservations());
        dateInput.setValue(DateUtils.asLocalDate(project.getDateIni()));
        isNew = false;
    }

    /**
     * Save a new project on the DB
     * User must input all necessary info (*Name, *StartDate, Observations {optional})
     */
    @Override
    @FXML
    void save() {
        try {
            logger.info("Saving project...");

            if (project == null) {
                project = new Project();
            }

            Date startDate = DateUtils.asUtilDate(dateInput.getValue());

            project.setName(nameInput.getText());
            project.setObservations(observationsInput.getText());
            project.setDateIni(startDate);

            projectService.saveOrUpdate(project);

            VistaNavigator.closeModal(titledPane);
            if (VistaNavigator.getController() != null && isNew) {
                // Only necessary if base view needs to know about the new project creation
                VistaNavigator.getController().reload(project);
            }

            logger.info("Saved: " + project);

        } catch (ControlledTemperatusException ex) {
            logger.warn("Exception while saving project: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, language.get(Lang.DUPLICATE_ENTRY));
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, language.get(Lang.UNKNOWN_ERROR));
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Lang.NEW_PROJECT));
        nameInput.setPromptText(language.get(Lang.NAME_PROMPT));
        observationsInput.setPromptText(language.get(Lang.OBSERVATIONS_PROMPT));
        nameLabel.setText(language.get(Lang.NAME_LABEL));
        observationsLabel.setText(language.get(Lang.OBSERVATIONS_LABEL));
        startDateLabel.setText(language.get(Lang.START_DATE_LABEL));
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
    }

}
