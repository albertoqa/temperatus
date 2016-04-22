package temperatus.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.model.pojo.Measurement;
import temperatus.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Operations to perform over a list of measurements to check if the data contained is valid
 * <p>
 * Created by alberto on 7/2/16.
 */
public class IButtonDataValidator {

    private static final Integer RANGE = Integer.valueOf(Constants.prefs.get(Constants.PREFRANGE, Constants.DEFAULT_RANGE));

    private static Logger logger = LoggerFactory.getLogger(IButtonDataValidator.class.getName());

    /**
     * Return all measurements found that differ in +- RANGE from the average
     *
     * @param measurements measurements to compare
     * @return measurements found that appear to be incorrect
     */
    public static List<Measurement> getAllOutliers(final List<Measurement> measurements) {
        logger.debug("Generating list of outliers using a range of: [+-" + RANGE + "]");

        List<Measurement> outliers = new ArrayList<>();
        Double average = IButtonDataAnalysis.getAverage(measurements);
        Double maxValue = average + RANGE;
        Double minValue = average - RANGE;

        for (Measurement measurement : measurements) {
            Double data = measurement.getData();
            if (data < minValue || data > maxValue) {
                outliers.add(measurement);
            }
        }
        return outliers;
    }


}
