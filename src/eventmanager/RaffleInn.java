package eventmanager;

import eventmanager.base.GriderGui;
import eventmanager.base.RaffleGui;
import javafx.application.Application;
import org.rmj.appdriver.GRider;

public class RaffleInn {
    public static void main(String [] args){                
        String path;
        
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        
        System.setProperty("sys.default.path.config", path); 
        
        GRider oApp = new GRider();
        
        if (!oApp.loadEnv("gRider")) {
            System.err.println(oApp.getErrMsg());
            System.exit(1);
        }
        if (!oApp.logUser("gRider", "M001111122")) {
            System.err.println(oApp.getErrMsg());
            System.exit(1);
        }   
        
        RaffleGui instance = new RaffleGui();
        instance.setGRider(oApp);
        
        Application.launch(instance.getClass());
    }
}