package temperatus.exporter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by alberto on 8/4/16.
 */
public class MissionExporter{

    private Mission mission;

    public Workbook export() {

        Workbook wb = new HSSFWorkbook();
        String missionName = mission.getName();
        Sheet missionSheet = wb.createSheet(missionName);
        Row headerRow = missionSheet.createRow(0);

        int row = 1;
        for(Record record: mission.getRecords()) {

            Row dataRow = missionSheet.createRow(row);

            Cell device = dataRow.createCell(0);
            device.setCellValue(record.getIbutton().getSerial());

            int col = 1;
            List<Measurement> measurementList = new ArrayList<>(record.getMeasurements());
            Collections.sort(measurementList, (a, b) -> a.getDate().compareTo(b.getDate()));

            for(Measurement measurement: measurementList) {
                Cell data = dataRow.createCell(col);
                data.setCellValue(measurement.getData());
                col++;
            }

            if(row == 1) {
                int c = 1;
                for(Measurement measurement: measurementList) {
                    Cell time = headerRow.createCell(c);
                    time.setCellValue(measurement.getDate().toString());
                    c++;
                }
            }

            row++;
        }

        return wb;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }
}
