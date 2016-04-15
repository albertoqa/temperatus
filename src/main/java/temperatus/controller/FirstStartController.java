package temperatus.controller;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * Created by alberto on 12/2/16.
 */
@Controller
public class FirstStartController {

    private static Logger logger = LoggerFactory.getLogger(FirstStartController.class.getName());

    private Wizard wizard;

    private TextField createTextField(String id) {
        TextField textField = new TextField();
        textField.setId(id);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        return textField;
    }

    public void startWizard() {
        logger.info("Starting wizard...");

        wizard = new Wizard();

        int row = 0;

        GridPane page1Grid = new GridPane();
        page1Grid.setVgap(10);
        page1Grid.setHgap(10);

        page1Grid.add(new Label("First Name:"), 0, row);
        TextField txFirstName = createTextField("firstName");

        //wizard.getValidationSupport().registerValidator(txFirstName, Validator.createEmptyValidator("First Name is mandatory"));
        page1Grid.add(txFirstName, 1, row++);

        page1Grid.add(new Label("Last Name:"), 0, row);
        TextField txLastName = createTextField("lastName");
        //wizard.getValidationSupport().registerValidator(txLastName, Validator.createEmptyValidator("Last Name is mandatory"));
        page1Grid.add(txLastName, 1, row);

        WizardPane page1 = new WizardPane();
        page1.setHeaderText("Please Enter Your Details");
        page1.setContent(page1Grid);

        WizardPane wizardPane1 = new WizardPane();

        wizard.setFlow(new Wizard.LinearFlow(page1, wizardPane1));

        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                System.out.println("Wizard finished, settings: " + wizard.getSettings());
            }
        });
    }

}
