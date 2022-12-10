package eventmanager.controller;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import eventmanager.base.LMasDetTrans;
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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.constants.EditMode;

/**
 * FXML Controller class
 *
 * @author User
 */
public class RegistrationController implements Initializable, ScreenInterface {
    @FXML
    private AnchorPane AnchorMainPanaloInfo,searchBar;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse,btnNew,btnSave,btnCancel,btnConfirm,
            btnClose;
    @FXML
    private Pane btnMin;
    
    @FXML
    private Pane btnExit;
    @FXML
    private TableView tblClients,tblRedemption;

    @FXML
    private TableColumn clientsIndex01,clientsIndex02,clientsIndex03;
    @FXML
    private TableColumn redeemIndex01,redeemIndex02,redeemIndex03,redeemIndex04,redeemIndex05,redeemIndex06;
    @FXML
    private Pagination pagination;
    @FXML
    private TextField txtField01,txtField02,txtField03,txtField04,txtField05,txtField06,txtField07,txtField08,txtSeeks99,txtSeeks98;
    @FXML
    private MenuButton menuStatus;
    @FXML
    private CheckMenuItem itemStat01,itemStat02,itemStat03;

    
    private GRider oApp;
    private Registration oTrans;
    private LMasDetTrans oListener;
    
    private boolean pbLoaded = false;
    private int pnRow = -1;
    private int oldPnRow = -1;
    private int pnEditMode;
    private int pagecounter;
    private String oldTransNo = "";
    private String TransNo = "";
    
    private static final int ROWS_PER_PAGE = 30;
  
    private FilteredList<ClientInfoModel> filteredData;

