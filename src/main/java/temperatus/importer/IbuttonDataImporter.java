package temperatus.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Measurement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by alberto on 6/2/16.
 *
 * Ready to parse CSV files with the following format:
 *
 * Lines 1 - 18 iButton information
 * Line 20 Headers
 * Line 21 - ... Data: DateTime, Unit Of Measurement, Measurement
 *
 */
public class IbuttonDataImporter extends AbstractImporter {

    private static final String [] FILE_HEADER_MAPPING = {"Date/Time","Unit","Value"};
    private static final SimpleDateFormat timeStampFormatter = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    private static final String TIMESTAMP_HEADER = "Date/Time";
    private static final String UNIT_HEADER = "Unit";
    private static final String VALUE_HEADER = "Value";

    public IbuttonDataImporter(File fileToRead) {
        super(fileToRead);
        readData();
    }

    @Override
    public void readData() {

        FileReader fileReader = null;
        CSVParser csvFileParser = null;

        try {
            checkIfFileIsValid();

            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING);

            fileReader = new FileReader(fileToRead);
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

            List<CSVRecord> csvRecords = csvFileParser.getRecords();

            int line;

            // Read the iButton info data
            for(line = 0; line < csvRecords.size(); line++) {
                //TODO save ibutton info
                CSVRecord csvRecord = csvRecords.get(line);

                if(csvRecord.get(0).contains("Part Number")) {
                    deviceModel = csvRecord.get(0).split(":")[1];
                    deviceModel.replace(" ", "");
                } else if(csvRecord.get(0).contains("Registration Number")) {
                    deviceSerial = csvRecord.get(0).split(":")[1];
                    deviceSerial.replace(" ", "");
                } else if(csvRecord.get(0).contains("Sample Rate")) {
                    sampleRate = csvRecord.get(0).split(":")[1];
                    sampleRate.replace(" ", "");
                } else if(isHeaderLine(csvRecord)) {
                    line++;
                    break;
                }
            }

            // Read the measurements
            while(line < csvRecords.size()) {
                CSVRecord record = csvRecords.get(line);

                Date measurementDate = timeStampFormatter.parse(record.get(TIMESTAMP_HEADER));
                String unit = record.get(UNIT_HEADER);

                double measurementData = 0.0;
                if(unit.equals("C")) {
                    measurementData = getData(record.get(VALUE_HEADER), record.get(3));
                } else if(unit.equals("F")) {
                    measurementData = fahrenheitToCelsius(getData(record.get(VALUE_HEADER), record.get(3)));
                }

                Measurement measurement = new Measurement(measurementDate, measurementData, null); // recordId must be set before save to db
                measurements.add(measurement);

                line++;
            }

            startDate = measurements.get(0).getDate();
            finishDate = measurements.get(measurements.size()-1).getDate();

        } catch (ControlledTemperatusException ex) {

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
                csvFileParser.close();
            } catch (IOException e) {
                System.out.println("Error while closing fileReader/csvFileParser !!!");
                e.printStackTrace();
            }
        }

    }

    /**
     * Conver the two strings to a double with decimals
     *
     * @param integer
     * @param decimal
     * @return
     */
    private double getData(String integer, String decimal) {
        Double measurementData = Double.parseDouble(integer);
        String dec = "0." + decimal;
        Double decimals = Double.parseDouble(dec);
        return measurementData + decimals;
    }

    /**
     * Convert fahrenheit to celsius
     *
     * @param fahrenheit
     * @return
     */
    private Double fahrenheitToCelsius(Double fahrenheit) {
        return (fahrenheit - 32) * (5/9);
    }

    private boolean isHeaderLine(CSVRecord record) {
        if(FILE_HEADER_MAPPING[0].equals(record.get(0)) && FILE_HEADER_MAPPING[1].equals(record.get(1)) && FILE_HEADER_MAPPING[2].equals(record.get(2))) {
            return true;
        }
        return false;
    }

    @Override
    protected void checkIfFileIsValid() throws ControlledTemperatusException{

    }

}
