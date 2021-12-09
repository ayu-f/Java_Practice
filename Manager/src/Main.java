import com.java_polytech.pipeline_interfaces.RC;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) {
        if(args.length == 1){
            Logger logger = Logger.getLogger("Pipeline:Lab3");
            Manager manager = new Manager(logger);
            RC rc = manager.run(args[0]);
            if(!rc.isSuccess())
                logger.log(Level.SEVERE, "ERROR: " + rc.who.get() + " : " + rc.info);
        }
    }
}
