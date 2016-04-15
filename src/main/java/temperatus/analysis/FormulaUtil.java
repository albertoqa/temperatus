package temperatus.analysis;

/**
 * Utility functions to parse and generate formulas
 *
 * Created by alberto on 14/4/16.
 */
public class FormulaUtil {

    // Regex used to split a formula in all its operands/operators - split by +-*/()
    public static final String formulaRegex = "((?<=\\+)|(?=\\+))|((?<=\\-)|(?=\\-))|((?<=\\*)|(?=\\*))|((?<=\\/)|(?=\\/))|((?<=\\())|(?=\\()|(?<=\\))|(?=\\))";

    /**
     * Check if a given string is a operator or a operand
     * @param o string to check
     * @return is operator?
     */
    public static boolean isOperator(String o) {
        return o.equals("+") || o.equals("-") || o.equals("*") || o.equals("/") || o.equals("(") || o.equals(")");
    }

    /**
     * Generate a formula in a unique string
     * @param elements elements of the formula
     * @return generated formula
     */
    public static String generateFormula(String[] elements) {
        String toEval = "";
        for(String operator: elements) {
            toEval = toEval.concat(operator);
        }
        return toEval;
    }

}
