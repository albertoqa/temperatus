package temperatus.controller.creation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.lang.Language;
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
@Controller
public class NewMissionController extends AbstractCreationController implements Initializable {

    @FXML private StackPane stackPane;
    @FXML private Label title;
    @FXML private Label projectLabel;
    @FXML private Label nameLabel;
    @FXML private Label authorLabel;
    @FXML private Label startDateLabel;
    @FXML private Label observationsLabel;
    @FXML private Label gameLabel;
    @FXML private Label subjectLabel;

    @FXML private Button newProjectButton;
    @FXML private Button newGameButton;
    @FXML private Button newSubjectButton;

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

    private final Language language = Language.getInstance();
    static Logger logger = Logger.getLogger(NewProjectController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //TODO set default start date
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
    void save() {
        Mission mission = new Mission();
        mission.setName(nameInput.getText());
        mission.setAuthor(authorInput.getText());
        mission.setObservations(observationsInput.getText());

        Date startDate = null;
        try {
            startDate = Date.from(dateInput.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e){

        }

        mission.setDateIni(startDate);
        mission.setGameId(((Choice) gameChooser.getSelectionModel().getSelectedItem()).getId());
        mission.setProjectId(((Choice) projectChooser.getSelectionModel().getSelectedItem()).getId());
        mission.setSubjectId(((Choice) subjectChooser.getSelectionModel().getSelectedItem()).getId());

        missionService.save(mission);

        NewRecordController newRecordController = VistaNavigator.loadVista(Constants.NEW_RECORD);
        newRecordController.loadData(mission);

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

    @Override
    public void translate() {
        saveButton.setText(language.get(Constants.CONTINUE));
        cancelButton.setText(language.get(Constants.CANCEL));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        observationsLabel.setText(language.get(Constants.OBSERVATIONSLABEL));
        nameInput.setPromptText(language.get(Constants.NAMEPROMPT));
        observationsInput.setPromptText(language.get(Constants.OBSERVATIONSPROMPT));
        title.setText(language.get(Constants.NEWMISSIONTITLE));
        projectLabel.setText(language.get(Constants.PROJECTLABEL));
        authorLabel.setText(language.get(Constants.AUTHORLABEL));
        startDateLabel.setText(language.get(Constants.STARTDATELABEL));
        gameLabel.setText(language.get(Constants.GAMELABEL));
        subjectLabel.setText(language.get(Constants.SUBJECTLABEL));
        newProjectButton.setText(language.get(Constants.NEWPROJECTBUTTON));
        newGameButton.setText(language.get(Constants.NEWGAMEBUTTON));
        newSubjectButton.setText(language.get(Constants.NEWSUBJECTBUTTON));
        authorInput.setPromptText(language.get(Constants.AUTHORPROMPT));
    }

}
