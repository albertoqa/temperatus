package temperatus.exporter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by alberto on 8/4/16.
 */
public class MissionExporter{

    private int period;
    private String missionName;
    private List<Record> records;
    private List<Formula> formulas;

    public Workbook export() {

        Workbook wb = new HSSFWorkbook();
        Sheet missionSheet = wb.createSheet(missionName);
        Row headerRow = missionSheet.createRow(0);

        int row = 1;
        for(Record record: records) {

            Row dataRow = missionSheet.createRow(row);

            Cell device = dataRow.createCell(0);
            device.setCellValue(record.getIbutton().getSerial());

            int col = 1;
            List<Measurement> measurementList = new ArrayList<>(record.getMeasurements());
            Collections.sort(measurementList, (a, b) -> a.getDate().compareTo(b.getDate()));

            List<Measurement> toExport = new ArrayList<>();
            int index = 0;
            Measurement measurementToExport = null;
            for(Measurement measurement: measurementList) {
                if(index%period == 0) {
                    if(measurementToExport != null) {
                        measurementToExport.setData(measurementToExport.getData()/period);
                        toExport.add(measurementToExport);
                    }
                    measurementToExport = new Measurement();
                    measurementToExport.setData(0);
                    measurementToExport.setDate(measurement.getDate());
                }
                measurementToExport.setData(measurementToExport.getData()+measurement.getData());
                index++;
            }

            for(Measurement measurement: toExport) {
                Cell data = dataRow.createCell(col);
                data.setCellValue(measurement.getData());
                col++;
            }

            if(row == 1) {
                int c = 1;
                for(Measurement measurement: toExport) {
                    Cell time = headerRow.createCell(c);
                    time.setCellValue(measurement.getDate().toString());
                    c++;
                }
            }

            row++;
        }

        return wb;
    }

    public void setData(int period, String missionName, List<Record> records, List<Formula> formulas) {
        this.period = period;
        this.missionName = missionName;
        this.records = records;
        this.formulas = formulas;
    }
}
