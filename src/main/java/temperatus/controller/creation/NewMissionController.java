package temperatus.controller.creation;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.*;
import temperatus.model.pojo.utils.AutoCompleteComboBoxListener;
import temperatus.model.service.*;
import temperatus.util.Constants;
import temperatus.util.DateUtils;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

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
    @FXML private Button newAuthorButton;

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

    private Mission mission;

    private static Logger logger = LoggerFactory.getLogger(NewMissionController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mission = null;
        /**
         * Set actual controller to reload if necessary
         * For example: if the user create a new project while in this page
         * the project will be added to the choiceBox and pre-selected
         *
         * Works for: New Project, New Author, New Game and New Subject
         */
        VistaNavigator.setController(this);
        logger.debug("VistaNavigator -> AbstractController set to NewMissionController");

        dateInput.setValue(LocalDate.now());    // Default date: today

        projectBox.setItems(FXCollections.observableArrayList());
        authorBox.setItems(FXCollections.observableArrayList());
        gameBox.setItems(FXCollections.observableArrayList());
        subjectBox.setItems(FXCollections.observableArrayList());

        new AutoCompleteComboBoxListener<>(projectBox);
        new AutoCompleteComboBoxListener<>(authorBox);
        new AutoCompleteComboBoxListener<>(gameBox);
        new AutoCompleteComboBoxListener<>(subjectBox);

        translate();

        getAllElements();
    }

    /**
     * Load all projects, authors, games and subjects from database and allow the user to choose them
     * Use a different thread than the UI thread.
     */
    private void getAllElements() {
        Task<List<Project>> getProjectsTask = new Task<List<Project>>() {
            @Override
            public List<Project> call() throws Exception {
                return projectService.getAll();
            }
        };

        Task<List<Author>> getAuthorsTask = new Task<List<Author>>() {
            @Override
            public List<Author> call() throws Exception {
                return authorService.getAll();
            }
        };

        Task<List<Game>> getGamesTask = new Task<List<Game>>() {
            @Override
            public List<Game> call() throws Exception {
                return gameService.getAll();
            }
        };

        Task<List<Subject>> getSubjectsTask = new Task<List<Subject>>() {
            @Override
            public List<Subject> call() throws Exception {
                return subjectService.getAll();
            }
        };

        getProjectsTask.setOnSucceeded(e -> projectBox.getItems().addAll(getProjectsTask.getValue()));
        getAuthorsTask.setOnSucceeded(e -> authorBox.getItems().addAll(getAuthorsTask.getValue()));
        getGamesTask.setOnSucceeded(e -> gameBox.getItems().addAll(getGamesTask.getValue()));
        getSubjectsTask.setOnSucceeded(e -> subjectBox.getItems().addAll(getSubjectsTask.getValue()));

        // run the tasks using a thread from the thread pool:
        databaseExecutor.submit(getProjectsTask);
        databaseExecutor.submit(getAuthorsTask);
        databaseExecutor.submit(getGamesTask);
        databaseExecutor.submit(getSubjectsTask);
    }

    /**
     * When editing a mission, pre-load its data
     *
     * @param mission mission to update/edit
     */
    public void setMissionForUpdate(Mission mission) {
        title.setText(language.get(Lang.UPDATE_MISSION_TITLE)); // change title to Update
        saveButton.setText(language.get(Lang.UPDATE));  // change save button text to update
        this.mission = mission;
        projectBox.getSelectionModel().select(mission.getProject());
        authorBox.getSelectionModel().select(mission.getAuthor());
        gameBox.getSelectionModel().select(mission.getGame());
        subjectBox.getSelectionModel().select(mission.getSubject());
        nameInput.setText(mission.getName());
        observationsInput.setText(mission.getObservations());
        dateInput.setValue(DateUtils.asLocalDate(mission.getDateIni()));
    }

    /**
     * If project was previously selected to add a mission to it -> load it and pre-select it
     *
     * @param project - project to load
     */
    public void setProject(Project project) {
        projectBox.getSelectionModel().select(project);
    }

    /**
     * Save or update a mission to db
     */
    @FXML
    void save() {
        try {
            logger.info("Saving mission...");

            String name = nameInput.getText();
            Author author = authorBox.getSelectionModel().getSelectedItem();
            String observations = observationsInput.getText();
            Date startDate = DateUtils.asUtilDate(dateInput.getValue());
            Project project = projectBox.getSelectionModel().getSelectedItem();
            Game game = gameBox.getSelectionModel().getSelectedItem();
            Subject subject = subjectBox.getSelectionModel().getSelectedItem();

            if (project == null) {
                throw new ControlledTemperatusException(language.get(Lang.MUST_SELECT_PROJECT));
            } else if (game == null) {
                throw new ControlledTemperatusException(language.get(Lang.MUST_SELECT_GAME));
            } else if (subject == null) {
                throw new ControlledTemperatusException(language.get(Lang.MUST_SELECT_SUBJECT));
            } else if(author == null) {
                throw new ControlledTemperatusException(language.get(Lang.MUST_SELECT_AUTHOR));
            }

            boolean isUpdate = true;
            if(mission == null) {
                isUpdate = false;
                mission = new Mission();
            }

            mission.setAuthor(author);
            mission.setGame(game);
            mission.setProject(project);
            mission.setSubject(subject);
            mission.setName(name);
            mission.setDateIni(startDate);
            mission.setObservations(observations);

            missionService.saveOrUpdate(mission);

            // Continue to new Record View -> preselect this mission
            NewRecordController newRecordController = VistaNavigator.loadVista(Constants.NEW_RECORD);
            if (newRecordController != null) {
                newRecordController.loadData(mission, isUpdate);
            }

            logger.info("Saved: " + mission);

        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid input date: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, language.get(Lang.INVALID_DATE));
        } catch (ControlledTemperatusException ex) {
            logger.warn("Exception while saving mission: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, language.get(Lang.DUPLICATE_ENTRY));
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, language.get(Lang.UNKNOWN_ERROR));
        }
    }

    /**
     * Cancel mission creation and go to Archive view
     */
    @FXML
    private void cancel() {
        if (VistaNavigator.confirmationAlert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION))) {
            VistaNavigator.loadVista(Constants.ARCHIVED);
            VistaNavigator.baseController.selectMenuButton(Constants.ARCHIVED);
            VistaNavigator.baseController.setActualBaseView(Constants.ARCHIVED);
        }
    }

    /**
     * Open new project modal view -> if a new project is added reload(project) is called and the new project preselected
     */
    @FXML
    private void newProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, Constants.EMPTY);
    }

    /**
     * Open new game modal view -> if a new game is added reload(game) is called and the new game preselected
     */
    @FXML
    private void newGame() {
        VistaNavigator.openModal(Constants.NEW_GAME, Constants.EMPTY);
    }

    /**
     * Open new subject modal view -> if a new subject is added reload(subject) is called and the new subject preselected
     */
    @FXML
    private void newSubject() {
        VistaNavigator.openModal(Constants.NEW_SUBJECT, Constants.EMPTY);
    }

    /**
     * Open new author modal view -> if a new author is added reload(author) is called and the new author preselected
     */
    @FXML
    private void newAuthor() {
        VistaNavigator.openModal(Constants.NEW_AUTHOR, Constants.EMPTY);
    }

    /**
     * While in this view, we can add a new {projects - authors - games - subjects} without close the view
     * If we add one new object we want to add it to its corresponding combo-box and select it.
     * <p>
     * Depending on the type of object passed as a parameter we will add it to a different combo-box.
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
        } else if (object instanceof Author) {
            Author author = (Author) object;
            authorBox.getItems().add(author);
            authorBox.getSelectionModel().select(author);
        }
    }

    @Override
    public void translate() {
        saveButton.setText(language.get(Lang.CONTINUE));
        cancelButton.setText(language.get(Lang.CANCEL));
        nameLabel.setText(language.get(Lang.NAME_LABEL));
        observationsLabel.setText(language.get(Lang.OBSERVATIONS_LABEL));
        nameInput.setPromptText(language.get(Lang.NAME_PROMPT));
        observationsInput.setPromptText(language.get(Lang.OBSERVATIONS_PROMPT));
        title.setText(language.get(Lang.NEW_MISSION_TITLE));
        projectLabel.setText(language.get(Lang.PROJECT_LABEL));
        authorLabel.setText(language.get(Lang.AUTHOR_LABEL));
        startDateLabel.setText(language.get(Lang.START_DATE_LABEL));
        gameLabel.setText(language.get(Lang.GAME_LABEL));
        subjectLabel.setText(language.get(Lang.SUBJECT_LABEL));
        newProjectButton.setText(language.get(Lang.NEW_PROJECT_BUTTON));
        newAuthorButton.setText(language.get(Lang.NEW_AUTHOR_BUTTON));
        newGameButton.setText(language.get(Lang.NEW_GAME_BUTTON));
        newSubjectButton.setText(language.get(Lang.NEW_SUBJECT_BUTTON));
        projectBox.setPromptText(language.get(Lang.PROJECT_PROMPT));
        authorBox.setPromptText(language.get(Lang.AUTHOR_PROMPT));
        subjectBox.setPromptText(language.get(Lang.SUBJECT_PROMPT));
        gameBox.setPromptText(language.get(Lang.GAME_PROMPT));
    }

}
