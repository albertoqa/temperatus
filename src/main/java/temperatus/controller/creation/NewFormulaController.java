package temperatus.controller.creation;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.FormulaUtil;
import temperatus.calculator.Calculator;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Position;
import temperatus.model.service.FormulaService;
import temperatus.model.service.PositionService;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * View to create/update a formula. A formula can contain any of this operators: + - * / ( )
 * <p>
 * Created by alberto on 26/1/16.
 */
@Controller
@Scope("prototype")
public class NewFormulaController extends AbstractCreationController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private Label referenceLabel;
    @FXML private Label operationLabel;

    @FXML private TextField nameInput;
    @FXML private TextField referenceInput;
    @FXML private TextArea operationArea;

    @FXML private ListView<Position> positionsSelector; // list of all possible positions

    @Autowired PositionService positionService;
    @Autowired FormulaService formulaService;

    private static Logger logger = LoggerFactory.getLogger(NewFormulaController.class.getName());

    private static final String PLUS = "+";
    private static final String MINUS = "-";
    private static final String MULT = "*";
    private static final String DIV = "/";
    private static final String LEFT = "(";
    private static final String RIGHT = ")";

    private SimpleStringProperty operation = new SimpleStringProperty("");  // contains the actual formula created

    private Formula formula;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formula = null;

        positionsSelector.getItems().addAll(positionService.getAll());
        addListenerToList();    // if a position is selected try to add it to the formula

        operationArea.textProperty().bind(operation);

        // user is not allowed to insert characters directly on the operation, only delete them
        operationArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
                String value = operation.getValue();
                if (value.length() > 0) {
                    operation.set(value.substring(0, value.length() - 1));
                }
            }
            keyEvent.consume();
        });

        translate();
    }

    /**
     * When editing a formula, pre-load its data
     *
     * @param formula formula to update
     */
    public void setFormulaForUpdate(Formula formula) {
        saveButton.setText(language.get(Lang.UPDATE));  // change save button text to update
        this.formula = formula;
        nameInput.setText(formula.getName());
        referenceInput.setText(formula.getReference());
        operation.set(formula.getOperation());
    }

    /**
     * When user select a position from the list, try to add it to the current operation
     */
    private void addListenerToList() {
        positionsSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (isValidToAddAnOperand()) {
                    operation.set(operation.getValue() + newValue.getPlace());
                }
            }
        });
    }

    /**
     * @return is valid to add an operand to the formula RIGHT now?
     */
    private boolean isValidToAddAnOperand() {
        return operation.length().get() < 1 || isLastCharAnOperator();
    }

    /**
     * @return is valid to add an operator to the formula RIGHT now?
     */
    private boolean isValidToAddAnOperator() {
        return !isValidToAddAnOperand();
    }

    /**
     * @return the last element of the operation is an operator?
     */
    private boolean isLastCharAnOperator() {
        String lastChar = operation.getValue().substring(operation.length().get() - 1);
        return PLUS.equals(lastChar) || MINUS.equals(lastChar) || MULT.equals(lastChar) || DIV.equals(lastChar) || LEFT.equals(lastChar);
    }

    /**
     * @return is valid to add a "(" LEFT bracket to the formula RIGHT now?
     */
    private boolean isValidToAddLeftBracket() {
        return operation.length().get() == 0 || operation.getValue().substring(operation.length().get() - 1).equals(LEFT) || isValidToAddAnOperand();
    }

    /**
     * Check if is valid to add an operator, add it to the formula and clear the selection
     *
     * @param operator operator to add
     */
    private void operator(String operator) {
        if (isValidToAddAnOperator()) {
            operation.set(operation.getValue() + operator);
        }
        positionsSelector.getSelectionModel().clearSelection();
    }

    @FXML
    private void plusOperation() {
        operator(PLUS);
    }

    @FXML
    private void minusOperation() {
        operator(MINUS);
    }

    @FXML
    private void multOperation() {
        operator(MULT);
    }

    @FXML
    private void divOperation() {
        operator(DIV);
    }

    @FXML
    private void leftOperation() {
        if (isValidToAddLeftBracket()) {
            operation.set(operation.getValue() + LEFT);
        }
        positionsSelector.getSelectionModel().clearSelection();
    }

    @FXML
    private void rightOperation() {
        operation.set(operation.getValue() + RIGHT);
        positionsSelector.getSelectionModel().clearSelection();
    }

    /**
     * Check if the actual formula is valid
     *
     * @return is the formula valid?
     */
    private boolean isValidFormula() {

        String op = operation.getValue();   // current formula
        String[] elements = op.split(FormulaUtil.FORMULA_REGEX);    // split formula in all its elements

        // replace all operands (positions) for 1
        for (int i = 0; i < elements.length; i++) {
            if (!FormulaUtil.isOperator(elements[i])) {
                for (Position position : positionsSelector.getItems()) {
                    if (elements[i].equals(position.getPlace())) {
                        elements[i] = "1";
                        break;
                    }
                }
            }
        }

        // reconstruct the formula from its elements (with all the operands (positions) replaced by 1s
        String toEval = FormulaUtil.generateFormula(elements);

        // try to perform the operation
        try {
            Calculator.eval(toEval);
            return true;
        } catch (Exception ex) {
            logger.warn("Invalid formula: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Save or update a formula to the db
     */
    @Override
    @FXML
    protected void save() {

        if (!isValidFormula()) {
            showAlert(Alert.AlertType.ERROR, language.get(Lang.INVALID_FORMULA));
        } else {
            try {
                logger.info("Saving formula...");

                // no update, new formula
                if (formula == null) {
                    formula = new Formula();
                }

                formula.setName(nameInput.getText());
                formula.setOperation(operation.getValue());
                formula.setReference(referenceInput.getText());

                formulaService.saveOrUpdate(formula);

                VistaNavigator.closeModal(titledPane);
                if (VistaNavigator.getController() != null) {
                    // Only necessary if base view needs to know about the new formula creation
                    VistaNavigator.getController().reload(formula);
                }

                logger.info("Saved" + formula);

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
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Lang.NEWFORMULA));
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
        nameLabel.setText(language.get(Lang.NAMELABEL));
        nameInput.setPromptText(language.get(Lang.NAMEPROMPT));
        referenceLabel.setText(language.get(Lang.REFERENCELABEL));
        referenceInput.setPromptText(language.get(Lang.REFERENCEPROMPT));
        operationLabel.setText(language.get(Lang.OPERATIONLABEL));
        operationArea.setPromptText(language.get(Lang.OPERATIONPROMPT));
    }

}
