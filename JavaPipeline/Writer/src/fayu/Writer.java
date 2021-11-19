package fayu;

import com.java_polytech.pipeline_interfaces.IWriter;
import com.java_polytech.pipeline_interfaces.RC;
import config.Grammar;
import config.ConfigReader;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;



public class Writer implements IWriter {
    class WriterGrammar extends Grammar {
        static final String bufferSize = "buffer_size";
        @Override
        protected void setGrammar() {
            this.grammarList.addAll(List.of(bufferSize));
        }
    }
    private OutputStream outputStream;
    private BufferedOutputStream bufferOutput;
    private int bufferSize = 0;
    private final int maxBufferSize = 10000000;

    @Override
    public RC setOutputStream(OutputStream outputStream) {
        if (outputStream == null)
            return RC.RC_WRITER_FAILED_TO_WRITE;
        this.outputStream = outputStream;
        if(bufferSize != 0)
            bufferOutput = new BufferedOutputStream(this.outputStream, bufferSize);
        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConfig(String configPath) {
        Grammar grammar = new WriterGrammar();
        ConfigReader configer = new ConfigReader(configPath, grammar, RC.RCWho.READER);
        RC rc = configer.ReadConfig();
        if(!rc.isSuccess())
            return rc;

        rc = checkValidOfBuffer(configer.configElements.get(WriterGrammar.bufferSize));
        if(!rc.isSuccess())
            return rc;

        if(outputStream != null && bufferOutput == null)
            bufferOutput = new BufferedOutputStream(this.outputStream, bufferSize);
        return RC.RC_SUCCESS;
    }

    RC checkValidOfBuffer(String buffer){
        int tmpSize = 0;
        try{
            tmpSize = Integer.parseInt(buffer);
        }
        catch (NumberFormatException ex){
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        }

        if(tmpSize > 0 && tmpSize <= maxBufferSize){
            bufferSize = tmpSize;
            return RC.RC_SUCCESS;
        }

        return RC.RC_WRITER_CONFIG_SEMANTIC_ERROR;
    }

    @Override
    public RC consume(byte[] bytes) {
        if (bytes == null) {
            try {
                bufferOutput.flush();
            }
            catch (IOException e) {
                return RC.RC_WRITER_FAILED_TO_WRITE;
            }
            return RC.RC_SUCCESS;
        }
        try {
            bufferOutput.write(bytes);
        }
        catch (IOException e) {
            return RC.RC_WRITER_FAILED_TO_WRITE;
        }
        return RC.RC_SUCCESS;
    }
}
