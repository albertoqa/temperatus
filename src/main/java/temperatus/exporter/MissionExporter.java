package temperatus.exporter;

import javafx.scene.control.Alert;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import temperatus.analysis.IButtonDataAnalysis;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;
import temperatus.util.Constants;

import java.util.ArrayList;
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

    private boolean showWarn = false;

    public Workbook export() {

        Workbook wb = new HSSFWorkbook();
        Sheet missionSheet = wb.createSheet(missionName);
        Row headerRow = missionSheet.createRow(0);

        int row = 1;
        for (Record record : records) {

            Row dataRow = missionSheet.createRow(row);

            Cell device = dataRow.createCell(0);
            device.setCellValue(record.getIbutton().getSerial());

            List<Measurement> toExport = IButtonDataAnalysis.getListOfMeasurementsForPeriod(new ArrayList<>(record.getMeasurements()), period);

            int col = 1;
            for (Measurement measurement : toExport) {
                Cell data = dataRow.createCell(col);
                data.setCellValue(measurement.getData());
                col++;
            }


            if (row == 1) {

                boolean writeAsIndex = Constants.prefs.getBoolean(Constants.WRITE_AS_INDEX, Constants.WRITE_INDEX);
                int c = 1;

                if(writeAsIndex) {
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

        // Formulas
        for (Formula formula : formulas) {

            List<Measurement> measurements = IButtonDataAnalysis.getListOfMeasurementsForFormulaAndPeriod(new ArrayList<>(allRecords), formula, period);

            Row dataRow = missionSheet.createRow(row);
            Cell device = dataRow.createCell(0);
            device.setCellValue(formula.getName());

            int col = 1;
            for (Measurement measurement: measurements) {
                Cell data = dataRow.createCell(col);
                data.setCellValue(measurement.getData());
                col++;

                if(measurement.getData() == Double.NaN) {
                    showWarn = true;
                }
            }

            row++;
        }

        if (showWarn) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Some formulas cannot be calculated due to an error in the operation. Please check that selected formulas are correct.");
            alert.show();
        }

        return wb;
    }

    public void setData(int period, String missionName, List<Record> records, List<Formula> formulas, Set<Record> allRecords) {
        this.period = period;
        this.missionName = missionName;
        this.records = records;
        this.formulas = formulas;
        this.allRecords = allRecords;
    }
}
