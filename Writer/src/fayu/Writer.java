package fayu;

import com.java_polytech.pipeline_interfaces.*;
import config.Grammar;
import config.ConfigReader;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;



public class Writer implements IWriter {
    IProvider provider;
    private byte[] outputBytes = null;
    private final TYPE[] supportedTypes = {TYPE.BYTE_ARRAY, TYPE.CHAR_ARRAY, TYPE.INT_ARRAY};
    private TYPE currentType = null;
    private IMediator mediator;

    private OutputStream outputStream;
    private BufferedOutputStream bufferOutput;
    private int bufferSize = 0;
    private final int maxBufferSize = 10000000;

    class WriterGrammar extends Grammar {
        static final String bufferSize = "buffer_size";
        @Override
        protected void setGrammar() {
            this.grammarList.addAll(List.of(bufferSize));
        }
    }

    @Override
    public RC setProvider(IProvider iProvider) {
        if(iProvider == null){
            return new RC(RC.RCWho.WRITER, RC.RCType.CODE_CUSTOM_ERROR, "Invalid IProvider");
        }
        provider = iProvider;
        RC rc = IntersectionTypes(provider.getOutputTypes());
        if(!rc.isSuccess())
            return rc;

        mediator = provider.getMediator(currentType);
        return RC.RC_SUCCESS;
    }

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

    private RC IntersectionTypes(TYPE[] providerTypes) {
        for (int i = 0; i < providerTypes.length; i++)
            for (int j = 0; j < supportedTypes.length; j++)
                if(supportedTypes[j] == providerTypes[i]){
                    currentType = supportedTypes[j];
                    return RC.RC_SUCCESS;
                }
        return RC.RC_WRITER_TYPES_INTERSECTION_EMPTY_ERROR;
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
    public RC consume() {
        outputBytes = GetBytesFromCurType();
        if (outputBytes == null) {
            try {
                bufferOutput.flush();
            }
            catch (IOException e) {
                return RC.RC_WRITER_FAILED_TO_WRITE;
            }
            return RC.RC_SUCCESS;
        }
        try {
            bufferOutput.write(outputBytes);
        }
        catch (IOException e) {
            return RC.RC_WRITER_FAILED_TO_WRITE;
        }
        return RC.RC_SUCCESS;
    }

    private byte[] GetBytesFromCurType() {
        Object data = mediator.getData();
        if(data == null)
            return null;

        if(currentType == TYPE.BYTE_ARRAY)
            return (byte[])data;
        else if(currentType == TYPE.CHAR_ARRAY){
            char[] chars = (char[])data;
            return new String(chars).getBytes(StandardCharsets.UTF_8);
        }
        else if(currentType == TYPE.INT_ARRAY){
            int[] arrInt = (int[])data;
            byte[] out = new byte[arrInt.length * 4];
            for (int i = 0; i < arrInt.length; i++) {
                int j = i * 2;
                out[j++] = (byte) ((arrInt[i] & 0xFF000000) >> 24);
                out[j++] = (byte) ((arrInt[i] & 0x00FF0000) >> 16);
                out[j++] = (byte) ((arrInt[i] & 0x0000FF00) >> 8);
                out[j] = (byte) ((arrInt[i] & 0x000000FF));
            }
            return out;
        }
        return null;
    }
}
