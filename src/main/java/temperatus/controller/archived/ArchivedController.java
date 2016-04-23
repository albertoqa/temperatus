package temperatus.controller.archived;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.controller.creation.NewMissionController;
import temperatus.controller.creation.NewProjectController;
import temperatus.lang.Lang;
import temperatus.listener.DatabaseThreadFactory;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;
import temperatus.model.pojo.types.TreeElement;
import temperatus.model.pojo.types.TreeElementType;
import temperatus.model.pojo.utils.FilterableTreeItem;
import temperatus.model.pojo.utils.TreeItemPredicate;
import temperatus.model.service.MissionService;
import temperatus.model.service.ProjectService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Show all the projects and missions saved to the db
 * <p>
 * Created by alberto on 17/1/16.
 */
@Controller
@Scope("prototype")
public class ArchivedController implements Initializable, AbstractController {

    //#####################################################
    // Project Information Pane
    @FXML private AnchorPane projectInfoPane;

    @FXML private Label projectNameLabel;
    @FXML private Label projectStartDateLabel;
    @FXML private Label projectNumberOfMissionsLabel;
    @FXML private Label projectObservationsLabel;
    @FXML private Label projectAuthorsLabel;

    @FXML private Label projectName;
    @FXML private Label projectDate;
    @FXML private Label projectNumberOfMissions;
    @FXML private Label projectObservations;
    @FXML private Label projectAuthors;

    @FXML private Button deleteProjectButton;
    @FXML private Button editProjectButton;

    //#####################################################
    // Mission Information Pane labels
    @FXML private AnchorPane missionInfoPane;

    @FXML private Label missionNameLabel;
    @FXML private Label missionDateLabel;
    @FXML private Label missionObservationsLabel;
    @FXML private Label missionGameLabel;
    @FXML private Label missionProjectLabel;
    @FXML private Label missionSubjectLabel;
    @FXML private Label missionAuthorLabel;

    @FXML private Label missionName;
    @FXML private Label missionDate;
    @FXML private Label missionObservations;
    @FXML private Label missionGame;
    @FXML private Label missionProject;
    @FXML private Label missionSubject;
    @FXML private Label missionAuthor;

    @FXML private Button missionInfoButton;
    @FXML private Button deleteMissionButton;
    @FXML private Button editMissionButton;

    //#####################################################

    @FXML private Button newMissionButton;
    @FXML private Button newProjectButton;

    @FXML private TreeTableView<TreeElement> treeTable;
    @FXML private TextField filterField;

    private TreeTableColumn<TreeElement, String> nameColumn = new TreeTableColumn<>();
    private TreeTableColumn<TreeElement, String> dateColumn = new TreeTableColumn<>();
    private TreeTableColumn<TreeElement, String> subjectColumn = new TreeTableColumn<>();

    private FilterableTreeItem<TreeElement> root;   // root element of the tree

    @Autowired ProjectService projectService;
    @Autowired MissionService missionService;

    private ExecutorService databaseExecutor;     // executes database operations concurrent to JavaFX operations.

    private static Logger logger = LoggerFactory.getLogger(ArchivedController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing archived controller");

        VistaNavigator.setController(this);     // let the application know that this is the current controller
        translate();

        /* Executor is used to perform long operations in a different thread than the UI elements
        in this case, is used to load elements from the DB. ThreadPool is set to 1 to ensure that
        only one database operation is performed at a time*/
        databaseExecutor = Executors.newFixedThreadPool(1, new DatabaseThreadFactory());

        nameColumn.setCellValueFactory(param -> param.getValue().getValue().getName());
        dateColumn.setCellValueFactory(param -> param.getValue().getValue().getDate());
        subjectColumn.setCellValueFactory(param -> param.getValue().getValue().getSubject());

        treeTable.getColumns().setAll(nameColumn, dateColumn, subjectColumn);
        treeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);   // adjust width of columns to distribute on the screen
        treeTable.setShowRoot(false);

