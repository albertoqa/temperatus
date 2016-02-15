package temperatus.controller.creation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.*;
import temperatus.model.pojo.types.AutoCompleteComboBoxListener;
import temperatus.model.service.*;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * We can open this view by two different ways:
 * 1. Select a Project and add a New Mission to it
 * 2. Create a New Mission from start
 * <p>
 * If (1) setProject must be called to pre-select the project
 * <p>
 * Created by alberto on 24/1/16.
 */
@Controller
@Scope("prototype")
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

    @FXML private ComboBox<Project> projectBox;
    @FXML private ComboBox<Author> authorBox;
    @FXML private ComboBox<Game> gameBox;
    @FXML private ComboBox<Subject> subjectBox;

    @FXML private TextField nameInput;
    @FXML private TextArea observationsInput;
    @FXML private DatePicker dateInput;

    @Autowired ProjectService projectService;
    @Autowired GameService gameService;
    @Autowired MissionService missionService;
    @Autowired SubjectService subjectService;
    @Autowired AuthorService authorService;

    static Logger logger = Logger.getLogger(NewMissionController.class.getName());

    private final int invalidSelectionId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /**
         * Set actual controller to reload if necessary
         * For example: if the user create a new project while in this page
         * the project will be added to the choiceBox and pre-selected
         *
         * Works for: New Project, New Game and New Subject
         */
        VistaNavigator.setController(this);
        logger.info("VistaNavigator -> AbstractController set to NewMissionController");

        dateInput.setValue(LocalDate.now());    // Default date: today

        //Choice noSelectionChoice = new Choice(invalidSelectionId, language.get(Constants.NOSELECTION));    // Preselected choice

        // * Load all projects from database and allow the user to choose them
        ObservableList<Project> projects = FXCollections.observableArrayList();
        projects.addAll(projectService.getAll().stream().collect(Collectors.toList()));
        projectBox.setItems(projects);

        // * Load all authors from database and allow the user to choose them
        ObservableList<Author> authors = FXCollections.observableArrayList();
        authors.addAll(authorService.getAll().stream().collect(Collectors.toList()));
        authorBox.setItems(authors);

        // * Load all games from database and allow the user to choose them
        ObservableList<Game> games = FXCollections.observableArrayList();
        games.addAll(gameService.getAll().stream().collect(Collectors.toList()));
        gameBox.setItems(games);

        // * Load all subjects from database and allow the user to choose them
        ObservableList<Subject> subjects = FXCollections.observableArrayList();
        subjects.addAll(subjectService.getAll().stream().collect(Collectors.toList()));
        subjectBox.setItems(subjects);

        new AutoCompleteComboBoxListener<>(projectBox);
        new AutoCompleteComboBoxListener<>(authorBox);
        new AutoCompleteComboBoxListener<>(gameBox);
        new AutoCompleteComboBoxListener<>(subjectBox);

        translate();
    }

    /**
     * If project was previously selected to add a mission to it -> load it and pre-select it
     *
     * @param project - project to load
     */
    public void setProject(Project project) {
        projectBox.getSelectionModel().select(project);
    }

    @FXML
    void save() {

        String name;
        Author author;
        String observations;
        Date startDate;
        Project project;
        Game game;
        Subject subject;

        try {
            logger.info("Saving mission...");

            name = nameInput.getText();
            author = authorBox.getSelectionModel().getSelectedItem();
            observations = observationsInput.getText();
            startDate = Date.from(dateInput.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            project = projectBox.getSelectionModel().getSelectedItem();
            game = gameBox.getSelectionModel().getSelectedItem();
            subject = subjectBox.getSelectionModel().getSelectedItem();

            if (project == null) {
                throw new ControlledTemperatusException("A project must be selected.");
            } else if (game == null) {
                throw new ControlledTemperatusException("A game must be selected");
            } else if (subject == null) {
                throw new ControlledTemperatusException("A subject must be selected");
            }

            Mission mission = new Mission(author, game, project, subject, name, startDate, observations);
            missionService.save(mission);

            // Continue to new Record View -> preselect this mission
            NewRecordController newRecordController = VistaNavigator.loadVista(Constants.NEW_RECORD);
            newRecordController.loadData(mission);

            logger.info("Saved: " + mission);

        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid input date: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Invalid input date.");
        } catch (ControlledTemperatusException ex) {
            logger.warn("Exception while saving project: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Unknown error.");
        }
    }

    @FXML
    private void cancel() {
        // TODO
    }

    /**
     * Open new project modal view -> if a new project is added reload(project) is called and the new project preselected
     */
    @FXML
    private void newProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, language.get(Constants.NEWPROJECT));
    }

    /**
     * Open new game modal view -> if a new game is added reload(game) is called and the new game preselected
     */
    @FXML
    private void newGame() {
        VistaNavigator.openModal(Constants.NEW_GAME, language.get(Constants.NEWGAME));
    }

    /**
     * Open new subject modal view -> if a new subject is added reload(subject) is called and the new subject preselected
     */
    @FXML
    private void newSubject() {
        VistaNavigator.openModal(Constants.NEW_SUBJECT, language.get(Constants.NEWSUBJECT));
    }

    /**
     * While in this view, we can add a new {project - game - subject} without close the view
     * If we add one new object we want to add it to its corresponding choicebox and select it.
     * <p>
     * Depending on the type of object passed as a parameter we will add it to a different choicebox.
     *
     * @param object - new object to be added and selected
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Project) {
            Project project = (Project) object;
            projectBox.getItems().add(project);
            projectBox.getSelectionModel().select(project);
        } else if (object instanceof Game) {
            Game game = (Game) object;
            gameBox.getItems().add(game);
            gameBox.getSelectionModel().select(game);
        } else if (object instanceof Subject) {
            Subject subject = (Subject) object;
            subjectBox.getItems().add(subject);
            subjectBox.getSelectionModel().select(subject);
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
    }

}
