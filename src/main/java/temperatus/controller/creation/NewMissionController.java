package temperatus.controller.creation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.controller.AbstractController;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.Choice;
import temperatus.model.pojo.Game;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;
import temperatus.model.pojo.Subject;
import temperatus.model.service.GameService;
import temperatus.model.service.MissionService;
import temperatus.model.service.ProjectService;
import temperatus.model.service.SubjectService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by alberto on 24/1/16.
 */
@Component
public class NewMissionController implements Initializable, AbstractController {

    @FXML private ChoiceBox projectChooser;
    @FXML private ChoiceBox gameChooser;
    @FXML private ChoiceBox subjectChooser;

    @FXML private TextField nameInput;
    @FXML private TextArea observationsInput;
    @FXML private TextField authorInput;
    @FXML private DatePicker dateInput;


    @Autowired ProjectService projectService;
    @Autowired MissionService missionService;
    @Autowired GameService gameService;
    @Autowired SubjectService subjectService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        VistaNavigator.setController(this);

        ObservableList<Choice> choicesProject = FXCollections.observableArrayList();
        choicesProject.add(new Choice(-1, "No selection"));
        for (Project project : projectService.getAll()) {
            choicesProject.add(new Choice(project.getId(), project.getName()));
        }

        projectChooser.setItems(choicesProject);
        projectChooser.getSelectionModel().select(0);

        ObservableList<Choice> choicesGame = FXCollections.observableArrayList();
        choicesGame.add(new Choice(-1, "No selection"));
        for (Game game : gameService.getAll()) {
            choicesGame.add(new Choice(game.getId(), game.getTitle()));
        }

        gameChooser.setItems(choicesGame);
        gameChooser.getSelectionModel().select(0);

        ObservableList<Choice> choicesSubject = FXCollections.observableArrayList();
        choicesSubject.add(new Choice(-1, "No selection"));
        for (Subject subject : subjectService.getAll()) {
            choicesSubject.add(new Choice(subject.getId(), subject.getName()));
        }

        subjectChooser.setItems(choicesSubject);
        subjectChooser.getSelectionModel().select(0);

    }

    public void setProject(int projectId) {
        for(int i = 0; i < projectChooser.getItems().size(); i++) {
            if(((Choice) projectChooser.getItems().get(i)).getId().equals(projectId)) {
                projectChooser.getSelectionModel().select(i);
                break;
            }
        }
    }

    @FXML
    private void continueToRecords() throws ControlledTemperatusException {
        Mission mission = new Mission();
        mission.setName(nameInput.getText());
        mission.setAuthor(authorInput.getText());
        mission.setObservations(observationsInput.getText());

        Date startDate = null;
        try {
            startDate = Date.from(dateInput.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e){
            throw new ControlledTemperatusException("Invalid date");
        }

        mission.setDateIni(startDate);
        mission.setGameId(((Choice) gameChooser.getSelectionModel().getSelectedItem()).getId());
        mission.setProjectId(((Choice) projectChooser.getSelectionModel().getSelectedItem()).getId());
        mission.setSubjectId(((Choice) subjectChooser.getSelectionModel().getSelectedItem()).getId());

        missionService.save(mission);

        // TODO get mission default positions
        // TODO detect iButtons and look for them in the db, if they appear and the selected game has the same position of the default
        // TODO position of the button, set automatically that button to that position
        // TODO set default position to iButton
        // TODO set default position to Game

    }

    @FXML
    private void cancel() {

    }

    @FXML
    private void newProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, "New project");
    }
    @FXML
    private void newGame() {
        VistaNavigator.openModal(Constants.NEW_GAME, "New Game");
    }
    @FXML
    private void newSubject() {
        VistaNavigator.openModal(Constants.NEW_SUBJECT, "New Subject");
    }

    @Override
    public void reload(Object object) {
        if(object instanceof Project) {
            Choice choice = new Choice(((Project) object).getId(), ((Project) object).getName());
            projectChooser.getItems().add(choice);
            projectChooser.getSelectionModel().select(choice);
        } else if(object instanceof Game) {
            Choice choice = new Choice(((Game) object).getId(), ((Game) object).getTitle());
            gameChooser.getItems().add(choice);
            gameChooser.getSelectionModel().select(choice);
        } else if(object instanceof Subject) {
            Choice choice = new Choice(((Subject) object).getId(), ((Subject) object).getName());
            subjectChooser.getItems().add(choice);
            subjectChooser.getSelectionModel().select(choice);
        }
    }

}
