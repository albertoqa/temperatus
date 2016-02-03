package temperatus.controller.archived;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.controller.AbstractController;
import temperatus.controller.creation.NewMissionController;
import temperatus.controller.creation.NewRecordController;
import temperatus.model.TreeElement;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;
import temperatus.model.service.MissionService;
import temperatus.model.service.ProjectService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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

    @FXML private AnchorPane projectInfoPane;
    @FXML private AnchorPane missionInfoPane;

    @FXML private TextField editableProjectName;
    @FXML private TextArea editableProjectObservations;
    @FXML private DatePicker editableProjectDate;

    @FXML private Button deleteProjectButton;
    @FXML private Button saveProjectButton;
    @FXML private Button editProjectButton;
    @FXML private Button newMissionButton;
    @FXML private Button cancelProjectButton;

    @Autowired
    ProjectService projectService;
    @Autowired
    MissionService missionService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        final TreeItem<TreeElement> root = new TreeItem<>(new TreeElement());
        root.setExpanded(true);
        treeTable.setShowRoot(false);
        treeTable.setRoot(root);

        Platform.runLater(() -> {

            List<Project> projects = projectService.getAll();
            projects.stream().forEach((project) -> {
                TreeItem<TreeElement> treeItemProject = new TreeItem<>(new TreeElement(project));

                List<Mission> missions = missionService.getAllForProject(project.getId());
                treeItemProject.setExpanded(true);
                missions.stream().forEach((mission) -> {
                    treeItemProject.getChildren().add(new TreeItem<>(new TreeElement(mission)));
                });

                root.getChildren().add(treeItemProject);
            });

            TreeTableColumn<TreeElement, String> nameColumn = new TreeTableColumn<>("  Project");
            nameColumn.setCellValueFactory(param -> param.getValue().getValue().nameProperty());

            TreeTableColumn<TreeElement, String> dateColumn = new TreeTableColumn<>("  Start Date");
            dateColumn.setCellValueFactory(param -> param.getValue().getValue().dateProperty());

            TreeTableColumn<TreeElement, String> authorsColumn = new TreeTableColumn<>("  Author");
            authorsColumn.setCellValueFactory(param -> param.getValue().getValue().authorsProperty());

            treeTable.getColumns().setAll(nameColumn, dateColumn, authorsColumn);
            treeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

            treeTable.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.getValue().getaClass().equals(Project.class)) {
                            Animation.fadeOutTransition(missionInfoPane);
                            Animation.fadeInTransition(projectInfoPane);
                            projectInfoPane.setDisable(false);
                            missionInfoPane.setDisable(true);
                            Project project = projectService.getById(newValue.getValue().getId());
                            List<Mission> missions = missionService.getAllForProject(newValue.getValue().getId());

                            projectName.setText(project.getName());
                            projectDate.setText(project.getDateIni().toString());
                            projectNumberOfMissions.setText(String.valueOf(missions.size()));
                            projectObservations.setText(project.getObservations());
                            //projectAuthors.setText(missions.get(0).getAuthor());
                        } else {
                            Animation.fadeOutTransition(projectInfoPane);
                            Animation.fadeInTransition(missionInfoPane);
                            projectInfoPane.setDisable(true);
                            missionInfoPane.setDisable(false);
                        }
                    });
        });

        VistaNavigator.setController(this);
    }

    private void addNewProjectToTree(Project project) {
        treeTable.getRoot().getChildren().add(new TreeItem<>(new TreeElement(project)));
    }

    @FXML
    private void newProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, "new project");
    }

    @FXML
    private void editProject() {
        editingVisibility();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(projectDate.getText().toString(), formatter);

        //editableProjectDate.setValue(date);
        editableProjectName.setText(projectName.getText());
        editableProjectObservations.setText(projectObservations.getText());
    }

    @FXML
    private void saveUpdatedProject() {
        Project project = projectService.getById(getSelectedElement().getId());
        project.setName(editableProjectName.getText());
        project.setObservations(editableProjectObservations.getText());
        //project.setDateIni(Date.from(editableProjectDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        projectService.saveOrUpdate(project);

        projectName.setText(project.getName());
        projectDate.setText(project.getDateIni().toString());
        projectObservations.setText(project.getObservations());

        getSelectedElement().setName(project.getName());
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
            int projectId = getSelectedElement().getId();
            //projectService.delete(projectId); // TODO
            TreeItem<TreeElement> treeItem = treeTable.getSelectionModel().getSelectedItem();
            treeItem.getParent().getChildren().remove(treeItem);
        }
    }

    @FXML
    private void newMission() {
        NewMissionController missionController = VistaNavigator.loadVista(Constants.NEW_MISSION);
        missionController.setProject(getSelectedElement().getId());
    }

    @FXML
    private void missionInfo() {
        MissionInfoController missionInfoController = VistaNavigator.pushViewToStack(Constants.MISSION_INFO);
        missionInfoController.setData(getSelectedElement().getId());
    }

    @FXML
    private void addDataToMission() {   // TODO remember to remove if not needed
        Mission mission = missionService.getById(getSelectedElement().getId());
        NewRecordController newRecordController = VistaNavigator.loadVista(Constants.NEW_RECORD);
        newRecordController.loadData(mission);
    }

    private TreeElement getSelectedElement() {
        return treeTable.getSelectionModel().getSelectedItem().getValue();
    }

    @Override
    public void reload(Object object) {
        if(object instanceof Project) {
            treeTable.getRoot().getChildren().add(new TreeItem<>(new TreeElement((Project) object)));
        }
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
