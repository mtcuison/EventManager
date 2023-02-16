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
public class RafflePromo {
    
   private final String MASTER_TABLE = "Raffle_With_Sms_Source";
   private final String DETAIL_TABLE = "Raffle_With_Sms_Entry";
    
    private final GRider p_oApp;
    private final boolean p_bWithParent;
    
    private String p_sBranchCd;
    private int p_nEditMode;

    private String p_sMessage;
    private boolean p_bWithUI = true;
    
    private LMasDetTran p_oListener;
    
    private CachedRowSet p_oMaster;
    private CachedRowSet p_oMasterDetail;
    private CachedRowSet p_oDetail;
    
    public RafflePromo(GRider foApp, String fsBranchCd, boolean fbWithParent){
        p_oApp = foApp;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;        
                
        if (p_sBranchCd.isEmpty()) p_sBranchCd = p_oApp.getBranchCode();
                
        p_nEditMode = EditMode.UNKNOWN;
    }
    
    public void setListener(LMasDetTran foValue){
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
    
    
    public boolean ActivateRecord() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid edit mode.";
            return false;
        }
        
        p_oMaster.first();
        
        if ("1".equals(p_oMaster.getString("cRaffledx"))){
            p_sMessage = "Record is already activated..";
            return false;
        }
        
        String lsSQL = "UPDATE " + DETAIL_TABLE + " SET" +
                            "  cRaffledx = '1'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL(p_oDetail.getString("sTransNox"));

        if (!p_bWithParent) p_oApp.beginTrans();
        if (p_oApp.executeQuery(lsSQL, DETAIL_TABLE, p_sBranchCd, "") <= 0){
            if (!p_bWithParent) p_oApp.rollbackTrans();
            p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
            return false;
        }
        
        
        lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                            "  cRaffledx = '1'" +
                        " WHERE sTransNox = " + SQLUtil.toSQL(p_oMaster.getString("sTransNox"));
        
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
        
