package temperatus.util;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLSX (*.xlsx)", "*.xlsx"));   //Set extension filter
        return fileChooser.showSaveDialog(window);
    }

    /**
     * Show save dialog allowing to choose excel and csv files
     *
     * @param name   default name of the file
     * @param dir    default directory
     * @param window owner window
     * @return file to save
     */
    public static File saveCSVAndExcelDialog(String name, File dir, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));      //Set extension filter
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLSX (*.xlsx)", "*.xlsx"));   //Set extension filter

        if (name != null) {
            fileChooser.setInitialFileName(name);
        }
        if (dir != null) {
            fileChooser.setInitialDirectory(dir);
        }

        return fileChooser.showSaveDialog(window);
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
