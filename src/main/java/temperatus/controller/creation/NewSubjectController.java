package temperatus.controller.creation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Subject;
import temperatus.model.service.SubjectService;
import temperatus.util.Animation;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 27/1/16.
 */
@Component
public class NewSubjectController extends AbstractCreation implements Initializable {

    @FXML private RadioButton isPerson;
    @FXML private RadioButton isObject;
    @FXML private RadioButton isMale;
    @FXML private RadioButton isFemale;

    @FXML private TextField nameInput;
    @FXML private TextArea observationsInput;
    @FXML private TextField ageInput;
    @FXML private TextField weightInput;
    @FXML private TextField sizeInput;

    @FXML private AnchorPane personDataPane;

    @Autowired SubjectService subjectService;

    final ToggleGroup person = new ToggleGroup();
    final ToggleGroup gender = new ToggleGroup();

    @Override @FXML
    void save() throws ControlledTemperatusException {

        Subject subject = new Subject();
        subject.setName(nameInput.getText());
        subject.setObservations(observationsInput.getText());

        if(person.getSelectedToggle() == isPerson) {
            subject.setIsPerson(true);
            subject.setAge(Integer.valueOf(ageInput.getText()));
            subject.setWeight(Double.valueOf(weightInput.getText()));
            subject.setSize(Double.valueOf(sizeInput.getText()));
            if(gender.getSelectedToggle() == isMale) {
                subject.setSex(true);
            } else {
                subject.setSex(false);
            }
        } else {
            subject.setIsPerson(false);
        }

        subjectService.save(subject);

        Animation.fadeInOutClose(titledPane);
        if(VistaNavigator.getController() != null) {
            VistaNavigator.getController().reload(subject);
        }
    }

    @Override
    void translate() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        isPerson.setToggleGroup(person);
        isObject.setToggleGroup(person);
        isMale.setToggleGroup(gender);
        isFemale.setToggleGroup(gender);

        isPerson.setSelected(true);
        isMale.setSelected(true);

        person.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (person.getSelectedToggle() == isPerson) {
                    Animation.fadeInTransition(personDataPane);
                } else {
                    Animation.fadeOutTransition(personDataPane);
                }
            }
        });

        translate();
    }
}
