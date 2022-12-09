/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eventmanager.base;

import eventmanager.FXMLDocumentController;
import eventmanager.controller.RegistrationController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.rmj.appdriver.GRider;

public class GriderGui extends Application {
    public final static String pxeMainFormTitle = "Registration";
    public final static String pxeMainForm = "/eventmanager/view/Registration.fxml";
    public final static String pxeStageIcon = "/eventmanager//images/GLOGO.png";
    public static GRider oApp;
    
    @Override
    public void start(Stage stage) throws Exception {        
        FXMLLoader view = new FXMLLoader();
        view.setLocation(getClass().getResource(pxeMainForm));
        
        RegistrationController controller = new RegistrationController();
        controller.setGRider(oApp);
        
        view.setController(controller);        
        Parent parent = view.load();
        Scene scene = new Scene(parent);
//        Parent root = FXMLLoader.load(getClass().getResource(pxeMainForm));
//        Scene scene = new Scene(root);
//        stage.setScene(scene);
//        stage.show();


        //get the screen size
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image(pxeStageIcon));
        stage.setTitle(pxeMainFormTitle);
        
        // set stage as maximized but not full screen
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.centerOnScreen();
        stage.show();
    }
     /*Parameters*/
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public void setGRider(GRider foValue){
        oApp = foValue;
    }
}
