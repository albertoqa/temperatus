package temperatus.util;

import javafx.util.StringConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alberto on 4/4/16.
 */
public class DateStringConverter extends StringConverter<Number> {

    boolean slider;

    public DateStringConverter(boolean slider) {
        this.slider = slider;
    }

    @Override
    public String toString(Number number) {
        Date date = new Date(number.longValue());
        SimpleDateFormat localDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        SimpleDateFormat localTimeFormat = new SimpleDateFormat("HH:mm:ss");

        if(slider) {
            return localTimeFormat.format(date);
        } else {
            return localDateFormat.format(date);
        }
    }

    @Override
    public Number fromString(String s) {
        SimpleDateFormat localDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date d = null;
        try {
            d = localDateFormat.parse(s);
        } catch (ParseException e) {
        }

        long a = 0;

        if(d != null) {
            a = d.getTime();
        }

        return a;
    }

}