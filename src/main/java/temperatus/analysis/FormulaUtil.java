package temperatus.analysis;

/**
 * Created by alberto on 14/4/16.
 */
public class FormulaUtil {

    public static final String formulaRegex = "((?<=\\+)|(?=\\+))|((?<=\\-)|(?=\\-))|((?<=\\*)|(?=\\*))|((?<=\\/)|(?=\\/))|((?<=\\())|(?=\\()|(?<=\\))|(?=\\))";

    public static boolean isOperator(String o) {
        if (o.equals("+") || o.equals("-") || o.equals("*") || o.equals("/") || o.equals("(") || o.equals(")")) {
            return true;
        }
        return false;
    }

    public static String generateFormula(String[] elements) {
        String toEval = "";
        for(String operator: elements) {
            toEval = toEval.concat(operator);
        }
        return toEval;
    }

}
