import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Reader {
    FileInputStream inputStream;

    String filePath;
    Logger logger;

    int bufferSize = 0;

    Reader(String filePath, Logger logger, int bufferSize) throws FileNotFoundException {
        this.filePath = filePath;
        this.logger = logger;
        if(bufferSize > 0) {
            this.bufferSize = bufferSize;
        }

        inputStream = new FileInputStream(filePath);
    }

    /**
     * BRIEF:
     * read input file
     * ARGS:
     * arrByte - array to reading the bytes
     * RETURN:
     * count of read bytes
     */
    public int ReadInputFile(byte[] arrByte){
        int isRead = -1;
        try {
            isRead = inputStream.read(arrByte, 0, bufferSize);
            //isRead = bufRead.read(arrChar, 0, arrChar.length - 1);
            return isRead;
        }
        catch (IOException ex){
            Error.UpdError(Error.ErrorCode.READ_ERROR);
            logger.log(Level.SEVERE, Log.LogItems.LOG_FAILED_TO_READ.getTitle());
        }
        return isRead;
    }
}
