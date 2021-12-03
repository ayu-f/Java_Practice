import com.java_polytech.pipeline_interfaces.*;
import config.*;

import java.io.*;
import java.util.List;

class Manager implements IConfigurable {
    private class ManagerGrammar extends Grammar {
        static final String inputFile = "input_file";
        static final String outputFile = "output_file";
        static final String readerClassName = "reader_class";
        static final String writerClassName = "writer_class";
        static final String executorClassName = "executors_classes";
        static final String rConfigFile = "reader_config_file";
        static final String wConfigFile = "writer_config_file";
        static final String eConfigFile = "executors_config_file";

        static final String separatorForExecutors = ",";

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
    IExecutor[] executors;


    class PipelineParams {
        public final String inputFile;
        public final String outputFile;
        public final String readerClassName;
        public final String writerClassName;
        public final String[] executorClassesName;
        public final String rConfigFile;
        public final String wConfigFile;
        public final String[] exConfigFiles;

        PipelineParams(String inputFile, String outputFile, String readerClassName, String writerClassName,
                       String[] executorClassesName, String rConfigFile, String wConfigFile, String[] exConfigFiles) {
            this.inputFile = inputFile;
            this.outputFile = outputFile;
            this.readerClassName = readerClassName;
            this.writerClassName = writerClassName;
            this.executorClassesName = executorClassesName;
            this.rConfigFile = rConfigFile;
            this.wConfigFile = wConfigFile;
            this.exConfigFiles = exConfigFiles;
        }
    }

    @Override
    public RC setConfig(String configFile) {
        Grammar grammar = new ManagerGrammar();
        ConfigReader configer = new ConfigReader(configFile, grammar, RC.RCWho.MANAGER);
        RC rc = configer.ReadConfig();
        if (!rc.isSuccess())
            return rc;

        String[] tmp = configer.configElements.get(ManagerGrammar.executorClassName).
                split(ManagerGrammar.separatorForExecutors);
        String[] executors = new String[tmp.length];
        for (int i = 0; i < tmp.length; i++)
            executors[i] = tmp[i].trim();

        tmp = configer.configElements.get(ManagerGrammar.eConfigFile).
                split(ManagerGrammar.separatorForExecutors);
        String[] cfgFilesExecutors = new String[tmp.length];
        for (int i = 0; i < tmp.length; i++)
            cfgFilesExecutors[i] = tmp[i].trim();

        plParams = new PipelineParams(configer.configElements.get(ManagerGrammar.inputFile),
                configer.configElements.get(ManagerGrammar.outputFile),
                configer.configElements.get(ManagerGrammar.readerClassName),
                configer.configElements.get(ManagerGrammar.writerClassName),
                executors,
                configer.configElements.get(ManagerGrammar.rConfigFile),
                configer.configElements.get(ManagerGrammar.wConfigFile),
                cfgFilesExecutors);

        return RC.RC_SUCCESS;
    }

    RC run(String managerConfigFile) {
        RC currentRC;
        currentRC = setConfig(managerConfigFile);
        if (!currentRC.isSuccess())
            return currentRC;

        if (!(currentRC = openStreams()).isSuccess())
            return currentRC;

        // get classes
        if (plParams.executorClassesName.length != plParams.exConfigFiles.length)
            return new RC(RC.RCWho.MANAGER, RC.RCType.CODE_CUSTOM_ERROR, "The number of executors must be equal to the number of config files");

        executors = new IExecutor[plParams.executorClassesName.length];
        if (!(currentRC = getClassesByName_SetConfigs()).isSuccess())
            return currentRC;

        // set consumers
        if (executors.length == 0) {
            if (!(currentRC = reader.setConsumer(writer)).isSuccess())
                return currentRC;
        } else {
            if (!(currentRC = reader.setConsumer(executors[0])).isSuccess())
                return currentRC;

            for (int i = 1; i < executors.length; i++) {
                if (!(currentRC = executors[i - 1].setConsumer(executors[i])).isSuccess())
                    return currentRC;
            }

            if (!(currentRC = executors[executors.length - 1].setConsumer(writer)).isSuccess())
                return currentRC;
        }

        if (!(currentRC = reader.setInputStream(inputStream)).isSuccess())
            return currentRC;
        if (!(currentRC = writer.setOutputStream(outputStream)).isSuccess())
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

    private RC openStreams() {
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

    private RC getClassesByName_SetConfigs() {
        RC rc;
        // reader
        try {
            Class<?> tmp = Class.forName(plParams.readerClassName);
            if (IReader.class.isAssignableFrom(tmp))
                reader = (IReader) tmp.getDeclaredConstructor().newInstance();
            else
                return RC.RC_MANAGER_INVALID_READER_CLASS;
            // config
            if (!(rc = reader.setConfig(plParams.rConfigFile)).isSuccess())
                return rc;
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_READER_CLASS;
        }
        // writer
        try {
            Class<?> tmp = Class.forName(plParams.writerClassName);
            if (IWriter.class.isAssignableFrom(tmp))
                writer = (IWriter) tmp.getDeclaredConstructor().newInstance();
            else
                return RC.RC_MANAGER_INVALID_WRITER_CLASS;
            // config
            if (!(rc = writer.setConfig(plParams.wConfigFile)).isSuccess())
                return rc;
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_WRITER_CLASS;
        }
        // executor
        try {
            for (int i = 0; i < plParams.executorClassesName.length; i++) {
                Class<?> tmp = Class.forName(plParams.executorClassesName[i]);
                if (IExecutor.class.isAssignableFrom(tmp))
                    executors[i] = (IExecutor) tmp.getDeclaredConstructor().newInstance();
                else
                    return new RC(RC.RCWho.MANAGER, RC.RCType.CODE_CUSTOM_ERROR, "Invalid Executor name - number " + Integer.toString(i));

                if (!(rc = executors[i].setConfig(plParams.exConfigFiles[i])).isSuccess())
                    return new RC(RC.RCWho.MANAGER, RC.RCType.CODE_CUSTOM_ERROR, rc.info + " Config number = " + Integer.toString(i));
            }
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_EXECUTOR_CLASS;
        }


        return RC.RC_SUCCESS;
    }

    /*private RC SetConfigsToClasses() {
        RC rc;
        if (!(rc = reader.setConfig(plParams.rConfigFile)).isSuccess())
            return rc;

        if (!(rc = writer.setConfig(plParams.wConfigFile)).isSuccess())
            return rc;

        if (!(rc = executor.setConfig(plParams.eConfigFile)).isSuccess())
            return rc;

        return RC.RC_SUCCESS;
    }*/
}
