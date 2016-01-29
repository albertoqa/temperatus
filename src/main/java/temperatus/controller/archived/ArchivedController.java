package temperatus.controller.archived;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Component
public class ArchivedController implements Initializable, AbstractController{

    @FXML
    private TreeTableView<TreeElement> treeTable;

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
        nameColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<TreeElement, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getName())
        );

        TreeTableColumn<TreeElement, String> dateColumn =
                new TreeTableColumn<>("  Start Date");
        dateColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<TreeElement, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getDate().toString())
        );

        TreeTableColumn<TreeElement, String> authorsColumn =
                new TreeTableColumn<>("  Author");
        authorsColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<TreeElement, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getAuthors())
        );

        treeTable.getColumns().setAll(nameColumn, dateColumn, authorsColumn);

        VistaNavigator.setController(this);
        //loadTreeViewData();
    }

    //private void loadTreeViewData() {

        /*List<String> projects = projectService.getAllProjectNames();
        TreeItem<String> rootItem = new TreeItem<String>("Projects");
        rootItem.setExpanded(true);
        for (String project : projects) {
            TreeItem<String> projectItem = new TreeItem<String>(project);
            rootItem.getChildren().add(projectItem);
            /*List<Integer> experiments = misionService.getMisionNamesRelatedToProject(project);
            for (Integer experiment : experiments) {
                TreeItem<String> experimentItem = new TreeItem<String>(String.valueOf(experiment));
                projectItem.getChildren().add(experimentItem);
                /*List<String> tests = null;
                for (String test : tests) {
                    TreeItem<String> testItem = new TreeItem<String>(test);
                    experimentItem.getChildren().add(testItem);
                }
            }
        }
        treeView.setRoot(rootItem);
        addTreeViewListener();*/
    //}

    /*private void loadProjectInfoView(String projectName) {
        ProjectInfoController projectInfoController = VistaNavigator.setViewInStackPane(stackPane, Constants.PROJECT_INFO);
        projectInfoController.setProject(projectService.getByName(projectName));
    }*/

    @FXML
    private void newProject() {
        VistaNavigator.openModal(Constants.NEW_PROJECT, "new project");
    }

    /*private void addTreeViewListener() {
        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {

                TreeItem<String> selectedItem = (TreeItem<String>) newValue;

                if(selectedItem.getValue().equals(treeView.getRoot().getValue())) {
                    // TODO nothing selected
                } else if(selectedItem.getParent().getValue().equals(treeView.getRoot().getValue())) {
                    loadProjectInfoView(selectedItem.getValue());
                } else {
                    //loadMissionInfoView(selectedItem.getValue()); //TODO
                }
            }
        });
    }*/

    @Override
    public void reload() {
        //loadTreeViewData(); //TODO reload only what changed
    }
}
