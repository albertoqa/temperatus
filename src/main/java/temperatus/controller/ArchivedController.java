package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.model.service.ExperimentService;
import temperatus.model.service.ProjectService;

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

    @Autowired
    ProjectService projectService;

    @Autowired
    ExperimentService experimentService;

    //@Autowired
    //TestService testService;

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
            List<Integer> experiments = experimentService.getExperimentNamesRelatedToProject(project);
            for (Integer experiment : experiments) {
                TreeItem<String> experimentItem = new TreeItem<String>(String.valueOf(experiment));
                projectItem.getChildren().add(experimentItem);
                /*List<String> tests = null;
                for (String test : tests) {
                    TreeItem<String> testItem = new TreeItem<String>(test);
                    experimentItem.getChildren().add(testItem);
                }*/
            }
        }
        treeView.setRoot(rootItem);
    }

}