        String lsSQL = getSQ_Master();
        
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                lsSQL, 
                                fsValue, 
                                "TransNox»ClientName", 
                                "sTransNox»sClientNme", 
                                "a.sClientNme»a.sClientNme", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenRecord((String) loJSON.get("TransNox"));
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sTransNox = " + SQLUtil.toSQL(fsValue));   
        else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sClientNm LIKE " + SQLUtil.toSQL(fsValue + "%")); 
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
        lsSQL = getSQ_Master() + " AND MID(" + SQLUtil.toSQL(fsValue) + ", 4, 9) BETWEEN(sRaffleFr AND sRaffleTr)";
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
        loRS = p_oApp.executeQuery(getSQ_Detail());
        p_oDetail = factory.createCachedRowSet();
        p_oDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        p_nEditMode = EditMode.READY;
        return true;
    }
   
    
    public int getItemCount() throws SQLException{
        if (p_oDetail == null) return 0;
        
        p_oDetail.last();
        return p_oDetail.getRow();
    }
    
     public Object getDetail(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oDetail.first();
        return p_oDetail.getObject(fnIndex);
    }
    
    public Object getDetail(String fsIndex) throws SQLException{
        return getDetail(getColumnIndex(p_oDetail,fsIndex));
    }
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oDetail.absolute(fnRow);
        return p_oDetail.getObject(fnIndex);
    }
    
    public void setDetail(String fsIndex, Object foValue) throws SQLException{        
        setDetail(getColumnIndex(p_oDetail,fsIndex), foValue);
    }
    public void setDetail(int fnIndex, Object foValue) throws SQLException{
        p_oMaster.first();
        switch (fnIndex){
            case 2://sRaffleNo
                p_oDetail.updateString(fnIndex, ((String) foValue).trim());
                p_oDetail.updateRow();

                if (p_oListener != null) p_oListener.DetailRetreive(fnIndex, p_oDetail.getString(fnIndex));
                break;
            case 3://cRaffledx
                
                if (foValue instanceof Integer)
                    p_oDetail.updateInt(fnIndex, (int) foValue);
                else 
                    p_oDetail.updateInt(fnIndex, 0);
                
                p_oDetail.updateRow();
                if (p_oListener != null) p_oListener.DetailRetreive(fnIndex, p_oDetail.getString(fnIndex));
                break;
        }
    }
    
    
    public int getMasterCount() throws SQLException{
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
            case 3://sBranchCd
            case 4://sSourceCd
            case 5://sSourceNo
            case 6://sReferNox
            case 7://sAcctNmbr
            case 8://sClientID
            case 9://sClientNm
            case 10://sAddressx
            case 11://sMobileNo
            case 13://sRandomNo
            case 14://sRaffleFr
            case 15://sRaffleTr
                p_oMaster.updateString(fnIndex, ((String) foValue).trim());
                p_oMaster.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 12://cDivision
            case 16://nEntryNox
            case 17://cMsgSentx
            case 18://cCltCnfrm
            case 19://cSysCnfrm
            case 20://cRaffledx
                
                if (foValue instanceof Integer)
                    p_oMaster.updateInt(fnIndex, (int) foValue);
                else 
                    p_oMaster.updateInt(fnIndex, 0);
                
                p_oMaster.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 2://dTransact
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
    
    private void initMaster() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(20);
        
        meta.setColumnName(1, "sTransNox");
        meta.setColumnLabel(1, "sTransNox");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 12);
        
        meta.setColumnName(2, "dTransact");
        meta.setColumnLabel(2, "dTransact");
        meta.setColumnType(2, Types.DATE);
        
        meta.setColumnName(3, "sBranchCd");
        meta.setColumnLabel(3, "sBranchCd");
        meta.setColumnType(3, Types.VARCHAR);
        meta.setColumnDisplaySize(3, 4);
        
        meta.setColumnName(4, "sSourceCd");
        meta.setColumnLabel(4, "sSourceCd");
        meta.setColumnType(4, Types.VARCHAR);
        meta.setColumnDisplaySize(4, 4);
        
        meta.setColumnName(5, "sSourceNo");
        meta.setColumnLabel(5, "sSourceNo");
        meta.setColumnType(5, Types.VARCHAR);
        meta.setColumnDisplaySize(5, 12);
        
        meta.setColumnName(6, "sReferNox");
        meta.setColumnLabel(6, "sReferNox");
        meta.setColumnType(6, Types.VARCHAR);
        meta.setColumnDisplaySize(6, 12);
        
        meta.setColumnName(7, "sAcctNmbr");
        meta.setColumnLabel(7, "sAcctNmbr");
        meta.setColumnType(7, Types.VARCHAR);
        meta.setColumnDisplaySize(7, 10);
        
        meta.setColumnName(8, "sClientID");
        meta.setColumnLabel(8, "sClientID");
        meta.setColumnType(8, Types.VARCHAR);
        meta.setColumnDisplaySize(8, 12);
        
        meta.setColumnName(9, "sClientNm");
        meta.setColumnLabel(9, "sClientNm");
        meta.setColumnType(9, Types.VARCHAR);
        meta.setColumnDisplaySize(9, 128);
        
        meta.setColumnName(10, "sAddressx");
        meta.setColumnLabel(10, "sAddressx");
        meta.setColumnType(10, Types.VARCHAR);
        meta.setColumnDisplaySize(10, 256);
        
        
        meta.setColumnName(11, "sMobileNo");
        meta.setColumnLabel(11, "sMobileNo");
        meta.setColumnType(11, Types.VARCHAR);
        meta.setColumnDisplaySize(11, 13);
        
        
        meta.setColumnName(12, "cDivision");
        meta.setColumnLabel(12, "cDivision");
        meta.setColumnType(12, Types.VARCHAR);
        meta.setColumnDisplaySize(12, 1);
        
        
        meta.setColumnName(13, "sRandomNo");
        meta.setColumnLabel(13, "sRandomNo");
        meta.setColumnType(13, Types.VARCHAR);
        meta.setColumnDisplaySize(13, 2);
        
        
        meta.setColumnName(14, "sRaffleFr");
        meta.setColumnLabel(14, "sRaffleFr");
        meta.setColumnType(14, Types.VARCHAR);
        meta.setColumnDisplaySize(14, 9);
        
        
        meta.setColumnName(15, "sRaffleTr");
        meta.setColumnLabel(15, "sRaffleTr");
        meta.setColumnType(15, Types.VARCHAR);
        meta.setColumnDisplaySize(15, 9);
        
        
        meta.setColumnName(16, "nNoEntryx");
        meta.setColumnLabel(16, "nNoEntryx");
        meta.setColumnType(16, Types.SMALLINT);
        
        
        meta.setColumnName(17, "cMsgSentx");
        meta.setColumnLabel(17, "cMsgSentx");
        meta.setColumnType(17, Types.VARCHAR);
        meta.setColumnDisplaySize(17, 1);
        
        
        meta.setColumnName(18, "cCltCnfrm");
        meta.setColumnLabel(18, "cCltCnfrm");
        meta.setColumnType(18, Types.VARCHAR);
        meta.setColumnDisplaySize(18, 1);
        
        
        meta.setColumnName(19, "cSysCnfrm");
        meta.setColumnLabel(19, "cSysCnfrm");
        meta.setColumnType(19, Types.VARCHAR);
        meta.setColumnDisplaySize(19, 1);
        
        
        meta.setColumnName(20, "cRaffledx");
        meta.setColumnLabel(20, "cRaffledx");
        meta.setColumnType(20, Types.VARCHAR);
        meta.setColumnDisplaySize(20, 1);
        
        
        p_oMaster = new CachedRowSetImpl();
        p_oMaster.setMetaData(meta);
        
        p_oMaster.last();
        p_oMaster.moveToInsertRow();
        
        MiscUtil.initRowSet(p_oMaster);    
        
        p_oMaster.updateString("sTransNox", MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", true, p_oApp.getConnection(), p_sBranchCd));
        
        p_oMaster.insertRow();
        p_oMaster.moveToCurrentRow();
    }
