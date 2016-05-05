package temperatus.exporter;

import javafx.scene.control.Alert;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.analysis.IButtonDataAnalysis;
import temperatus.calculator.Calculator;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;
import temperatus.model.pojo.types.Unit;
import temperatus.util.Constants;

import java.util.HashMap;
import java.util.List;

/**
 * Export mission data to excel format
 * Needs org.apache.poi to work
 * <p>
 * Created by alberto on 8/4/16.
 */
public class MissionExporter {

    private int period;
    private String missionName;
    private List<Record> records;       // Records (positions) selected by the user to export
    private HashMap<Record, List<Measurement>> dataMap;     // All the records-measurements of the mission - used to calculate values of formulas
    private List<Formula> formulas;     // Formulas selected to export
    private Unit unit;                  // Unit to use in the export

    private boolean showWarn = false;   // Show alert if formula cannot be computed

    private static Logger logger = LoggerFactory.getLogger(MissionExporter.class.getName());

    /**
     * Export data to excel
     *
     * @return workbook containing formatted data
     */
    public Workbook export() {
        logger.info("Generating data to export excel");

        Workbook wb = new HSSFWorkbook();
        Sheet missionSheet = wb.createSheet(missionName);
        Row headerRow = missionSheet.createRow(0);      // Date-Time or Index of the measurement

        /**
         * Calculate independent positions first
         */
        int row = 1;
        for (Record record : records) {
            logger.debug("Exporting data for record: " + record);

            Row dataRow = missionSheet.createRow(row);

            Cell device = dataRow.createCell(0);
            device.setCellValue(record.getPosition().getPlace());

            List<Measurement> toExport = IButtonDataAnalysis.getListOfMeasurementsForPeriod(dataMap.get(record), period);

            int col = 1;
            for (Measurement measurement : toExport) {
                Cell data = dataRow.createCell(col);

                if(unit.equals(Unit.C)) {
                    data.setCellValue(measurement.getData());
                } else {
                    data.setCellValue(Calculator.celsiusToFahrenheit(measurement.getData()));
                }

                col++;
            }

            // Write the header as Index or DateTime
            if (row == 1) {
                logger.debug("Generating header");

                boolean writeAsIndex = Constants.prefs.getBoolean(Constants.WRITE_AS_INDEX, Constants.WRITE_INDEX);
                int c = 1;  // column

                if (writeAsIndex) {
                    for (Measurement measurement : toExport) {
                        Cell time = headerRow.createCell(c);
                        time.setCellValue(c);
                        c++;
                    }
                } else {
                    for (Measurement measurement : toExport) {
                        Cell time = headerRow.createCell(c);
                        time.setCellValue(measurement.getDate().toString());
                        c++;
                    }
                }
            }

            row++;
        }

        /**
         * Calculate formulas
         */
        for (Formula formula : formulas) {
            logger.debug("Exporting data for formula: " + formula);

            List<Measurement> measurements = IButtonDataAnalysis.getListOfMeasurementsForFormulaAndPeriod(dataMap, formula, period);

            Row dataRow = missionSheet.createRow(row);
            Cell device = dataRow.createCell(0);
            device.setCellValue(formula.getName());

            int col = 1;
            for (Measurement measurement : measurements) {
                Cell data = dataRow.createCell(col);

                if(unit.equals(Unit.C)) {
                    data.setCellValue(measurement.getData());
                } else {
                    data.setCellValue(Calculator.celsiusToFahrenheit(measurement.getData()));
                }

                col++;

                if (measurement.getData() == Double.NaN) {  // error calculating value, show warn to user
                    logger.warn("Error in formula: " + formula);
                    showWarn = true;
                }
            }

            row++;
        }

        // Inform the user of the error/incorrect value
        if (showWarn) {
            Alert alert = new Alert(Alert.AlertType.WARNING, Lang.ERROR_CALCULATING_FORMULA);
            alert.show();
        }

        return wb;
    }

    /**
     * Set mission data to export
     *
     * @param period      calculate average of <<period>> measurements
     * @param missionName name of the mission -- name of the excel sheet
     * @param records     records selected by the user to export
     * @param formulas    formulas related to the mission and selected to export
     * @param dataMap  all records-measurements of the mission
     */
    public void setData(int period, String missionName, List<Record> records, List<Formula> formulas, HashMap<Record, List<Measurement>> dataMap, Unit unit) {
        this.period = period;
        this.missionName = missionName;
        this.records = records;
        this.formulas = formulas;
        this.dataMap = dataMap;
        this.unit = unit;
    }
}
