package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Subject;
import temperatus.model.service.SubjectService;
import temperatus.util.Animation;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 14/2/16.
 */
@Controller
@Scope("prototype")
public class ManageSubjectController implements Initializable, AbstractController {

    @FXML private TableView<Subject> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;

    private TableColumn<Subject, String> subjectType = new TableColumn<>();
    private TableColumn<Subject, String> name = new TableColumn<>();
    private TableColumn<Subject, String> sex = new TableColumn<>();
    private TableColumn<Subject, Integer> age = new TableColumn<>();
    private TableColumn<Subject, String> weight = new TableColumn<>();
    private TableColumn<Subject, String> height = new TableColumn<>();

    private ObservableList<Subject> subjects;

    @Autowired SubjectService subjectService;

    static Logger logger = Logger.getLogger(ManageSubjectController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        subjects = FXCollections.observableArrayList();
        addAllSubjects();

        subjectType.setText("Type");
        subjectType.setCellValueFactory(cellData -> cellData.getValue().getType());
        name.setText("Name");
        name.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        sex.setText("Sex");
        sex.setCellValueFactory(cellData -> cellData.getValue().getSexProperty());
        age.setText("Age");
        age.setCellValueFactory(cellData -> cellData.getValue().getAgeProperty().asObject());
        weight.setText("Weight");
        weight.setCellValueFactory(cellData -> cellData.getValue().getWeightProperty());
        height.setText("Height");
        height.setCellValueFactory(cellData -> cellData.getValue().getHeightProperty());

        FilteredList<Subject> filteredData = new FilteredList<>(subjects, p -> true);

        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(subject -> {
                // If filter text is empty, display all subjects.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (subject.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (subject.isIsPerson()) {
                    if (subject.getAge() != null && subject.getAge().toString().contains(lowerCaseFilter)) {
                        return true;
                    } else if (subject.getHeight() != null && subject.getHeight().toString().contains(lowerCaseFilter)) {
                        return true;
                    } else if (subject.getWeight() != null && subject.getWeight().toString().contains(lowerCaseFilter)) {
                        return true;
                    } else if (subject.getSex()) {
                        if ("male".contains(lowerCaseFilter)) {
                            return true;
                        }
                    } else if (!subject.getSex()) {
                        if ("female".contains(lowerCaseFilter)) {
                            return true;
                        }
                    }
                }
                return false; // Does not match.
            });
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Animation.fadeInTransition(infoPane);

            Subject subject = newValue;
            // TODO

        });

        SortedList<Subject> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(subjectType, name, sex, age, weight, height);
        table.setItems(sortedData);
    }

    private void addAllSubjects() { //TODO
        subjects.addAll(subjectService.getAll());
    }


    @Override
    public void reload(Object object) {

    }

    @Override
    public void translate() {

    }
}
