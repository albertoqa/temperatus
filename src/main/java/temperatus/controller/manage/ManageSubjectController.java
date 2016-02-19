package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.controller.manage.ampliate.SubjectInfoController;
import temperatus.model.pojo.Subject;
import temperatus.model.service.SubjectService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Optional;
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

    @FXML private Label nameLabel;
    @FXML private Label ageLabel;
    @FXML private Label weightLabel;
    @FXML private Label heightLabel;
    @FXML private Label numberOfMissions;
    @FXML private Label firstParticipationLabel;
    @FXML private Label observations;

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

            if(subject != null) {
                nameLabel.setText(subject.getName());
                if(subject.isIsPerson()) {
                    ageLabel.setText(subject.getAge().toString());
                    weightLabel.setText(subject.getWeight().toString());
                    heightLabel.setText(subject.getHeight().toString());
                } else {
                    ageLabel.setText("");
                    weightLabel.setText("");
                    heightLabel.setText("");
                }
                numberOfMissions.setText("Has participated in " + String.valueOf(subject.getMissions().size()) + " missions");
                firstParticipationLabel.setText("His first participation date was on 12/12/12");
                observations.setText(subject.getObservations());
            }
        });

        SortedList<Subject> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(subjectType, name, sex, age, weight, height);
        table.setItems(sortedData);
    }

    private void addAllSubjects() { //TODO
        subjects.addAll(subjectService.getAll());
    }

    @FXML
    private void showCompleteInfo() {
        SubjectInfoController subjectInfoController = VistaNavigator.pushViewToStack(Constants.SUBJECT_INFO);
        subjectInfoController.setSubject(table.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void newSubject() {
        VistaNavigator.openModal(Constants.NEW_SUBJECT, language.get(Constants.NEWSUBJECT));
    }

    @FXML
    private void deleteSubject() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Subject subject = table.getSelectionModel().getSelectedItem();
            subjectService.delete(subject);
            subjects.remove(subject);
        }
    }

    @Override
    public void reload(Object object) {
        if(object instanceof Subject) {
            subjects.add((Subject) object);
            table.getSelectionModel().select((Subject) object);
        }
    }

    @Override
    public void translate() {

    }
}
