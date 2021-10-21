public class Grammar {
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

    public enum Info {
        inputFile("input_file"),
        outputFile("output_file"),
        separator("="),
        bufferSize("buffer_size"),
        mode("mode");

        private final String type;

        Info(String  info){
            this.type = info;
        }
        public String getType(){
            return this.type;
        }
    }
}
