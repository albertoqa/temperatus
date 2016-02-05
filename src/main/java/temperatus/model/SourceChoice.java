package temperatus.model;

import temperatus.model.pojo.Ibutton;

import java.io.File;

/**
 * Created by alberto on 5/2/16.
 */
public class SourceChoice {

    File file;
    Ibutton ibutton;

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

    @Override
    public String toString() {
        if(ibutton != null) {
            return ibutton.getSerial();
        } else if(file == null) {
            return "Import from file";
        } else if(file != null) {
            return file.getPath();
        } else {
            return "Error";
        }
    }
}
