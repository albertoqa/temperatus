package temperatus.analysis.pojo;

import temperatus.model.pojo.Measurement;

import java.util.List;

/**
 * Show data of a mission when a device is selected
 * <p>
 * Created by alberto on 24/4/16.
 */
public class DeviceMissionData {

    private String inProgress;              // is there a mission currently running on the device?
    private String isSuta;                  // is start upon temperature alarm configured?
    private String waitingForTempAlarm;     // is waiting for the temperature alarm?
    private String sampleRate;              // sample rate of measurement
    private String missionStartTime;        // start date-time of the mission
    private String missionSampleCount;      // number of measurements registered
    private String rollOverEnabled;         // is roll-over enabled?
    private String firstSampleTime;         // date-time of the first sample measurement
    private String totalMissionSamples;     // total number of samples registered by the mission
    private String totalDeviceSamples;      // total samples registered by the device
    private String highAlarm;               // high alarm value (if set)
    private String lowAlarm;                // low alarm value (if set)
    private String resolution;              // resolution of measurement
    private String partNumber;              // name of the device
    private String serial;                  // serial of the device
    private List<Measurement> measurements; // list of samples measured by the device

    public String getInProgress() {
        return inProgress;
    }

    public void setInProgress(String inProgress) {
        this.inProgress = inProgress;
    }

    public String getIsSuta() {
        return isSuta;
    }

    public void setIsSuta(String isSuta) {
        this.isSuta = isSuta;
    }

    public String getWaitingForTempAlarm() {
        return waitingForTempAlarm;
    }

    public void setWaitingForTempAlarm(String waitingForTempAlarm) {
        this.waitingForTempAlarm = waitingForTempAlarm;
    }

    public String getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getMissionStartTime() {
        return missionStartTime;
    }

    public void setMissionStartTime(String missionStartTime) {
        this.missionStartTime = missionStartTime;
    }

    public String getMissionSampleCount() {
        return missionSampleCount;
    }

    public void setMissionSampleCount(String missionSampleCount) {
        this.missionSampleCount = missionSampleCount;
    }

    public String getRollOverEnabled() {
        return rollOverEnabled;
    }

    public void setRollOverEnabled(String rollOverEnabled) {
        this.rollOverEnabled = rollOverEnabled;
    }

    public String getFirstSampleTime() {
        return firstSampleTime;
    }

    public void setFirstSampleTime(String firstSampleTime) {
        this.firstSampleTime = firstSampleTime;
    }

    public String getTotalMissionSamples() {
        return totalMissionSamples;
    }

    public void setTotalMissionSamples(String totalMissionSamples) {
        this.totalMissionSamples = totalMissionSamples;
    }

    public String getTotalDeviceSamples() {
        return totalDeviceSamples;
    }

    public void setTotalDeviceSamples(String totalDeviceSamples) {
        this.totalDeviceSamples = totalDeviceSamples;
    }

    public String getHighAlarm() {
        return highAlarm;
    }

    public void setHighAlarm(String highAlarm) {
        this.highAlarm = highAlarm;
    }

    public String getLowAlarm() {
        return lowAlarm;
    }

    public void setLowAlarm(String lowAlarm) {
        this.lowAlarm = lowAlarm;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }
}
