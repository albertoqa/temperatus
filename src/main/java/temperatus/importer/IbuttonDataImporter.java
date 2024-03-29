package temperatus.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.calculator.Calculator;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.types.Unit;
import temperatus.util.Constants;
import temperatus.util.DateUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by alberto on 6/2/16.
 * <p/>
 * Ready to parse CSV files with the following format:
 * <p/>
 * Lines 1 - 18 iButton information
 * Line 20 Headers
 * Line 21 - ... Data: DateTime, Unit Of Measurement, [Measurement, Decimal]
 * <p/>
 * A sample file can be found in resources/samples/1.csv
 */
public class IbuttonDataImporter extends AbstractImporter {

    private static final String[] FILE_HEADER_MAPPING = {"Date/Time", "Unit", "Value"};

    private static final String TIMESTAMP_HEADER = "Date/Time";
    private static final String UNIT_HEADER = "Unit";
    private static final String VALUE_HEADER = "Value";

    private static final String MODEL = "Part Number";
    private static final String SERIAL = "Registration Number";
    private static final String RATE = "Sample Rate";

    private static final String SEPARATOR = ":";

    private static Logger logger = LoggerFactory.getLogger(IbuttonDataImporter.class.getName());

    public IbuttonDataImporter(File fileToRead) throws ControlledTemperatusException {
        super(fileToRead);
        readData();
    }

    /**
     * Parse the csv and read all its data
     *
     * @throws ControlledTemperatusException
     */
    @Override
    public void readData() throws ControlledTemperatusException {
        logger.info("Reading csv data");

        FileReader fileReader = null;
        CSVParser csvFileParser = null;

        try {
            checkIfFileIsValid();   // if not valid exception is thrown

            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING);

            fileReader = new FileReader(fileToRead);
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

            List<CSVRecord> csvRecords = csvFileParser.getRecords();

            int line;   // current line being read

            // Read the iButton info data until the header is found
            logger.debug("Reading iButton info data");
            for (line = 0; line < csvRecords.size(); line++) {
                CSVRecord csvRecord = csvRecords.get(line);

                if (csvRecord.get(0).contains(MODEL)) {
                    deviceModel = csvRecord.get(0).split(SEPARATOR)[1];
                    deviceModel = deviceModel.replace(" ", "");
                } else if (csvRecord.get(0).contains(SERIAL)) {
                    deviceSerial = csvRecord.get(0).split(SEPARATOR)[1];
                    deviceSerial = deviceSerial.replace(" ", "");
                } else if (csvRecord.get(0).contains(RATE)) {
                    sampleRate = csvRecord.get(0).split(SEPARATOR)[1];
                    sampleRate = sampleRate.trim();
                } else if (isHeaderLine(csvRecord)) {
                    line++;
                    break;
                }
            }

            // Read the measurements
            logger.debug("Reading measurements");
            while (line < csvRecords.size()) {
                CSVRecord record = csvRecords.get(line);

                Date measurementDate = DateUtils.tryParse(record.get(TIMESTAMP_HEADER));
                String unit = record.get(UNIT_HEADER);
                Unit u = unit.equals(Constants.UNIT_C) ? Unit.C : Unit.F;

                /**
                 * In this css the decimal separator is a comma which interferes with the csv read... in this case
                 * instead of 3 records we have 4 records. Form the measurement as the concatenation of the record
                 * three and four like:
                 * 12/12/2016 00:00:00, C, 12,3
                 * Measurement = record.get(2) + , + record.get(3)
                 */
                double measurementData = 0.0;
                if (unit.equals(Constants.UNIT_C)) {
                    if (record.size() == 4) {
                        measurementData = getData(record.get(VALUE_HEADER), record.get(3));
                    } else {
                        measurementData = Double.parseDouble(record.get(VALUE_HEADER));
                    }
                } else if (unit.equals(Constants.UNIT_F)) {
                    // All data saved to db must be in celsius
                    if (record.size() == 4) {
                        measurementData = Calculator.fahrenheitToCelsius(getData(record.get(VALUE_HEADER), record.get(3)));
                    } else {
                        measurementData = Calculator.fahrenheitToCelsius(Double.parseDouble(record.get(VALUE_HEADER)));
                    }
                }

                Measurement measurement = new Measurement(measurementDate, measurementData, u);
                measurements.add(measurement);

                line++;
            }

            if (measurements.size() > 1) {
                startDate = measurements.get(0).getDate();
                finishDate = measurements.get(measurements.size() - 1).getDate();
            } else {
                throw new ControlledTemperatusException(Language.getInstance().get(Lang.INVALID_FILE_FORMAT) + Constants.SPACE + fileToRead.getName());
            }

        } catch (ControlledTemperatusException ex) {
            logger.error("Invalid file format");
            throw new ControlledTemperatusException(ex.getMessage());
        } catch (FileNotFoundException e) {
            logger.error("File not found");
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.FILE_NOT_FOUND));
        } catch (IOException e) {
            logger.error("Error while reading the file");
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.READING_ERROR));
        } catch (ParseException e) {
            logger.error("Error parsing the file" + e);
            throw new ControlledTemperatusException(Language.getInstance().get(Lang.PARSE_ERROR));
        } finally {
            try {
                if (fileReader != null) fileReader.close();
                if (csvFileParser != null) csvFileParser.close();
            } catch (IOException e) {
                logger.error("Error while closing fileReader/csvFileParser");
            }
        }

    }

    /**
     * Convert the two strings to a double with decimals
     *
     * @param integer integer part of the temperature
     * @param decimal decimal part of the temperature
     * @return complete temperature as a double or NaN if error
     */
    private double getData(String integer, String decimal) {
        try {
            Double measurementData = Double.parseDouble(integer);
            String dec = "0." + decimal;
            Double decimals = Double.parseDouble(dec);
            return measurementData + decimals;
        } catch (NumberFormatException ex) {
            logger.warn("Error generating decimal data for: " + integer + " , " + decimal);
            return Double.NaN;
        }
    }

    /**
     * Check if current line is the header line
     *
     * @param record current line
     * @return is header?
     */
    private boolean isHeaderLine(CSVRecord record) {
        return FILE_HEADER_MAPPING[0].equals(record.get(0)) && FILE_HEADER_MAPPING[1].equals(record.get(1)) && FILE_HEADER_MAPPING[2].equals(record.get(2));
    }

    @Override
    protected void checkIfFileIsValid() throws ControlledTemperatusException {
        logger.debug("The check is performed in the read itself");
    }

}
