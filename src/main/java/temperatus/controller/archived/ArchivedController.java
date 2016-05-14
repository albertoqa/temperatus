package temperatus.controller.archived;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.controller.creation.NewMissionController;
import temperatus.controller.creation.NewProjectController;
import temperatus.exporter.ProjectExporter;
import temperatus.lang.Lang;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;
import temperatus.model.pojo.types.TreeElement;
import temperatus.model.pojo.types.TreeElementType;
import temperatus.model.pojo.types.Unit;
import temperatus.model.pojo.utils.FilterableTreeItem;
import temperatus.model.pojo.utils.TreeItemPredicate;
import temperatus.model.service.MissionService;
import temperatus.model.service.ProjectService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

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
    @FXML private Button exportProjectButton;

    //#####################################################
    // Mission Information Pane labels
    @FXML private AnchorPane missionInfoPane;

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

    private static Logger logger = LoggerFactory.getLogger(ArchivedController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing archived controller");

        VistaNavigator.setController(this);     // let the application know that this is the current controller
        translate();

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

        nameColumn.setSortable(false);
        subjectColumn.setSortable(false);
        dateColumn.setSortable(false);
    }

    /**
     * Get all projects and missions from database on a new thread so the UI remain responsive
     *
     * @return tree root element
     */
    private FilterableTreeItem<TreeElement> getTreeProjects() {

        // root element holds all projects but it is not shown in the tree
        FilterableTreeItem<TreeElement> rootNode = new FilterableTreeItem<>(new TreeElement(new Project(Constants.EMPTY, new Date())));

        // Get all projects on a different thread than the JavaFX UI thread to remain responsive
        Task<List<Project>> getAllProjectsTask = new Task<List<Project>>() {
            @Override
            public List<Project> call() throws Exception {
                return projectService.getAll();
            }
        };

        // on task completion add all projects to the tree and get all missions for each project
        getAllProjectsTask.setOnSucceeded(e -> getTreeMissions(getAllProjectsTask.getValue(), rootNode));

        // run the task using a thread from the thread pool
        logger.info("Submit task to retrieve all projects");
        databaseExecutor.submit(getAllProjectsTask);

        return rootNode;
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
            logger.info("Submit task to retrieve all missions for project: " + project.getName());
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
        projectDate.setText(Constants.dateFormat.format(project.getDateIni()));
        projectNumberOfMissions.setText(String.valueOf(project.getMissions().size()));
        projectObservations.setText(project.getObservations());

        String authors = Constants.EMPTY;
        for (Mission mission : project.getMissions()) {
            if (!authors.contains(mission.getAuthor().getName())) {
                authors = authors + mission.getAuthor().getName() + ", ";
            }
        }
        if (!authors.equals(Constants.EMPTY)) {   // remove last comma
            authors = authors.substring(0, authors.length() - 2);
        }

        projectAuthors.setText(authors);
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
        missionDate.setText(Constants.dateFormat.format(mission.getDateIni()));
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
        VistaNavigator.openModal(Constants.NEW_PROJECT, Constants.EMPTY);
    }

    /**
     * Open modal window for edit a new project
     * The project to edit is the selected project
     */
    @FXML
    private void editProject() {
        NewProjectController newProjectController = VistaNavigator.openModal(Constants.NEW_PROJECT, Constants.EMPTY);
        newProjectController.setProject(getSelectedElement().getElement());
    }

    /**
     * Delete selected project (and all its missions) from the database
     * Ask user for confirmation first.
     */
    @FXML
    private void deleteProject() {
        if (VistaNavigator.confirmationAlert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION))) {
            projectService.delete(getSelectedElement().getElement());
            TreeItem<TreeElement> treeItem = treeTable.getSelectionModel().getSelectedItem();
            root.getInternalChildren().remove(treeItem);
            treeTable.getSelectionModel().clearSelection();
            Animation.fadeOutTransition(projectInfoPane);
            projectInfoPane.setDisable(true);
        }
    }

    /**
     * Delete selected mission (and all its data) from the database
     * Ask user for confirmation first.
     */
    @FXML
    private void deleteMission() {
        if (VistaNavigator.confirmationAlert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION))) {
            ((Project) treeTable.getSelectionModel().getSelectedItem().getParent().getValue().getElement()).getMissions().remove(getSelectedElement().getElement());
            missionService.delete(getSelectedElement().getElement());
            FilterableTreeItem<TreeItem<TreeElement>> treeItem = (FilterableTreeItem) treeTable.getSelectionModel().getSelectedItem();
            ((FilterableTreeItem<TreeItem<TreeElement>>) treeItem.getParent()).getInternalChildren().remove(treeItem);
            treeTable.getSelectionModel().clearSelection();
            Animation.fadeOutTransition(missionInfoPane);
            missionInfoPane.setDisable(true);
        }
    }

    /**
     * Create a new mission -> load view and select its icon from the lateral menu
     * Preselect project if currently selecting a project
     */
    @FXML
    private void newMission() {
        NewMissionController missionController = VistaNavigator.loadVista(Constants.NEW_MISSION);
        if (getSelectedElement().getType().equals(TreeElementType.Project) && missionController != null) {
            missionController.setProject(getSelectedElement().getElement());
        }
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

    /**
     * Edit the selected mission
     */
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

    /**
     * Export all the missions of the selected project to a excel file
     * This option is only activated for premium version of the program
     *
     * @throws IOException
     */
    @FXML
    private void exportProject() throws IOException {
        // Only allow export if complete version of the application, trial version cannot export data
        if (Constants.prefs.getBoolean(Constants.ACTIVATED, false)) {
            logger.info("Exporting project data...");

            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLS (*.xls)", "*.xls");
            fileChooser.getExtensionFilters().add(extFilter);   //Set extension filter

            File file = fileChooser.showSaveDialog(projectInfoPane.getScene().getWindow());   //Show save file dialog

            if (file != null) {
                // Export the data using the preferred unit
                Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C: Unit.F;

                // create a new project exporter and set the data to export
                ProjectExporter projectExporter = new ProjectExporter();
                projectExporter.setData(getSelectedElement().getElement(), unit);

                Workbook workBook = projectExporter.export();

                FileOutputStream fileOut = new FileOutputStream(file);  // write generated data to a file
                workBook.write(fileOut);
                fileOut.close();

                showAlertAndWait(Alert.AlertType.INFORMATION, language.get(Lang.SUCCESSFULLY_EXPORTED));
            }
        } else {
            VistaNavigator.openModal(Constants.BUY_COMPLETE, Constants.EMPTY);
        }
    }

    /**
     * Reload if create/edit a project
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Project) {
            if (treeTable.getSelectionModel().getSelectedItem() != null && getSelectedElement().getType().equals(TreeElementType.Project) && ((Project) object).getId().equals(((Project) getSelectedElement().getElement()).getId())) {
                projectName.setText(((Project) object).getName());
                projectDate.setText(Constants.dateFormat.format(((Project) object).getDateIni()));
            } else {
                root.getInternalChildren().add(new FilterableTreeItem<>(new TreeElement((Project) object)));
            }
        }
    }

    @Override
    public void translate() {
        nameColumn.setText(language.get(Lang.PROJECT_COLUMN));
        dateColumn.setText(language.get(Lang.DATE_COLUMN));
        subjectColumn.setText(language.get(Lang.SUBJECT_COLUMN));

        filterField.setPromptText(language.get(Lang.FILTER));

        newProjectButton.setText(language.get(Lang.NEW_PROJECT_BUTTON));
        newMissionButton.setText(language.get(Lang.NEW_MISSION_BUTTON));
        exportProjectButton.setText(language.get(Lang.EXPORT));

        projectStartDateLabel.setText(language.get(Lang.START_DATE_LABEL));
        projectNumberOfMissionsLabel.setText(language.get(Lang.NUMBER_OF_MISSIONS));
        projectObservationsLabel.setText(language.get(Lang.OBSERVATIONS_LABEL));
        projectAuthorsLabel.setText(language.get(Lang.PROJECT_AUTHORS_LABEL));
        editProjectButton.setText(language.get(Lang.EDIT));
        deleteProjectButton.setText(language.get(Lang.DELETE));

        missionInfoButton.setText(language.get(Lang.COMPLETE_INFO));
        deleteMissionButton.setText(language.get(Lang.DELETE));
        editMissionButton.setText(language.get(Lang.EDIT));
        missionAuthorLabel.setText(language.get(Lang.AUTHOR_LABEL));
        missionProjectLabel.setText(language.get(Lang.PROJECT_LABEL));
        missionDateLabel.setText(language.get(Lang.START_DATE_LABEL));
        missionSubjectLabel.setText(language.get(Lang.SUBJECT_LABEL));
        missionGameLabel.setText(language.get(Lang.GAME_LABEL));
        missionObservationsLabel.setText(language.get(Lang.OBSERVATIONS_LABEL));
        treeTable.setPlaceholder(new Label(language.get(Lang.EMPTY_TABLE_ARCHIVE)));
    }

}
