import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Manager {
    private final Configer config;
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

    // открытие потоков чтение/запись, инициалищация reader/writer
    void OpenStreams(){
        try {
            reader = new Reader(config.inputFile, logger, bufferSize);
        }
        catch (FileNotFoundException ex){
            logger.log(Level.SEVERE, Log.LogItems.LOG_FAILED_TO_READ.getTitle());
        }
        try {
            writer = new Writer(config.outputFile, logger, bufferSize);
        }
        catch (FileNotFoundException ex){
            logger.log(Level.SEVERE, Log.LogItems.LOG_FAILED_TO_WRITE.getTitle());
        }
    }

    // семантический разбор параметров конфига
    void ParseConfig(){
        int tmpSize = 0;
        try{
            tmpSize = Integer.parseInt(config.bufferSize);
        }
        catch (NumberFormatException ex){
            logger.log(Level.SEVERE, Log.LogItems.LOG_CONFIG_SEMANTIC_ERROR.getTitle());
            return;
        }

        if(tmpSize > 0 && tmpSize <= 100000000){
            bufferSize = tmpSize;
        }
        else{
            logger.log(Level.SEVERE, Log.LogItems.LOG_CONFIG_SEMANTIC_ERROR.getTitle());
        }

        if(config.mode.equalsIgnoreCase(Grammar.Mode.ENCODE.GetTypeMode())){
            mode = Grammar.Mode.ENCODE;
        }
        else if(config.mode.equalsIgnoreCase(Grammar.Mode.DECODE.GetTypeMode())){
            mode = Grammar.Mode.DECODE;
        }
        else{
            logger.log(Level.SEVERE, Log.LogItems.LOG_CONFIG_SEMANTIC_ERROR.getTitle());
        }

    }

    public void Run(){
        //ArrayList<Match> match;
        if(!config.ReadConfig()){
            return;
        }
        ParseConfig();
        OpenStreams();

        byteInput = new byte[bufferSize];
        executor = new Executor(bufferSize);
        Tmp tmp = new Tmp();
        int readenBytesCount = 0;
        if(mode == Grammar.Mode.ENCODE) {
            while ((readenBytesCount = reader.ReadInputFile(byteInput)) != -1) {
                byteOutput = executor.Encode(byteInput);
                //byteOutput = tmp.compress(byteInput, readenBytesCount);
                writer.WriteOutputFile((byteOutput));
            }
        }
        else if(mode == Grammar.Mode.DECODE){
            while((readenBytesCount = reader.ReadInputFile(byteInput)) != -1){
                byteOutput = executor.Decode(byteInput);
                //byteOutput = tmp.decode(byteInput, readenBytesCount);
                writer.WriteOutputFile(byteOutput);
            }
        }

    }
}
