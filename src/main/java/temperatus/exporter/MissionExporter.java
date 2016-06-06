package temperatus.exporter;

import javafx.scene.control.Alert;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.analysis.IButtonDataAnalysis;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;
import temperatus.model.pojo.types.Unit;
import temperatus.util.VistaNavigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Export mission data to excel format
 * Needs org.apache.poi to work
 * <p>
 * Created by alberto on 8/4/16.
 */
public class MissionExporter extends AbstractExporter {

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
    public XSSFWorkbook export() {
        logger.info("Generating data to export excel");

        XSSFWorkbook wb = new XSSFWorkbook();
        try {
            XSSFSheet missionSheet = wb.createSheet(missionName);
            XSSFRow headerRow = missionSheet.createRow(0);      // Date-Time or Index of the measurement

            XSSFSheet missionSheetOneRow = wb.createSheet(missionName + "OneRow");
            XSSFRow headerRowOneRow = missionSheetOneRow.createRow(0);      // Date-Time or Index of the measurement
            XSSFRow dataRowOneRow = missionSheetOneRow.createRow(1);
            int prevIndex = 0;

            /**
             * Calculate independent positions first
             */
            records.sort((o1, o2) -> o1.getPosition().getPlace().compareTo(o2.getPosition().getPlace()));

            int row = 1;
            for (Record record : records) {
                logger.debug("Exporting data for record: " + record);

                XSSFRow dataRow = missionSheet.createRow(row);

                XSSFCell device = dataRow.createCell(0);
                device.setCellValue(record.getPosition().getPlace());

                List<Measurement> toExport = IButtonDataAnalysis.getListOfMeasurementsForPeriod(dataMap.get(record), period);

                exportMission(toExport, dataRow, unit, headerRow, row);

                // in this case create another sheet and write all the data in only one row
                if (dataMap.size() > 1) {
                    XSSFCell deviceOneRow = dataRowOneRow.createCell(prevIndex);
                    deviceOneRow.setCellValue(record.getPosition().getPlace());
                    prevIndex++;

                    exportMission(toExport, headerRowOneRow, dataRowOneRow, unit, prevIndex);
                    prevIndex += toExport.size();
                }

                row++;
            }

            /**
             * Calculate formulas
             */
            List<Formula> f = new ArrayList<>(formulas);
            f.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));

            for (Formula formula : f) {
                logger.debug("Exporting data for formula: " + formula);

                List<Measurement> measurements = IButtonDataAnalysis.getListOfMeasurementsForFormulaAndPeriod(dataMap, formula, period);

                XSSFRow dataRow = missionSheet.createRow(row);
                XSSFCell device = dataRow.createCell(0);
                device.setCellValue(formula.getName());

                if (exportFormula(measurements, dataRow, unit, row)) {
                    showWarn = true;
                }

                if (dataMap.size() > 1) {
                    XSSFCell deviceOneRow = dataRowOneRow.createCell(prevIndex);
                    deviceOneRow.setCellValue(formula.getName());
                    prevIndex++;

                    exportMission(measurements, headerRowOneRow, dataRowOneRow, unit, prevIndex);
                    prevIndex += measurements.size();
                }

                row++;
            }

            // Inform the user of the error/incorrect value
            if (showWarn) {
                VistaNavigator.showAlertAndWait(Alert.AlertType.WARNING, Language.getInstance().get(Lang.ERROR_CALCULATING_FORMULA));
            }

            VistaNavigator.showAlertAndWait(Alert.AlertType.INFORMATION, Language.getInstance().get(Lang.SUCCESSFULLY_EXPORTED));

        } catch (Exception ex) {
            logger.error("Error exporting data: " + ex);
            VistaNavigator.showAlertAndWait(Alert.AlertType.ERROR, Language.getInstance().get(Lang.ERROR_EXPORTING_DATA));
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
     * @param dataMap     all records-measurements of the mission
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
