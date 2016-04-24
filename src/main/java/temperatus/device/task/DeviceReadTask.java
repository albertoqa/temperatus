package temperatus.device.task;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.container.MissionContainer;
import com.dalsemi.onewire.container.OneWireContainer41;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import temperatus.analysis.pojo.DeviceMissionData;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.types.Unit;
import temperatus.util.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author aquesada
 */
@Component
public class DeviceReadTask extends DeviceTask {

    private static final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private static final NumberFormat nf = new DecimalFormat();

    private static final String NEW_LINE_SEPARATOR = "\n";                          //Delimiter used in CSV file
    private static final Object[] FILE_HEADER = {"Date/Time", "Unit", "Value"};     // CSV file Header

    private static final String[] INFO_TO_WRITE = {"1-Wire/iButton Part Number: ", "1-Wire/iButton Registration Number: ",
            "Mission in Progress?  ", "SUTA Mission?  ", "Waiting for Temperature Alarm?  ", "Sample Rate:  ",
            "Mission Start Time:  ", "Mission Sample Count:  ", "Roll Over Enabled?  ", "First Sample Timestamp:  ",
            "Total Mission Samples:  ", "Total Device Samples:  ", "Temperature Logging:  ", "Temperature High Alarm:  ",
            "Temperature Low Alarm:  "};

    /* indices for feature labels */
    private static final int PART_NUMBER = 0;
    private static final int SERIAL = 1;
    private static final int IS_ACTIVE = 2;
    private static final int MISSION_SUTA = 3;
    private static final int MISSION_WFTA = 4;
    private static final int SAMPLE_RATE = 5;
    private static final int MISSION_START = 6;
    private static final int MISSION_SAMPLES = 7;
    private static final int ROLL_OVER = 8;
    private static final int FIRST_SAMPLE_TIMESTAMP = 9;
    private static final int TOTAL_SAMPLES = 10;
    private static final int DEVICE_SAMPLES = 11;
    private static final int TEMP_LOGGING = 12;
    private static final int TEMP_HIGH_ALARM = 13;
    private static final int TEMP_LOW_ALARM = 14;
    private static final int TOTAL_FEATURES = 15;

    private static final int CHANNEL_O = 0;
    private static final int CHANNEL_1 = 1;
    private static final int FIRST_SAMPLE = 0;

    private String fileName;

    // mission features
    private String[] info = new String[TOTAL_FEATURES];

    // get the temperature log
    private Measurement[] measurements = null;

    private static Logger logger = LoggerFactory.getLogger(DeviceReadTask.class.getName());

    @Override
    public Object call() throws Exception {
        try {
            logger.info("Reading device...");
            deviceSemaphore.acquire();
            logger.debug("Read Semaphore acquired!");

            if(saveToFile) {
                return readDataAndSaveToFile();
            } else {
                return readDataAsObject();
            }

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.debug("Read Semaphore released");
        }
    }

