package temperatus.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.calculator.Calculator;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility functions to operate list of measurements and formulas
 * <p>
 * Created by alberto on 7/2/16.
 */
public class IButtonDataAnalysis {

    private static Logger logger = LoggerFactory.getLogger(IButtonDataAnalysis.class.getName());

    /**
     * Generate a list of measurements from a given list and period
     * each generated measurement is the average of X adjacent measurements
     * <p>
     * For example if: M1[10º], M2[12º], M3[10º], M4[5º]  -  PERIOD = 2
     * then the generated list will contain: M1[11º], M2[7.5º]
     *
     * @param measurementList complete list of measurements
     * @param period          generate a measurement withe the average temperature of each <<period>> measurements
     * @return the list of generated measurements
     */
    public static List<Measurement> getListOfMeasurementsForPeriod(List<Measurement> measurementList, int period) {
        logger.debug("Generating average measurements for period " + period);

        List<Measurement> toExport = new ArrayList<>();
        Collections.sort(measurementList, (a, b) -> a.getDate().compareTo(b.getDate()));    // sort list by date

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
            measurementToExport.setData(measurementToExport.getData() + measurement.getData());
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
     * @param records all records of the mission
     * @param formula operation to perform
     * @param period  number of measurements of each group
     * @return list of measurements in groups and with the operation performed
     */
    public static List<Measurement> getListOfMeasurementsForFormulaAndPeriod(List<Record> records, Formula formula, int period) {
        logger.debug("Performing formula [" + formula.getOperation() + "] and average measurements with period " + period);

        String operation = formula.getOperation();
        List<Measurement> measurements = getListOfMeasurementsForPeriod(new ArrayList<>(records.get(0).getMeasurements()), period).stream().map(measurement -> new Measurement(measurement.getDate(), 0.0, measurement.getUnit())).collect(Collectors.toList());

        // Split operation in all its elements and save it one time for each group of measurements
        List<String[]> operations = new ArrayList<>();
        for (int i = 0; i < measurements.size(); i++) {
            operations.add(operation.split(FormulaUtil.formulaRegex));
        }

        // for each record check if its position is part of the operation, if yes - replace the position name for its temperature value
        for (Record record : records) {
            String position = record.getPosition().getPlace();
            if (operation.contains(position)) {
                List<Measurement> recordMeasurements = getListOfMeasurementsForPeriod(new ArrayList<>(record.getMeasurements()), period);
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
            } catch (Exception ex) {
                logger.warn("Cannot perform operation: " + FormulaUtil.generateFormula(operations.get(i)));
                measurements.get(i).setData(Double.NaN);
            }
        }

        return measurements;
    }

}



