package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Project;
import temperatus.model.service.ProjectService;
import temperatus.util.Animation;

import java.net.URL;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by alberto on 19/1/16.
 */
@Component
public class NewProjectController extends AbstractCreation implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label startDateLabel;
    @FXML private Label observationsLabel;

    @FXML private TextField nameInput;
    @FXML private DatePicker dateInput;
    @FXML private TextArea observationsInput;

    @Autowired ProjectService projectService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
    }

    @Override @FXML
    void save() throws ControlledTemperatusException {

        String name = nameInput.getText();
        Date startDate = null;
        String observations = observationsInput.getText();

        try {
            startDate = Date.from(dateInput.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e){
            throw new ControlledTemperatusException("Invalid date");
        }

        Project project = new Project();
        project.setName(name);
        project.setDateIni(startDate);
        project.setObservations(observations);

        projectService.save(project);

        //TODO show alert and close
        Animation.fadeInOutClose(titledPane);
        getCaller().reload();
    }

    @Override
    void translate() {
        nameLabel.setText("Name");
        observationsLabel.setText("Observations");
        startDateLabel.setText("Number of Buttons");
        saveButton.setText("Save");
        cancelButton.setText("Cancel");
    }
}
