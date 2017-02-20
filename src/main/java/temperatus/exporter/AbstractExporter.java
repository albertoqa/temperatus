package temperatus.exporter;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.calculator.Calculator;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.types.Unit;
import temperatus.util.Constants;

import java.util.Date;
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
    void exportMission(List<Measurement> toExport, XSSFRow dataRow, Unit unit, XSSFRow headerRow, int row, boolean separateWithTags) {
        exportMeasurements(toExport, dataRow, unit, 1, separateWithTags);

        // Write the header as Index or DateTime
        if (row == 1) {
            logger.debug("Generating header");
            exportHeader(toExport, headerRow, 1, separateWithTags);
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
    int exportMission(List<Measurement> toExport, XSSFRow headerRow, XSSFRow dataRow, Unit unit, int prevIndex, boolean separateWithTags) {
        exportMeasurements(toExport, dataRow, unit, prevIndex, separateWithTags);
        return exportHeader(toExport, headerRow, prevIndex, separateWithTags);
    }

    /**
     * Export the header of a list of measurements to the given row
     *
     * @param toExport  list of measurements to export
     * @param headerRow row
     * @param prevIndex index where we should start to write
     * @return the number of ranges in the list
     */
    private int exportHeader(List<Measurement> toExport, XSSFRow headerRow, int prevIndex, boolean separateWithTags) {
        boolean writeAsIndex = Constants.prefs.getBoolean(Constants.WRITE_AS_INDEX, Constants.WRITE_INDEX);
        int c = prevIndex;
        int range = 0;

        int index = 1;
        if (!separateWithTags || toExport.size() < 2) {
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
        } else {
            range = 1;
            Date prevDate = toExport.get(0).getDate();
            long rate = toExport.get(1).getDate().getTime() - toExport.get(0).getDate().getTime();
            if (writeAsIndex) {
                for (Measurement measurement : toExport) {
                    if (measurement.getDate().getTime() - prevDate.getTime() > rate) {
                        headerRow.createCell(c++);  // empty cell
                        XSSFCell rangeTag = headerRow.createCell(c++);
                        rangeTag.setCellValue("R" + range++);
                    }
                    XSSFCell time = headerRow.createCell(c);
                    time.setCellValue(index);
                    index++;
                    c++;
                    prevDate = measurement.getDate();
                }
            } else {
                for (Measurement measurement : toExport) {
                    if (measurement.getDate().getTime() - prevDate.getTime() > rate) {
                        headerRow.createCell(c++);  // empty cell
                        XSSFCell rangeTag = headerRow.createCell(c++);
                        rangeTag.setCellValue("R" + range++);
                    }
                    XSSFCell time = headerRow.createCell(c);
                    time.setCellValue(measurement.getDate().toString());
                    c++;
                    prevDate = measurement.getDate();
                }
            }
        }
        return range;
    }

    /**
     * Export the list of measurements to the given row, starting in the given index and with the given unit
     *
     * @param toExport  list of measurements to export
     * @param dataRow   row where the data must be exported
     * @param unit      unit of measurement
     * @param prevIndex column of the row where we should start writing
     */
    private void exportMeasurements(List<Measurement> toExport, XSSFRow dataRow, Unit unit, int prevIndex, boolean separateWithTags) {
        int c = prevIndex;

        if (!separateWithTags || toExport.size() < 2) {
            for (Measurement measurement : toExport) {
                XSSFCell data = dataRow.createCell(c);

                if (unit.equals(Unit.C)) {
                    data.setCellValue(measurement.getData());
                } else {
                    data.setCellValue(Calculator.celsiusToFahrenheit(measurement.getData()));
                }

                c++;
            }
        } else {
            Date prevDate = toExport.get(0).getDate();
            long rate = toExport.get(1).getDate().getTime() - toExport.get(0).getDate().getTime();

            for (Measurement measurement : toExport) {
                if (measurement.getDate().getTime() - prevDate.getTime() > rate) {
                    dataRow.createCell(c++);  // empty cell
                    dataRow.createCell(c++);
                }

                XSSFCell data = dataRow.createCell(c);

                if (unit.equals(Unit.C)) {
                    data.setCellValue(measurement.getData());
                } else {
                    data.setCellValue(Calculator.celsiusToFahrenheit(measurement.getData()));
                }

                c++;
                prevDate = measurement.getDate();
            }
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
    boolean exportFormula(List<Measurement> measurements, XSSFRow dataRow, Unit unit, int row, boolean separateWithTags) {
        boolean showWarn = false;
        int col = 1;

        if (!separateWithTags || measurements.size() < 2) {
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
        } else {
            Date prevDate = measurements.get(0).getDate();
            long rate = measurements.get(1).getDate().getTime() - measurements.get(0).getDate().getTime();

            for (Measurement measurement : measurements) {
                if (measurement.getDate().getTime() - prevDate.getTime() > rate) {
                    dataRow.createCell(col++);  // empty cell
                    dataRow.createCell(col++);
                }

                XSSFCell data = dataRow.createCell(col);

                if (unit.equals(Unit.C)) {
                    data.setCellValue(measurement.getData());
                } else {
                    data.setCellValue(Calculator.celsiusToFahrenheit(measurement.getData()));
                }
                col++;
                prevDate = measurement.getDate();

                if (measurement.getData() == Double.NaN) {  // error calculating value, show warn to user
                    logger.warn("Error in formula...");
                    showWarn = true;
                }
            }
        }

        return showWarn;
    }

}
