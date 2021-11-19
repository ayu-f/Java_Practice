package fayu;

import com.java_polytech.pipeline_interfaces.IConsumer;
import com.java_polytech.pipeline_interfaces.IReader;
import com.java_polytech.pipeline_interfaces.RC;
import config.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Reader implements IReader {
    public static final RC RC_READER_INPUT_STREAM = new RC(RC.RCWho.READER, RC.RCType.CODE_FAILED_TO_READ, "Input stream error");
    private InputStream inputStream;
    private byte[] inputBytes;
    private int bufferSize;
    private final int maxBufferSize = 10000000;
    private IConsumer consumer;

    class ReaderGrammar extends Grammar {
        static final String bufferSize = "buffer_size";
        @Override
        protected void setGrammar() {
            this.grammarList.addAll(List.of(bufferSize));
        }
    }

    @Override
    public RC setInputStream(InputStream inputStream) {
        if (inputStream == null)
            return RC.RC_READER_FAILED_TO_READ;
        this.inputStream = inputStream;
        return RC.RC_SUCCESS;
    }

    @Override
    public RC run() {
        RC rc = execute();
        if(!rc.isSuccess())
            return rc;

        rc = consumer.consume(null);
        return rc;
    }

    @Override
    public RC setConfig(String configPath) {
        Grammar grammar = new ReaderGrammar();
        ConfigReader configer = new ConfigReader(configPath, grammar, RC.RCWho.READER);
        RC rc = configer.ReadConfig();
        if(!rc.isSuccess())
            return rc;

        rc = checkValidOfBuffer(configer.configElements.get(ReaderGrammar.bufferSize));
        if(!rc.isSuccess())
            return rc;

        return RC.RC_SUCCESS;
    }

    RC checkValidOfBuffer(String buffer){
        int tmpSize;
        try{
            tmpSize = Integer.parseInt(buffer);
        }
        catch (NumberFormatException ex){
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        }

        if(tmpSize > 0 && tmpSize <= maxBufferSize){
            bufferSize = tmpSize;
        }
        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer consumer) {
        if(consumer == null)
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        this.consumer = consumer;
        return RC.RC_SUCCESS;
    }

    RC execute(){
        int countReadBytes = -1;
        byte[] tmpBytes = new byte[bufferSize];
        do {
            try {
                countReadBytes = inputStream.read(tmpBytes, 0, bufferSize);
            } catch (IOException ex) {
                return RC.RC_READER_FAILED_TO_READ;
            }

            if (countReadBytes > 0) {
                inputBytes = new byte[countReadBytes];
                System.arraycopy(tmpBytes, 0, inputBytes, 0, countReadBytes);
            } else
                inputBytes = null;
            RC rc = consumer.consume(inputBytes);
            if(!rc.isSuccess())
                return rc;
        } while (countReadBytes > 0);
        return RC.RC_SUCCESS;
    }
}
