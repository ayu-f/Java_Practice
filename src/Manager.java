import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Manager {
    private final Configer config;
    final int maxBufferSize = 100000000;
    Grammar.Mode mode; // mode ENCODE/DECODE
    int bufferSize;
    Reader reader;
    Writer writer;
    Executor executor;
    Logger logger;
    
    byte[] byteInput; // reading arr
    byte[] byteOutput; // writing arr

    Manager(String configPath, Logger logger){
        this.logger = logger;
        config = new Configer(configPath, logger);
    }

    /**
     * BRIEF:
     * open streams input/output
     * ARGS:
     * None
     * RETURN:
     * None
     */
    void OpenStreams(){
        try {
            reader = new Reader(config.inputFile, logger, bufferSize);
        }
        catch (FileNotFoundException ex){
            Error.UpdError(Error.ErrorCode.CONFIG_SEMANTIC_ERROR);
            logger.log(Level.SEVERE, Log.LogItems.LOG_FAILED_TO_READ.getTitle());
        }
        try {
            writer = new Writer(config.outputFile, logger, bufferSize);
        }
        catch (FileNotFoundException ex){
            Error.UpdError(Error.ErrorCode.CONFIG_SEMANTIC_ERROR);
            logger.log(Level.SEVERE, Log.LogItems.LOG_FAILED_TO_WRITE.getTitle());
        }
    }

    /**
     * BRIEF:
     * semantic analysis config params
     * ARGS:
     * None
     * RETURN:
     * None
     */
    void ParseConfig(){
        int tmpSize = 0;
        try{
            tmpSize = Integer.parseInt(config.bufferSize);
        }
        catch (NumberFormatException ex){
            Error.UpdError(Error.ErrorCode.CONFIG_SEMANTIC_ERROR);
            logger.log(Level.SEVERE, Log.LogItems.LOG_CONFIG_SEMANTIC_ERROR.getTitle());
            return;
        }

        if(tmpSize > 0 && tmpSize <= maxBufferSize){
            bufferSize = tmpSize;
        }
        else{
            Error.UpdError(Error.ErrorCode.CONFIG_SEMANTIC_ERROR);
            logger.log(Level.SEVERE, Log.LogItems.LOG_CONFIG_SEMANTIC_ERROR.getTitle());
        }

        if(config.mode.equalsIgnoreCase(Grammar.Mode.ENCODE.GetTypeMode())){
            mode = Grammar.Mode.ENCODE;
        }
        else if(config.mode.equalsIgnoreCase(Grammar.Mode.DECODE.GetTypeMode())){
            mode = Grammar.Mode.DECODE;
        }
        else{
            Error.UpdError(Error.ErrorCode.CONFIG_SEMANTIC_ERROR);
            logger.log(Level.SEVERE, Log.LogItems.LOG_CONFIG_SEMANTIC_ERROR.getTitle());
        }

    }

    /**
     * BRIEF:
     * main method. Read config and parse config params, start loop of read-encode/decode-write
     * checking the errors
     * ARGS:
     * None
     * RETURN:
     * None
     */
    public void Run(){
        config.ReadConfig();
        if(Error.errNo != Error.ErrorCode.NO_ERROR)
            return;

        ParseConfig();
        if(Error.errNo != Error.ErrorCode.NO_ERROR)
            return;

        OpenStreams();
        if(Error.errNo != Error.ErrorCode.NO_ERROR)
            return;

        byteInput = new byte[bufferSize];
        executor = new Executor();
        int readenBytesCount = 0;
        if(mode == Grammar.Mode.ENCODE) {
            while ((readenBytesCount = reader.ReadInputFile(byteInput)) != -1) {
                byteOutput = executor.encode(byteInput, readenBytesCount);
                writer.WriteOutputFile((byteOutput));
                if(Error.errNo != Error.ErrorCode.NO_ERROR)
                    return;
            }
        }
        else if(mode == Grammar.Mode.DECODE){
            while((readenBytesCount = reader.ReadInputFile(byteInput)) != -1){
                byteOutput = executor.decode(byteInput, readenBytesCount);
                writer.WriteOutputFile(byteOutput);
                if(Error.errNo != Error.ErrorCode.NO_ERROR)
                    return;
            }
        }

    }
}
