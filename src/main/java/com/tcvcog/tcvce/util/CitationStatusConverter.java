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
package com.tcvcog.tcvce.util;

import com.tcvcog.tcvce.entities.CitationStatus;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Eric C. Darsow
 */
@FacesConverter(value="citationStatusConverter")
public class CitationStatusConverter extends EntityConverter implements Converter{
    
     @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String titleS) {
        System.out.println("CitationStatusConverter| title: " +titleS);
        if(titleS.isEmpty()) {
            return null; 
        }
        CitationStatus o = (CitationStatus) this.getViewMap(fc).get(titleS);
        return o;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        System.out.println("CitationStatusConverter.getAsString");
        
        if (o == null){
            return "";
        }
        
        CitationStatus cs = (CitationStatus) o;
        String title = cs.getStatusTitle();  
        if (title != null){
            this.getViewMap(fc).put(title,o);
            return title;
            
        } else {
            return "status error";
        }
        
    }
}
