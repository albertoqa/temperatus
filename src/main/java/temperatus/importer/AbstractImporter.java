package temperatus.importer;

import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Measurement;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Import data from a file to a list of measurements and some general data
 * Extend this class if you need to import data in any format
 * <p>
 * Created by alberto on 6/2/16.
 */
public abstract class AbstractImporter {

    File fileToRead;                    // File of data to import
    List<Measurement> measurements;     // Data imported. Temperature data must be in celsius
    String deviceModel;                 // Model of device used to read the data
    String deviceSerial;                // Serial of the device
    String sampleRate;                  // Rate/Period of measurement
    Date startDate;                     // Start date
    Date finishDate;                    // End date

    AbstractImporter(File fileToRead) {
        this.fileToRead = fileToRead;
        measurements = new ArrayList<>();
    }

    /**
     * Parse the file and fill all the variables
     *
     * @throws ControlledTemperatusException
     */
    abstract void readData() throws ControlledTemperatusException;

    /**
     * Check if given file has the required format
     *
     * @throws ControlledTemperatusException
     */
    protected abstract void checkIfFileIsValid() throws ControlledTemperatusException;

    public File getReadedFile() {
        return fileToRead;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public String getSampleRate() {
        return sampleRate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

}
