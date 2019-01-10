/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.UserIntegrator;

import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Dominic Pimpinella
 */
public class UserAuthMuniManageBB extends BackingBeanUtils implements Serializable {
    
    private ArrayList<Municipality> muniList;
    private ArrayList<User> userList;
    private User selectedUser;
    private Municipality selectedMuni;
 
    public UserAuthMuniManageBB() {
        
        
    }
    
        /**
     * @return the selectedMuni
     */
    public Municipality getSelectedMuni() {
        return selectedMuni;
    }

    /**
     * @param selectedMuni the selectedMuni to set
     */
    public void setSelectedMuni(Municipality selectedMuni) {
        this.selectedMuni = selectedMuni;
    }
    
    public ArrayList<User> getUserList() {
        UserIntegrator ui = getUserIntegrator();
        try {
            userList = ui.getCompleteUserList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Unable to acquire list of users",
                            "This is a system-level error that msut be corrected by an administrator"));
        }
        return userList;
    }
    
    public void setUserList(ArrayList<User> userList) {
        this.userList = userList;
    }
    
    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }    
    
    public ArrayList<Municipality> getMuniList() {
    MunicipalityIntegrator mi = getMunicipalityIntegrator();
    try {
        muniList = mi.getCompleteMuniList();
    } catch (IntegrationException ex) {
        System.out.println(ex.toString());
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Hark--No elemented selected. Please click on a code element first.", ""));
    }
    return muniList;
    }
    
    public void setMuniList(ArrayList<Municipality> muniList) {
        
        this.muniList = muniList;
    }
    
    
//    
//            
        
//    public void thisisntreal(){
//        MunicipalityIntegrator mi = new MunicipalityIntegrator();
//        muniList = mi.getCompleteMuniList();
//        authMuniList = mi.getUserAuthMunis();       
//        UserIntegrator ui = new UserIntegrator();
//        userList = ui.getCompleteUserList();
//        
//        UserIntegrator ui = new UserIntegrator();
//        User u = ui.getUser(101);
//        MunicipalityIntegrator mi = new MunicipalityIntegrator();
//        ArrayList<Municipality> list = mi.getCompleteMuniList();
//        ui.setUserAuthMunis(u, list);
//    }
//  
//    public User getSelectedUser() {
//        return selectedUser;
//    }
//
//    public void setSelectedUser(User selectedUser) {
//        this.selectedUser = selectedUser;
//    }
//    
//    public String addAuthMuni() throws IntegrationException {
//        UserIntegrator ui = new UserIntegrator();
//        User u = ui.getUser(101);
//        MunicipalityIntegrator mi = new MunicipalityIntegrator();
//        ArrayList<Municipality> list = mi.getCompleteMuniList();
//        ui.setUserAuthMunis(u, list);
//        
//        return "success";
//    }



}
