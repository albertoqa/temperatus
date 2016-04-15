package temperatus.util;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.function.UnaryOperator;

/**
 * Set ValueFactory to given Spinner so it only accepts Integers
 * <p>
 * Created by alberto on 14/4/16.
 */
public class IntegerSpinner {

    private static Logger logger = LoggerFactory.getLogger(IntegerSpinner.class.getName());

    public static void setSpinner(Spinner<Integer> spinner) {
        // get a localized format for parsing
        NumberFormat format = NumberFormat.getIntegerInstance();

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
                    c.setText("1");
                }
            }
            return c;
        };

        TextFormatter<Integer> periodFormatter = new TextFormatter<Integer>(new IntegerStringConverter(), 1, filter);

        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 1));   // between 1-1000 and first value 1
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(periodFormatter);
    }

}
