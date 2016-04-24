package temperatus.analysis.pojo;

import temperatus.model.pojo.Measurement;

import java.util.List;

/**
 * Show data of a mission when a device is selected
 * <p>
 * Created by alberto on 24/4/16.
 */
public class DeviceMissionData {

    private String inProgress;
    private String isSuta;
    private String waitingForTempAlarm;
    private String sampleRate;
    private String missionStartTime;
    private String missionSampleCount;
    private String rollOverEnabled;
    private String firstSampleTime;
    private String totalMissionSamples;
    private String totalDeviceSamples;
    private String highAlarm;
    private String lowAlarm;
    private String Resolution;
    private List<Measurement> measurements;

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
        return Resolution;
    }

    public void setResolution(String resolution) {
        Resolution = resolution;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }
}
