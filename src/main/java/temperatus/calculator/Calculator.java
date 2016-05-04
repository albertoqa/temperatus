package temperatus.calculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.util.Constants;

/**
 * Evaluate a math expression given in string form
 * <p>
 * It does addition, subtraction, multiplication, division, exponentiation (using the ^ symbol),
 * and a few basic functions like sqrt. It supports grouping using (...), and it gets the operator
 * precedence and associativity rules correct.
 * <p>
 * http://stackoverflow.com/questions/3422673/evaluating-a-math-expression-given-in-string-form
 * <p>
 * Created by alberto on 12/4/16.
 */
public class Calculator {

    private static Logger logger = LoggerFactory.getLogger(Calculator.class.getName());

    public static double eval(final String str) {

        logger.debug("Evaluating string: " + str);

        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    /**
     * Convert fahrenheit to celsius
     *
     * @param fahrenheit temperature to convert in fahrenheit
     * @return temperature in celsius
     */
    public static Double fahrenheitToCelsius(Double fahrenheit) {
        return Double.valueOf(Constants.decimalFormat.format((fahrenheit - 32) * (5.0 / 9)).replace(",", "."));
    }

    /**
     * Convert celsius to fahrenheit
     *
     * @param celsius temperature to convert in celsius
     * @return temperature in fahrenheit
     */
    public static Double celsiusToFahrenheit(Double celsius) {
        return Double.valueOf(Constants.decimalFormat.format((celsius * 1.8 + 32)).replace(",", "."));
    }
}
