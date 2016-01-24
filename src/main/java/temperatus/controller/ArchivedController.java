package temperatus.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.controller.archived.ProjectInfoController;
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
public class ArchivedController implements Initializable{

    @FXML
    private TreeView<String> treeView;
    @FXML
    private StackPane stackPane;

    @Autowired
    ProjectService projectService;
    @Autowired
    MissionService missionService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadTreeViewData();
    }

    private void loadTreeViewData() {

        List<String> projects = projectService.getAllProjectNames();
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
            }*/
        }
        treeView.setRoot(rootItem);
        addTreeViewListener();
    }

    private void loadProjectInfoView(String projectName) {
        ProjectInfoController projectInfoController = VistaNavigator.setViewInStackPane(stackPane, Constants.PROJECT_INFO);
        projectInfoController.setProject(projectService.getByName(projectName));
    }

    private void addTreeViewListener() {
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
    }

}
