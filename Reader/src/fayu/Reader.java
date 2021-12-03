package fayu;

import com.java_polytech.pipeline_interfaces.*;
import config.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Reader implements IReader {
    private InputStream inputStream;
    private byte[] inputBytes = null;
    private int bufferSize;
    private final int maxBufferSize = 10000000;
    private IConsumer consumer;

    private int countReadBytes = -1;
    private final TYPE[] supportedTypes = {TYPE.BYTE_ARRAY, TYPE.CHAR_ARRAY, TYPE.INT_ARRAY};

    class ReaderGrammar extends Grammar {
        static final String bufferSize = "buffer_size";

        @Override
        protected void setGrammar() {
            this.grammarList.addAll(List.of(bufferSize));
        }
    }

    class ByteMediator implements IMediator {
        @Override
        public Object getData() {
            if (inputBytes == null) {
                return null;
            }
            byte[] data = new byte[countReadBytes];
            System.arraycopy(inputBytes, 0, data, 0, countReadBytes);
            return data;
        }
    }

    class CharMediator implements IMediator {
        @Override
        public Object getData() {
            if (inputBytes == null) {
                return null;
            }
            return new String(inputBytes, StandardCharsets.UTF_8).toCharArray();
        }
    }

    class IntMediator implements IMediator {
        @Override
        public Object getData() {
            if (inputBytes == null) {
                return null;
            }
            IntBuffer intBuffer = ByteBuffer.wrap(inputBytes).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
            int[] arr = new int[intBuffer.remaining()];
            intBuffer.get(arr);
            return arr;
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
        if (!rc.isSuccess())
            return rc;

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConfig(String configPath) {
        Grammar grammar = new ReaderGrammar();
        ConfigReader configer = new ConfigReader(configPath, grammar, RC.RCWho.READER);
        RC rc = configer.ReadConfig();
        if (!rc.isSuccess())
            return rc;

        rc = checkValidOfBuffer(configer.configElements.get(ReaderGrammar.bufferSize));
        if (!rc.isSuccess())
            return rc;

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer consumer) {
        if (consumer == null)
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        this.consumer = consumer;
        RC rc = consumer.setProvider(this);
        if (!rc.isSuccess())
            return rc;
        return RC.RC_SUCCESS;
    }

    @Override
    public TYPE[] getOutputTypes() {
        return supportedTypes;
    }

    @Override
    public IMediator getMediator(TYPE type) {
        switch (type){
            case BYTE_ARRAY -> {
                return new ByteMediator();
            }
            case CHAR_ARRAY -> {
                return new CharMediator();
            }
            case INT_ARRAY -> {
                return new IntMediator();
            }
            default -> {
                return null;
            }
        }
    }

    RC execute() {
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


            RC rc = consumer.consume();
            if (!rc.isSuccess())
                return rc;
        } while (countReadBytes > 0);
        return RC.RC_SUCCESS;
    }

    RC checkValidOfBuffer(String buffer) {
        int tmpSize;
        try {
            tmpSize = Integer.parseInt(buffer);
        } catch (NumberFormatException ex) {
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        }

        if (tmpSize > 0 && tmpSize <= maxBufferSize) {
            bufferSize = tmpSize;
        }
        return RC.RC_SUCCESS;
    }
}
