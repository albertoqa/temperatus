package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Subject;
import temperatus.model.service.SubjectService;
import temperatus.util.Animation;
import temperatus.util.DateUtils;
import temperatus.util.TextValidation;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * View to create and save a new subject
 * <p>
 * Created by alberto on 27/1/16.
 */
@Controller
@Scope("prototype")
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
    @FXML private DatePicker ageInput;
    @FXML private TextField weightInput;
    @FXML private TextField heightInput;

    @FXML private AnchorPane personDataPane; // this pane will be hidden if subject is not a person

    @Autowired SubjectService subjectService;

    private static final int MAX_DIGITS_FOR_SIZE = 8;

    private static Logger logger = LoggerFactory.getLogger(NewSubjectController.class.getName());

    private final ToggleGroup person = new ToggleGroup();
    private final ToggleGroup gender = new ToggleGroup();

    private Subject subject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subject = null;

        // form groups of toggle buttons
        isPerson.setToggleGroup(person);
        isObject.setToggleGroup(person);
        isMale.setToggleGroup(gender);
        isFemale.setToggleGroup(gender);

        isPerson.setSelected(true); // default select person
        isMale.setSelected(true);   // default select male

        // show or hide the pane depending on if subject is a person or not
        person.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            if (person.getSelectedToggle() == isPerson) {
                logger.debug("Showing person pane");
                Animation.fadeInTransition(personDataPane);
            } else {
                logger.debug("Hiding person pane");
                Animation.fadeOutTransition(personDataPane);
            }
        });

        weightInput.addEventFilter(KeyEvent.KEY_TYPED, TextValidation.numeric(MAX_DIGITS_FOR_SIZE));
        heightInput.addEventFilter(KeyEvent.KEY_TYPED, TextValidation.numeric(MAX_DIGITS_FOR_SIZE));
        ageInput.setEditable(false);

        translate();
    }

    /**
     * When editing a subject, pre-load its data
     *
     * @param subject subject to update/edit
     */
    public void setSubjectForUpdate(Subject subject) {
        this.subject = subject;
        saveButton.setText(language.get(Lang.UPDATE));
        nameInput.setText(subject.getName());
        observationsInput.setText(subject.getObservations());

        if (subject.isIsPerson()) {
            person.selectToggle(isPerson);
            ageInput.setValue(DateUtils.asLocalDate(subject.getAge()));
            weightInput.setText(String.valueOf(subject.getWeight()));
            heightInput.setText(String.valueOf(subject.getHeight()));

            if (subject.getSex()) {
                gender.selectToggle(isMale);
            } else {
                gender.selectToggle(isFemale);
            }
        } else {
            person.selectToggle(isObject);
        }
    }

    /**
     * Save or update the subject to database
     */
    @Override
    @FXML
    void save() {
        try {
            logger.info("Saving subject...");

            if (subject == null) {
                subject = new Subject();
            }

            subject.setName(nameInput.getText());
            subject.setObservations(observationsInput.getText());

            // if subject is not a person only set the name and observations
            if (person.getSelectedToggle() == isPerson) {
                subject.setIsPerson(true);
                if(ageInput.getValue() != null) {
                    subject.setAge(DateUtils.asUtilDate(ageInput.getValue()));
                }
                if(weightInput.getText() != null && !weightInput.getText().isEmpty()) {
                    subject.setWeight(Double.valueOf(weightInput.getText()));
                }
                if(heightInput.getText() != null && !heightInput.getText().isEmpty()) {
                    subject.setHeight(Double.valueOf(heightInput.getText()));
                }
                subject.setSex(gender.getSelectedToggle() == isMale);   // true = male, false = female
            } else {
                subject.setIsPerson(false);
            }

            subjectService.saveOrUpdate(subject);

            showAlertAndWait(Alert.AlertType.INFORMATION, language.get(Lang.SUCCESSFULLY_SAVED));

            VistaNavigator.closeModal(titledPane);
            if (VistaNavigator.getController() != null) {
                VistaNavigator.getController().reload(subject);
            }

            logger.info("Saved: " + subject);

        } catch (NumberFormatException ex) {
            logger.warn("Invalid input number (age, weight or size)");
            showAlert(Alert.AlertType.ERROR, language.get(Lang.INVALID_INPUT_NUMBER));
        } catch (ControlledTemperatusException ex) {
            logger.warn("Exception: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, language.get(Lang.DUPLICATE_ENTRY));
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, language.get(Lang.UNKNOWN_ERROR));
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Lang.NEW_SUBJECT));
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
        nameLabel.setText(language.get(Lang.NAME_LABEL));
        observationsLabel.setText(language.get(Lang.OBSERVATIONS_LABEL));
        nameInput.setPromptText(language.get(Lang.NAME_PROMPT));
        observationsInput.setPromptText(language.get(Lang.OBSERVATIONS_PROMPT));
        ageLabel.setText(language.get(Lang.AGE_LABEL));
        ageInput.setPromptText(language.get(Lang.AGE_PROMPT));
        weightLabel.setText(language.get(Lang.WEIGHT_LABEL));
        weightInput.setPromptText(language.get(Lang.WEIGHT_PROMPT));
        sizeLabel.setText(language.get(Lang.HEIGHT_LABEL));
        heightInput.setPromptText(language.get(Lang.SIZE_PROMPT));
        isPerson.setText(language.get(Lang.ISPERSON));
        isObject.setText(language.get(Lang.ISOBJECT));
        isMale.setText(language.get(Lang.ISMALE));
        isFemale.setText(language.get(Lang.ISFEMALE));
    }

}
