package temperatus.analysis;

import temperatus.model.pojo.Measurement;
import temperatus.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alberto on 7/2/16.
 */
public class IButtonDataValidator {

    private static final Integer RANGE = Integer.valueOf(Constants.prefs.get(Constants.PREFRANGE, Constants.DEFAULT_RANGE));

    /**
     * Return all measurements found that differ in +- RANGE from the average of its list
     * @param measurementsLists
     * @return
     */
    public static List<Measurement> getAllOutliers(final List<List<Measurement>> measurementsLists) {
        List<Measurement> outliers = new ArrayList<>();

        for(List<Measurement> measurements: measurementsLists) {
            outliers.addAll(getOutliers(measurements));
        }

        return outliers;
    }

    /**
     * Return measurements found that differ in +- RANGE from the average
     * @param measurements
     * @return
     */
    private static List<Measurement> getOutliers(final List<Measurement> measurements) {
        List<Measurement> outliers = new ArrayList<>();
        Integer average = getAverage(measurements);
        Integer maxValue = average + RANGE;
        Integer minValue = average - RANGE;

        for(Measurement measurement: measurements) {
            Integer data = measurement.getData();

            if(data < minValue || data > maxValue) {
                outliers.add(measurement);
            }
        }

        return outliers;
    }

    /**
     * Return the average value of the list of measurements
     * @param measurements
     * @return
     */
    private static Integer getAverage(final List<Measurement> measurements) {
        Integer sum = 0;
        if(!measurements.isEmpty()) {
            for(Measurement measurement: measurements) {
                sum += measurement.getData();
            }
            return sum / measurements.size();
        }
        return sum;
    }

}
