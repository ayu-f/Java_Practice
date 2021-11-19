package config;

import com.java_polytech.pipeline_interfaces.RC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ConfigReader {
    Grammar grammar;
    final String configPath;
    final protected RC.RCWho who;
    public HashMap<String, String> configElements;

    public ConfigReader(String configPath, Grammar grammar, RC.RCWho who) {
        this.configPath = configPath;
        this.grammar = grammar;
        this.who = who;
        configElements = new HashMap<>();
    }

    /**
     * BRIEF:
     * read config file
     * ARGS:
     * None
     * RETURN:
     * None
     */
    public RC ReadConfig() {
        //final ArrayList<String> grammar = this.grammar.setGrammar();
        grammar.setGrammar();
        try {
            BufferedReader bufRead = new BufferedReader(new FileReader(configPath));
            String readLine;

            while ((readLine = bufRead.readLine()) != null) {
                String[] elems = readLine.split(Grammar.separator);

                /** check left param in config file */
                if (elems.length == 2) {
                    String leftPart = elems[0].trim();
                    //boolean isAdded = false;
                    //int prevSize = configElements.size();

                    String key = this.grammar.getKeyGrammar(leftPart);
                    if (key == null || configElements.containsKey(leftPart)) {
                        return new RC(who, RC.RCType.CODE_CONFIG_GRAMMAR_ERROR, "Input stream error in config file");
                    }
                    configElements.put(key, elems[1].trim());
                    /*for(String gr : grammar){
                        if(leftPart.equalsIgnoreCase(gr)){
                            configElements.put(gr, elems[1].trim());
                            isAdded = true;
                            break;
                        }
                    }
                    if(!isAdded || prevSize == configElements.size()){
                        return new RC(who, RC.RCType.CODE_CONFIG_GRAMMAR_ERROR, "Input stream error in config file");
                    }*/

                } else {
                    return new RC(who, RC.RCType.CODE_CONFIG_GRAMMAR_ERROR, "Input stream error in config file");
                }
            }

            /*for(String gr : grammar){
                if(!configElements.containsKey(gr)){
                    return new RC(who, RC.RCType.CODE_CONFIG_GRAMMAR_ERROR, "Input stream error in config file");
                }
            }*/
            if (configElements.size() != this.grammar.getSizeGrammarList()) {
                return new RC(who, RC.RCType.CODE_CONFIG_GRAMMAR_ERROR, "Input stream error in config file");
            }

            bufRead.close();
        } catch (IOException ex) {
            return new RC(who, RC.RCType.CODE_CONFIG_FILE_ERROR, "Input stream error in config file");
        }
        return RC.RC_SUCCESS;
    }
}
