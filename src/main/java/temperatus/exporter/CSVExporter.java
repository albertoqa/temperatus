package temperatus.exporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.analysis.pojo.DeviceMissionData;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.types.Unit;
import temperatus.util.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Export data to a CSV file
 * <p>
 * Created by alberto on 11/11/2016.
 */
public class CSVExporter {

    private static final String NEW_LINE_SEPARATOR = "\n";                          //Delimiter used in CSV file
    private static final Object[] FILE_HEADER = {"Date/Time", "Unit", "Value"};     // CSV file Header

    private static Logger logger = LoggerFactory.getLogger(CSVExporter.class.getName());

    /**
     * Export device's data to csv
     *
     * @param file              file to write to
     * @param deviceMissionData data of the mission to export
     */
    public static void exportToCsv(File file, DeviceMissionData deviceMissionData) {

        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        try {
            logger.info("Writing csv");

            fileWriter = new FileWriter(file);
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            csvFilePrinter.printRecord("1-Wire/iButton Part Number: " + deviceMissionData.getPartNumber());
            csvFilePrinter.printRecord("1-Wire/iButton Registration Number: " + deviceMissionData.getSerial());
            csvFilePrinter.printRecord("Mission in Progress?  " + deviceMissionData.getInProgress());
            csvFilePrinter.printRecord("SUTA Mission?  " + deviceMissionData.getIsSuta());
            csvFilePrinter.printRecord("Waiting for Temperature Alarm?  " + deviceMissionData.getWaitingForTempAlarm());
            csvFilePrinter.printRecord("Sample Rate:  " + deviceMissionData.getSampleRate());
            csvFilePrinter.printRecord("Mission Start Time:  " + deviceMissionData.getMissionStartTime());
            csvFilePrinter.printRecord("Mission Sample Count:  " + deviceMissionData.getMissionSampleCount());
            csvFilePrinter.printRecord("Roll Over Enabled?  " + deviceMissionData.getRollOverEnabled());
            csvFilePrinter.printRecord("First Sample Timestamp:  " + deviceMissionData.getFirstSampleTime());
            csvFilePrinter.printRecord("Total Mission Samples:  " + deviceMissionData.getTotalMissionSamples());
            csvFilePrinter.printRecord("Total Device Samples:  " + deviceMissionData.getTotalDeviceSamples());
            csvFilePrinter.printRecord("Temperature Logging:  " + deviceMissionData.getResolution());
            csvFilePrinter.printRecord("Temperature High Alarm:  " + deviceMissionData.getLowAlarm());
            csvFilePrinter.printRecord("Temperature Low Alarm:  " + deviceMissionData.getHighAlarm());

            csvFilePrinter.println();
            csvFilePrinter.printRecord(FILE_HEADER);    // CSV file Header

            if (deviceMissionData.getMeasurements() != null) {
                for (Measurement measurement : deviceMissionData.getMeasurements()) {
                    csvFilePrinter.printRecord(CSVExporter.generateRow(measurement.getDate(), Unit.C, measurement.getData()));
                }
            }

        } catch (Exception e) {
            logger.error("Error in CsvFileWriter: " + e.getMessage());
        } finally {
            try {
                if(fileWriter != null) {
                    fileWriter.close();
                    fileWriter.close();
                } if(csvFilePrinter != null) {
                    csvFilePrinter.close();
                }
            } catch (IOException e) {
                logger.error("Error while flushing/closing fileWriter/csvPrinter: " + e.getMessage());
            }
        }
    }

    /**
     * Generate csv row
     *
     * @param d    date
     * @param u    unit
     * @param data value
     * @return list of value for the row
     */
    private static List<String> generateRow(Date d, Unit u, double data) {
        List<String> m = new ArrayList<>();
        m.add(Constants.dateTimeFormat.format(d));
        m.add(String.valueOf(u));
        m.add(String.valueOf(data));
        return m;
    }

}