    private final ObservableList<ClientInfoModel> clientinfo_data= FXCollections.observableArrayList();
 
 
        private Stage getStage(){
	return (Stage) txtField01.getScene().getWindow();
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
        btnNew.setOnAction(this::cmdButton_Click);
        btnSave.setOnAction(this::cmdButton_Click);
        btnCancel.setOnAction(this::cmdButton_Click);
        btnConfirm.setOnAction(this::cmdButton_Click);
        btnClose.setOnAction(this::cmdButton_Click);

        
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
//      text field focus
        txtField01.focusedProperty().addListener(txtField_Focus);
        txtField02.focusedProperty().addListener(txtField_Focus);
        txtField03.focusedProperty().addListener(txtField_Focus);
        txtField04.focusedProperty().addListener(txtField_Focus);
        txtField05.focusedProperty().addListener(txtField_Focus);
        txtField06.focusedProperty().addListener(txtField_Focus);
        txtField07.focusedProperty().addListener(txtField_Focus);
        txtField08.focusedProperty().addListener(txtField_Focus);
//      text field  key pressed
        txtField08.setOnKeyPressed(this::txtField_KeyPressed);
        pagination.setPageFactory(this::createPage); 
    }    
    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, clientinfo_data.size());
        if(clientinfo_data.size()>0){
           tblClients.setItems(FXCollections.observableArrayList(clientinfo_data.subList(fromIndex, toIndex))); 
        }
        return tblClients;
    }

    @Override
    public void setGRider(GRider foValue) {
        oApp = foValue;
    }
    private void initClass(){
        oTrans = new Registration(oApp, oApp.getBranchCode(), false);
        oTrans.setListener(oListener);
        oTrans.setWithUI(true);
        pnEditMode = EditMode.UNKNOWN;
    }
    private void initButton(int fnValue){
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        
        btnCancel.setVisible(lbShow);
        btnSave.setVisible(lbShow);
        btnConfirm.setVisible(!lbShow);
        
        btnSave.setManaged(lbShow);
        btnCancel.setManaged(lbShow);
//        btnUpdate.setVisible(!lbShow);
        btnBrowse.setVisible(!lbShow);
        btnNew.setVisible(!lbShow);
        
        txtSeeks99.setDisable(!lbShow);
        txtField01.setDisable(true);
        txtField02.setDisable(!lbShow);
        txtField03.setDisable(!lbShow);
        txtField04.setDisable(!lbShow);
        txtField05.setDisable(!lbShow);
        txtField06.setDisable(!lbShow);
        txtField07.setDisable(!lbShow);
        txtField08.setDisable(!lbShow);
        
       
        if (lbShow){
            txtSeeks99.setDisable(lbShow);
            txtSeeks99.clear();
            txtField02.requestFocus();
            btnCancel.setVisible(lbShow);
            btnSave.setVisible(lbShow);
//            btnUpdate.setVisible(!lbShow);
            btnBrowse.setVisible(!lbShow);
            btnNew.setVisible(!lbShow);
            btnBrowse.setManaged(false);
            btnNew.setManaged(false);
//            btnUpdate.setManaged(false);
            btnConfirm.setManaged(false);
//            btnDeactivate.setManaged(false);
        }
        else{
            txtSeeks99.setDisable(lbShow);
            txtSeeks99.requestFocus();
        }
    }
    final ChangeListener<? super Boolean> txtField_Focus = (o,ov,nv)->{
        if (!pbLoaded) return;
        
        TextField txtField = (TextField)((ReadOnlyBooleanPropertyBase)o).getBean();
        
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
       
        String lsValue = txtField.getText();
        if (lsValue == null) return;
            
        if(!nv){ //Lost Focus
            try {
                switch (lnIndex){
                    case 2: //sPrefixNm
                        oTrans.setMaster(lnIndex, lsValue);
                        break;
                    case 3: //sLastName
                        oTrans.setMaster(lnIndex, lsValue);
                        break;
                    case 4: //sFirstNme
                        oTrans.setMaster(lnIndex, lsValue);
                        break;
                    case 5: //sMiddName
                        oTrans.setMaster(lnIndex, lsValue);
                        break;
                    case 6: //sSuffixNm
                        oTrans.setMaster(lnIndex, lsValue);
                        break;
                    case 7: //sEmailAdd
                        oTrans.setMaster(8, lsValue);
                        break;
                    case 8: //sPanaloDss
                        oTrans.setMaster(11, lsValue);
                        break;
                }
            } catch (SQLException e) {
                ShowMessageFX.Warning(getStage(),e.getMessage(), "Warning", null);
            }
            
        } else{ //Focus
//            pnIndex = lnIndex;
            txtField.selectAll();
        }
    };   
    private void txtField_KeyPressed(KeyEvent event){
        TextField txtField = (TextField)event.getSource();        
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
             
        try{
           switch (event.getCode()){
            case F3:
                switch (lnIndex){
              
                case 8: /*Search*/
                    if (oTrans.SearchPosition(txtField08.getText(), false)){
                        LoadMaster();
                    } else 
                        ShowMessageFX.Warning(getStage(), oTrans.getMessage(),"Warning", null);
                     break;
                }   
            case ENTER:
            case DOWN:
                CommonUtils.SetNextFocus(txtField); break;
            case UP:
                CommonUtils.SetPreviousFocus(txtField);
        } 
        }catch(SQLException e){
                ShowMessageFX.Warning(getStage(),e.getMessage(), "Warning", null);
        }
        
    }
    private void loadClient(){
        int lnCtr;
        try {
            clientinfo_data.clear();
            if (oTrans.LoadRecord()){
                for (lnCtr = 1; lnCtr <= oTrans.getItemCount(); lnCtr++){
                    clientinfo_data.add(new ClientInfoModel(String.valueOf(lnCtr),
                            (String) oTrans.getMaster(lnCtr,"sAttndIDx"),
                            oTrans.getMaster(lnCtr,"sPrefixNm").toString(),
                            oTrans.getMaster(lnCtr,"sLastName").toString(),
                            oTrans.getMaster(lnCtr,"sFirstNme").toString(),
                            oTrans.getMaster(lnCtr,"sMiddName").toString(),
                            oTrans.getMaster(lnCtr,"sSuffixNm").toString(),
                            oTrans.getMaster(lnCtr,"sAttendNm").toString(),
                            oTrans.getMaster(lnCtr,"sEmailAdd").toString(),
                            (String) oTrans.getMaster(lnCtr,"sCompnyID"),
                            (String) oTrans.getMaster(lnCtr,"sPositnID"),
                            (String) oTrans.getMaster(lnCtr,"sPositnNm"),
                            (String) oTrans.getMaster(lnCtr,"cAttndTyp"),
                            (String) oTrans.getMaster(lnCtr,"cIsVIPxxx"),
                            (String) oTrans.getMaster(lnCtr,"cAttndTyp"),
                            oTrans.getMaster(lnCtr,"cPresentx").toString(),
                            oTrans.getMaster(lnCtr,"cMailSent").toString(),
                            (String) oTrans.getMaster(lnCtr,"cPrintedx"),
                            (String) oTrans.getMaster(lnCtr,"cRaffledx"),
                            oTrans.getMaster(lnCtr,"nEntryNox").toString(),
                            (String) oTrans.getMaster(lnCtr,"sEventIDx")));
                }
                initGrid();
                loadTab();
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
                case "btnNew": //create new transaction
                        pbLoaded = true;
                         if (oTrans.NewRecord()){
                            
                            clearFields();
                            LoadMaster();
                            pnEditMode = oTrans.getEditMode();
                        } else{
                            ShowMessageFX.Warning(getStage(), oTrans.getMessage(),"Warning", null);
                        }
                    break;
                case "btnSave": //create new transaction
                        pbLoaded = true;
                        if (oTrans.SaveRecord()){
                            ShowMessageFX.Warning(getStage(),"Record added successfully.", "Information", null);
                            clearFields();
                            initClass();
                            loadClient();
                            pnEditMode = oTrans.getEditMode();
                        } else{
                            ShowMessageFX.Warning(getStage(), oTrans.getMessage(),"Warning", null);
                        }
                    break;
                case "btnConfirm": //Confirm
                        pbLoaded = true;
                         if(ShowMessageFX.OkayCancel(null, "Confirm", "Do you want to confirm attendance of this record?") == true){
                            if (oTrans.ActivateRecord()){
                                ShowMessageFX.Warning(getStage(),"Attendance was confirmed for this record.","Warning", null);
                                clearFields();
                                initClass();
                                loadClient();
                                pnEditMode = oTrans.getEditMode();
                            } else{
                                ShowMessageFX.Warning(getStage(), oTrans.getMessage(),"Warning", null);
                            }
                        } else
                            return;
                    break;
                case "btnCancel":
                    clearFields();
                    initClass();
                    break;
               
                case "btnClose":
                    if(ShowMessageFX.OkayCancel(null, "Raffle Draw", "Are you sure, do you want to close?") == true){
                        
                        Stage stage = (Stage) btnExit.getScene().getWindow();
                        stage.close();
                        break;
                    } else
                        return;
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
    private void initGrid() {
        
        clientsIndex01.setStyle("-fx-alignment: CENTER;");
        clientsIndex02.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        clientsIndex03.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");

       
        clientsIndex01.setCellValueFactory(new PropertyValueFactory<>("clientIndex01"));
        clientsIndex02.setCellValueFactory(new PropertyValueFactory<>("clientIndex08"));
        clientsIndex03.setCellValueFactory(new PropertyValueFactory<>("clientIndex16"));

        
        tblClients.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblClients.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
        });
        filteredData = new FilteredList<>(clientinfo_data, b -> true);
        autoSearch(txtSeeks99);

        // 3. Wrap the FilteredList in a SortedList. 
        SortedList<ClientInfoModel> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.
        // 	  Otherwise, sorting the TableView would have no effect.
        sortedData.comparatorProperty().bind(tblClients.comparatorProperty());

        // 5. Add sorted (and filtered) data to the table.
        tblClients.setItems(sortedData);
        tblClients.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblClients.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
            header.setDisable(true);
        });
        
        if(oldPnRow >= 0){
//           tblClients.getSelectionModel().select(oldPnRow);
           
            tblClients.getSelectionModel().select(oldPnRow);
           
       }
    }
    
    private void autoSearch(TextField txtField){
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        boolean fsCode = true;
        txtField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(orders-> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                // Compare order no. and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();
                if(lnIndex == 99){
                    return (orders.getClientIndex04().toLowerCase().contains(lowerCaseFilter)); // Does not match.   
                }else {
                    return (orders.getClientIndex05().toLowerCase().contains(lowerCaseFilter)); // Does not match.
                }
            });
            changeTableView(0, ROWS_PER_PAGE);
        });
        loadTab();
    } 
    
    private void loadTab(){

                int totalPage = (int) (Math.ceil(clientinfo_data.size() * 1.0 / ROWS_PER_PAGE));
                pagination.setPageCount(totalPage);
                pagination.setCurrentPageIndex(0);
                changeTableView(0, ROWS_PER_PAGE);
                pagination.currentPageIndexProperty().addListener(
                        (observable, oldValue, newValue) -> changeTableView(newValue.intValue(), ROWS_PER_PAGE));
            
    }    
    private void changeTableView(int index, int limit) {
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, clientinfo_data.size());

            int minIndex = Math.min(toIndex, filteredData.size());
            SortedList<ClientInfoModel> sortedData = new SortedList<>(
                    FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
            sortedData.comparatorProperty().bind(tblClients.comparatorProperty());
            tblClients.setItems(sortedData); 
    }
    
    @FXML
    private void itemStat01_Filter(ActionEvent event) {
         if (itemStat01.isSelected()){
         itemStat02.setSelected(FALSE);
         itemStat03.setSelected(FALSE);
         txtSeeks98.setText("0");
         }else {
             txtSeeks98.setText("");
                 }
         
     
    }

    @FXML
    private void itemStat02_Filter(ActionEvent event) {
         if (itemStat02.isSelected()){
         itemStat01.setSelected(FALSE);
         itemStat03.setSelected(FALSE);
         txtSeeks98.setText("1");
     }else {
             txtSeeks98.setText("");
                 }
    }
        @FXML
    private void itemStat03_Filter(ActionEvent event) {
         if (itemStat02.isSelected()){
         itemStat01.setSelected(FALSE);
         itemStat02.setSelected(FALSE);
         txtSeeks98.setText("4");
     }else {
             txtSeeks98.setText("");
                 }
    }
   @FXML
    void tblClients_Clicked(MouseEvent event) {
        pnRow = tblClients.getSelectionModel().getSelectedIndex(); 
        pagecounter = pnRow + pagination.getCurrentPageIndex() * ROWS_PER_PAGE;
           if (pagecounter >= 0){
            oldPnRow = pagecounter;
             if(event.getClickCount() > 0){

//                clear();
                getSelectedItem(filteredData.get(pagecounter).getClientIndex02());
//                loadRedeemDetail(filteredData.get(pagecounter).getPanaloInfoIndex02());
                tblClients.setOnKeyReleased((KeyEvent t)-> {
                    KeyCode key = t.getCode();
                    switch (key){
                        case DOWN:
                            pnRow = tblClients.getSelectionModel().getSelectedIndex();
                            pagecounter = pnRow + pagination.getCurrentPageIndex() * ROWS_PER_PAGE;
                            if (pagecounter == tblClients.getItems().size()) {
                                pagecounter = tblClients.getItems().size();
                                getSelectedItem(filteredData.get(pagecounter).getClientIndex02());
                            }else {
                               int y = 1;
                              pnRow = pnRow + y;
                                getSelectedItem(filteredData.get(pagecounter).getClientIndex02());
                            }
                            break;
                        case UP:
                            pnRow = tblClients.getSelectionModel().getSelectedIndex();
                            pagecounter = pnRow + pagination.getCurrentPageIndex() * ROWS_PER_PAGE;
                            getSelectedItem(filteredData.get(pagecounter).getClientIndex02());
                            break;
                        default:
                            return; 
                    }
                });
            }  
        }      
    }
    private void getSelectedItem(String TransNo){
        oldTransNo = TransNo;
        try {
                if (oTrans.OpenRecord(TransNo)){
                    txtField01.setText(filteredData.get(pagecounter).getClientIndex02());
                    txtField02.setText(filteredData.get(pagecounter).getClientIndex03());
                    txtField03.setText(filteredData.get(pagecounter).getClientIndex04());
                    txtField04.setText(filteredData.get(pagecounter).getClientIndex05());
                    txtField05.setText(filteredData.get(pagecounter).getClientIndex06());
                    txtField06.setText(filteredData.get(pagecounter).getClientIndex07());
                    txtField07.setText(filteredData.get(pagecounter).getClientIndex09());
                    txtField08.setText(filteredData.get(pagecounter).getClientIndex12());
                             oldPnRow = pagecounter;   
                } 

        }   catch (SQLException ex) {
            Logger.getLogger(RegistrationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void LoadMaster(){
//        oldTransNo = TransNo;
        try {
                txtField01.setText((String) oTrans.getMaster("sAttndIDx"));
                txtField02.setText((String)oTrans.getMaster("sPrefixNm"));
                txtField03.setText((String)oTrans.getMaster("sLastName"));
                txtField04.setText((String)oTrans.getMaster("sFirstNme"));
                txtField05.setText((String)oTrans.getMaster("sMiddName"));
                txtField06.setText((String)oTrans.getMaster("sSuffixNm"));
                txtField07.setText((String)oTrans.getMaster("sEmailAdd"));
                txtField08.setText((String)oTrans.getMaster("sPositnNm"));
        }   catch (SQLException ex) {
            Logger.getLogger(RegistrationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void clearFields(){
        txtField01.clear();
        txtField02.clear();
        txtField03.clear();
        txtField04.clear();
        txtField05.clear();
        txtField06.clear();
        txtField07.clear();
        txtField08.clear();
        txtSeeks99.clear();
//        lblStatus.setVisible(false);
//        oTrans = new Registration(oApp, oApp.getBranchCode(), false);
//        oTrans.setListener(oListener);
//        oTrans.setWithUI(true);
//        pbLoaded = true;
    }
    public static String dateToWord (String dtransact) {
       
        SimpleDateFormat dateParser1 = new SimpleDateFormat("yyyy-MM-dd");
        try {
//            Date date = dateParser.parse("2022-04-29 16:59:13");
//            SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
//             String str_date="2012-08-11+05:30";
//           
            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            date = (Date)formatter.parse(dtransact);  
            SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy");
            String todayStr = fmt.format(date);
            
            return todayStr;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
