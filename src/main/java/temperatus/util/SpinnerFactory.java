package temperatus.util;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.function.UnaryOperator;

/**
 * Set spinner to different text formatter -> Integer, Double
 * <p>
 * Created by alberto on 25/4/16.
 */
public class SpinnerFactory {

    private static Logger logger = LoggerFactory.getLogger(SpinnerFactory.class.getName());

    /**
     * Set the spinner to a Integer spinner with range 0-10000 starting in 0
     *
     * @param spinner spinner to set to Integer
     */
    public static void setIntegerSpinner(Spinner<Integer> spinner) {
        TextFormatter<Integer> periodFormatter = new TextFormatter<>(new IntegerStringConverter(), 0, getFilter(NumberFormat.getIntegerInstance()));
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000000, 0));   // between 0-1000 and first value 0
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(periodFormatter);
    }

    /**
     * Set the spinner to a Integer spinner with range 0-10000 starting in the value passed as default
     *
     * @param spinner spinner to set to Integer
     */
    public static void setIntegerSpinner(Spinner<Integer> spinner, int defaultValue) {
        TextFormatter<Integer> periodFormatter = new TextFormatter<>(new IntegerStringConverter(), defaultValue, getFilter(NumberFormat.getIntegerInstance()));
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, defaultValue));   // between 0-1000 and first value 0
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(periodFormatter);
    }

    /**
     * Set the spinner to a Double spinner with range -10000 - 10000 starting in 0
     *
     * @param spinner spinner to set to Double
     */
    public static void setDoubleSpinner(Spinner<Double> spinner) {
        TextFormatter<Double> periodFormatter = new TextFormatter<>(setConverter(), 0.0, getFilter(Constants.decimalFormat));
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10000.0, 10000.0, 0.0, 0.5));   // between -1000-1000 and first value 0
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(periodFormatter);
    }

    /**
     * Filter the input of the spinner with the given format
     *
     * @param format format of the filter
     * @return filter to use in the spinner
     */
    private static UnaryOperator<TextFormatter.Change> getFilter(NumberFormat format) {
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                ParsePosition parsePosition = new ParsePosition(0);
                try {
                    format.parse(c.getControlNewText(), parsePosition);

                    if (parsePosition.getIndex() == 0 || parsePosition.getIndex() < c.getControlNewText().length()) {
                        // reject parsing the complete text failed
                        return null;
                    }

                } catch (NumberFormatException ex) {
                    logger.warn("Error parsing spinner value");
                    c.setText("0");
                }
            }
            return c;
        };
        return filter;
    }

    /**
     * String to Double converter
     *
     * @return the converter created for Double input
     */
    private static StringConverter<Double> setConverter() {
        return new StringConverter<Double>() {
            private final DecimalFormat df = Constants.decimalFormat;

            @Override
            public String toString(Double value) {
                // If the specified value is null, return a zero-length String
                if (value == null) {
                    return "";
                }

                return df.format(value);
            }

            @Override
            public Double fromString(String value) {
                try {
                    // If the specified value is null or zero-length, return null
                    if (value == null) {
                        return null;
                    }
                    value = value.trim();
                    if (value.length() < 1) {
                        return null;
                    }
                    // Perform the requested parsing
                    return df.parse(value).doubleValue();
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }


}
