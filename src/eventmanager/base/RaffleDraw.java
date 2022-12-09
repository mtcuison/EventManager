/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eventmanager.base;

import com.sun.rowset.CachedRowSetImpl;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.RecordStatus;

/**
 *
 * @author User
 */
public class RaffleDraw {
    
   private final String MASTER_TABLE = "Event_Attendee_List";
   private final String DETAIL_TABLE = "Event_Detail";
    
    private final GRider p_oApp;
    private final boolean p_bWithParent;
    
    private String p_sBranchCd;
    private int p_nEditMode;

    private String p_sMessage;
    private boolean p_bWithUI = true;
    
    private LMasDetTrans p_oListener;
    
    private CachedRowSet p_oMaster;
    private CachedRowSet p_oMasterDetail;
    private CachedRowSet p_oDetail;
    
    public RaffleDraw(GRider foApp, String fsBranchCd, boolean fbWithParent){
        p_oApp = foApp;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;        
                
        if (p_sBranchCd.isEmpty()) p_sBranchCd = p_oApp.getBranchCode();
                
        p_nEditMode = EditMode.UNKNOWN;
    }
    
    public void setListener(LMasDetTrans foValue){
        p_oListener = foValue;
    }
    
    public void setWithUI(boolean fbValue){
        p_bWithUI = fbValue;
    }
    
    public int getEditMode(){
        return p_nEditMode;
    }
    
    public String getMessage(){
        return p_sMessage;
    }
    
