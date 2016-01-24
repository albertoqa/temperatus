package temperatus.controller.mission;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.model.Choice;
import temperatus.model.pojo.Project;
import temperatus.model.service.MissionService;
import temperatus.model.service.ProjectService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 24/1/16.
 */
@Component
public class NewMissionController implements Initializable {

    @FXML
    private ChoiceBox projectChooser;

    @Autowired
    ProjectService projectService;
    @Autowired
    MissionService missionService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Choice> choices = FXCollections.observableArrayList();
        choices.add(new Choice(null, "No selection"));
        for (Project project : projectService.getAll()) {
            choices.add(new Choice(project.getId(), project.getName()));
        }

        projectChooser.setItems(choices);
        projectChooser.getSelectionModel().select(0);

    }

    @FXML
    private void newProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, "New project");
    }

}
