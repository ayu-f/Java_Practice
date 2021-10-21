import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Writer {
    FileOutputStream outputStream;
    String filePath;
    Logger logger;
    int bufferSize = 0;

    Writer(String filePath, Logger logger, int bufferSize) throws FileNotFoundException {
        this.filePath = filePath;
        this.logger = logger;

        if (bufferSize > 0)
            this.bufferSize = bufferSize;

        outputStream = new FileOutputStream(filePath);
    }

    public boolean WriteOutputFile(byte[] arrByte) {
        try {
            outputStream.write(arrByte, 0, arrByte.length);
            return true;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, Log.LogItems.LOG_FAILED_TO_WRITE.getTitle());
        }
        return false;
    }
}
