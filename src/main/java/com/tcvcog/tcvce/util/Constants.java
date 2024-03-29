/*
 * Copyright (C) 2017 cedba
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

import com.tcvcog.tcvce.entities.search.QueryCEAREnum;
import java.io.Serializable;

/**
 *
 * @author cedba
 */
public class Constants implements Serializable {

    /**
     * Creates a new instance of Constants
     */
    public Constants() {
    }
    
    // Coordinator Keys
    public final static String PROPERTY_COORDINATOR_SCOPE = "sessionScope.";
    public final static String PROPERTY_COORDINATOR_KEY = "propertyCoordinator";
    
    public final static String USER_COORDINATOR_SCOPE = "applicationScope.";
    public final static String USER_COORDINATOR_KEY = "userCoordinator";
    
    
    public final static String SERVER_NAME = "localhost:5432";
    public final static String DB_NAME = "cogdb";
    public final static String DB_USERNAME = "sylvia";
    public final static String DB_PASS = "c0d3";
    public final static int MAX_CONNECTIONS = 1000;
    
    // these are the names of the properties files/bundles

    public final static String MESSAGE_TEXT= "messagetext";
    public final static String LOGGING_CATEGORIES = "loggingcategories";
    public final static String EVENT_CATEGORY_BUNDLE = "eventCategories";
    public final static String DB_FIXED_VALUE_BUNDLE = "dbFixedValueLookup";
    public final static String DB_CONNECTION_PARAMS = "dbConnectionParams";
    public final static String VIOLATIONS_BUNDLE = "violations";
    
    public final static String STYLE_CLASS_INACTIVE_CASE_PHASE = "stage-inactive";
    
    // starting BOB Queroes
    public final static QueryCEAREnum QUERY_INITIAL_CEAR = QueryCEAREnum.ALL_PAST30;
    
    
    
    
    
    
}