    public boolean NewRecord() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        initMaster();
        System.out.println("sAttndIDx = " + getMaster("sAttndIDx").toString());
        p_nEditMode = EditMode.ADDNEW;
        return true;
    }
    
    public boolean SaveRecord() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            p_sMessage = "Invalid edit mode detected.";
            return false;
        }
        
        if (!isEntryOK()) return false;
        
        p_oMaster.updateObject("sModified", p_oApp.getUserID());
        p_oMaster.updateObject("dModified", p_oApp.getServerDate());
        p_oMaster.updateObject("sAttendNm", getMaster("sLastName").toString() + ", " + getMaster("sFirstNme").toString() +  " " + getMaster("sMiddName").toString());
        p_oMaster.updateRow();
        
        
        String lsSQL;
        
        if (p_nEditMode == EditMode.ADDNEW){           
//            lsSQL = MiscUtil.rowset2SQL(p_oDetail, "Incentive_Detail", "sPositnNm;cPresentx;cMailSent;cPrintedx;cRaffledx;nEntryNox;sEventIDx");
            lsSQL = MiscUtil.rowset2SQL(p_oMaster, MASTER_TABLE, "sPositnNm;cPresentx;cMailSent;cPrintedx;cRaffledx;nEntryNox;sEventIDx");
        } else {            
            lsSQL = MiscUtil.rowset2SQL(p_oMaster, 
                                        MASTER_TABLE, 
                                        "sPositnNm;cPresentx;cMailSent;cPrintedx;cRaffledx;nEntryNox;sEventIDx", 
                                        "sAttndIDx = " + SQLUtil.toSQL(p_oMaster.getString("sAttndIDx")));
        }
        
        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();
            
            if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oApp.rollbackTrans();
                p_sMessage = p_oApp.getErrMsg() + ";" + p_oApp.getMessage();
                return false;
            }
            

            if (!insertDetail(getMaster("sAttndIDx").toString())){
                if (!p_bWithParent) p_oApp.rollbackTrans();
                return false;
            }
        
            if (!p_bWithParent) p_oApp.commitTrans();
            
            return true;
        } else{
            p_sMessage = "No record to update.";
        }
       
        
        
        p_nEditMode = EditMode.UNKNOWN;
        return false;
    }
    private boolean insertDetail(String lsCodex) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            p_sMessage = "Invalid edit mode detected.";
            return false;
        }
        p_oDetail.first();
        p_oDetail.updateObject("sModified", p_oApp.getUserID());
        p_oDetail.updateObject("dModified", p_oApp.getServerDate());
        p_oDetail.updateObject("sAttndIDx", lsCodex);
        p_oDetail.updateRow();
        
        
        String lsSQL;
        
        if (p_nEditMode == EditMode.ADDNEW){           
            lsSQL = MiscUtil.rowset2SQL(p_oDetail, DETAIL_TABLE, "");
        } else {            
            lsSQL = MiscUtil.rowset2SQL(p_oDetail, 
                                        DETAIL_TABLE, 
                                        "", 
                                        "sEventIDx = " + SQLUtil.toSQL(p_oDetail.getString("sEventIDx")));
        }
        
        if (!lsSQL.isEmpty()){
            if (p_oApp.executeQuery(lsSQL, DETAIL_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oApp.rollbackTrans();
                p_sMessage = p_oApp.getErrMsg() + ";" + p_oApp.getMessage();
                return false;
            }
            
            return true;
        } else{
            p_sMessage = "No record to update.";
        }
       
        
        
        p_nEditMode = EditMode.UNKNOWN;
        return false;
    }
    
    public boolean ActivateRecord() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid edit mode.";
            return false;
        }
        
        p_oMaster.first();
        
        if ("1".equals(p_oMaster.getString("cPresentx"))){
            p_sMessage = "Record is already activated..";
            return false;
        }
        
        String lsSQL = "UPDATE " + DETAIL_TABLE + " SET" +
                            "  cPresentx = '1'" +
                            ", sModified = " + SQLUtil.toSQL(p_oApp.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(p_oApp.getServerDate()) +
                        " WHERE sEventIDx = " + SQLUtil.toSQL(p_oMaster.getString("sEventIDx"));

        if (!p_bWithParent) p_oApp.beginTrans();
        if (p_oApp.executeQuery(lsSQL, DETAIL_TABLE, p_sBranchCd, "") <= 0){
            if (!p_bWithParent) p_oApp.rollbackTrans();
            p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
            return false;
        }
        if (!p_bWithParent) p_oApp.commitTrans();
        
        p_nEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean DeactivateRecord() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid edit mode.";
            return false;
        }
        
        p_oMaster.first();
        
        if ("0".equals(p_oMaster.getString("cRecdStat"))){
            p_sMessage = "Record is already deactivated..";
            return false;
        }
        
        String lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                            "  cRecdStat = '0'" +
                            ", sModified = " + SQLUtil.toSQL(p_oApp.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(p_oApp.getServerDate()) +
                        " WHERE sPanaloCD = " + SQLUtil.toSQL(p_oMaster.getString("sPanaloCD"));

        if (!p_bWithParent) p_oApp.beginTrans();
        if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
            if (!p_bWithParent) p_oApp.rollbackTrans();
            p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
            return false;
        }
        if (!p_bWithParent) p_oApp.commitTrans();
        
        p_nEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean SearchRecord(String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL = getSQ_Record();
        
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                lsSQL, 
                                fsValue, 
                                "Code»Description", 
                                "sPanaloCD»sPanaloDs", 
                                "sPanaloCD»sPanaloDs", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenRecord((String) loJSON.get("sPanaloCD"));
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sAttndIDx = " + SQLUtil.toSQL(fsValue));   
        else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sPanaloDs LIKE " + SQLUtil.toSQL(fsValue + "%")); 
            lsSQL += " LIMIT 1";
        }
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            p_sMessage = "No transaction found for the givern criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sPanaloCD");
        MiscUtil.close(loRS);
        
        return OpenRecord(lsSQL);
    }
    
    public boolean OpenRecord(String fsValue) throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        lsSQL = getSQ_Record() + " WHERE a.sAttndIDx = " + SQLUtil.toSQL(fsValue);
        System.out.println(lsSQL);
        loRS = p_oApp.executeQuery(lsSQL);
        p_oMaster = factory.createCachedRowSet();
        p_oMaster.populate(loRS);
        MiscUtil.close(loRS);
        
        
        p_nEditMode = EditMode.READY;
        return true;
    }
    
    public boolean LoadRecord() throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
