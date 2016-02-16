package temperatus.controller.manage.ampliate;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Subject;
import temperatus.util.VistaNavigator;

/**
 * Created by alberto on 16/2/16.
 */
@Controller
@Scope("prototype")
public class SubjectInfoController implements AbstractController {

    @FXML private Label subjectLabel;
    @FXML private Label nameLabel;
    @FXML private Button backButton;

    private Subject subject;

    public void setSubject(Subject subject) {
        this.subject = subject;
        loadData();
    }

    private void loadData() {
        nameLabel.setText(subject.getName());
    }

    @FXML
    private void back() {
        VistaNavigator.popViewFromStack();
    }

    @Override
    public void translate() {

    }


}
