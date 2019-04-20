/*
 * Copyright (C) 2019 Eric C. Darsow
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import java.io.Serializable;
import com.tcvcog.tcvce.occupancy.coordinators.SquareCheckoutCoordinator;
import java.io.IOException;

/**
 *
 * @author Dominic Pimpinella
 */
public class OccPermitApplicationPaymentBB extends BackingBeanUtils implements Serializable {
    
    private String checkoutId;
    private String transactionId;
    private Long amount;
    /**
     * Creates a new instance of OccPermitApplicationPaymentBB
     */
    public OccPermitApplicationPaymentBB() {   
        
    }

    /**
     * @return the amount
     */
    public int getAmount() {
        return 10000;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public void makePayment() throws IOException{        
        SquareCheckoutCoordinator scc = getSquareCheckoutCoordinator();
        scc.sendToCheckout();
    }

    /**
     * @return the checkoutId
     */
    public String getCheckoutId() {
        return checkoutId;
    }

    /**
     * @param checkoutId the checkoutId to set
     */
    public void setCheckoutId(String checkoutId) {
        this.checkoutId = checkoutId;
    }

    /**
     * @return the transactionId
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * @param transactionId the transactionId to set
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public void verifyPayment(){
        SquareCheckoutCoordinator scc = getSquareCheckoutCoordinator();
        scc.verifyTransaction(transactionId, checkoutId);
    }
    
}
