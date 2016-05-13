package temperatus.exporter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.calculator.Calculator;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.types.Unit;
import temperatus.util.Constants;

import java.util.List;

/**
 * Common functions for export data to excel
 * <p>
 * Created by alberto on 13/5/16.
 */
abstract class AbstractExporter {

    private static Logger logger = LoggerFactory.getLogger(AbstractExporter.class.getName());

    /**
     * Export the given list of measurements to the given row.
     * If first row generate the header.
     *
     * @param toExport list of measurements to export
     * @param dataRow row of the sheet where the data will be written
     * @param unit unit of measurement
     * @param headerRow header row (date-time or index)
     * @param row actual row
     */
    void exportMission(List<Measurement> toExport, Row dataRow, Unit unit, Row headerRow, int row) {
        int col = 1;
        for (Measurement measurement : toExport) {
            Cell data = dataRow.createCell(col);

            if (unit.equals(Unit.C)) {
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
    }

    /**
     * Export the given list of measurements (formula already calculated) to the given row of the sheet
     *
     * @param measurements list of measurements to export
     * @param dataRow row where the data will be written
     * @param unit unit of measurement
     * @param row actual row
     * @return true if an error occurred
     */
    boolean exportFormula(List<Measurement> measurements, Row dataRow, Unit unit, int row) {
        boolean showWarn = false;
        int col = 1;
        for (Measurement measurement : measurements) {
            Cell data = dataRow.createCell(col);

            if (unit.equals(Unit.C)) {
                data.setCellValue(measurement.getData());
            } else {
                data.setCellValue(Calculator.celsiusToFahrenheit(measurement.getData()));
            }
            col++;

            if (measurement.getData() == Double.NaN) {  // error calculating value, show warn to user
                logger.warn("Error in formula...");
                showWarn = true;
            }
        }
        return showWarn;
    }

}
