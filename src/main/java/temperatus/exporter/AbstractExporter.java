package temperatus.exporter;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
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
     * @param toExport  list of measurements to export
     * @param dataRow   row of the sheet where the data will be written
     * @param unit      unit of measurement
     * @param headerRow header row (date-time or index)
     * @param row       actual row
     */
    void exportMission(List<Measurement> toExport, XSSFRow dataRow, Unit unit, XSSFRow headerRow, int row) {
        exportMeasurements(toExport, dataRow, unit, 1);

        // Write the header as Index or DateTime
        if (row == 1) {
            logger.debug("Generating header");
            exportHeader(toExport, headerRow, 1);
        }
    }

    /**
     * Export missions to excel but all the positions/formulas in the same row
     *
     * @param toExport  measurements to export
     * @param headerRow header row
     * @param dataRow   data row
     * @param unit      unit of measurement
     * @param prevIndex index to start writing
     */
    void exportMission(List<Measurement> toExport, XSSFRow headerRow, XSSFRow dataRow, Unit unit, int prevIndex) {
        exportMeasurements(toExport, dataRow, unit, prevIndex);
        exportHeader(toExport, headerRow, prevIndex);
    }

    /**
     * Export the header of a list of measurements to the given row
     *
     * @param toExport  list of measurements to export
     * @param headerRow row
     * @param prevIndex index where we should start to write
     */
    private void exportHeader(List<Measurement> toExport, XSSFRow headerRow, int prevIndex) {
        boolean writeAsIndex = Constants.prefs.getBoolean(Constants.WRITE_AS_INDEX, Constants.WRITE_INDEX);
        int c = prevIndex;

        int index = 1;
        if (writeAsIndex) {
            for (Measurement measurement : toExport) {
                XSSFCell time = headerRow.createCell(c);
                time.setCellValue(index);
                index++;
                c++;
            }
        } else {
            for (Measurement measurement : toExport) {
                XSSFCell time = headerRow.createCell(c);
                time.setCellValue(measurement.getDate().toString());
                c++;
            }
        }
    }

    /**
     * Export the list of measurements to the given row, starting in the given index and with the given unit
     *
     * @param toExport  list of measurements to export
     * @param dataRow   row where the data must be exported
     * @param unit      unit of measurement
     * @param prevIndex column of the row where we should start writing
     */
    private void exportMeasurements(List<Measurement> toExport, XSSFRow dataRow, Unit unit, int prevIndex) {
        int c = prevIndex;
        for (Measurement measurement : toExport) {
            XSSFCell data = dataRow.createCell(c);

            if (unit.equals(Unit.C)) {
                data.setCellValue(measurement.getData());
            } else {
                data.setCellValue(Calculator.celsiusToFahrenheit(measurement.getData()));
            }

            c++;
        }
    }

    /**
     * Export the given list of measurements (formula already calculated) to the given row of the sheet
     *
     * @param measurements list of measurements to export
     * @param dataRow      row where the data will be written
     * @param unit         unit of measurement
     * @param row          actual row
     * @return true if an error occurred
     */
    boolean exportFormula(List<Measurement> measurements, XSSFRow dataRow, Unit unit, int row) {
        boolean showWarn = false;
        int col = 1;
        for (Measurement measurement : measurements) {
            XSSFCell data = dataRow.createCell(col);

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
