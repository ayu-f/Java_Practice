package config;

import java.util.ArrayList;

public abstract class Grammar {
    final static String separator = "=";
    public final ArrayList<String>  grammarList = new ArrayList<>();

    protected abstract void setGrammar();

    String getKeyGrammar(String value){
        for(String gr : grammarList){
            if(gr.equalsIgnoreCase(value)){
                return gr;
            }
        }
        return null;
    }

    int getSizeGrammarList(){
        return grammarList.size();
    }
}
