package temperatus.analysis;

import temperatus.calculator.Calculator;
import temperatus.model.pojo.Position;

import java.util.List;

/**
 * Utility functions to parse and generate formulas
 * <p>
 * Created by alberto on 14/4/16.
 */
public class FormulaUtil {

    // Regex used to split a formula in all its operands/operators - split by +-*/()
    static final String FORMULA_REGEX = "((?<=\\+)|(?=\\+))|((?<=\\-)|(?=\\-))|((?<=\\*)|(?=\\*))|((?<=\\/)|(?=\\/))|((?<=\\())|(?=\\()|(?<=\\))|(?=\\))";

    private FormulaUtil() {
    }

    /**
     * Check if a given string is a operator or a operand
     *
     * @param o string to check
     * @return is operator?
     */
    private static boolean isOperator(String o) {
        return "+".equals(o) || "-".equals(o) || "*".equals(o) || "/".equals(o) || "(".equals(o) || ")".equals(o);
    }

    /**
     * Generate a formula in a unique string
     *
     * @param elements elements of the formula
     * @return generated formula
     */
    static String generateFormula(String[] elements) {
        String toEval = "";
        for (String operator : elements) {
            toEval = toEval.concat(operator);
        }
        return toEval;
    }

    /**
     * Check if the formula is valid to apply to the passed positions
     *
     * @param operation formula to validate
     * @param positions positions to check
     * @return is the formula valid?
     */
    public static boolean isValidFormula(final String operation, final List<Position> positions) {

        String[] elements = operation.split(FORMULA_REGEX);    // split formula in all its elements

        // replace all operands (positions) for 1
        for (int i = 0; i < elements.length; i++) {
            if (!isOperator(elements[i])) {
                for (Position position : positions) {
                    if (elements[i].equals(position.getPlace())) {
                        elements[i] = "1";
                        break;
                    }
                }
            }
        }

        // reconstruct the formula from its elements (with all the operands (positions) replaced by 1s
        String toEval = generateFormula(elements);

        // try to perform the operation
        try {
            Calculator.eval(toEval);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
