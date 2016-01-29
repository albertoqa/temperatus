package temperatus.controller.archived;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.controller.AbstractController;
import temperatus.model.TreeElement;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;
import temperatus.model.service.MissionService;
import temperatus.model.service.ProjectService;
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

        //Platform.runLater(() -> {

        final TreeItem<TreeElement> root = new TreeItem<>(new TreeElement());
        root.setExpanded(true);
        treeTable.setShowRoot(false);
        treeTable.setRoot(root);

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

        TreeTableColumn<TreeElement, String> nameColumn =
                new TreeTableColumn<>("  Project");
        nameColumn.setPrefWidth(286.33);
        nameColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<TreeElement, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getName())
        );

        TreeTableColumn<TreeElement, String> dateColumn =
                new TreeTableColumn<>("  Start Date");
        dateColumn.setPrefWidth(226.33);
        dateColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<TreeElement, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getDate().toString())
        );

        TreeTableColumn<TreeElement, String> authorsColumn =
                new TreeTableColumn<>("  Author");
        authorsColumn.setPrefWidth(218.33);
        authorsColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<TreeElement, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getAuthors())
        );

        treeTable.getColumns().setAll(nameColumn, dateColumn, authorsColumn);

        treeTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if(newValue.getValue().getaClass().equals(Project.class)) {
                        projectInfoPane.setOpacity(100);
                        Project project = projectService.getById(newValue.getValue().getId());
                        List<Mission> missions = missionService.getAllForProject(newValue.getValue().getId());

                        projectName.setText(project.getName());
                        projectDate.setText(project.getDateIni().toString());
                        projectNumberOfMissions.setText(String.valueOf(missions.size()));
                        projectObservations.setText(project.getObservations());
                        //projectAuthors.setText(missions.get(0).getAuthor());
                    } else {
                        projectInfoPane.setOpacity(0);
                    }

                });



        //});

        VistaNavigator.setController(this);
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
        Project project = projectService.getById(treeTable.getSelectionModel().getSelectedItem().getValue().getId());
        project.setName(editableProjectName.getText());
        project.setObservations(editableProjectObservations.getText());
        //project.setDateIni(Date.from(editableProjectDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        projectService.saveOrUpdate(project);

        // TODO update treeView

        projectName.setText(project.getName());
        projectDate.setText(project.getDateIni().toString());
        projectObservations.setText(project.getObservations());

        notEditingVisibility();

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

    @FXML
    private void cancelProjectEdition() {
        notEditingVisibility();
    }

    @FXML
    private void deleteProject() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            int projectId = treeTable.getSelectionModel().getSelectedItem().getValue().getId();
            //projectService.delete(projectId);
            TreeItem<TreeElement> treeItem = treeTable.getSelectionModel().getSelectedItem();
            treeItem.getParent().getChildren().remove(treeItem);
        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    @FXML
    private void newMission() {

    }



    @Override
    public void reload() {
        //loadTreeViewData(); //TODO reload only what changed
    }
}
