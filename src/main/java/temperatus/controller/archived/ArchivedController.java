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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.controller.AbstractController;
import temperatus.controller.creation.NewMissionController;
import temperatus.controller.creation.NewProjectController;
import temperatus.controller.creation.NewRecordController;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by alberto on 17/1/16.
 */
@Component
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

    private TreeTableColumn<TreeElement, String> nameColumn = new TreeTableColumn<>("  Project");
    private TreeTableColumn<TreeElement, String> dateColumn = new TreeTableColumn<>("  Start Date");
    private TreeTableColumn<TreeElement, String> authorsColumn = new TreeTableColumn<>("  Subject");
    private FilterableTreeItem<TreeElement> root;

    @Autowired ProjectService projectService;

    private Executor exec ;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        VistaNavigator.setController(this);
        translate();

        /* Executor is used to perform long operations in a different thread than the UI elements
        in this case, is used to load elements from the DB */
        exec = Executors.newCachedThreadPool(runnable -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        nameColumn.setCellValueFactory(param -> param.getValue().getValue().getName());
        dateColumn.setCellValueFactory(param -> param.getValue().getValue().getDate());
        authorsColumn.setCellValueFactory(param -> param.getValue().getValue().getSubject());

        treeTable.getColumns().setAll(nameColumn, dateColumn, authorsColumn);
        treeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        treeTable.setShowRoot(false);

        treeTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (TreeElementType.Project == newValue.getValue().getType()) {
                        Animation.fadeOutTransition(missionInfoPane);
                        Animation.fadeInTransition(projectInfoPane);
                        projectInfoPane.setDisable(false);
                        missionInfoPane.setDisable(true);
                        Project project = getSelectedElement().getElement();

                        projectName.setText(project.getName());
                        projectDate.setText(project.getDateIni().toString());
                        projectNumberOfMissions.setText(String.valueOf(project.getMissions().size()));
                        projectObservations.setText(project.getObservations());
                        //projectAuthors.setText(missions.get(0).getAuthor());
                    } else {
                        Animation.fadeOutTransition(projectInfoPane);
                        Animation.fadeInTransition(missionInfoPane);
                        projectInfoPane.setDisable(true);
                        missionInfoPane.setDisable(false);
                    }
                });


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
        exec.execute(getAllProjectsTask);

        return root;
    }

    private void getTreeMissions(List<Project> projects, FilterableTreeItem<TreeElement> root) {
        for (Project project : projects) {
            FilterableTreeItem<TreeElement> treeItemProject = new FilterableTreeItem<>(new TreeElement(project));
            List<TreeElement> missions = new ArrayList<>();

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
            exec.execute(getMissionsTask);
        }
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
        missionController.setProject(getSelectedElement().getElement());//TODO
    }

    @FXML
    private void missionInfo() {
        MissionInfoController missionInfoController = VistaNavigator.pushViewToStack(Constants.MISSION_INFO);
        //missionInfoController.setData(getSelectedElement().getId());
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
}
