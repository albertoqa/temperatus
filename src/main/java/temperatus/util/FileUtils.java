package temperatus.util;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility functions to show file dialogs
 * <p>
 * Created by alberto on 10/11/2016.
 */
public class FileUtils {

    /**
     * Show save dialog only allowing to choose excel files
     *
     * @param window owner window
     * @return file to save
     */
    public static File saveExcelDialog(Window window) {
        return saveDialog(null, window, new FileChooser.ExtensionFilter("XLSX (*.xlsx)", "*.xlsx"));
    }

    /**
     * Show save dialog allowing to choose excel and csv files
     *
     * @param name   default name of the file
     * @param window owner window
     * @return file to save
     */
    public static File saveCSVAndExcelDialog(String name, Window window) {
        return saveDialog(name, window, new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"), new FileChooser.ExtensionFilter("XLSX (*.xlsx)", "*.xlsx"));
    }

    /**
     * Show save dialog
     *
     * @param name       name for the file
     * @param window     owner window
     * @param extensions allowed extensions
     * @return file to save
     */
    public static File saveDialog(String name, Window window, FileChooser.ExtensionFilter... extensions) {
        FileChooser fileChooser = new FileChooser();

        for (FileChooser.ExtensionFilter extension : extensions) {
            fileChooser.getExtensionFilters().add(extension);      //Set extension filter
        }

        if (name != null) {
            fileChooser.setInitialFileName(name);
        }

        // if default directory, load it
        if (VistaNavigator.directory != null && !VistaNavigator.directory.isEmpty()) {
            fileChooser.setInitialDirectory(new File(VistaNavigator.directory));
        }

        return fileChooser.showSaveDialog(window);
    }

    /**
     * Show open dialog
     *
     * @param window     owner window
     * @param extensions allowed extensions
     * @return file to save
     */
    public static File openDialog(Window window, FileChooser.ExtensionFilter... extensions) {
        FileChooser fileChooser = new FileChooser();

        for (FileChooser.ExtensionFilter extension : extensions) {
            fileChooser.getExtensionFilters().add(extension);      //Set extension filter
        }

        // if default directory, load it
        if (VistaNavigator.directory != null && !VistaNavigator.directory.isEmpty()) {
            fileChooser.setInitialDirectory(new File(VistaNavigator.directory));
        }

        //Show open file dialog
        return fileChooser.showOpenDialog(window);
    }

    /**
     * Show directory dialog
     *
     * @param window owner window
     * @return directory to save to
     */
    public static File showDirectoryDialog(Window window) {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        // if default directory, load it
        if (VistaNavigator.directory != null && !VistaNavigator.directory.isEmpty()) {
            directoryChooser.setInitialDirectory(new File(VistaNavigator.directory));
        }

        return directoryChooser.showDialog(window);
    }

    /**
     * Write workbook data to the given file
     *
     * @param file     file to write
     * @param workBook workbook containing the data
     * @throws IOException
     */
    public static void writeDataToFile(File file, XSSFWorkbook workBook) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(file);  // write generated data to a file
        workBook.write(fileOut);
        fileOut.flush();
        fileOut.close();

        // TODO catch exception and show error alert
    }

}
