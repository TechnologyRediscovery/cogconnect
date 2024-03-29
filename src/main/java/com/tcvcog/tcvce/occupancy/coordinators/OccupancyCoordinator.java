/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
package com.tcvcog.tcvce.occupancy.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.occupancy.entities.InspectedElement;
import com.tcvcog.tcvce.occupancy.entities.InspectedSpace;
import com.tcvcog.tcvce.occupancy.entities.LocationDescriptor;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplication;
import com.tcvcog.tcvce.occupancy.entities.OccupancyInspection;
import com.tcvcog.tcvce.occupancy.entities.PersonsRequirement;
import com.tcvcog.tcvce.occupancy.entities.Space;
import com.tcvcog.tcvce.occupancy.integration.ChecklistIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Eric C. Darsow
 */
public class OccupancyCoordinator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of OccupancyCoordinator
     */
    public OccupancyCoordinator() {
    }
    
    public InspectedSpace generateNewlyInspectedSpace(Space space, String locationDescription){
        InspectedSpace is = new InspectedSpace();
        LocationDescriptor ld = new LocationDescriptor();
        ld.setLocationDescription(locationDescription);
        ListIterator<CodeElement> elementIterator = space.getElementList().listIterator();
        InspectedElement ie;
        
        while(elementIterator.hasNext()){
            CodeElement ce = elementIterator.next();
            ie = new InspectedElement();
            ie.setElement(ce);
            is.getInspectedElementList().add(ie);
            // each element in this space gets a reference to the same LocationDescriptor object
            is.setLocation(ld);
        }
        return is;
    }    
    
    public void saveNewlyInspectedSpace(OccupancyInspection oi, InspectedSpace is) throws IntegrationException{
        ChecklistIntegrator ci = getChecklistIntegrator();
        ci.insertNewlyInspectedSpace(oi, is);
    }
    
    public OccPermitApplication getNewOccPermitApplication(){
        OccPermitApplication occpermitapp = new OccPermitApplication();        
        occpermitapp.setSubmissionDate(LocalDateTime.now());        
        return occpermitapp;       
    }
    
    /**
     * Sets boolean requirementSatisfied on an OccPermitApplication based on the application reason,
     * the person requirement for that reason, and the PersonTypes of the Persons attached to the 
     * application.
     * @param opa 
     */
    public void verifyOccPermitPersonsRequirement (OccPermitApplication opa){
        boolean isRequirementSatisfied = true;
        PersonsRequirement pr = opa.getReason().getPersonsRequirement();        
        List<PersonType> requiredPersonTypes = pr.getRequiredPersonTypes();
        List<Person> applicationPersons = opa.getAttachedPersons();
        List<PersonType> applicationPersonTypes = new ArrayList<>();
        for (Person applicationPerson:applicationPersons){
            applicationPersonTypes.add(applicationPerson.getPersonType());
        }
        for (PersonType personType:requiredPersonTypes) {
            if (!applicationPersonTypes.contains(personType)){
                isRequirementSatisfied = false;               
            }
        }
        pr.setRequirementSatisfied(isRequirementSatisfied);
    }
}
