import java.util.logging.Logger;
import java.util.logging.Level;


public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("Lab1");
        if(args.length == 1 && args[0] != null){
           Manager manager = new Manager(args[0], logger);
           manager.Run();
        }
        else{
            logger.log(Level.SEVERE, Log.LogItems.LOG_INVALID_ARGUMENT.getTitle());
        }
    }
}
