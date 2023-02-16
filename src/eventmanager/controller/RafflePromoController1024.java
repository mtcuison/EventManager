/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package eventmanager.controller;

import eventmanager.base.LMasDetTran;
import eventmanager.base.RafflePromo;
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
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.constants.EditMode;

/**
 * FXML Controller class
 *
 * @author User
 */
public class RafflePromoController1024 implements Initializable, ScreenInterface {

    @FXML
    private AnchorPane AnchorMainPanaloInfo,searchBar;
    @FXML
    private StackPane stackPane;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnStart,btnStop,btnSave;
    @FXML
    private Pane btnMin;
    @FXML
    private BorderPane body;
    @FXML
    private Pane btnExit;
    @FXML
    private Label lblWinner, lblRaffleNo, lblAddress, lblMobileNo,lblBranch;

    private GRider oApp;
    private RafflePromo oTrans;
    private LMasDetTran oListener;
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
    private final ObservableList<ClientInfoModel> cliententry_data= FXCollections.observableArrayList();
    private ClientInfoModel model;
    private ClientInfoModel entry;
 
        private Stage getStage(){
  return (Stage) lblWinner.getScene().getWindow();
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        System.out.println(screenBounds.getWidth());
        oListener = new LMasDetTran() {
            @Override
            public void MasterRetreive(int i, Object o) {
                System.out.println("index = " + i);
                System.out.println("object = " + o.toString());
                
            }

            @Override
            public void DetailRetreive(int i, Object o) {
            }
        };
        initClass();
        pbLoaded = true;
        loadClient();
//        lblCongrats.setVisible(false);
        btnStart.setOnAction(this::cmdButton_Click);
        btnStop.setOnAction(this::cmdButton_Click);
        btnSave.setOnAction(this::cmdButton_Click);
        

        
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
//      text field focus
       
    }    
     

    @Override
    public void setGRider(GRider foValue) {
        oApp = foValue;
    }
    private void initClass(){
        oTrans = new RafflePromo(oApp, oApp.getBranchCode(), false);
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
//                    clientinfo_data.add(new ClientInfoModel(String.valueOf(lnCtr),
//                            (String) oTrans.getMaster(lnCtr,"sAttndIDx"),
//                            oTrans.getMaster(lnCtr,"sAttendNm").toString(),
//                            oTrans.getMaster(lnCtr,"cPresentx").toString(),
//                            oTrans.getMaster(lnCtr,"cMailSent").toString(),
//                            oTrans.getMaster(lnCtr,"cPrintedx").toString(),
//                            oTrans.getMaster(lnCtr,"cRaffledx").toString(),
//                            oTrans.getMaster(lnCtr,"cPrintedx").toString(),
//                            oTrans.getMaster(lnCtr,"nEntryNox").toString(),
//                            (String) oTrans.getMaster(lnCtr,"sEventIDx")));
                    entry = new ClientInfoModel(String.valueOf(lnCtr),
                            (String) oTrans.getMaster(lnCtr,"sTransNox"),
                            oTrans.getMaster(lnCtr,"sRaffleNo").toString(),
                            oTrans.getMaster(lnCtr,"cRaffledx").toString());
                }
            }
        } catch (SQLException ex) {
            if(pbStart){
                thread.stop();
                pbStart = false;
            }
            System.out.println("SQLException" + ex.getMessage());
        } catch (NullPointerException ex) {
            if(pbStart){
                thread.stop();
                pbStart = false;
            }
            System.out.println("NullPointerException" + ex.getMessage());
        } catch (DateTimeException ex) {
            if(pbStart){
                thread.stop();
                pbStart = false;
            }
//            MsgBox.showOk(ex.getMessage());
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
                        createNewThread();
                        clearFields();
                        pbStart = true;
//                        lblCongrats.setVisible(false);
                        body.getStyleClass().removeAll("body");
                        if (thread.getState().toString().equalsIgnoreCase("new"))
                            thread.start();
                        else thread.resume();

                        
                    break;
                case "btnStop": //create new transaction
                        pbLoaded = true;
                        if(pbStart){
                            if (oTrans.OpenRecord(model.getClientIndex03())){
                                thread.stop();
                             
                                pbStart = false;
    //                            glyphStart.setIcon(FontAwesomeIcon.PLAY);
                                LoadMaster();
                                if (!model.getClientIndex04().equals("")){

                                    body.getStyleClass().add("body");
                                    lblWinner.setText(model.getClientIndex04());
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
                case "btnSave":
                    if(oTrans.ActivateRecord()){
                        clearFields();
                        initClass();  
                    }
                    break;
//                case "btnClose":btnConfirm
//                    if(ShowMessageFX.OkayCancel(null, "Panalo Parameter", "Are you sure, do you want to close?") == true){
//                        unloadForm();
//                        break;
//                    } else
//                        return;
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
    
    private void LoadMaster(){
        try {
            for (int lnCtr = 1; lnCtr <= oTrans.getMasterCount(); lnCtr++){
                model = new ClientInfoModel(String.valueOf(lnCtr),
                    (String) oTrans.getMaster(lnCtr,"sTransNox"),
                    oTrans.getMaster(lnCtr,"sClientID").toString(),
                    oTrans.getMaster(lnCtr,"sClientNm").toString(),
                    oTrans.getMaster(lnCtr,"sAddressx").toString(),
                    oTrans.getMaster(lnCtr,"sMobileNo").toString(),
                    oTrans.getMaster(lnCtr,"sBranchNm").toString(),
                    oTrans.getMaster(lnCtr,"cRaffledx").toString(),
                    "",
                    "");
            }
            if(oTrans.getMasterCount()>0){
                lblRaffleNo.setText(model.getClientIndex03());
                lblWinner.setText(model.getClientIndex04());
                lblAddress.setText(model.getClientIndex05());
                lblMobileNo.setText(model.getClientIndex06());
                lblBranch.setText(model.getClientIndex07());
            }
        } catch (SQLException ex) {
            if(pbStart){
                thread.stop();
                pbStart = false;
            }
            Logger.getLogger(RafflePromoController1024.class.getName()).log(Level.SEVERE, null, ex);
        }
                
    }
    
    public void clearFields(){
        lblWinner.setText("");
        lblRaffleNo.setText("");
        lblWinner.setText("");
        lblAddress.setText("");
        lblMobileNo.setText("");
        lblBranch.setText("");
        body.getStyleClass().removeAll("body");
//        body.getStyleClass().remove("body");
//        initClass();
    }
    private void createNewThread(){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Runnable updater = new Runnable() {
                    @Override
                    public void run() {
                        loadClient();
                        LoadMaster();
                    }
                };

                while (pbStart) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                    }
                    // UI update is run on the Application thread
                    Platform.runLater(updater);
                }
            }
        });
    }
}
