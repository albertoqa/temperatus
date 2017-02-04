package temperatus.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.calculator.Calculator;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;
import temperatus.model.pojo.types.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility functions to operate list of measurements and formulas
 * <p>
 * Created by alberto on 7/2/16.
 */
public class IButtonDataAnalysis {

    private static Logger logger = LoggerFactory.getLogger(IButtonDataAnalysis.class.getName());

    private IButtonDataAnalysis() {
    }

    /**
     * Generate a list of measurements from a given list and period
     * each generated measurement is the average of X adjacent measurements
     * <p>
     * For example if: M1[10º], M2[12º], M3[10º], M4[5º]  -  PERIOD = 2
     * then the generated list will contain: M1[11º], M2[7.5º]
     *
     * @param measurementList complete list of measurements
     * @param periodInput     generate a measurement withe the average temperature of each <<period>> measurements
     * @return the list of generated measurements
     */
    public static List<Measurement> getListOfMeasurementsForPeriod(List<Measurement> measurementList, int periodInput, boolean separateWithTags) {
        logger.debug("Generating average measurements for period " + periodInput);

        List<Measurement> toExport = new ArrayList<>();
        Collections.sort(measurementList, (a, b) -> a.getDate().compareTo(b.getDate()));    // sort list by date

        int period = periodInput;
        if (period <= 0) {  // empty list
            return toExport;
        } else if (period == 1) {  // same list as input
            return measurementList;
        } else if (period > measurementList.size()) {    // max value is the size of the input list
            period = measurementList.size() - 1;
        }

        int index = 0;
        Measurement measurementToExport = null;
        for (Measurement measurement : measurementList) {
            if (index % period == 0) {
                if (measurementToExport != null) {  // this prevent to save in the first iteration
                    measurementToExport.setData(measurementToExport.getData() / period);
                    toExport.add(measurementToExport);
                }
                measurementToExport = new Measurement();
                measurementToExport.setData(0);
                measurementToExport.setDate(measurement.getDate()); // the date will be the date of the first measurement of the group
            }
            if (measurementToExport != null) {
                measurementToExport.setData(measurementToExport.getData() + measurement.getData());
            }
            index++;
        }

        // check if last group is saved
        if (!toExport.contains(measurementToExport)) {
            int div = ((index - 1) % period) + 1;
            if (div > 0) {
                if (measurementToExport != null) {
                    measurementToExport.setData(measurementToExport.getData() / div);
                    toExport.add(measurementToExport);
                }
            }
        }

        return toExport;
    }

    /**
     * Generate a list of measurements in which each measurement is calculated performing the operation of the formula
     * and making groups of measurements and calculating its average value
     *
     * @param dataMap all records-measurements of the mission
     * @param formula operation to perform
     * @param period  number of measurements of each group
     * @return list of measurements in groups and with the operation performed
     */
    public static List<Measurement> getListOfMeasurementsForFormulaAndPeriod(HashMap<Record, List<Measurement>> dataMap, Formula formula, int period, boolean separateWithTags) {
        logger.debug("Performing formula [" + formula.getOperation() + "] and average measurements with period " + period);

        String operation = formula.getOperation();
        List<Measurement> measurements = getListOfMeasurementsForPeriod(dataMap.values().iterator().next(), period, separateWithTags).stream().map(measurement -> new Measurement(measurement.getDate(), 0.0, measurement.getUnit())).collect(Collectors.toList());

        // Split operation in all its elements and save it one time for each group of measurements
        List<String[]> operations = new ArrayList<>();
        for (int i = 0; i < measurements.size(); i++) {
            operations.add(operation.split(FormulaUtil.FORMULA_REGEX));
        }

        // for each record check if its position is part of the operation, if yes - replace the position name for its temperature value
        for (Record record : dataMap.keySet()) {
            String position = record.getPosition().getPlace();
            if (operation.contains(position)) {
                List<Measurement> recordMeasurements = getListOfMeasurementsForPeriod(dataMap.get(record), period, separateWithTags);
                int index = 0;
                for (Measurement measurement : recordMeasurements) {
                    String[] elements = operations.get(index);

                    for (int i = 0; i < elements.length; i++) {
                        if (elements[i].equals(position)) {
                            elements[i] = String.valueOf(measurement.getData());
                        }
                    }

                    operations.set(index, elements);
                    index++;
                }
            }
        }

        // calculate the result of each operation
        for (int i = 0; i < operations.size(); i++) {
            try {
                double result = Calculator.eval(FormulaUtil.generateFormula(operations.get(i)));
                measurements.get(i).setData(result);
            } catch (ControlledTemperatusException ex) {
                logger.debug("Cannot perform operation: " + FormulaUtil.generateFormula(operations.get(i)) + " " + ex.getMessage());
                measurements.get(i).setData(Double.NaN);
            }
        }

        return measurements;
    }

    /**
     * Return the average value of the list of measurements
     *
     * @param measurements list of measurements to calculate its average
     * @return average temperature of the list
     */
    public static Double getAverage(final List<Measurement> measurements) {
        logger.debug("Calculating average temperature");

        Double sum = 0.0;
        if (!measurements.isEmpty()) {
            for (Measurement measurement : measurements) {
                sum += measurement.getData();
            }
            return sum / measurements.size();
        }
        return sum;
    }

    /**
     * Return the maximum temperature registered in the list of measurements
     *
     * @param measurements list to analyze
     * @return maximum temperature
     */
    public static Double getMaxTemperature(final List<Measurement> measurements) {
        double maxTemp = Double.MIN_VALUE;
        if (!measurements.isEmpty()) {
            for (Measurement measurement : measurements) {
                if (measurement.getData() > maxTemp) {
                    maxTemp = measurement.getData();
                }
            }
        }
        return maxTemp;
    }

    /**
     * Return the minimum temperature registered in the list of measurements
     *
     * @param measurements list to analyze
     * @return minimum temperature
     */
    public static Double getMinTemperature(final List<Measurement> measurements) {
        double maxTemp = Double.MAX_VALUE;
        if (!measurements.isEmpty()) {
            for (Measurement measurement : measurements) {
                if (measurement.getData() < maxTemp) {
                    maxTemp = measurement.getData();
                }
            }
        }
        return maxTemp;
    }

    /**
     * Generate a histogram of temperatures.
     *
     * @param measurementsLists measurements to categorize
     * @param max               maximum temperature
     * @param min               minimum temperature
     * @param numBins           number of bins to generate
     * @param unit              unit of measure
     * @return histogram of temperatures
     */
    public static int[] calcHistogram(List<List<Measurement>> measurementsLists, double min, double max, int numBins, Unit unit) {
        final int[] result = new int[numBins];
        final double binSize;

        if (unit.equals(Unit.C)) {
            binSize = (max - min) / numBins;
            for (List<Measurement> measurementsList : measurementsLists) {
                for (Measurement measurement : measurementsList) {
                    int bin = (int) ((measurement.getData() - min) / binSize);
                    if (bin >= 0 && bin < numBins) {
                        result[bin] += 1;
                    }
                }
            }
        } else {
            binSize = (Calculator.celsiusToFahrenheit(max) - Calculator.celsiusToFahrenheit(min)) / numBins;
            for (List<Measurement> measurementsList : measurementsLists) {
                for (Measurement measurement : measurementsList) {
                    int bin = (int) ((Calculator.celsiusToFahrenheit(measurement.getData()) - Calculator.celsiusToFahrenheit(min)) / binSize);
                    if (bin >= 0 && bin < numBins) {
                        result[bin] += 1;
                    }
                }
            }
        }

        return result;
    }
}



