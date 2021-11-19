import com.java_polytech.pipeline_interfaces.*;
import config.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Manager implements IConfigurable {
    private class ManagerGrammar extends Grammar {
        static final String inputFile = "input_file";
        static final String outputFile = "output_file";
        static final String readerClassName = "reader_class";
        static final String writerClassName = "writer_class";
        static final String executorClassName = "executor_class";
        static final String rConfigFile = "reader_config_file";
        static final String wConfigFile = "writer_config_file";
        static final String eConfigFile = "executor_config_file";

        @Override
        protected void setGrammar() {
            this.grammarList.addAll(List.of(inputFile, outputFile, readerClassName, writerClassName,
                    executorClassName, rConfigFile, wConfigFile, eConfigFile));
        }
    }

    PipelineParams plParams;
    InputStream inputStream;
    OutputStream outputStream;

    IReader reader;
    IWriter writer;
    IExecutor executor;


    class PipelineParams {
        public final String inputFile;
        public final String outputFile;
        public final String readerClassName;
        public final String writerClassName;
        public final String executorClassName;
        public final String rConfigFile;
        public final String wConfigFile;
        public final String eConfigFile;

        PipelineParams(String inputFile, String outputFile, String readerClassName, String writerClassName,
                       String executorClassName, String rConfigFile, String wConfigFile, String eConfigFile) {
            this.inputFile = inputFile;
            this.outputFile = outputFile;
            this.readerClassName = readerClassName;
            this.writerClassName = writerClassName;
            this.executorClassName = executorClassName;
            this.rConfigFile = rConfigFile;
            this.wConfigFile = wConfigFile;
            this.eConfigFile = eConfigFile;
        }
    }

    @Override
    public RC setConfig(String configFile) {
        Grammar grammar = new ManagerGrammar();
        ConfigReader configer = new ConfigReader(configFile, grammar, RC.RCWho.MANAGER);
        RC rc = configer.ReadConfig();
        if (!rc.isSuccess())
            return rc;
        plParams = new PipelineParams(configer.configElements.get(ManagerGrammar.inputFile),
                configer.configElements.get(ManagerGrammar.outputFile),
                configer.configElements.get(ManagerGrammar.readerClassName),
                configer.configElements.get(ManagerGrammar.writerClassName),
                configer.configElements.get(ManagerGrammar.executorClassName),
                configer.configElements.get(ManagerGrammar.rConfigFile),
                configer.configElements.get(ManagerGrammar.wConfigFile),
                configer.configElements.get(ManagerGrammar.eConfigFile));

        return RC.RC_SUCCESS;
    }


    RC run(String managerConfigFile) {
        RC currentRC;
        currentRC = setConfig(managerConfigFile);
        if (!currentRC.isSuccess())
            return currentRC;

        if (!(currentRC = OpenStreams()).isSuccess())
            return currentRC;
        if (!(currentRC = GetClassesByName()).isSuccess())
            return currentRC;
        if (!(currentRC = reader.setConsumer(executor)).isSuccess())
            return currentRC;
        if (!(currentRC = executor.setConsumer(writer)).isSuccess())
            return currentRC;


        if (!(currentRC = reader.setInputStream(inputStream)).isSuccess())
            return currentRC;
        if (!(currentRC = writer.setOutputStream(outputStream)).isSuccess())
            return currentRC;
        if (!(currentRC = SetConfigsToClasses()).isSuccess())
            return currentRC;

        // start pipeline
        currentRC = reader.run();
        if (!currentRC.isSuccess())
            return currentRC;

        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            return RC.RC_MANAGER_INVALID_OUTPUT_FILE;
        }
        return currentRC;
    }

    private RC OpenStreams() {
        try {
            inputStream = new FileInputStream(plParams.inputFile);
        } catch (IOException e) {
            return RC.RC_MANAGER_INVALID_INPUT_FILE;
        }

        try {
            outputStream = new FileOutputStream(plParams.outputFile);
        } catch (IOException e) {
            return RC.RC_MANAGER_INVALID_OUTPUT_FILE;
        }

        return RC.RC_SUCCESS;
    }

    private RC GetClassesByName() {
        Class<?> tmp;
        // reader
        try {
            tmp = Class.forName(plParams.readerClassName);
            if (IReader.class.isAssignableFrom(tmp))
                reader = (IReader) tmp.getDeclaredConstructor().newInstance();
            else
                return RC.RC_MANAGER_INVALID_READER_CLASS;
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_READER_CLASS;
        }
        // writer
        try {
            tmp = Class.forName(plParams.writerClassName);
            if (IWriter.class.isAssignableFrom(tmp))
                writer = (IWriter) tmp.getDeclaredConstructor().newInstance();
            else
                return RC.RC_MANAGER_INVALID_WRITER_CLASS;
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_WRITER_CLASS;
        }
        // executor
        try {
            tmp = Class.forName(plParams.executorClassName);
            if (IExecutor.class.isAssignableFrom(tmp))
                executor = (IExecutor) tmp.getDeclaredConstructor().newInstance();
            else
                return RC.RC_MANAGER_INVALID_EXECUTOR_CLASS;
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_EXECUTOR_CLASS;
        }

        return RC.RC_SUCCESS;
    }

    private RC SetConfigsToClasses() {
        RC rc;
        if (!(rc = reader.setConfig(plParams.rConfigFile)).isSuccess())
            return rc;

        if (!(rc = writer.setConfig(plParams.wConfigFile)).isSuccess())
            return rc;

        if (!(rc = executor.setConfig(plParams.eConfigFile)).isSuccess())
            return rc;

        return RC.RC_SUCCESS;
    }
}