//    private String getSQ_Master(){
//        return "SELECT" +
//                    " IFNULL(sTransNox,'')  sTransNox" +
//                    ", IFNULL(dTransact,'') dTransact" +
//                    ", IFNULL(sBranchCd,'') sBranchCd" +
//                    ", IFNULL(sSourceCd,'') sSourceCd" +
//                    ", IFNULL(sSourceNo,'') sSourceNo" +
//                    ", IFNULL(sReferNox,'') sReferNox" +
//                    ", IFNULL(sAcctNmbr,'') sAcctNmbr" +
//                    ", IFNULL(sClientID,'') sClientID" +
//                    ", IFNULL(sClientNm,'') sClientNm" +
//                    ", IFNULL(sAddressx,'')  sAddressx" +
//                    ", IFNULL(sMobileNo,'') sMobileNo" +
//                    ", IFNULL(cDivision,'') cDivision" +
//                    ", IFNULL(sRandomNo,'') sRandomNo" +
//                    ", IFNULL(sRaffleFr,'') sRaffleFr" +
//                    ", IFNULL(sRaffleTr,'') sRaffleTr" +
//                    ", IFNULL(nNoEntryx,0) nNoEntryx" +
//                    ", IFNULL(cMsgSentx,'') cMsgSentx" +
//                    ", IFNULL(cCltCnfrm,'') cCltCnfrm" +
//                    ", IFNULL(cSysCnfrm,'') cSysCnfrm" +
//                    ", IFNULL(cRaffledx,'0') cRaffledx" +  
//                " FROM " + MASTER_TABLE + " a " +
//                " WHERE  b.cRaffledx = '0' ";
//    }
    private String getSQ_Master(){
        return "SELECT" +
                    " IFNULL(a.sTransNox,'')  sTransNox" +
                    ", IFNULL(a.dTransact,'') dTransact" +
                    ", IFNULL(a.sBranchCd,'') sBranchCd" +
                    ", IFNULL(a.sSourceCd,'') sSourceCd" +
                    ", IFNULL(a.sSourceNo,'') sSourceNo" +
                    ", IFNULL(a.sReferNox,'') sReferNox" +
                    ", IFNULL(a.sAcctNmbr,'') sAcctNmbr" +
                    ", IFNULL(a.sClientID,'') sClientID" +
                    ", IFNULL(a.sClientNm,'') sClientNm" +
                    ", IFNULL(a.sAddressx,'') sAddressx" +
                    ", IFNULL(a.sMobileNo,'') sMobileNo" +
                    ", IFNULL(a.cDivision,'') cDivision" +
                    ", IFNULL(a.sRandomNo,'') sRandomNo" +
                    ", IFNULL(a.sRaffleFr,'') sRaffleFr" +
                    ", IFNULL(a.sRaffleTr,'') sRaffleTr" +
                    ", IFNULL(a.nNoEntryx,0) nNoEntryx" +
                    ", IFNULL(a.cMsgSentx,'') cMsgSentx" +
                    ", IFNULL(a.cCltCnfrm,'') cCltCnfrm" +
                    ", IFNULL(a.cSysCnfrm,'') cSysCnfrm" +
                    ", IFNULL(a.cRaffledx,'0') cRaffledx" +  
                    ", IFNULL(b.sBranchNm,'') sBranchNm" +
                " FROM " + MASTER_TABLE + " a " +
                "  , Branch b " + 
                " WHERE a.sBranchCd = b.sBranchCd";
    }
//  
    private String getSQ_Detail(){
        return "SELECT" +
                    " IFNULL(sTransNox,'')  sTransNox" +
                    ", IFNULL(sRaffleNo,'') sRaffleNo" +
                    ", IFNULL(cRaffledx,'0') cRaffledx" +
                " FROM " + DETAIL_TABLE + 
                " WHERE cRaffledx = '0' "+
                " ORDER BY RAND() LIMIT 1 ";
    }
}

