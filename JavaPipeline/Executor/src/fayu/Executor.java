package fayu;

import com.java_polytech.pipeline_interfaces.IConsumer;
import com.java_polytech.pipeline_interfaces.IExecutor;
import com.java_polytech.pipeline_interfaces.RC;
import config.*;
import fayu.Encoder;

import java.util.List;

public class Executor implements IExecutor {
    public static final RC RC_EXECUTOR_PROCESS = new RC(RC.RCWho.EXECUTOR, RC.RCType.CODE_CUSTOM_ERROR, "fayu.Executor process error");

    private IConsumer consumer;
    private ExecutorGrammar.Mode mode;
    private final Encoder encoder = new Encoder();

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
    public RC consume(byte[] bytes) {
        if(bytes != null){
            byte[] output = null;
            if(mode == ExecutorGrammar.Mode.ENCODE){
                output = this.encoder.encode(bytes, bytes.length);
            }
            else if(mode == ExecutorGrammar.Mode.DECODE){
                output = this.encoder.decode(bytes, bytes.length);
            }

            if(output != null){
                consumer.consume(output);
            }
        }
        else {
            if(!encoder.isRawBytesEmpty())
                return new RC(RC.RCWho.EXECUTOR, RC.RCType.CODE_CUSTOM_ERROR, "Not enough bytes to decode");
            return consumer.consume(null);
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
