package temperatus.model.pojo.types;

import temperatus.model.pojo.Ibutton;

import java.io.File;

/**
 * Created by alberto on 5/2/16.
 */
public class SourceChoice {

    File file;
    Ibutton ibutton;

    public SourceChoice() {
    }

    public SourceChoice(Ibutton ibutton) {
        this.ibutton = ibutton;
    }

    public SourceChoice(File file) {
        this.file = file;
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

    public boolean isSameiButton(Ibutton ibutton) {
       return ibutton.equals(this.ibutton);
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
        if(ibutton != null) {
            return ibutton.getSerial();
        } else if(file == null) {
            return "Import from file";
        } else if(file != null) {
            return file.getName();
        } else {
            return "Error";
        }
    }
}
