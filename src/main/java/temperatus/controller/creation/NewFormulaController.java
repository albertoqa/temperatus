package temperatus.controller.creation;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.FormulaUtil;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Position;
import temperatus.model.service.FormulaService;
import temperatus.model.service.PositionService;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.List;
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

    private static final String PLUS = "+";
    private static final String MINUS = "-";
    private static final String MULT = "*";
    private static final String DIV = "/";
    private static final String LEFT = "(";
    private static final String RIGHT = ")";

    private static final String DECIMAL_REGEX = "[0-9.]";

    //private SimpleStringProperty operation = new SimpleStringProperty("");  // contains the current formula created

    private Formula formula;

    private static Logger logger = LoggerFactory.getLogger(NewFormulaController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
        formula = null;

        addListenerToList();    // if a position is selected try to add it to the formula

        //operationArea.textProperty().bind(operation);

        // user is not allowed to insert characters directly on the operation, only delete them
        // if character is a number or a . allow it
        /*operationArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
                String value = operation.getValue();
                if (value.length() > 0) {
                    // if last char is an operator, delete it
                    if (isLastCharAnOperator() || value.substring(value.length() - 1).equals(RIGHT)) {
                        operation.set(value.substring(0, value.length() - 1));
                    }
                    // if it is not an operator, delete char until we found an operator or the operation is empty
                    else {
                        while (value.length() > 0 && !isLastCharAnOperator()) {
                            operation.set(operation.getValue().substring(0, operation.getValue().length() - 1));
                        }
                    }
                }
            } else if (keyEvent.getText().matches(DECIMAL_REGEX)) {
                String value = keyEvent.getText();
                operation.set(operation.getValue() + value);
            }
            keyEvent.consume();
        });*/

        getAllElements();
    }

    /**
     * Fetch all Positions from database and add it to the table.
     * Use a different thread than the UI thread.
     */
    private void getAllElements() {
        Task<List<Position>> getPositionsTask = new Task<List<Position>>() {
            @Override
            public List<Position> call() throws Exception {
                return positionService.getAll();
            }
        };

        // on task completion add all positions to the table
        getPositionsTask.setOnSucceeded(e -> positionsSelector.getItems().addAll(getPositionsTask.getValue()));

        // run the task using a thread from the thread pool:
        databaseExecutor.submit(getPositionsTask);
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
        //operation.set(formula.getOperation());
        operationArea.setText(formula.getOperation());
    }

    /**
     * When user select a position from the list, try to add it to the current operation
     * Before add it, check if is valid to add a position at this point of the formula
     */
    private void addListenerToList() {
        positionsSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (isValidToAddAnOperand()) {
                    operationArea.setText(operationArea.getText() + newValue.getPlace());
                }
            }
        });
    }

    /**
     * @return is valid to add an operand to the formula RIGHT now?
     */
    private boolean isValidToAddAnOperand() {
        return operationArea.getText().length() < 1 || isLastCharAnOperator();
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
        if (operationArea.getText().length() > 0) {
            String lastChar = operationArea.getText().substring(operationArea.getText().length() - 1);
            return PLUS.equals(lastChar) || MINUS.equals(lastChar) || MULT.equals(lastChar) || DIV.equals(lastChar) || LEFT.equals(lastChar);
        } else {
            return false;
        }
    }

    /**
     * @return is valid to add a "(" LEFT bracket to the formula RIGHT now?
     */
    private boolean isValidToAddLeftBracket() {
        return operationArea.getText().length() == 0 || operationArea.getText().substring(operationArea.getText().length() - 1).equals(LEFT) || isValidToAddAnOperand();
    }

    /**
     * Check if is valid to add an operator, add it to the formula and clear the selection
     *
     * @param operator operator to add
     */
    private void operator(String operator) {
        if (isValidToAddAnOperator()) {
            operationArea.setText(operationArea.getText() + operator);
        }
        positionsSelector.getSelectionModel().clearSelection();
    }

    /**
     * Add a plus operator
     */
    @FXML
    private void plusOperation() {
        operator(PLUS);
    }

    /**
     * Add a minus operator
     */
    @FXML
    private void minusOperation() {
        operator(MINUS);
    }

    /**
     * Add a multiplication operator
     */
    @FXML
    private void multOperation() {
        operator(MULT);
    }

    /**
     * Add a division operator
     */
    @FXML
    private void divOperation() {
        operator(DIV);
    }

    /**
     * Add a left bracket (only if allowed)
     */
    @FXML
    private void leftOperation() {
        if (isValidToAddLeftBracket()) {
            operationArea.setText(operationArea.getText() + LEFT);
        }
        positionsSelector.getSelectionModel().clearSelection();
    }

    /**
     * Add a right bracket
     */
    @FXML
    private void rightOperation() {
        operationArea.setText(operationArea.getText() + RIGHT);
        positionsSelector.getSelectionModel().clearSelection();
    }

    /**
     * Save or update a formula to the db
     */
    @Override
    @FXML
    protected void save() {

        String operation = operationArea.getText();
        operation = operation.replace(",", ".");

        if (!FormulaUtil.isValidFormula(operation, positionsSelector.getItems())) {
            showAlert(Alert.AlertType.ERROR, language.get(Lang.INVALID_FORMULA));
        } else {
            try {
                logger.info("Saving formula...");

                // no update, new formula
                if (formula == null) {
                    formula = new Formula();
                }

                formula.setName(nameInput.getText());
                formula.setOperation(operation);
                formula.setReference(referenceInput.getText());

                formulaService.saveOrUpdate(formula);

                showAlertAndWait(Alert.AlertType.INFORMATION, language.get(Lang.SUCCESSFULLY_SAVED));

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
        titledPane.setText(language.get(Lang.NEW_FORMULA_BUTTON));
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
        nameLabel.setText(language.get(Lang.NAME_LABEL));
        nameInput.setPromptText(language.get(Lang.NAME_PROMPT));
        referenceLabel.setText(language.get(Lang.FORMULA_REFERENCE_LABEL));
        referenceInput.setPromptText(language.get(Lang.FORMULA_REFERENCE_PROMPT));
        operationLabel.setText(language.get(Lang.FORMULA_OPERATION_LABEL));
        operationArea.setPromptText(language.get(Lang.FORMULA_OPERATION_PROMPT));
    }

}
