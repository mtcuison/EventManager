/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eventmanager.model;

import javafx.beans.property.SimpleStringProperty;
/**
 *
 * @author user
 */
public class ClientInfoModel {



    private SimpleStringProperty clientIndex01;
    private SimpleStringProperty clientIndex02;  
    private SimpleStringProperty clientIndex03;
    private SimpleStringProperty clientIndex04;
    private SimpleStringProperty clientIndex05;  
    private SimpleStringProperty clientIndex06;
    private SimpleStringProperty clientIndex07;
    private SimpleStringProperty clientIndex08;  
    private SimpleStringProperty clientIndex09; 
    private SimpleStringProperty clientIndex10;
    private SimpleStringProperty clientIndex11; 
    private SimpleStringProperty clientIndex12;
    private SimpleStringProperty clientIndex13;

    private SimpleStringProperty clientIndex14;  
    private SimpleStringProperty clientIndex15;
    private SimpleStringProperty clientIndex16;
    private SimpleStringProperty clientIndex17;  
    private SimpleStringProperty clientIndex18;
    private SimpleStringProperty clientIndex19;
    private SimpleStringProperty clientIndex20;  
    private SimpleStringProperty clientIndex21; 
    private SimpleStringProperty clientIndex22;

    
    public ClientInfoModel(){
        
    }
//    Raffle Draw
    public ClientInfoModel(String clientIndex01,
            String clientIndex02,
            String clientIndex03,
            String clientIndex04,
            String clientIndex05,
            String clientIndex06,
            String clientIndex07,
            String clientIndex08,
            String clientIndex09,
            String clientIndex10) 
{
        this.clientIndex01 = new SimpleStringProperty(clientIndex01);
        this.clientIndex02 = new SimpleStringProperty(clientIndex02);
        this.clientIndex03 = new SimpleStringProperty(clientIndex03);
        this.clientIndex04 = new SimpleStringProperty(clientIndex04);
        this.clientIndex05 = new SimpleStringProperty(clientIndex05);
        this.clientIndex07 = new SimpleStringProperty(clientIndex07);
        this.clientIndex08 = new SimpleStringProperty(clientIndex08);
        if(clientIndex06.equalsIgnoreCase("0")){
            this.clientIndex06 = new SimpleStringProperty("Absent");
        }else{
            this.clientIndex06 = new SimpleStringProperty("Present");
        }
        
        this.clientIndex09 = new SimpleStringProperty(clientIndex09);
        this.clientIndex10 = new SimpleStringProperty(clientIndex10);
    }
    
//    Entry/Tagging
    public ClientInfoModel(String clientIndex01,
            String clientIndex02,
            String clientIndex03,
            String clientIndex04,
            String clientIndex05,
            String clientIndex06,
            String clientIndex07,
            String clientIndex08,
            String clientIndex09,
            String clientIndex10,
            String clientIndex11,
            String clientIndex12,
            String clientIndex13,
            String clientIndex14,
            String clientIndex15,
            String clientIndex16,
            String clientIndex17,
            String clientIndex18,
            String clientIndex19,
            String clientIndex20,
            String clientIndex21) 
{
        this.clientIndex01 = new SimpleStringProperty(clientIndex01);
        this.clientIndex02 = new SimpleStringProperty(clientIndex02);
        this.clientIndex03 = new SimpleStringProperty(clientIndex03);
        this.clientIndex04 = new SimpleStringProperty(clientIndex04);
        this.clientIndex05 = new SimpleStringProperty(clientIndex05);
        this.clientIndex06 = new SimpleStringProperty(clientIndex06);
        this.clientIndex07 = new SimpleStringProperty(clientIndex07);
        this.clientIndex08 = new SimpleStringProperty(clientIndex08);
        this.clientIndex09 = new SimpleStringProperty(clientIndex09);
        this.clientIndex10 = new SimpleStringProperty(clientIndex10);
        this.clientIndex11 = new SimpleStringProperty(clientIndex11);
        this.clientIndex12 = new SimpleStringProperty(clientIndex12);
        this.clientIndex13 = new SimpleStringProperty(clientIndex13);
        this.clientIndex14 = new SimpleStringProperty(clientIndex14);
        this.clientIndex15 = new SimpleStringProperty(clientIndex15);
        if(clientIndex16.equalsIgnoreCase("0")){
            this.clientIndex16 = new SimpleStringProperty("Absent");
        }else{
            this.clientIndex16 = new SimpleStringProperty("Present");
        }
        this.clientIndex17 = new SimpleStringProperty(clientIndex17);
        this.clientIndex18 = new SimpleStringProperty(clientIndex18);
        this.clientIndex19 = new SimpleStringProperty(clientIndex19);
        this.clientIndex20 = new SimpleStringProperty(clientIndex20);
        this.clientIndex21 = new SimpleStringProperty(clientIndex21);
    }
    
    public String getClientIndex01() {
        return clientIndex01.get();
    }

    public String getClientIndex02() {
        return clientIndex02.get();
    }

    public String getClientIndex03() {
        return clientIndex03.get();
    }
    
    public String getClientIndex04() {
        return clientIndex04.get();
    }
    public String getClientIndex05() {
        return clientIndex05.get();
    }
    public String getClientIndex06() {
        return clientIndex06.get();
    }
    public String getClientIndex07() {
        return clientIndex07.get();
    }
    public String getClientIndex08() {
        return clientIndex08.get();
    }
    public String getClientIndex09() {
        return clientIndex09.get();
    }
    public String getClientIndex10() {
        return clientIndex10.get();
    }
    public String getClientIndex11() {
        return clientIndex11.get();
    }
    public String getClientIndex12() {
        return clientIndex12.get();
    }

    public String getClientIndex13() {
        return clientIndex13.get();
    }

    public String getClientIndex14() {
        return clientIndex14.get();
    }

    public String getClientIndex15() {
        return clientIndex15.get();
    }
    
    public String getClientIndex16() {
        return clientIndex16.get();
    }

    public String getClientIndex17() {
        return clientIndex17.get();
    }
    
    public String getClientIndex18() {
        return clientIndex18.get();
    }

    public String getClientIndex19() {
        return clientIndex19.get();
    }

    public String getClientIndex20() {
        return clientIndex20.get();
    }
    public String getClientIndex21() {
        return clientIndex21.get();
    }
//
//    public String getClientIndex22() {
//        return clientIndex22.get();
//    }

}