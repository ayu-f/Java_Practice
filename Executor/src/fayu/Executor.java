package fayu;

import com.java_polytech.pipeline_interfaces.*;
import config.*;

import java.util.List;

public class Executor implements IExecutor {
    public static final RC RC_EXECUTOR_PROCESS = new RC(RC.RCWho.EXECUTOR, RC.RCType.CODE_CUSTOM_ERROR, "fayu.Executor process error");

    private IConsumer consumer;
    private ExecutorGrammar.Mode mode;
    private IProvider provider;
    private IMediator mediator;
    private final Encoder encoder = new Encoder();

    private final TYPE[] supportedTypes = {TYPE.BYTE_ARRAY};
    private TYPE currentType;
    private byte[] byteToOut;



    private class ExecutorGrammar extends Grammar{
        public enum Mode{
            ENCODE("ENCODE"),
            DECODE("DECODE");

            private final String type;
            Mode(String type){
                this.type = type;
            }
            public String GetTypeMode(){
                return this.type;
            }
        }

        static final String mode = "mode";

        @Override
        protected void setGrammar() {
            this.grammarList.addAll(List.of(mode));
        }
    }

    class ByteMediator implements IMediator {
        @Override
        public Object getData() {
            if (byteToOut == null) {
                return null;
            }
            byte[] data = new byte[byteToOut.length];
            System.arraycopy(byteToOut, 0, data, 0, byteToOut.length);
            return data;
        }
    }

    @Override
    public RC setProvider(IProvider iProvider) {
        if(iProvider == null){
            return new RC(RC.RCWho.EXECUTOR, RC.RCType.CODE_CUSTOM_ERROR, "Invalid IProvider");
        }
        provider = iProvider;
        RC rc = IntersectionTypes(provider.getOutputTypes());
        if(!rc.isSuccess())
            return rc;

        mediator = provider.getMediator(currentType);
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

    @Override
    public RC setConfig(String configPath) {
        Grammar grammar = new ExecutorGrammar();
        ConfigReader configer = new ConfigReader(configPath, grammar, RC.RCWho.EXECUTOR);
        RC rc = configer.ReadConfig();
        if(!rc.isSuccess())
            return rc;

        rc = checkValidMode(configer.configElements.get(ExecutorGrammar.mode));
        if(!rc.isSuccess())
            return rc;

        return RC.RC_SUCCESS;
    }

    @Override
    public RC consume() {
        byte[] bytes = (byte[])mediator.getData();
        if(bytes != null){

            if(mode == ExecutorGrammar.Mode.ENCODE){
                byteToOut = encoder.encode(bytes, bytes.length);
            }
            else if(mode == ExecutorGrammar.Mode.DECODE){
                byteToOut = encoder.decode(bytes, bytes.length);
            }

            if(byteToOut != null){
                consumer.consume();
            }
        }
        else {
            if(!encoder.isRawBytesEmpty())
                return new RC(RC.RCWho.EXECUTOR, RC.RCType.CODE_CUSTOM_ERROR, "Not enough bytes to decode");
            byteToOut = null;
            return consumer.consume();
        }

        return RC.RC_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer consumer) {
        if(consumer == null)
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        this.consumer = consumer;

        return consumer.setProvider(this);
    }

    @Override
    public TYPE[] getOutputTypes() {
        return supportedTypes;
    }

    @Override
    public IMediator getMediator(TYPE type) {
        if(type == TYPE.BYTE_ARRAY)
            return new ByteMediator();

        return null;
    }

    private RC checkValidMode(String mode){
        if(mode.equalsIgnoreCase(ExecutorGrammar.Mode.ENCODE.GetTypeMode())){
            this.mode = ExecutorGrammar.Mode.ENCODE;
        }
        else if(mode.equalsIgnoreCase(ExecutorGrammar.Mode.DECODE.GetTypeMode())){
            this.mode = ExecutorGrammar.Mode.DECODE;
        }
        else{
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        return RC.RC_SUCCESS;
    }
}
