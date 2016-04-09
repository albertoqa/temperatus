package temperatus.controller.archived;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.controller.creation.NewMissionController;
import temperatus.controller.creation.NewProjectController;
import temperatus.controller.creation.NewRecordController;
import temperatus.listener.DatabaseThreadFactory;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;
import temperatus.model.pojo.types.FilterableTreeItem;
import temperatus.model.pojo.types.TreeElement;
import temperatus.model.pojo.types.TreeElementType;
import temperatus.model.pojo.types.TreeItemPredicate;
import temperatus.model.service.ProjectService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @FXML private TreeTableView<TreeElement> treeTable;

    @FXML private Label projectName;
    @FXML private Label projectDate;
    @FXML private Label projectNumberOfMissions;
    @FXML private Label projectObservations;
    @FXML private Label projectAuthors;
    @FXML private Label firstMissionDate;
    @FXML private Label firstMissionName;
    @FXML private Label lastMissionDate;
    @FXML private Label lastMissionName;
    @FXML private Label projectSubjects;

    @FXML private Label missionName;
    @FXML private Label missionDate;
    @FXML private Label missionObservations;
    @FXML private Label missionGame;
    @FXML private Label missionProject;
    @FXML private Label missionSubject;
    @FXML private Label missionAuthor;

    @FXML private AnchorPane projectInfoPane;
    @FXML private AnchorPane missionInfoPane;

    @FXML private TextField filterField;

    @FXML private TextField editableProjectName;
    @FXML private TextArea editableProjectObservations;
    @FXML private DatePicker editableProjectDate;

    @FXML private Button deleteProjectButton;
    @FXML private Button saveProjectButton;
    @FXML private Button editProjectButton;
    @FXML private Button newMissionButton;
    @FXML private Button cancelProjectButton;

    private TreeTableColumn<TreeElement, String> nameColumn = new TreeTableColumn<>();
    private TreeTableColumn<TreeElement, String> dateColumn = new TreeTableColumn<>();
    private TreeTableColumn<TreeElement, String> subjectColumn = new TreeTableColumn<>();

    private FilterableTreeItem<TreeElement> root;

    @Autowired ProjectService projectService;
    @Autowired SessionFactory sessionFactory;

    private ExecutorService databaseExecutor;

    static Logger logger = LoggerFactory.getLogger(ArchivedController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Initializing archived controller");

        VistaNavigator.setController(this);
        translate();

        /* Executor is used to perform long operations in a different thread than the UI elements
        in this case, is used to load elements from the DB. ThreadPool is set to 1 to ensure that
        only one database operation is performed at a time*/
        databaseExecutor = Executors.newFixedThreadPool(1, new DatabaseThreadFactory());

        nameColumn.setCellValueFactory(param -> param.getValue().getValue().getName());
        dateColumn.setCellValueFactory(param -> param.getValue().getValue().getDate());
        subjectColumn.setCellValueFactory(param -> param.getValue().getValue().getSubject());

        treeTable.getColumns().setAll(nameColumn, dateColumn, subjectColumn);
        treeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        treeTable.setShowRoot(false);

        // Check the type of element selected and show its information
        treeTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if(newValue != null) {
                        if (TreeElementType.Project == newValue.getValue().getType()) {
                            projectSelection(newValue.getValue().getElement());
                        } else {
                            missionSelection(newValue.getValue().getElement());
                        }
                    }
                });


        // When double click on a row, open edition window
        treeTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    TreeItem<TreeElement> item = treeTable.getSelectionModel().getSelectedItem();
                    if (TreeElementType.Project == item.getValue().getType()) {
                        Project project = (Project) item.getValue().getElement();
                        NewProjectController newProjectController = VistaNavigator.openModal(Constants.NEW_PROJECT, language.get(Constants.NEWPROJECT));
                        newProjectController.setProject(project);
                    }
                    // TODO
                }
            }

        });

        root = getTreeProjects();
        root.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            if (filterField.getText() == null || filterField.getText().isEmpty())
                return null;
            return TreeItemPredicate.create(treeElement -> treeElement.toString().toLowerCase().contains(filterField.getText().toLowerCase()));
        }, filterField.textProperty()));

        treeTable.setRoot(root);

    }

    private FilterableTreeItem<TreeElement> getTreeProjects() {

        FilterableTreeItem<TreeElement> root = new FilterableTreeItem<>(new TreeElement(new Project("", new Date())));
        List<Project> projects = new ArrayList<>();

        Task<List<Project>> getAllProjectsTask = new Task<List<Project>>() {
            @Override
            public List<Project> call() throws Exception {
                return projectService.getAll();
            }
        };

        getAllProjectsTask.setOnSucceeded(e -> {
            projects.addAll(getAllProjectsTask.getValue());
            getTreeMissions(projects, root);
        });

        // run the task using a thread from the thread pool:
        logger.debug("Submiting task to retrieve all projects");
        databaseExecutor.submit(getAllProjectsTask);

        return root;
    }

    private void getTreeMissions(List<Project> projects, FilterableTreeItem<TreeElement> root) {
        for (Project project : projects) {
            FilterableTreeItem<TreeElement> treeItemProject = new FilterableTreeItem<>(new TreeElement(project));
            List<TreeElement> missions = new ArrayList<>();

            // getMissions FetchType is LAZY so it's necessary another access to DB when called
            Task<Set<Mission>> getMissionsTask = new Task<Set<Mission>>() {
                @Override
                public Set<Mission> call() throws Exception {
                    return project.getMissions();
                }
            };

            getMissionsTask.setOnSucceeded(e -> {
                getMissionsTask.getValue().stream().forEach(mission -> missions.add(new TreeElement(mission)));
                ObservableList<TreeElement> missionList = FXCollections.observableArrayList(missions);
                missionList.forEach(mission -> treeItemProject.getInternalChildren().add(new FilterableTreeItem<>(mission)));

                treeItemProject.setExpanded(true);
                root.getInternalChildren().add(treeItemProject);
            });

            // run the task using a thread from the thread pool:
            logger.debug("Submiting task to retrieve all missions for project: " + project.getName());
            databaseExecutor.submit(getMissionsTask);
        }
    }

    private void projectSelection(Project project) {
        Animation.fadeOutTransition(missionInfoPane);
        Animation.fadeInTransition(projectInfoPane);
        projectInfoPane.setDisable(false);
        missionInfoPane.setDisable(true);

        projectName.setText(project.getName());
        projectDate.setText(project.getDateIni().toString());
        projectNumberOfMissions.setText(String.valueOf(project.getMissions().size()));
        projectObservations.setText(project.getObservations());
        //projectAuthors.setText(missions.get(0).getAuthor());
    }

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
     * If project is inserted, the treetableview is automatically updated by the newProject view
     */
    @FXML
    private void newProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, language.get(Constants.NEWPROJECT));
    }

    /**
     * Show the edition elements for the selected Project
     */
    @FXML
    private void editProject() {
        editingVisibility();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(projectDate.getText(), formatter);

        editableProjectDate.setValue(date);
        editableProjectName.setText(projectName.getText());
        editableProjectObservations.setText(projectObservations.getText());
    }

    @FXML
    private void saveUpdatedProject() {
        Project project = getSelectedElement().getElement();
        project.setName(editableProjectName.getText());
        project.setObservations(editableProjectObservations.getText());
        //project.setDateIni(Date.from(editableProjectDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        projectService.saveOrUpdate(project);

        projectName.setText(project.getName());
        projectDate.setText(project.getDateIni().toString());
        projectObservations.setText(project.getObservations());

        //getSelectedElement().setName(project.getName());
        //getSelectedElement().setDate(project.getDateIni());

        notEditingVisibility();
    }

    @FXML
    private void cancelProjectEdition() {
        notEditingVisibility();
    }

    @FXML
    private void deleteProject() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            projectService.delete(getSelectedElement().getElement());
            TreeItem<TreeElement> treeItem = treeTable.getSelectionModel().getSelectedItem();
            root.getInternalChildren().remove(treeItem);
        }
    }

    @FXML
    private void newMission() {
        NewMissionController missionController = VistaNavigator.loadVista(Constants.NEW_MISSION);
        //missionController.setProject(getSelectedElement().getElement());//TODO
        VistaNavigator.baseController.selectMenuButton(Constants.NEW_MISSION);
    }

    @FXML
    private void missionInfo() {
        MissionInfoController missionInfoController = VistaNavigator.pushViewToStack(Constants.MISSION_INFO);
        missionInfoController.setData(((Mission) getSelectedElement().getElement()).getId());
    }

    @FXML
    private void addDataToMission() {   // TODO remember to remove if not needed
        Mission mission = getSelectedElement().getElement();
        NewRecordController newRecordController = VistaNavigator.loadVista(Constants.NEW_RECORD);
        newRecordController.loadData(mission);
    }

    private TreeElement getSelectedElement() {
        return treeTable.getSelectionModel().getSelectedItem().getValue();
    }

    @Override
    public void reload(Object object) {
        if (object instanceof Project) {
            // TODO me falta por actualizar si se hace una actualización
            root.getInternalChildren().add(new FilterableTreeItem<>(new TreeElement((Project) object)));
        }
    }

    @Override
    public void translate() {
        nameColumn.setText(language.get(Constants.PROJECT_COLUMN));
        dateColumn.setText(language.get(Constants.DATE_COLUMN));
        subjectColumn.setText(language.get(Constants.SUBJECT_COLUMN));
    }

    private void notEditingVisibility() {
        saveProjectButton.setVisible(false);
        cancelProjectButton.setVisible(false);
        editProjectButton.setVisible(true);
        deleteProjectButton.setVisible(true);
        editableProjectDate.setVisible(false);
        editableProjectName.setVisible(false);
        editableProjectObservations.setVisible(false);
    }

    private void editingVisibility() {
        editableProjectDate.setVisible(true);
        editableProjectName.setVisible(true);
        editableProjectObservations.setVisible(true);
        saveProjectButton.setVisible(true);
        cancelProjectButton.setVisible(true);
        editProjectButton.setVisible(false);
        deleteProjectButton.setVisible(false);
    }


    /*public void printStatistics() {
        Statistics stat = sessionFactory.getStatistics();
        String regions[] = stat.getSecondLevelCacheRegionNames();
        logger.info(regions.toString());
        for(String regionName:regions) {
            SecondLevelCacheStatistics stat2 = stat.getSecondLevelCacheStatistics(regionName);
            logger.info("2nd Level Cache(" +regionName+") Put Count: "+stat2.getPutCount());
            logger.info("2nd Level Cache(" +regionName+") Hit Count: "+stat2.getHitCount());
            logger.info("2nd Level Cache(" +regionName+") Miss Count: "+stat2.getMissCount());
            logger.info("2nd Level Cache(" +regionName+") Element Count: "+stat2.getElementCountInMemory());
        }
    }*/

}