    private void readData() {
        try {
            setUpAdapter();

            MissionContainer mContainer = (MissionContainer) container;

            logger.info("Starting the device's information reading...");

            // load results from device
            if (!mContainer.isMissionLoaded()) {
                mContainer.loadMissionResults();
            }

            boolean missionActive = mContainer.isMissionRunning();
            int sample_rate = mContainer.getMissionSampleRate(CHANNEL_O);

            info[IS_ACTIVE] = String.valueOf(missionActive);
            info[MISSION_SUTA] = String.valueOf(((OneWireContainer41) mContainer).isMissionSUTA());
            info[MISSION_WFTA] = String.valueOf(((OneWireContainer41) mContainer).isMissionWFTA());
            info[SAMPLE_RATE] = String.valueOf(sample_rate);

            int sample_count = mContainer.getMissionSampleCount(CHANNEL_O) | mContainer.getMissionSampleCount(CHANNEL_1);
            if (sample_count > 0) {
                info[MISSION_START] = new Date(mContainer.getMissionTimeStamp(CHANNEL_O)).toString();
            } else {
                info[MISSION_START] = "No samples yet";
            }

            info[MISSION_SAMPLES] = nf.format(sample_count);
            info[ROLL_OVER] = mContainer.isMissionRolloverEnabled() + (mContainer.hasMissionRolloverOccurred() ? "(rolled over)" : "(no rollover)");
            info[FIRST_SAMPLE_TIMESTAMP] = new Date(mContainer.getMissionSampleTimeStamp(CHANNEL_O, FIRST_SAMPLE) | mContainer.getMissionSampleTimeStamp(CHANNEL_1, FIRST_SAMPLE)).toString();

            info[TOTAL_SAMPLES] = String.valueOf(mContainer.getMissionSampleCountTotal(CHANNEL_O));

            if (mContainer.getMissionChannelEnable(CHANNEL_O)) {
                info[TEMP_LOGGING] = mContainer.getMissionResolution(CHANNEL_O) + " bit";
            } else {
                info[TEMP_LOGGING] = " disabled";
            }

            if (mContainer.getMissionChannelEnable(CHANNEL_O) && mContainer.getMissionAlarmEnable(CHANNEL_O, MissionContainer.ALARM_HIGH)) {
                // read the high temperature alarm setting
                String highAlarmText = nf.format(mContainer.getMissionAlarm(CHANNEL_O, MissionContainer.ALARM_HIGH));
                info[TEMP_HIGH_ALARM] = highAlarmText + " C";

                if (mContainer.hasMissionAlarmed(CHANNEL_O, MissionContainer.ALARM_LOW)) {
                    info[TEMP_HIGH_ALARM] = info[TEMP_HIGH_ALARM] + " (ALARMED)";
                }
            } else {
                info[TEMP_HIGH_ALARM] = " disabled";
            }

            // read the low temperature alarm setting
            if (mContainer.getMissionChannelEnable(CHANNEL_O) && mContainer.getMissionAlarmEnable(CHANNEL_O, MissionContainer.ALARM_LOW)) {
                String lowAlarmText = nf.format(mContainer.getMissionAlarm(CHANNEL_O, MissionContainer.ALARM_LOW));
                info[TEMP_LOW_ALARM] = lowAlarmText + " C";

                if (mContainer.hasMissionAlarmed(CHANNEL_O, MissionContainer.ALARM_LOW)) {
                    info[TEMP_LOW_ALARM] = info[TEMP_LOW_ALARM] + " (ALARMED)";
                }
            } else {
                info[TEMP_LOW_ALARM] = " disabled";
            }

            info[DEVICE_SAMPLES] = String.valueOf(((OneWireContainer41) mContainer).getDeviceSampleCount());

            String useTempCal = OneWireAccessProvider.getProperty("DS1922H.useTemperatureCalibrationRegisters");
            if (useTempCal != null) {
                ((OneWireContainer41) mContainer).setTemperatureCalibrationRegisterUsage(!useTempCal.toLowerCase().equals("false"));
            } else {
                ((OneWireContainer41) mContainer).setTemperatureCalibrationRegisterUsage(true);
            }

            ///////////////////////////////////////////////

            logger.info("Reading mission measurements...");

            if (mContainer.getMissionChannelEnable(CHANNEL_O)) {
                measurements = new Measurement[mContainer.getMissionSampleCount(CHANNEL_O)];

                for (int i = 0; i < mContainer.getMissionSampleCount(CHANNEL_O); i++) {
                    measurements[i] = new Measurement(new Date(mContainer.getMissionSampleTimeStamp(CHANNEL_O, i)), mContainer.getMissionSample(CHANNEL_O, i), Unit.C);
                }
            }

            info[PART_NUMBER] = ((OneWireContainer41) mContainer).getName();
            info[SERIAL] = ((OneWireContainer41) mContainer).getAddressAsString();

        } catch (Exception e) {
            logger.error("Cannot start mission: " + e.getMessage());
        } finally {
            releaseAdapter();
        }
    }

    private DeviceMissionData readDataAsObject() {
        readData();

        DeviceMissionData deviceMissionData = new DeviceMissionData();

        deviceMissionData.setMeasurements(new ArrayList<>(Arrays.asList(measurements)));
        deviceMissionData.setFirstSampleTime(df.format(info[FIRST_SAMPLE_TIMESTAMP]));
        deviceMissionData.setHighAlarm(info[TEMP_HIGH_ALARM]);
        deviceMissionData.setInProgress(info[IS_ACTIVE]);
        deviceMissionData.setIsSuta(info[MISSION_SUTA]);
        deviceMissionData.setLowAlarm(info[TEMP_LOW_ALARM]);
        deviceMissionData.setMissionSampleCount(info[MISSION_SAMPLES]);

        if(!info[MISSION_START].contains("No samples yet")) {
            deviceMissionData.setMissionStartTime(df.format(info[MISSION_START]));
        } else {
            deviceMissionData.setMissionStartTime("No samples yet");
        }

        deviceMissionData.setResolution(info[TEMP_LOGGING]);
        deviceMissionData.setRollOverEnabled(info[ROLL_OVER]);
        deviceMissionData.setSampleRate(info[SAMPLE_RATE]);
        deviceMissionData.setWaitingForTempAlarm(info[MISSION_WFTA]);
        deviceMissionData.setTotalMissionSamples(info[TOTAL_SAMPLES]);
        deviceMissionData.setTotalDeviceSamples(info[DEVICE_SAMPLES]);

        return deviceMissionData;
    }

    private File readDataAndSaveToFile() {
        readData();

        if (fileName == null) {
            fileName = System.getProperty("java.io.tmpdir") + System.currentTimeMillis() + ".csv";
        }
        logger.info("Filename: " + fileName);

        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        try {
            logger.info("Writing csv");

            fileWriter = new FileWriter(fileName);
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            for (int i = 0; i < TOTAL_FEATURES; i++) {
                csvFilePrinter.printRecord(INFO_TO_WRITE[i] + info[i]);
            }

            csvFilePrinter.println();
            csvFilePrinter.printRecord(FILE_HEADER);

            if (measurements != null) {
                for (Measurement measurement : measurements) {
                    List<String> m = new ArrayList<>();
                    m.add(Constants.dateTimeFormat.format(measurement.getDate()));
                    m.add(String.valueOf(Unit.C));
                    m.add(String.valueOf(measurement.getData()));
                    csvFilePrinter.printRecord(m);
                }
            }

        } catch (Exception e) {
            logger.error("Error in CsvFileWriter: " + e.getMessage());
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
                if (csvFilePrinter != null) {
                    csvFilePrinter.close();
                }
            } catch (IOException e) {
                logger.error("Error while flushing/closing fileWriter/csvPrinter: " + e.getMessage());
            }
        }

        return new File(fileName);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
