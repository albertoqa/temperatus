package temperatus.analysis.pojo;

import temperatus.importer.AbstractImporter;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Position;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Validated data of a mission, used to calculate possible errors on the measurements and allow the user to fix them
 * <p>
 * Created by alberto on 2/4/16.
 */
public class ValidatedData {

    private Position position;  // position where the data was measured
    private Ibutton ibutton;    // device used to measure the data
    private File dataFile;      // file where the data is saved
    private List<Measurement> measurements; // complete list of measurements
    private List<Measurement> possibleErrors;    // possible errors on the data detected (outliers...)
    private String deviceModel; // model of the device used to measure the data
    private String deviceSerial;    // serial of the device
    private String sampleRate;  // sample rate of measure
    private Date startDate; // start date of the experiment
    private Date finishDate;    // end date of the experiment

    public ValidatedData(AbstractImporter importedData) {
        if (importedData != null) {
            this.dataFile = importedData.getReadedFile();
            this.deviceModel = importedData.getDeviceModel();
            this.deviceSerial = importedData.getDeviceSerial();
            this.measurements = importedData.getMeasurements();
            this.sampleRate = importedData.getSampleRate();
            this.startDate = importedData.getStartDate();
            this.finishDate = importedData.getFinishDate();
        }
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Ibutton getIbutton() {
        return ibutton;
    }

    public void setIbutton(Ibutton ibutton) {
        this.ibutton = ibutton;
    }

    public File getDataFile() {
        return dataFile;
    }

    public void setDataFile(File dataFile) {
        this.dataFile = dataFile;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public List<Measurement> getPossibleErrors() {
        return possibleErrors;
    }

    public void setPossibleErrors(List<Measurement> possibleErrors) {
        this.possibleErrors = possibleErrors;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    public String getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }
}
