package temperatus.exporter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import temperatus.calculator.Calculator;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by alberto on 8/4/16.
 */
public class MissionExporter {

    private int period;
    private String missionName;
    private List<Record> records;
    private Set<Record> allRecords;
    private List<Formula> formulas;

    public Workbook export() {

        Workbook wb = new HSSFWorkbook();
        Sheet missionSheet = wb.createSheet(missionName);
        Row headerRow = missionSheet.createRow(0);

        int row = 1;
        for (Record record : records) {

            Row dataRow = missionSheet.createRow(row);

            Cell device = dataRow.createCell(0);
            device.setCellValue(record.getIbutton().getSerial());

            int col = 1;
            List<Measurement> measurementList = new ArrayList<>(record.getMeasurements());
            Collections.sort(measurementList, (a, b) -> a.getDate().compareTo(b.getDate()));

            List<Measurement> toExport = aux(measurementList);

            for (Measurement measurement : toExport) {
                Cell data = dataRow.createCell(col);
                data.setCellValue(measurement.getData());
                col++;
            }

            if (row == 1) {
                int c = 1;
                for (Measurement measurement : toExport) {
                    Cell time = headerRow.createCell(c);
                    time.setCellValue(measurement.getDate().toString());
                    c++;
                }
            }

            row++;
        }

        // Formulas
        for (Formula formula : formulas) {
            String formu = formula.getOperation();

            List<String> operations = new ArrayList<>();
            for (int i = 0; i < aux(new ArrayList<>(records.get(0).getMeasurements())).size(); i++) {
                operations.add(formu);
            }

            for (Record record : allRecords) {

                String position = record.getPosition().getPlace();
                List<Measurement> measurements = new ArrayList<>(record.getMeasurements());
                Collections.sort(measurements, (a, b) -> a.getDate().compareTo(b.getDate()));

                List<Measurement> measurementsToExport = aux(measurements);

                if (formu.contains(position)) {
                    int index = 0;
                    for (Measurement measurement : measurementsToExport) {
                        String f = operations.get(index);
                        f = f.replace(position, String.valueOf(measurement.getData()));
                        operations.set(index, f);
                        index++;
                    }
                }
            }

            List<Double> results = new ArrayList<>();
            for (String operation : operations) {
                try {
                    double result = Calculator.eval(operation);
                    results.add(result);
                } catch (Exception ex) {
                    results.add(Double.NaN);
                    // TODO alguna posición de la formula no se ha encontrado, show warning
                }
            }

            Row dataRow = missionSheet.createRow(row);

            Cell device = dataRow.createCell(0);
            device.setCellValue(formula.getName());

            int col = 1;
            for (Double result : results) {
                Cell data = dataRow.createCell(col);
                data.setCellValue(result);
                col++;
            }

            row++;
        }

        return wb;
    }

    private List<Measurement> aux(List<Measurement> measurementList) {
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

    public void setData(int period, String missionName, List<Record> records, List<Formula> formulas, Set<Record> allRecords) {
        this.period = period;
        this.missionName = missionName;
        this.records = records;
        this.formulas = formulas;
        this.allRecords = allRecords;
    }
}
