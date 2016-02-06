package temperatus.importer;

import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Measurement;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by alberto on 6/2/16.
 */
public abstract class AbstractImporter {

    protected File fileToRead;
    protected List<Measurement> measurements;
    protected String deviceModel;
    protected String deviceSerial;
    protected String sampleRate;
    protected Date startDate;
    protected Date finishDate;

    public AbstractImporter(File fileToRead) {
        this.fileToRead = fileToRead;
        measurements = new ArrayList<>();
    }

    abstract void readData();
    protected abstract void checkIfFileIsValid() throws ControlledTemperatusException;

    public File getReadedFile() {
        return fileToRead;
    }
    public String getFilePath() {
        return fileToRead.getPath();
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
    public Integer getTotalMeasurements() {
        return measurements.size();
    }
}
