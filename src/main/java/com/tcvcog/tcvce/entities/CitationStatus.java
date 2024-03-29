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
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Eric C. Darsow
 */
public class CitationStatus extends Status {
    private boolean editsAllowed;
    private CasePhaseChangeRule phaseChangeRule;
    

    /**
     * @return the editsAllowed
     */
    public boolean isEditsAllowed() {
        return editsAllowed;
    }

    /**
     * @param editsAllowed the editsAllowed to set
     */
    public void setEditsAllowed(boolean editsAllowed) {
        this.editsAllowed = editsAllowed;
    }

    /**
     * @return the phaseChangeRule
     */
    public CasePhaseChangeRule getPhaseChangeRule() {
        return phaseChangeRule;
    }

    /**
     * @param phaseChangeRule the phaseChangeRule to set
     */
    public void setPhaseChangeRule(CasePhaseChangeRule phaseChangeRule) {
        this.phaseChangeRule = phaseChangeRule;
    }

    
    
    
}