//        lsSQL = MiscUtil.addCondition(getSQ_Record(), "sPanaloCD= " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(getSQ_Record());
        p_oMaster = factory.createCachedRowSet();
        p_oMaster.populate(loRS);
        MiscUtil.close(loRS);
        
        p_nEditMode = EditMode.READY;
        return true;
    }
   
    public boolean UpdateRecord() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid edit mode.";
            return false;
        }
        
        p_nEditMode = EditMode.UPDATE;
        return true;
    }
   
    
    public int getItemCount() throws SQLException{
        if (p_oMaster == null) return 0;
        
        p_oMaster.last();
        return p_oMaster.getRow();
    }
     public Object getMaster(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMaster.first();
        return p_oMaster.getObject(fnIndex);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(getColumnIndex(p_oMaster,fsIndex));
    }
    public Object getMaster(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMaster.absolute(fnRow);
        return p_oMaster.getObject(fnIndex);
    }
    
    public Object getMaster(int fnRow, String fsIndex) throws SQLException{
        return getMaster(fnRow, getColumnIndex(p_oMaster, fsIndex));
    }
    public void setMaster(String fsIndex, Object foValue) throws SQLException{        
        setMaster(getColumnIndex(p_oMaster,fsIndex), foValue);
    }
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        p_oMaster.first();
        switch (fnIndex){
            case 2://sPrefixNm
            case 3://sLastName
            case 4://sFirstNme
            case 5://sMiddName
            case 6://sSuffixNm
            case 7://sAttendNm
            case 8://sEmailAdd
            case 9://sCompnyID
            case 10://sPositnID
            case 11://sPositnNm
            case 14://sModified
                p_oMaster.updateString(fnIndex, ((String) foValue).trim());
                p_oMaster.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 12://cAttndTyp
            case 13://cIsVIPxxx
                
                if (foValue instanceof Integer)
                    p_oMaster.updateInt(fnIndex, (int) foValue);
                else 
                    p_oMaster.updateInt(fnIndex, 0);
                
                p_oMaster.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 15://dModified
                if (foValue instanceof Date){
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oMaster.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
        }
    }
    
    private int getColumnIndex(CachedRowSet loRS, String fsValue) throws SQLException{
        int lnIndex = 0;
        int lnRow = loRS.getMetaData().getColumnCount();
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            if (fsValue.equals(loRS.getMetaData().getColumnLabel(lnCtr))){
                lnIndex = lnCtr;
                break;
            }
        }
        
        return lnIndex;
    }
    
    
    private boolean isEntryOK() throws SQLException{
        p_oMaster.first();
        
        if (p_oMaster.getString("sLastName").isEmpty()){
            p_sMessage = "Last name must not be empty.";
            return false;
        }
        if (p_oMaster.getString("sFirstNme").isEmpty()){
            p_sMessage = "First name must not be empty.";
            return false;
        }
        if (p_oMaster.getString("sMiddName").isEmpty()){
            p_sMessage = "Middle name must not be empty.";
            return false;
        }
        
        return true;
    }
    
    private void initMaster() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(21);

        meta.setColumnName(1, "sAttndIDx");
        meta.setColumnLabel(1, "sAttndIDx");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 4);
        
        meta.setColumnName(2, "sPrefixNm");
        meta.setColumnLabel(2, "sPrefixNm");
        meta.setColumnType(2, Types.VARCHAR);
        meta.setColumnDisplaySize(2, 5);
        
        meta.setColumnName(3, "sLastName");
        meta.setColumnLabel(3, "sLastName");
        meta.setColumnType(3, Types.VARCHAR);
        meta.setColumnDisplaySize(3, 32);
        
        meta.setColumnName(4, "sFirstNme");
        meta.setColumnLabel(4, "sFirstNme");
        meta.setColumnType(4, Types.VARCHAR);
        meta.setColumnDisplaySize(4, 32);
        
        meta.setColumnName(5, "sMiddName");
        meta.setColumnLabel(5, "sMiddName");
        meta.setColumnType(5, Types.VARCHAR);
        meta.setColumnDisplaySize(5, 32);
        
        meta.setColumnName(6, "sSuffixNm");
        meta.setColumnLabel(6, "sSuffixNm");
        meta.setColumnType(6, Types.VARCHAR);
        meta.setColumnDisplaySize(6, 5);
        
        meta.setColumnName(7, "sAttendNm");
        meta.setColumnLabel(7, "sAttendNm");
        meta.setColumnType(7, Types.VARCHAR);
        meta.setColumnDisplaySize(7, 96);
        
        meta.setColumnName(8, "sEmailAdd");
        meta.setColumnLabel(8, "sEmailAdd");
        meta.setColumnType(8, Types.VARCHAR);
        meta.setColumnDisplaySize(8, 50);
        
        meta.setColumnName(9, "sCompnyID");
        meta.setColumnLabel(9, "sCompnyID");
        meta.setColumnType(9, Types.VARCHAR);
        meta.setColumnDisplaySize(9, 4);
        
        meta.setColumnName(10, "sPositnID");
        meta.setColumnLabel(10, "sPositnID");
        meta.setColumnType(10, Types.VARCHAR);
        meta.setColumnDisplaySize(10, 3);
        
        meta.setColumnName(11, "sPositnNm");
        meta.setColumnLabel(11, "sPositnNm");
        meta.setColumnType(11, Types.VARCHAR);
        meta.setColumnDisplaySize(11, 64);
        
        meta.setColumnName(12, "cAttndTyp");
        meta.setColumnLabel(12, "cAttndTyp");
        meta.setColumnType(12, Types.VARCHAR);
        meta.setColumnDisplaySize(12, 1);
        
        meta.setColumnName(13, "cIsVIPxxx");
        meta.setColumnLabel(13, "cIsVIPxxx");
        meta.setColumnType(13, Types.VARCHAR);
        meta.setColumnDisplaySize(13, 1);
        
        meta.setColumnName(14, "cPresentx");
        meta.setColumnLabel(14, "cPresentx");
        meta.setColumnType(14, Types.VARCHAR);
        meta.setColumnDisplaySize(14, 1);
        
        meta.setColumnName(15, "cMailSent");
        meta.setColumnLabel(15, "cMailSent");
        meta.setColumnType(15, Types.VARCHAR);
        meta.setColumnDisplaySize(15, 1);
        
        meta.setColumnName(16, "cPrintedx");
        meta.setColumnLabel(16, "cPrintedx");
        meta.setColumnType(16, Types.VARCHAR);
        meta.setColumnDisplaySize(16, 1);
        
        meta.setColumnName(17, "cRaffledx");
        meta.setColumnLabel(17, "cRaffledx");
        meta.setColumnType(17, Types.VARCHAR);
        meta.setColumnDisplaySize(17, 1);
        
        meta.setColumnName(18, "nEntryNox");
        meta.setColumnLabel(18, "nEntryNox");
        meta.setColumnType(18, Types.SMALLINT);
        
        meta.setColumnName(19, "sEventIDx");
        meta.setColumnLabel(19, "sEventIDx");
        meta.setColumnType(19, Types.VARCHAR);
        meta.setColumnDisplaySize(19, 4);
        
        meta.setColumnName(20, "sModified");
        meta.setColumnLabel(20, "sModified");
        meta.setColumnType(20, Types.VARCHAR);
        meta.setColumnDisplaySize(20, 12);
        
        meta.setColumnName(21, "dModified");
        meta.setColumnLabel(21, "dModified");
        meta.setColumnType(21, Types.DATE);
        
        p_oMaster = new CachedRowSetImpl();
        p_oMaster.setMetaData(meta);
        
        p_oMaster.last();
        p_oMaster.moveToInsertRow();
        
        MiscUtil.initRowSet(p_oMaster);    
        
        p_oMaster.updateString("sAttndIDx", MiscUtil.getNextCode(MASTER_TABLE, "sAttndIDx", false, p_oApp.getConnection(), ""));
        p_oMaster.updateString("cAttndTyp", RecordStatus.INACTIVE);
        p_oMaster.updateString("cIsVIPxxx", RecordStatus.INACTIVE);
        
        p_oMaster.insertRow();
        p_oMaster.moveToCurrentRow();
    }
    private String getSQ_Record(){
        return "SELECT" +
                    " IFNULL(a.sAttndIDx,'')  sAttndIDx" +
                    ", IFNULL(a.sPrefixNm,'') sPrefixNm" +
                    ", IFNULL(a.sLastName,'') sLastName" +
                    ", IFNULL(a.sFirstNme,'') sFirstNme" +
                    ", IFNULL(a.sMiddName,'') sMiddName" +
                    ", IFNULL(a.sSuffixNm,'') sSuffixNm" +
                    ", IFNULL(a.sAttendNm,'') sAttendNm" +
                    ", IFNULL(a.sEmailAdd,'') sEmailAdd" +
                    ", IFNULL(a.sCompnyID,'') sCompnyID" +
                    ", IFNULL(b.sPositnID,'') sPositnID" +
                    ", IFNULL(b.sPositnNm,'') sPositnNm" +
                    ", IFNULL(a.cAttndTyp,'0') cAttndTyp" +
                    ", IFNULL(a.cIsVIPxxx,'0') cIsVIPxxx" +
                    ", IFNULL(c.cPresentx,'0') cPresentx" +
                    ", IFNULL(c.cMailSent,'0') cMailSent" +
                    ", IFNULL(c.cPrintedx,'0') cPrintedx" +
                    ", IFNULL(c.cRaffledx,'0') cRaffledx" +
                    ", IFNULL(c.nEntryNox, 0 ) nEntryNox" +
                    ", IFNULL(c.sEventIDx, '') sEventIDx" +
                    ", IFNULL(a.sModified,'') sModified" +
                    ", IFNULL(a.dModified,'') dModified" +
                " FROM " + MASTER_TABLE + " a " +
                "   LEFT JOIN Position b " + 
                "       ON a.sPositnID = b.sPositnID " +
                "   LEFT JOIN Event_Detail c " + 
                "       ON a.sAttndIDx = c.sAttndIDx ";
    }
  
}

