package temperatus.controller.creation;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.FormulaUtil;
import temperatus.calculator.Calculator;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Position;
import temperatus.model.service.FormulaService;
import temperatus.model.service.PositionService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 26/1/16.
 */
@Controller
@Scope("prototype")
public class NewFormulaController extends AbstractCreationController implements Initializable {

    @FXML Label nameLabel;
    @FXML TextField nameInput;
    @FXML TextField referenceInput;

    @FXML TextArea operationArea;

    @FXML ListView<Position> positionsSelector;

    @Autowired PositionService positionService;
    @Autowired FormulaService formulaService;

    private Formula formula;

    static Logger logger = LoggerFactory.getLogger(NewFormulaController.class.getName());

    private final String plus = "+";
    private final String minus = "-";
    private final String mult = "*";
    private final String div = "/";
    private final String left = "(";
    private final String right = ")";

    private SimpleStringProperty operation = new SimpleStringProperty("");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formula = null;

        positionsSelector.getItems().addAll(positionService.getAll());
        addListenerToList();

        operationArea.textProperty().bind(operation);

        operationArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() != KeyCode.BACK_SPACE) {

                } else {
                    String value = operation.getValue();
                    if (value.length() > 0) {
                        operation.set(value.substring(0, value.length() - 1));
                    }
                }
            }
        });

        translate();
    }

    public void setFormulaForUpdate(Formula formula) {
        saveButton.setText(language.get(Constants.UPDATE));
        this.formula = formula;
        nameInput.setText(formula.getName());
        referenceInput.setText(formula.getReference());
        operation.set(formula.getOperation());
    }

    private void addListenerToList() {
        positionsSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Position>() {
            @Override
            public void changed(ObservableValue<? extends Position> observable, Position oldValue, Position newValue) {
                if (isValidToAddThisOperand(newValue)) {
                    operation.set(operation.getValue() + newValue.getPlace());
                }
                //positionsSelector.getSelectionModel().clearSelection();
            }
        });
    }

    private boolean isValidToAddThisOperand(Position position) {

        if (operation.length().get() < 1) {
            return true;
        }

        String lastChar = operation.getValue().substring(operation.length().get() - 1);
        if (plus.equals(lastChar) || minus.equals(lastChar) || mult.equals(lastChar) || div.equals(lastChar) || left.equals(lastChar)) {
            return true;
        }

        return false;
    }

    private boolean isValidToAddThisOperation() {

        if (operation.length().get() < 1) {
            return false;
        }

        String lastChar = operation.getValue().substring(operation.length().get() - 1);
        if (plus.equals(lastChar) || minus.equals(lastChar) || mult.equals(lastChar) || div.equals(lastChar) || left.equals(lastChar)) {
            return false;
        }

        return true;
    }

    private boolean isValidToAddLeftBracket() {

        if (operation.length().get() == 0 || operation.getValue().substring(operation.length().get() - 1).equals(left)) {
            return true;
        }

        return isValidToAddThisOperand(null);
    }

    private boolean isValidToAddRightBracket() {


        return true;
    }

    @FXML
    private void plusOperation() {
        if (isValidToAddThisOperation()) {
            operation.set(operation.getValue() + plus);
        }
    }

    @FXML
    private void minusOperation() {
        if (isValidToAddThisOperation()) {
            operation.set(operation.getValue() + minus);
        }
    }

    @FXML
    private void multOperation() {
        if (isValidToAddThisOperation()) {
            operation.set(operation.getValue() + mult);
        }
    }

    @FXML
    private void divOperation() {
        if (isValidToAddThisOperation()) {
            operation.set(operation.getValue() + div);
        }
    }

    @FXML
    private void leftOperation() {
        if (isValidToAddLeftBracket()) {
            operation.set(operation.getValue() + left);
        }
    }

    @FXML
    private void rightOperation() {
        if (isValidToAddRightBracket()) {
            operation.set(operation.getValue() + right);
        }
    }

    private boolean isValidFormula() {

        String op = operation.getValue();

        String[] elements = op.split(FormulaUtil.formulaRegex);

        for (int i = 0; i < elements.length; i++) {
            if (!FormulaUtil.isOperator(elements[i])) {
                for(Position position: positionsSelector.getItems()) {
                    if(elements[i].equals(position.getPlace())) {
                        elements[i] = "1";
                        break;
                    }
                }
            }
        }

        String toEval = FormulaUtil.generateFormula(elements);

        try {
            double result = Calculator.eval(toEval);
            return true;
        } catch (Exception ex) {
            return false;
        }

    }


    @Override
    @FXML
    protected void save() {

        if (!isValidFormula()) {
            showAlert(Alert.AlertType.ERROR, "Formula is not correct, please check.");
        } else {
            String name;

            try {
                logger.info("Saving formula...");

                name = nameInput.getText();

                if (formula == null) {
                    formula = new Formula();
                }

                formula.setName(name);
                formula.setOperation(operation.getValue());
                formula.setReference(referenceInput.getText());

                formulaService.saveOrUpdate(formula);

                VistaNavigator.closeModal(titledPane);
                if (VistaNavigator.getController() != null) {
                    VistaNavigator.getController().reload(formula);
                }

                logger.info("Saved" + formula);

            } catch (ControlledTemperatusException ex) {
                logger.warn("Exception: " + ex.getMessage());
                showAlert(Alert.AlertType.ERROR, ex.getMessage());
            } catch (ConstraintViolationException ex) {
                logger.warn("Duplicate entry");
                showAlert(Alert.AlertType.ERROR, "Duplicate Formula.");
            } catch (Exception ex) {
                logger.warn("Unknown exception" + ex.getMessage());
                showAlert(Alert.AlertType.ERROR, "Unknown error.");
            }
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.NEWFORMULA));
        saveButton.setText(language.get(Constants.SAVE));
        cancelButton.setText(language.get(Constants.CANCEL));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        nameInput.setPromptText(language.get(Constants.NAMEPROMPT));
    }

}
