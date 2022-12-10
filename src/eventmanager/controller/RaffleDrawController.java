/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package eventmanager.controller;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import eventmanager.base.LMasDetTrans;
import eventmanager.base.RaffleDraw;
import eventmanager.base.Registration;
import eventmanager.base.ScreenInterface;
import eventmanager.model.ClientInfoModel;
import static java.lang.Boolean.FALSE;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.constants.EditMode;

/**
 * FXML Controller class
 *
 * @author User
 */
public class RaffleDrawController implements Initializable, ScreenInterface {

    @FXML
    private AnchorPane AnchorMainPanaloInfo,searchBar;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnStart,btnStop;
    @FXML
    private Pane btnMin;
    @FXML
    private BorderPane body;
    @FXML
    private Pane btnExit;
    @FXML
    private Label lblWinner,lblCongrats,lblA;

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
                System.out.println("index = " + i);
                System.out.println("object = " + o.toString());
                
            }

            @Override
            public void DetailRetreive(int i, int i1, Object o) {
            }
        };
        initClass();
        pbLoaded = true;
        loadClient();
        lblCongrats.setVisible(false);
        btnStart.setOnAction(this::cmdButton_Click);
        btnStop.setOnAction(this::cmdButton_Click);
        

        
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
        
//        
//        btnCancel.setVisible(lbShow);
//        btnSave.setVisible(lbShow);
//        btnConfirm.setVisible(!lbShow);
//        
//        btnSave.setManaged(lbShow);
//        btnCancel.setManaged(lbShow);
////        btnUpdate.setVisible(!lbShow);
//        btnBrowse.setVisible(!lbShow);
//        btnNew.setVisible(!lbShow);
//        
//        txtSeeks99.setDisable(!lbShow);
//        txtField01.setDisable(true);
//        txtField02.setDisable(!lbShow);
//        txtField03.setDisable(!lbShow);
//        txtField04.setDisable(!lbShow);
//        txtField05.setDisable(!lbShow);
//        txtField06.setDisable(!lbShow);
//        txtField07.setDisable(!lbShow);
//        txtField08.setDisable(!lbShow);
//        
//       
//        if (lbShow){
//            txtSeeks99.setDisable(lbShow);
//            txtSeeks99.clear();
//            txtField02.requestFocus();
//            btnCancel.setVisible(lbShow);
//            btnSave.setVisible(lbShow);
////            btnUpdate.setVisible(!lbShow);
//            btnBrowse.setVisible(!lbShow);
//            btnNew.setVisible(!lbShow);
//            btnBrowse.setManaged(false);
//            btnNew.setManaged(false);
////            btnUpdate.setManaged(false);
//            btnConfirm.setManaged(false);
////            btnDeactivate.setManaged(false);
//        }
//        else{
//            txtSeeks99.setDisable(lbShow);
//            txtSeeks99.requestFocus();
//        }
    }
    
    private void loadClient(){
        int lnCtr;
        try {
            clientinfo_data.clear();
            
            if (oTrans.LoadRecord()){
                model = new ClientInfoModel(String.valueOf(1),
                            (String) oTrans.getMaster("sAttndIDx"),
                            oTrans.getMaster("sAttendNm").toString(),
                            oTrans.getMaster("cPresentx").toString(),
                            oTrans.getMaster("cMailSent").toString(),
                            oTrans.getMaster("cPrintedx").toString(),
                            oTrans.getMaster("cRaffledx").toString(),
                            oTrans.getMaster("cPrintedx").toString(),
                            oTrans.getMaster("nEntryNox").toString(),
                            (String) oTrans.getMaster("sEventIDx"));
//                for (lnCtr = 1; lnCtr <= oTrans.getItemCount(); lnCtr++){
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
//                    model = new ClientInfoModel(String.valueOf(lnCtr),
//                            (String) oTrans.getMaster(lnCtr,"sAttndIDx"),
//                            oTrans.getMaster(lnCtr,"sAttendNm").toString(),
//                            oTrans.getMaster(lnCtr,"cPresentx").toString(),
//                            oTrans.getMaster(lnCtr,"cMailSent").toString(),
//                            oTrans.getMaster(lnCtr,"cPrintedx").toString(),
//                            oTrans.getMaster(lnCtr,"cRaffledx").toString(),
//                            oTrans.getMaster(lnCtr,"cPrintedx").toString(),
//                            oTrans.getMaster(lnCtr,"nEntryNox").toString(),
//                            (String) oTrans.getMaster(lnCtr,"sEventIDx"));
//                }
            }
        } catch (SQLException ex) {
            System.out.println("SQLException" + ex.getMessage());
        } catch (NullPointerException ex) {
            System.out.println("NullPointerException" + ex.getMessage());
        } catch (DateTimeException ex) {
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
                        lblCongrats.setVisible(false);
                        body.getStyleClass().removeAll("body");
                        if (thread.getState().toString().equalsIgnoreCase("new"))
                            thread.start();
                        else thread.resume();

                        
                    break;
                case "btnStop": //create new transaction
                        pbLoaded = true;
                        thread.stop();
                        
                        if(pbStart){
                            if (oTrans.ActivateRecord(model.getClientIndex02())){
                                pbStart = false;
    //                            glyphStart.setIcon(FontAwesomeIcon.PLAY);
                                if (!model.getClientIndex03().equals("")){
                                    lblCongrats.setVisible(true);

                                    body.getStyleClass().add("body");
                                    lblWinner.setText(oTrans.getMaster("sAttendNm").toString());
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
            if(oTrans.getItemCount()>0){
                lblWinner.setText(model.getClientIndex03());
            }
        } catch (SQLException ex) {
            Logger.getLogger(RaffleDrawController.class.getName()).log(Level.SEVERE, null, ex);
        }
                
    }
    
    public void clearFields(){
        lblWinner.setText("");
        lblCongrats.setVisible(false);
        body.getStyleClass().removeAll("body");
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
