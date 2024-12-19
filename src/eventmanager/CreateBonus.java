package eventmanager;

import eventmanager.base.GriderGui;
import eventmanager.base.RaffleGui;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.application.Application;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;

public class CreateBonus {
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
        
        try {
            String lsSQL = "SELECT" +
                                "  a.sEventIDx" +
                                ", a.nEntryNox" +
                                ", a.sAttndIDx" +
                                ", a.sCompnyID" +
                                ", a.sPositnID" +
                                ", a.cAttndTyp" +
                                ", a.cIsVIPxxx" +
                                ", a.cPresentx" +
                                ", a.cRaffledx" +
                                ", b.nTenurexx" +
                            " FROM Event_Detail a" +
                                ", Event_Attendee_List b" +
                            " WHERE a.sAttndIDx = b.sAttndIDx" +
                                " AND a.sEventIDx = 'M001240001'" +
                                " AND a.cPresentx = '1'";

            ResultSet loRS = oApp.executeQuery(lsSQL);

            int lnEntry;
            int lnBonus;
            double lnTenure;
            
            oApp.beginTrans();
            while (loRS.next()){
                lnTenure = loRS.getDouble("nTenurexx");
                
                if (lnTenure <= 5.00){
                    lnBonus = 0;
                } else if (lnTenure <= 10.00){
                    lnBonus = 1;
                } else if (lnTenure <= 15.00){
                    lnBonus = 2;
                } else if (lnTenure <= 20.00){
                    lnBonus = 3;
                } else {
                    lnBonus = 4;
                }
                
                lsSQL = "SELECT a.*, b.nTenurexx" +
                        " FROM Event_Detail a" +
                            ", Event_Attendee_List b" +
                        " WHERE a.sAttndIDx = b.sAttndIDx" +
                            " AND a.sEventIDx = 'M001240002'" + 
                            " AND a.sAttndIDx = " + SQLUtil.toSQL(loRS.getString("sAttndIDx"));
                
                ResultSet loBonus = oApp.executeQuery(lsSQL);
                
                lnEntry = (int) MiscUtil.RecordCount(loBonus);
                
                if (lnEntry < lnBonus){       
                    while (lnEntry < lnBonus){
                        lsSQL = "INSERT INTO Event_Detail SET" +
                                "  sEventIDx = 'M001240002'" + 
                                ", nEntryNox =" + getNextEntry(oApp) +
                                ", sAttndIDx = " + SQLUtil.toSQL(loRS.getString("sAttndIDx")) +
                                ", sCompnyID = " + SQLUtil.toSQL(loRS.getString("sCompnyID")) +
                                ", sPositnID = " + SQLUtil.toSQL(loRS.getString("sPositnID")) +
                                ", cAttndTyp = " + SQLUtil.toSQL(loRS.getString("cAttndTyp")) +
                                ", cIsVIPxxx = " + SQLUtil.toSQL(loRS.getString("cIsVIPxxx")) +
                                ", cPresentx = " + SQLUtil.toSQL(loRS.getString("cPresentx")) +
                                ", cRaffledx = " + SQLUtil.toSQL(loRS.getString("cRaffledx"));                          ;
                        
                        System.out.println(lsSQL);
                        if (oApp.executeUpdate(lsSQL) <= 0){
                            oApp.rollbackTrans();
                            System.err.println("Unable to add bonus entry.");
                            System.exit(1);
                        }    
                        lnEntry++;
                    } 
                }
            }
            oApp.commitTrans();
        } catch (SQLException e) {
            oApp.rollbackTrans();
            e.printStackTrace();
            System.exit(1);
        }
        
    }
    
    private static int getNextEntry(GRider foApp) throws SQLException{
        String lsSQL = "SELECT nEntryNox FROM Event_Detail" +
                        " WHERE sEventIDx = 'M001240002'" +
                            " ORDER BY nEntryNox DESC LIMIT 1";
        
        ResultSet loRS = foApp.executeQuery(lsSQL);
        
        int lnCode;
        
        if (MiscUtil.RecordCount(loRS) == 0)
            lnCode = 0;
        else {
            loRS.next();
            lnCode = loRS.getInt("nEntryNox");
        }
                
        MiscUtil.close(loRS);
        return lnCode + 1;
    }
}