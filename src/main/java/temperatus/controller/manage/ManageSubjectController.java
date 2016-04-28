package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.controller.creation.NewSubjectController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Subject;
import temperatus.model.service.SubjectService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Allow the user to search, edit, create and create subjects
 * <p>
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
    @FXML private Label numberOfMissionsLabel;
    @FXML private Label firstParticipationLabel;
    @FXML private Label observationsLabel;

    @FXML private Label ageInfo;
    @FXML private Label weightInfo;
    @FXML private Label heightInfo;
    @FXML private Label observationsInfo;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private TableColumn<Subject, String> subjectType = new TableColumn<>();
    private TableColumn<Subject, String> name = new TableColumn<>();
    private TableColumn<Subject, String> sex = new TableColumn<>();
    private TableColumn<Subject, String> age = new TableColumn<>();
    private TableColumn<Subject, String> weight = new TableColumn<>();
    private TableColumn<Subject, String> height = new TableColumn<>();

    private ObservableList<Subject> subjects;

    @Autowired SubjectService subjectService;

    private static Logger logger = LoggerFactory.getLogger(ManageSubjectController.class.getName());

    private static final String MALE = "male";
    private static final String FEMALE = "female";
    private static final String SPACE = " ";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        subjects = FXCollections.observableArrayList(subjectService.getAll());
        subjectType.setCellValueFactory(cellData -> cellData.getValue().getType());
        name.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        sex.setCellValueFactory(cellData -> cellData.getValue().getSexProperty());
        age.setCellValueFactory(cellData -> cellData.getValue().getAgeProperty());
        weight.setCellValueFactory(cellData -> cellData.getValue().getWeightProperty());
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
                        if (MALE.contains(lowerCaseFilter)) {
                            return true;
                        }
                    } else {
                        if (FEMALE.contains(lowerCaseFilter)) {
                            return true;
                        }
                    }
                }
                return false; // Does not match.
            });
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, subject) -> {
            Animation.fadeInTransition(infoPane);
            if (subject != null) {
                nameLabel.setText(subject.getName().toUpperCase());
                if (subject.isIsPerson()) {
                    ageInfo.setText(subject.getAge().toString());
                    weightInfo.setText(subject.getWeight().toString());
                    heightInfo.setText(subject.getHeight().toString());
                } else {
                    ageInfo.setText(SPACE);
                    weightInfo.setText(SPACE);
                    heightInfo.setText(SPACE);
                }

                numberOfMissionsLabel.setText(language.get(Lang.PARTICIPATE_IN) + SPACE + String.valueOf(subject.getMissions().size()) + SPACE + language.get(Lang.MISSIONS));
                Date d = new Date();
                for(Mission mission: subject.getMissions()) {
                    if(mission.getDateIni().before(d)) {
                        d = mission.getDateIni();
                    }
                }
                firstParticipationLabel.setText(language.get(Lang.FIRST_PARTICIPATION) + SPACE + Constants.dateFormat.format(d));
                observationsInfo.setText(subject.getObservations());
            }
        });

        SortedList<Subject> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(subjectType, name, sex, age, weight, height);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();
    }

    /**
     * Open modal window to create a new subject
     */
    @FXML
    private void newSubject() {
        VistaNavigator.openModal(Constants.NEW_SUBJECT, language.get(Lang.NEWSUBJECT));
    }

    /**
     * Open a modal window to edit/update the selected user
     */
    @FXML
    private void editSubject() {
        NewSubjectController newSubjectController = VistaNavigator.openModal(Constants.NEW_SUBJECT, language.get(Lang.NEWSUBJECT));
        newSubjectController.setSubjectForUpdate(table.getSelectionModel().getSelectedItem());
    }

    /**
     * Delete selected user
     */
    @FXML
    private void deleteSubject() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Subject subject = table.getSelectionModel().getSelectedItem();
            subjectService.delete(subject);
            subjects.remove(subject);
            logger.info("Deleted subject... " + subject);
        }
    }

    /**
     * Reload any created or edited subject
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Subject) {
            if (!subjects.contains(object)) {
                subjects.add((Subject) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select((Subject) object);
        }
    }

    @Override
    public void translate() {
        subjectType.setText(language.get(Lang.TYPE_COLUMN));
        name.setText(language.get(Lang.NAME_COLUMN));
        sex.setText(language.get(Lang.SEX_COLUMN));
        age.setText(language.get(Lang.AGE_COLUMN));
        weight.setText(language.get(Lang.WEIGHT_COLUMN));
        height.setText(language.get(Lang.HEIGHT_COLUMN));

        ageLabel.setText(language.get(Lang.AGELABEL));
        weightLabel.setText(language.get(Lang.WEIGHTLABEL));
        heightLabel.setText(language.get(Lang.SIZELABEL));
        observationsLabel.setText(language.get(Lang.OBSERVATIONSLABEL));
        editButton.setText(language.get(Lang.EDIT));
        deleteButton.setText(language.get(Lang.DELETE));
        newElementButton.setText(language.get(Lang.NEWSUBJECT));
    }
}
