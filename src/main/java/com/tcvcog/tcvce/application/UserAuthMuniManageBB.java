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

import com.tcvcog.tcvce.coordinators.UserAuthMuniCoordinator;

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
    private ArrayList<Municipality> authMuniList;
    private ArrayList<Municipality> unauthorizedMuniList;
    private ArrayList<Municipality> selectedMunis;

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
            if (userList == null) {
                userList = ui.getCompleteUserList();
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Unable to acquire list of users",
                            "This is a system-level error that must be corrected by an administrator"));
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

    public ArrayList<Municipality> getSelectedMunis() {
        return selectedMunis;
    }

    public void setSelectedMunis(ArrayList<Municipality> selectedMunis) {
        this.selectedMunis = selectedMunis;
    }    
    
    public ArrayList<Municipality> getMuniList() {
    MunicipalityIntegrator mi = getMunicipalityIntegrator();
    try {
        muniList = mi.getCompleteMuniList();
    } catch (IntegrationException ex) {
        System.out.println(ex.toString());
    }
    return muniList;
    }
    
    public void setMuniList(ArrayList<Municipality> muniList) {        
        this.muniList = muniList;
    }
    
    
    public ArrayList<Municipality> getAuthMuniList() {
        System.out.println("UserAuthMuniManageBB.getUserAuthMuniList");
        return authMuniList;
    }

    public void setAuthMuniList(ArrayList<Municipality> authMuniList) {
        this.authMuniList = authMuniList;
    }
    
    
    public ArrayList<Municipality> getUnauthorizedMuniList() {
        return unauthorizedMuniList;
    }

    public void setUnauthorizedMuniList(ArrayList<Municipality> unauthorizedMuniList) {
        this.unauthorizedMuniList = unauthorizedMuniList;
    }
    
    public void onSelectedUserChange() throws IntegrationException {
        System.out.println("UserAuthMuniManageBB.onSelectedUserChange " + selectedUser.getUserID());
        clearAuthMuniList();
        clearUnauthorizedMuniList();

        UserAuthMuniCoordinator uac = new UserAuthMuniCoordinator();            
        unauthorizedMuniList = uac.getUnauthorizedMunis(selectedUser);

        UserIntegrator ui = getUserIntegrator();
        authMuniList = ui.getUserAuthMunis(selectedUser.getUserID());
        
        selectedMunis = new ArrayList<>();
            
    }
    
    public void onSelectedMuniChange() {
        System.out.println("UserAuthMuniManageBB.onSelectedMuniChange " + selectedMuni.getMuniName());
        try{
            addAuthMuni();
        } catch(IntegrationException ex){
        
        }
    }
    
    public void clearAuthMuniList() {
        authMuniList = null;
    }
    
    public void clearUnauthorizedMuniList(){
        unauthorizedMuniList = null;
    }

    public void addAuthMuni() throws IntegrationException {
        System.out.println("UserAuthMuniManageBB.addAuthMuni: " + selectedUser.getUserID() + " & " + selectedMuni.getMuniName());
        selectedMunis.add(selectedMuni);      
        authMuniList.add(selectedMuni);
    }
    
    public void updateAuthMunis() throws IntegrationException {
        System.out.println("UserAuthMuniManageBB.updateAuthMunis" + selectedUser + " & " + selectedMunis);
        UserIntegrator ui = getUserIntegrator();
        ui.setUserAuthMunis(selectedUser, selectedMunis);
        selectedMunis = new ArrayList<>();
        UserAuthMuniCoordinator uac = new UserAuthMuniCoordinator();
        selectedMunis.clear();
        unauthorizedMuniList = uac.getUnauthorizedMunis(selectedUser);
    }



}
