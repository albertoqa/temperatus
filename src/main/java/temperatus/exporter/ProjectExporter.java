package temperatus.exporter;

import javafx.scene.control.Alert;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.analysis.IButtonDataAnalysis;
import temperatus.importer.IbuttonDataImporter;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;
import temperatus.model.pojo.Record;
import temperatus.model.pojo.types.Unit;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.util.List;

/**
 * Export all project data to excel
 * <p>
 * Created by alberto on 13/5/16.
 */
public class ProjectExporter extends AbstractExporter {

    private Project project;
    private Unit unit;

    private static Logger logger = LoggerFactory.getLogger(ProjectExporter.class.getName());

    /**
     * Export data to excel
     *
     * @return workbook containing formatted data
     */
    public XSSFWorkbook export() {
        XSSFWorkbook wb = new XSSFWorkbook();
        try {
            for (Mission mission : project.getMissions()) {
                XSSFSheet missionSheet = wb.createSheet(mission.getName());
                XSSFRow headerRow = missionSheet.createRow(0);      // Date-Time or Index of the measurement

                int row = 1;
                for (Record record : mission.getRecords()) {
                    logger.debug("Exporting data for record: " + record);

                    XSSFRow dataRow = missionSheet.createRow(row);

                    XSSFCell device = dataRow.createCell(0);
                    device.setCellValue(record.getPosition().getPlace());

                    List<Measurement> measurements = new IbuttonDataImporter(new File(record.getDataPath())).getMeasurements();
                    List<Measurement> toExport = IButtonDataAnalysis.getListOfMeasurementsForPeriod(measurements, 1);

                    exportMission(toExport, dataRow, unit, headerRow, row);
                    row++;
                }
            }
        } catch (Exception ex) {
            VistaNavigator.showAlert(Alert.AlertType.ERROR, Language.getInstance().get(Lang.ERROR_EXPORTING_DATA));
        }
        return wb;

    }

    /**
     * Data of the project to export
     *
     * @param project project to export
     * @param unit    unit of measurement to export
     */
    public void setData(Project project, Unit unit) {
        this.project = project;
        this.unit = unit;
    }

}
