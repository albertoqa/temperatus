package temperatus.analysis.pojo;

import temperatus.importer.IbuttonDataImporter;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Position;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by alberto on 2/4/16.
 */
public class ValidatedData {

    Position position;
    Ibutton ibutton;
    private File dataFile;
    private List<Measurement> measurements;
    private List<Measurement> posibleErrors;
    private String deviceModel;
    private String deviceSerial;
    private String sampleRate;
    private Date startDate;
    private Date finishDate;

    public ValidatedData() {
    }

    public ValidatedData(IbuttonDataImporter importedData) {
        this.dataFile = importedData.getReadedFile();
        this.deviceModel = importedData.getDeviceModel();
        this.deviceSerial = importedData.getDeviceSerial();
        this.measurements = importedData.getMeasurements();
        this.sampleRate = importedData.getSampleRate();
        this.startDate = importedData.getStartDate();
        this.finishDate = importedData.getFinishDate();
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

    public List<Measurement> getPosibleErrors() {
        return posibleErrors;
    }

    public void setPosibleErrors(List<Measurement> posibleErrors) {
        this.posibleErrors = posibleErrors;
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
