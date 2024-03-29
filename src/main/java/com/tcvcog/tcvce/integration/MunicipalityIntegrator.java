/*
 * Copyright (C) 2018 Turtle Creek Valley
 * Council of Governments, PA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eric C. Darsow
 */
public class MunicipalityIntegrator extends BackingBeanUtils implements Serializable {

    
    /**
     * Creates a new instance of MunicipalityIntegrator
     */
    public MunicipalityIntegrator() {
    }
    
  
    
    public Municipality getMuni(int muniCode) throws IntegrationException{
        Municipality muni = null;
        PreparedStatement stmt = null;
        Connection con = null;
        // note that muniCode is not returned in this query since it is specified in the WHERE
        String query = "SELECT municode, muniname, address_street, address_city, address_state, \n" +
                        "       address_zip, phone, fax, email, managername, managerphone, population, \n" +
                        "       activeinprogram, defaultcodeset, occpermitissuingsource_sourceid, \n" +
                        "       defaultcodeofficeruser, defaultcourtentity, novprintstyle_styleid \n" +
                        " FROM public.municipality WHERE municode = ?;";
        ResultSet rs = null;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            //System.out.println("MunicipalityIntegrator.getMuni | query: " + stmt.toString());
            rs = stmt.executeQuery();
            while(rs.next()){
                System.out.println("MuniIntegrator.getMuni| inside while having at least one row");
                muni = generateMuni(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuni | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuni", ex);
        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return muni;
        
    }
    
    public Municipality generateMuni(ResultSet rs) throws SQLException, IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        UserIntegrator ui = getUserIntegrator();
        
        Municipality muni = new Municipality();
        
        muni.setMuniCode(rs.getInt("municode"));
        muni.setMuniName(rs.getString("muniname"));
        muni.setAddress_street(rs.getString("address_street"));
        muni.setAddress_city(rs.getString("address_city"));
        

        muni.setAddress_state(rs.getString("address_state"));
        muni.setAddress_zip(rs.getString("address_zip"));
        muni.setPhone(rs.getString("phone"));

        muni.setFax(rs.getString("fax"));
        muni.setEmail(rs.getString("email"));
        muni.setManagerName(rs.getString("managername"));

        muni.setManagerPhone(rs.getString("managerphone"));
        muni.setPopulation(rs.getInt("population"));
        muni.setActiveInProgram(rs.getBoolean("activeinprogram"));             
        muni.setDefaultCodeSetID(rs.getInt("defaultcodeset"));
        muni.setIssuingPermitCodeSourceID(rs.getInt("occpermitissuingsource_sourceid"));
        muni.setDefaultCodeOfficerUser(ui.getUser(rs.getInt("defaultcodeofficeruser")));
        muni.setDefaultCourtEntity(cei.getCourtEntity(rs.getInt("defaultcourtentity")));
        
        muni.setDefaultNOVStyleID(rs.getInt("novprintstyle_styleid"));
        
        return muni;
    }
    
    public List<Municipality> getMuniList() throws IntegrationException{
        List<Municipality> mList = new ArrayList<>();
        String query = "SELECT municode FROM municipality;";
        ResultSet rs = null;
        Statement stmt = null;
        System.out.println("MunicipalityIntegrator.getMuniList | about to get pgcon");
        Connection con = getPostgresCon();
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                System.out.println("MunicipalityIntegrator.getMuniList | added muni to list");
                mList.add(getMuni(rs.getInt("municode")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getMuniList", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return mList;
        
    }
    
   
    
    public User getDefaultCodeOfficer(int  muniCode) throws IntegrationException{
        User u = null;
        UserIntegrator ui = getUserIntegrator();
       
        Connection con = getPostgresCon();
        
        String query = "SELECT defaultcodeofficeruser FROM municipality WHERE municode = ?;";
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muniCode);
            rs = stmt.executeQuery();
            while(rs.next()){
                u = ui.getUser(rs.getInt("defaultcodeofficeruser"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getDefaultCodeOfficer", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return u;
    }
    
    
    
    //TODO: finish me
    public void updateMuni(Municipality muni) throws IntegrationException{
        
        Connection con = null;
        String query =  "UPDATE public.municipality\n" +
                        "   SET muniname=?, address_street=?, address_city=?, address_state=?, \n" +
                        "       address_zip=?, phone=?, fax=?, email=?, managername=?, managerphone=?, \n" +
                        "       population=?, activeinprogram=?, defaultcodeset=?, "
                        + "occpermitissuingsource_sourceid=?, defaultcodeofficeruser=?, defaultcourtentity=?, \n" +
                        " novprintstyle_styleid=? " +
                        " WHERE municode=?;";
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setString(1, muni.getMuniName());
            stmt.setString(2, muni.getAddress_street());
            stmt.setString(3, muni.getAddress_city());
            stmt.setString(4, muni.getAddress_state());
            stmt.setString(5, muni.getAddress_zip());
            stmt.setString(6, muni.getPhone());
            stmt.setString(7, muni.getFax());
            stmt.setString(8, muni.getEmail());
            stmt.setString(9, muni.getManagerName());
            stmt.setString(10, muni.getManagerPhone());
            stmt.setInt(11, muni.getPopulation());
            stmt.setBoolean(12, muni.isActiveInProgram());
            stmt.setInt(13, muni.getDefaultCodeSetID());
            stmt.setInt(14, muni.getIssuingPermitCodeSourceID());
            stmt.setInt(15, muni.getDefaultCodeOfficerUser().getUserID());
            stmt.setInt(16, muni.getDefaultCourtEntity().getCourtEntityID());
            stmt.setInt(17, muni.getDefaultNOVStyleID());
            stmt.setInt(18, muni.getMuniCode());
            
            System.out.println("MunicipalityIntegrator.updateMuni | stmt: " + stmt.toString());
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.updateMuni", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public Map<Integer, String> getMunicipalityMap() throws IntegrationException{
        Map<Integer, String> muniMap = null;
            
        muniMap = new HashMap<>();

        Connection con = getPostgresCon();

        String query = "SELECT muniCode, muniName FROM municipality;";
        ResultSet rs = null;
        Statement stmt = null;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            
            while(rs.next()){
                muniMap.put(rs.getInt("muniCode"),rs.getString("muniName"));
                System.out.println("MunicipalityIntegrator.getMunicipalityMap | got: " + muniMap.get(rs.getInt("muniCode")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.generateCompleteMuniNameIDMap", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return muniMap;
    }
    
    
    public HashMap<String, Integer> generateCompleteMuniNameIDMap() throws IntegrationException{
        HashMap<String, Integer> muniMap = new HashMap<>();
       
        Connection con = getPostgresCon();
        
        String query = "SELECT muniCode, muniName FROM municipality;";
        ResultSet rs = null;
        Statement stmt = null;
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                muniMap.put(rs.getString("muniName"), rs.getInt("muniCode"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.generateCompleteMuniNameIDMap", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return muniMap;
    }
    
      /**
     * @return the municipalityMap
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public HashMap<String, Integer> getMunicipalityStringIDMap() throws IntegrationException{
        return generateCompleteMuniNameIDMap();
    }
    
}
