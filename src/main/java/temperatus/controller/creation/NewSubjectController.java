package temperatus.controller.creation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.lang.Language;
import temperatus.model.pojo.Subject;
import temperatus.model.service.SubjectService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 27/1/16.
 */
@Controller
public class NewSubjectController extends AbstractCreationController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label observationsLabel;
    @FXML private Label ageLabel;
    @FXML private Label weightLabel;
    @FXML private Label sizeLabel;

    @FXML private RadioButton isPerson;
    @FXML private RadioButton isObject;
    @FXML private RadioButton isMale;
    @FXML private RadioButton isFemale;

    @FXML private TextField nameInput;
    @FXML private TextArea observationsInput;
    @FXML private TextField ageInput;
    @FXML private TextField weightInput;
    @FXML private TextField sizeInput;

    @FXML private AnchorPane personDataPane; // this pane will be hidden if subject is not a person

    @Autowired SubjectService subjectService;

    private final Language language = Language.getInstance();
    static Logger logger = Logger.getLogger(NewProjectController.class.getName());

    private final ToggleGroup person = new ToggleGroup();
    private final ToggleGroup gender = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // form groups os toggle buttons
        isPerson.setToggleGroup(person);
        isObject.setToggleGroup(person);
        isMale.setToggleGroup(gender);
        isFemale.setToggleGroup(gender);

        isPerson.setSelected(true); // default select person
        isMale.setSelected(true);   // default select male

        // show or hide the pane depending on if subject is a person or not
        person.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (person.getSelectedToggle() == isPerson) {
                    logger.debug("Showing person pane");
                    Animation.fadeInTransition(personDataPane);
                } else {
                    logger.debug("Hiding person pane");
                    Animation.fadeOutTransition(personDataPane);
                }
            }
        });

        translate();
    }

    @Override
    @FXML
    void save() {

        Subject subject = new Subject();
        subject.setName(nameInput.getText());
        subject.setObservations(observationsInput.getText());

        if (person.getSelectedToggle() == isPerson) {
            subject.setIsPerson(true);
            subject.setAge(Integer.valueOf(ageInput.getText()));
            subject.setWeight(Double.valueOf(weightInput.getText()));
            subject.setSize(Double.valueOf(sizeInput.getText()));
            if (gender.getSelectedToggle() == isMale) {
                subject.setSex(true);
            } else {
                subject.setSex(false);
            }
        } else {
            subject.setIsPerson(false);
        }

        subjectService.save(subject);

        Animation.fadeInOutClose(titledPane);
        if (VistaNavigator.getController() != null) {
            VistaNavigator.getController().reload(subject);
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.NEWGAME));
        saveButton.setText(language.get(Constants.SAVE));
        cancelButton.setText(language.get(Constants.CANCEL));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        observationsLabel.setText(language.get(Constants.OBSERVATIONSLABEL));
        nameInput.setPromptText(language.get(Constants.NAMEPROMP));
        observationsInput.setPromptText(language.get(Constants.OBSERVATIONSPROMP));
        ageLabel.setText(language.get(Constants.AGELABEL));
        ageInput.setPromptText(language.get(Constants.AGEPROMP));
        weightLabel.setText(language.get(Constants.WEIGHTLABEL));
        weightInput.setPromptText(language.get(Constants.WEIGHTPROMP));
        sizeLabel.setText(language.get(Constants.SIZELABEL));
        sizeInput.setPromptText(language.get(Constants.SIZEPROMP));
        isPerson.setText(language.get(Constants.ISPERSON));
        isObject.setText(language.get(Constants.ISOBJECT));
        isMale.setText(language.get(Constants.ISMALE));
        isFemale.setText(language.get(Constants.ISFEMALE));
    }

}
