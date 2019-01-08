/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcve.entities.MunicipalityIntegrator;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcve.entities.UserIntegrator;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Dominic Pimpinella
 */
public class UserAuthMuniManageBB extends BackingBeanUtils implements Serializable {
    
    private ArrayList<User> userList;
    private ArrayList<Municipality> muniList;
    private ArrayList<Municipality> authMuniList;
 
    public UserAuthMuniManageBB() {
    }
    
    public void thisisntreal(){
        MunicipalityIntegrator mi = new MunicipalityIntegrator();
        muniList = mi.getCompleteMuniList();
        authMuniList = mi.getUserAuthMunis();       
        UserIntegrator ui = new UserIntegrator();
        userList = ui.getCompleteUserList();
        
    }
    
}