        // Check the type of element selected and show its information (Project or Mission)
        treeTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        if (TreeElementType.Project == newValue.getValue().getType()) {
                            projectSelection(newValue.getValue().getElement());
                        } else {
                            missionSelection(newValue.getValue().getElement());
                        }
                    }
                });

        root = getTreeProjects();   // set data

        // bind filter text input to treeView so it filters with user input search
        root.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            if (filterField.getText() == null || filterField.getText().isEmpty())
                return null;
            return TreeItemPredicate.create(treeElement -> treeElement.toString().toLowerCase().contains(filterField.getText().toLowerCase()));
        }, filterField.textProperty()));

        treeTable.setRoot(root);
        treeTable.sort();
    }

    /**
     * Get all projects and missions from database on a new thread so the UI remain responsive
     *
     * @return tree root element
     */
    private FilterableTreeItem<TreeElement> getTreeProjects() {

        // root element holds all projects but it is not shown in the tree
        FilterableTreeItem<TreeElement> root = new FilterableTreeItem<>(new TreeElement(new Project("", new Date())));

        // Get all projects on a different thread than the JavaFX UI thread to remain responsive
        Task<List<Project>> getAllProjectsTask = new Task<List<Project>>() {
            @Override
            public List<Project> call() throws Exception {
                return projectService.getAll();
            }
        };

        // on task completion add all projects to the tree and get all missions for each project
        getAllProjectsTask.setOnSucceeded(e -> {
            getTreeMissions(getAllProjectsTask.getValue(), root);
        });

        // run the task using a thread from the thread pool
        logger.info("Submiting task to retrieve all projects");
        databaseExecutor.submit(getAllProjectsTask);

        return root;
    }

    /**
     * For each project get its missions and create a new element on the tree
     *
     * @param projects all the projects fetched from db
     * @param root     root element of the tree
     */
    private void getTreeMissions(List<Project> projects, FilterableTreeItem<TreeElement> root) {
        for (Project project : projects) {
            FilterableTreeItem<TreeElement> treeItemProject = new FilterableTreeItem<>(new TreeElement(project));

            // getMissions FetchType is LAZY so it's necessary another access to DB when called
            Task<Set<Mission>> getMissionsTask = new Task<Set<Mission>>() {
                @Override
                public Set<Mission> call() throws Exception {
                    return project.getMissions();
                }
            };

            // on task completion add all missions to their parent project
            getMissionsTask.setOnSucceeded(e -> {
                ObservableList<TreeElement> missionList = FXCollections.observableArrayList();

                getMissionsTask.getValue().stream().forEach(mission -> missionList.add(new TreeElement(mission)));
                missionList.forEach(mission -> treeItemProject.getInternalChildren().add(new FilterableTreeItem<>(mission)));

                treeItemProject.setExpanded(true);  // expanded by default
                root.getInternalChildren().add(treeItemProject);
            });

            // run the task using a thread from the thread pool:
            logger.info("Submiting task to retrieve all missions for project: " + project.getName());
            databaseExecutor.submit(getMissionsTask);
        }
    }

    /**
     * Show project info when a project is selected on the tree
     *
     * @param project project to show
     */
    private void projectSelection(Project project) {
        Animation.fadeOutTransition(missionInfoPane);
        Animation.fadeInTransition(projectInfoPane);
        projectInfoPane.setDisable(false);
        missionInfoPane.setDisable(true);

        projectName.setText(project.getName());
        projectDate.setText(project.getDateIni().toString());
        projectNumberOfMissions.setText(String.valueOf(project.getMissions().size()));
        projectObservations.setText(project.getObservations());
        projectAuthors.setText(""); // TODO
    }

    /**
     * Show mission info when a mission is selected on the tree
     *
     * @param mission mission to show
     */
    private void missionSelection(Mission mission) {
        Animation.fadeOutTransition(projectInfoPane);
        Animation.fadeInTransition(missionInfoPane);
        projectInfoPane.setDisable(true);
        missionInfoPane.setDisable(false);

        missionName.setText(mission.getName());
        missionAuthor.setText(mission.getAuthor().getName());
        missionDate.setText(mission.getDateIni().toString());
        missionGame.setText(mission.getGame().getTitle());
        missionProject.setText(mission.getProject().getName());
        missionSubject.setText(mission.getSubject().getName());
        missionObservations.setText(mission.getObservations());
    }

    /**
     * Open modal window for insert a new project
     * If project is inserted, the tree-table-view is automatically updated by the newProject view
     */
    @FXML
    private void newProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, language.get(Lang.NEWPROJECT));
    }

    /**
     * Open modal window for edit a new project
     * The project to edit is the selected project
     */
    @FXML
    private void editProject() {
        NewProjectController newProjectController = VistaNavigator.openModal(Constants.NEW_PROJECT, language.get(Lang.NEWPROJECT));
        newProjectController.setProject(getSelectedElement().getElement());
    }

    /**
     * Delete selected project (and all its missions) from the database
     * Ask user for confirmation first.
     */
    @FXML
    private void deleteProject() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, Lang.CONFIRMATION);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && ButtonType.OK == result.get()) {
            projectService.delete(getSelectedElement().getElement());
            TreeItem<TreeElement> treeItem = treeTable.getSelectionModel().getSelectedItem();
            root.getInternalChildren().remove(treeItem);
        }
    }

    /**
     * Delete selected mission (and all its data) from the database
     * Ask user for confirmation first.
     */
    @FXML
    private void deleteMission() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, Lang.CONFIRMATION);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && ButtonType.OK == result.get()) {
            ((Project) treeTable.getSelectionModel().getSelectedItem().getParent().getValue().getElement()).getMissions().remove((Mission) getSelectedElement().getElement());
            missionService.delete(getSelectedElement().getElement());
            FilterableTreeItem<TreeItem<TreeElement>> treeItem = (FilterableTreeItem) treeTable.getSelectionModel().getSelectedItem();
            ((FilterableTreeItem<TreeItem<TreeElement>>) treeItem.getParent()).getInternalChildren().remove(treeItem);
        }
    }

    /**
     * Create a new mission -> load view and select its icon from the lateral menu
     */
    @FXML
    private void newMission() {
        VistaNavigator.loadVista(Constants.NEW_MISSION);
        VistaNavigator.baseController.selectMenuButton(Constants.NEW_MISSION);
    }

    /**
     * Show complete mission information
     */
    @FXML
    private void missionInfo() {
        MissionInfoController missionInfoController = VistaNavigator.pushViewToStack(Constants.MISSION_INFO);
        missionInfoController.setData(((Mission) getSelectedElement().getElement()).getId());
    }

    @FXML
    private void editMission() {
        Mission mission = getSelectedElement().getElement();
        NewMissionController newMissionController = VistaNavigator.loadVista(Constants.NEW_MISSION);
        if (newMissionController != null) {
            newMissionController.setMissionForUpdate(mission);
        }
    }

    /**
     * @return currently selected element on the tree
     */
    private TreeElement getSelectedElement() {
        return treeTable.getSelectionModel().getSelectedItem().getValue();
    }

    @Override
    public void reload(Object object) {
        if (object instanceof Project) {
            root.getInternalChildren().add(new FilterableTreeItem<>(new TreeElement((Project) object)));
        }
    }

    @Override
    public void translate() {
        nameColumn.setText(language.get(Lang.PROJECT_COLUMN));
        dateColumn.setText(language.get(Lang.DATE_COLUMN));
        subjectColumn.setText(language.get(Lang.SUBJECT_COLUMN));


        /* TODO

    @FXML private Label missionNameLabel;
    @FXML private Label missionDateLabel;
    @FXML private Label missionObservationsLabel;
    @FXML private Label missionGameLabel;
    @FXML private Label missionProjectLabel;
    @FXML private Label missionSubjectLabel;
    @FXML private Label missionAuthorLabel;
    @FXML private Label projectNameLabel;
    @FXML private Label projectStartDateLabel;
    @FXML private Label projectNumberOfMissionsLabel;
    @FXML private Label projectObservationsLabel;
    @FXML private Label projectAuthorsLabel;

        @FXML private Button deleteProjectButton;
    @FXML private Button editProjectButton;

    @FXML private Button missionInfoButton;
    @FXML private Button deleteMissionButton;
    @FXML private Button editMissionButton;

        @FXML private TextField filterField;

        @FXML private Button newMissionButton;
    @FXML private Button newProjectButton;
         */
    }
}
