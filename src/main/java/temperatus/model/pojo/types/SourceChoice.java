package temperatus.model.pojo.types;

import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.Record;

import java.io.File;

/**
 * Used to add data to a mission (in Comboboxes). User can select data from a file (then file will be set)
 * or from a connected device (then iButton will be set). If both of them are set, we will
 * always prefer the iButton.
 * <p>
 * Created by alberto on 5/2/16.
 */
public class SourceChoice {

    private File file;          // csv file
    private Ibutton ibutton;    // connected device
    private Record record;  // if it is an update only show the name

    public SourceChoice(Ibutton ibutton) {
        this.ibutton = ibutton;
    }

    public SourceChoice(File file) {
        this.file = file;
    }

    public SourceChoice(Record record) {
        this.record = record;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Ibutton getIbutton() {
        return ibutton;
    }

    public void setIbutton(Ibutton ibutton) {
        this.ibutton = ibutton;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    /**
     * Check if passed button serial is the same as the stored one
     *
     * @param serial button serial
     * @return is the same?
     */
    public boolean isSameiButton(String serial) {
        return serial.equals(this.ibutton.getSerial());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceChoice that = (SourceChoice) o;

        if (file != null ? !file.equals(that.file) : that.file != null) return false;
        return !(ibutton != null ? !ibutton.equals(that.ibutton) : that.ibutton != null);

    }

    @Override
    public int hashCode() {
        int result = file != null ? file.hashCode() : 0;
        result = 31 * result + (ibutton != null ? ibutton.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (ibutton != null) {
            if(ibutton.getAlias() != null && ibutton.getAlias().length() > 0) {
                return ibutton.getAlias();
            }
            return ibutton.getSerial();
        } else if (file == null) {
            return record.getIbutton().getAlias() != null ? record.getIbutton().getAlias() : record.getIbutton().getSerial();
        } else {
            return file.getName();
        }
    }
}
