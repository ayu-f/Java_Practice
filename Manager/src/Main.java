import com.java_polytech.pipeline_interfaces.RC;

public class Main {
    public static void main(String[] args) {
        if(args.length == 1 && args[0] != null){
            Manager manager = new Manager();
            RC rc = manager.run(args[0]);
            if(!rc.isSuccess())
                System.out.println(rc.who.get() + " : " + rc.info);
        }
    }
}
