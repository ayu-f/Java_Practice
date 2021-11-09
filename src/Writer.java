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

    /**
     * BRIEF:
     * write output file
     * ARGS:
     * arrByte - array to reading the bytes
     * RETURN:
     * None
     */
    public void WriteOutputFile(byte[] arrByte) {
        if(arrByte == null)
            return;
        try {
            outputStream.write(arrByte, 0, arrByte.length);
        } catch (IOException ex) {
            Error.UpdError(Error.ErrorCode.WRITE_ERROR);
            logger.log(Level.SEVERE, Log.LogItems.LOG_FAILED_TO_WRITE.getTitle());
        }
    }
}
