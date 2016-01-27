package temperatus.controller.archived.info;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;
import temperatus.model.service.MissionService;
import temperatus.model.service.ProjectService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 24/1/16.
 */
@Component
public class ProjectInfoController implements Initializable {

    @FXML
    private Label projectName;
    @FXML
    private Label projectStartDate;
    @FXML
    private Label projectObservations;
    @FXML
    private Label numOfExperimentsLabel;
    @FXML
    private Label projectNumOfExperiments;
    @FXML
    private Label peopleInvolvedLabel;
    @FXML
    private Label projectPeopleInvolved;

    @Autowired
    ProjectService projectService;
    @Autowired
    MissionService missionService;

    private Project project;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void setProjectInfo() throws ControlledTemperatusException {

        if(project == null) {
            throw new ControlledTemperatusException("");
        }

        projectName.setText(project.getName());
        projectStartDate.setText(project.getDateIni().toString());
        projectObservations.setText(project.getObservations());

        List<Mission> missionList = missionService.getAll();
        if(missionList.isEmpty()) {
            numOfExperimentsLabel.setVisible(false);
            projectNumOfExperiments.setVisible(false);
            peopleInvolvedLabel.setVisible(false);
            projectPeopleInvolved.setVisible(false);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        try {
            setProjectInfo();
        } catch (ControlledTemperatusException e) {
            e.printStackTrace();    // TODO
        }
    }
}
