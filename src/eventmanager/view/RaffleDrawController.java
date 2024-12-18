/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package eventmanager.view;

import eventmanager.base.LMasDetTrans;
import eventmanager.base.RaffleDraw;
import eventmanager.base.ScreenInterface;
import eventmanager.model.ClientInfoModel;
import java.net.URL;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.constants.EditMode;

/**
 * FXML Controller class
 *
 * @author User
 */
public class RaffleDrawController implements Initializable, ScreenInterface {
    @FXML
    private AnchorPane AnchorMainPanaloInfo;
    @FXML
    private Button btnStart,btnStop;
    @FXML
    private Pane btnMin;
    @FXML
    private BorderPane body;
    @FXML
    private Pane btnExit;
    @FXML
    private Label lblWinner;

    private GRider oApp;
    private RaffleDraw oTrans;
    private LMasDetTrans oListener;
    private Thread thread;
    
    private boolean pbLoaded = false;
    private boolean pbStart = false;
    private int pnRow = -1;
    private int oldPnRow = -1;
    private int pnEditMode;
    private int pagecounter;
    private String oldTransNo = "";
    private String TransNo = "";
    
    private static final int ROWS_PER_PAGE = 30;
  
    private FilteredList<ClientInfoModel> filteredData;

    private final ObservableList<ClientInfoModel> clientinfo_data= FXCollections.observableArrayList();
    private ClientInfoModel model;
 
    private Stage getStage(){
        return (Stage) lblWinner.getScene().getWindow();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        oListener = new LMasDetTrans() {
            @Override
            public void MasterRetreive(int i, Object o) {
            }

            @Override
            public void DetailRetreive(int i, int i1, Object o) {
            }
        };
        initClass();
        pbLoaded = true;
        loadClient();
        btnStart.setOnAction(this::cmdButton_Click);
        btnStop.setOnAction(this::cmdButton_Click);
        clearFields();
        
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
//      text field focus
       
    }    
     

    @Override
    public void setGRider(GRider foValue) {
        oApp = foValue;
    }
    private void initClass(){
        oTrans = new RaffleDraw(oApp, oApp.getBranchCode(), false);
        oTrans.setListener(oListener);
        oTrans.setWithUI(true);
        pnEditMode = EditMode.UNKNOWN;
    }
    private void initButton(int fnValue){
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
    }
    
    private void loadClient(){
        int lnCtr;
        try {
            clientinfo_data.clear();
            if (oTrans.LoadRecord()){
                for (lnCtr = 1; lnCtr <= oTrans.getItemCount(); lnCtr++){
                    model = new ClientInfoModel(String.valueOf(lnCtr),
                            (String) oTrans.getMaster(lnCtr,"sAttndIDx"),
                            oTrans.getMaster(lnCtr,"sAttendNm").toString().replace("Ã±", "ñ"),
                            oTrans.getMaster(lnCtr,"cPresentx").toString(),
                            oTrans.getMaster(lnCtr,"cMailSent").toString(),
                            oTrans.getMaster(lnCtr,"cPrintedx").toString(),
                            oTrans.getMaster(lnCtr,"cRaffledx").toString(),
                            oTrans.getMaster(lnCtr,"cPrintedx").toString(),
                            oTrans.getMaster(lnCtr,"nEntryNox").toString(),
                            (String) oTrans.getMaster(lnCtr,"sEventIDx"),
                            oTrans.getMaster(lnCtr,"sCompnyNm").toString());
                    clientinfo_data.add(model);
                }
            }
        } catch (SQLException ex) {
            System.out.println("SQLException" + ex.getMessage());
        } catch (NullPointerException ex) {
            System.out.println("NullPointerException" + ex.getMessage());
        } catch (DateTimeException ex) {
            System.out.println("DateTimeException" + ex.getMessage());
        } 
    }
    @FXML
    private void handleButtonExitClick(MouseEvent event) {
        if(pbStart){
            thread.stop();
            pbStart = false;
        }
        
        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleButtonMinimizeClick(MouseEvent event) {
        Stage stage = (Stage) btnMin.getScene().getWindow();
        stage.setIconified(true);
    }
    private void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button)event.getSource()).getId();
        try {
            switch (lsButton){
                case "btnStart": //create new transaction
                    pbLoaded = true;
                    if (!pbStart){
                        createNewThread();
                        clearFields();
                        pbStart = true;
                        body.getStyleClass().removeAll("body");
                        if (thread.getState().toString().equalsIgnoreCase("new"))
                            thread.start();
                        else thread.resume();
                    }
                    break;
                case "btnStop": //create new transaction
                        pbLoaded = true;
                        if(pbStart){
                            pbStart = false;
                            //lblWinner.setText(clientinfo_data.get(0).getClientIndex11());
                            thread.stop();
                            
//                            try {
//                                lblWinner.setText(clientinfo_data.get(0).getClientIndex11());
//                                thread.sleep(2000);
//                            } catch (Exception e) {
//                            }
                            
                            if (oTrans.ActivateRecord()){
                                if (!clientinfo_data.get(0).getClientIndex03().equals("")){
                                    body.getStyleClass().add("body");
                                    lblWinner.setText(clientinfo_data.get(0).getClientIndex03());
                                    //lblWinner.setText(model.getClientIndex03());
                                }
                                else {
                                    clearFields();
                                }
                            } else{
                                clearFields();
                            }
                            
                        }else{
                            clearFields();
                        }
                        
                    break;
                case "btnCancel":
                    
                    clearFields();
                    initClass();
                    break;
            }
            
            initButton(pnEditMode);
        } catch (SQLException e) {
            e.printStackTrace();
            ShowMessageFX.Warning(getStage(),e.getMessage(), "Warning", null);
        }catch (NullPointerException e) {
            e.printStackTrace();
            ShowMessageFX.Warning(getStage(),e.getMessage(), "Warning", null);
        }
    } 
    
    public void clearFields(){
        lblWinner.setText("");
        body.getStyleClass().removeAll("body");
    }
    private void createNewThread(){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Runnable updater = new Runnable() {
                    @Override
                    public void run() {
                        if (pbStart) loadClient();
                        
                        lblWinner.setText(clientinfo_data.get(0).getClientIndex03());
                    }
                };

                while (pbStart) {
                    try {
                        Thread.sleep(30);
                        Platform.runLater(updater);
                    } catch (InterruptedException ex) {
                    }
                }
                //lblWinner.setText(clientinfo_data.get(0).getClientIndex11());
            }
        });
    }
}
