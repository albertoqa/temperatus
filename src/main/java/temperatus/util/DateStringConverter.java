package temperatus.util;

import javafx.util.StringConverter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alberto on 4/4/16.
 */
public class DateStringConverter extends StringConverter<Number> {

    @Override
    public String toString(Number number) {
        Date date = new Date(number.longValue());
        SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm:ss");
        return localDateFormat.format(date);
    }

    @Override
    public Number fromString(String s) {
        return 0;
    }

}