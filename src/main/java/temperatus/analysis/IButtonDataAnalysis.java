package temperatus.analysis;

import temperatus.calculator.Calculator;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alberto on 7/2/16.
 */
public class IButtonDataAnalysis {

    public static List<Measurement> getListOfMeasurementsForPeriod(List<Measurement> measurementList, int period) {

        Collections.sort(measurementList, (a, b) -> a.getDate().compareTo(b.getDate()));

        List<Measurement> measurements;
        if (period == 1) {
            return measurementList;
        }

        List<Measurement> toExport = new ArrayList<>();
        int index = 0;
        Measurement measurementToExport = null;
        for (Measurement measurement : measurementList) {
            if (index % period == 0) {
                if (measurementToExport != null) {
                    measurementToExport.setData(measurementToExport.getData() / period);
                    toExport.add(measurementToExport);
                }
                measurementToExport = new Measurement();
                measurementToExport.setData(0);
                measurementToExport.setDate(measurement.getDate());
            }
            measurementToExport.setData(measurementToExport.getData() + measurement.getData());
            index++;
        }

        if (!toExport.contains(measurementToExport)) {
            int div = ((index - 1) % period) + 1;
            if (div > 0) {
                measurementToExport.setData(measurementToExport.getData() / div);
                toExport.add(measurementToExport);
            }
        }

        return toExport;
    }

    public static List<Measurement> getListOfMeasurementsForFormulaAndPeriod(List<Record> records, Formula formula, int period) {
        String operation = formula.getOperation();
        List<Measurement> measurements = getListOfMeasurementsForPeriod(new ArrayList<>(records.get(0).getMeasurements()), period).stream().map(measurement -> new Measurement(measurement.getDate(), 0.0, measurement.getUnit())).collect(Collectors.toList());

        List<String[]> operations = new ArrayList<>();
        for (int i = 0; i < measurements.size(); i++) {
            operations.add(operation.split(FormulaUtil.formulaRegex));
        }

        for (Record record : records) {
            String position = record.getPosition().getPlace();
            if (operation.contains(position)) {
                List<Measurement> recordMeasurements = getListOfMeasurementsForPeriod(new ArrayList<>(record.getMeasurements()), period);
                int index = 0;
                for (Measurement measurement : recordMeasurements) {
                    String[] f = operations.get(index);

                    for(int i = 0; i < f.length; i++) {
                        if(f[i].equals(position)) {
                            f[i] = String.valueOf(measurement.getData());
                        }
                    }

                    operations.set(index, f);
                    index++;
                }
            }
        }

        for (int i = 0; i < operations.size(); i++) {
            try {
                double result = Calculator.eval(FormulaUtil.generateFormula(operations.get(i)));
                measurements.get(i).setData(result);
            } catch (Exception ex) {
                measurements.get(i).setData(Double.NaN);
            }
        }

        return measurements;
    }

}



