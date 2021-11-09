import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;



public class Configer {
    final String configPath;
    Logger logger;
    String mode;
    /** name if input file*/
    String inputFile;
    /** name if output file*/
    String outputFile;
    /** buffer size*/
    String bufferSize;

    Configer(String configPath, Logger logger){
        this.configPath = configPath;
        this.logger = logger;

        mode = inputFile = outputFile = bufferSize = null;
    }

    /**
     * BRIEF:
     * read config file
     * ARGS:
     * None
     * RETURN:
     * None
     */
    public void ReadConfig(){
        try {
            BufferedReader bufRead = new BufferedReader(new FileReader(configPath));
            String readLine;

            while((readLine = bufRead.readLine()) != null){
                String[] elems = readLine.split(Grammar.Info.separator.getType());

                /** check left param in config file */
                if(elems.length == 2){
                    if(elems[0].trim().equalsIgnoreCase(Grammar.Info.inputFile.getType())){
                        inputFile = elems[1].trim();
                    }
                    else if(elems[0].trim().equalsIgnoreCase(Grammar.Info.outputFile.getType())){
                        outputFile = elems[1].trim();
                    }
                    else if(elems[0].trim().equalsIgnoreCase(Grammar.Info.bufferSize.getType())){
                        bufferSize = elems[1].trim();
                    }
                    else if(elems[0].trim().equalsIgnoreCase(Grammar.Info.mode.getType())){
                        mode = elems[1].trim();
                    }
                    else{
                        Error.UpdError(Error.ErrorCode.CONFIG_ERROR);
                        logger.log(Level.SEVERE, Log.LogItems.LOG_CONFIG_GRAMMAR_ERROR.getTitle());
                        return;
                    }
                }
                else{
                    Error.UpdError(Error.ErrorCode.CONFIG_ERROR);
                    logger.log(Level.SEVERE, Log.LogItems.LOG_CONFIG_GRAMMAR_ERROR.getTitle());
                    return;
                }
            }

            if(inputFile == null || outputFile == null || bufferSize == null || mode == null) {
                logger.log(Level.SEVERE, Log.LogItems.LOG_CONFIG_GRAMMAR_ERROR.getTitle());
                Error.UpdError(Error.ErrorCode.CONFIG_ERROR);
            }

            bufRead.close();
        }
        catch (IOException ex){
            Error.UpdError(Error.ErrorCode.CONFIG_ERROR);
            logger.log(Level.SEVERE, Log.LogItems.LOG_FAILED_TO_READ.getTitle());
            return;
        }
    }

}
