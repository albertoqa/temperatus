package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
public class NewProjectController implements Initializable {

    @FXML
    private TitledPane titledPane;
    @FXML
    private TextField nameInput;
    @FXML
    private DatePicker dateInput;
    @FXML
    private TextArea observationsInput;

    @Autowired
    ProjectService projectService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void saveProject() {
        Project project = new Project();
        project.setName(nameInput.getText());
        project.setDateIni(Date.from(dateInput.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        project.setObservations(observationsInput.getText());

        projectService.save(project);
        System.out.println(project.getId());

        //TODO show alert and close
    }

    @FXML
    private void cancel() {
        Animation.fadeInOutClose(titledPane);
    }
}
