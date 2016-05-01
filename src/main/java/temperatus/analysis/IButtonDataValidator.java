package temperatus.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.model.pojo.Measurement;
import temperatus.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

/**
 * Operations to perform over a list of measurements to check if the data contained is valid
 * <p>
 * Created by alberto on 7/2/16.
 */
public class IButtonDataValidator {

    private static final Integer RANGE = Integer.valueOf(Constants.prefs.get(Constants.PREF_RANGE, Constants.DEFAULT_RANGE));

    private static Logger logger = LoggerFactory.getLogger(IButtonDataValidator.class.getName());

    /**
     * Return all measurements found that differ in +- RANGE from the average
     *
     * @param measurements measurements to compare
     * @return measurements found that appear to be incorrect
     */
    public static List<Measurement> getOutliers(final List<Measurement> measurements) {
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

    /**
     * Compare measurements and return a list with all the possible errors
     *
     * @param measurements to compare
     * @return possible errors
     */
    public static List<Measurement> getAllOutliers(final List<Measurement> measurements) {
        logger.debug("Generating list of outliers");

        List<Measurement> outliers = new ArrayList<>();

        double mean = getMean(measurements);
        double stdDev = getStdDev(measurements);

        //IF abs(x-mu) > 3*std  THEN  x is outlier
        outliers.addAll(measurements.stream().filter(measurement -> abs(measurement.getData() - mean) > 3 * stdDev).collect(Collectors.toList()));

        return outliers;
    }

    /**
     * Get the mean of the measurements
     *
     * @param measurementList list of measurements
     * @return mean of the list
     */
    private static double getMean(List<Measurement> measurementList) {
        double sum = 0.0;
        for (Measurement a : measurementList)
            sum += a.getData();
        return sum / measurementList.size();
    }

    /**
     * Get the variance of the list
     *
     * @param measurementList list to calculate variace from
     * @return variance
     */
    private static double getVariance(List<Measurement> measurementList) {
        double mean = getMean(measurementList);
        double temp = 0;
        for (Measurement a : measurementList)
            temp += (mean - a.getData()) * (mean - a.getData());
        return temp / measurementList.size();
    }

    /**
     * Get the standard deviation from the list
     *
     * @param measurementList list to calculate deviation from
     * @return standard deviation
     */
    private static double getStdDev(List<Measurement> measurementList) {
        return Math.sqrt(getVariance(measurementList));
    }

}
