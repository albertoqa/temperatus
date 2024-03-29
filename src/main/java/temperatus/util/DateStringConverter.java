package temperatus.util;

import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

/**
 * Convert long to Date and format with custom format as String
 * Used for the Slider and TextInput binding of RecordConfigController
 * <p>
 * Created by alberto on 4/4/16.
 */
public class DateStringConverter extends StringConverter<Number> {

    private boolean slider;     // is this being applied to a slider or to another element?

    private static Logger logger = LoggerFactory.getLogger(DateStringConverter.class.getName());

    public DateStringConverter(boolean slider) {
        this.slider = slider;
    }

    /**
     * If slider     -> show Date Time
     * If TextInput  -> show Time
     */
    @Override
    public String toString(Number number) {
        if (slider) {
            return Constants.timeFormat.format(new Date(number.longValue()));
        } else {
            return Constants.dateTimeFormat.format(new Date(number.longValue()));
        }
    }

    @Override
    public Number fromString(String s) {
        try {
            Date date = Constants.dateTimeFormat.parse(s);
            return date.getTime();
        } catch (ParseException e) {
            logger.error("Cannot parse String to Date");
            return 0;
        }
    }

}