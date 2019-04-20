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
package com.tcvcog.tcvce.occupancy.coordinators;

import com.squareup.connect.ApiClient;
import com.squareup.connect.ApiException;
import com.squareup.connect.Configuration;
import com.squareup.connect.api.CheckoutApi;
import com.squareup.connect.api.TransactionsApi;
import com.squareup.connect.auth.OAuth;
import com.squareup.connect.models.CreateCheckoutRequest;
import com.squareup.connect.models.CreateCheckoutResponse;
import com.squareup.connect.models.CreateOrderRequest;
import com.squareup.connect.models.CreateOrderRequestLineItem;
import com.squareup.connect.models.Money;
import com.squareup.connect.models.Order;
import com.squareup.connect.models.OrderLineItem;
import com.squareup.connect.models.RetrieveTransactionResponse;
import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.util.Constants;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author Dominic Pimpinella
 */
public class SquareCheckoutCoordinator extends BackingBeanUtils implements Serializable{
    
    private final Long APPLICATION_FEE = 1500L;
    private final String FEE_QUANTITY = "1";
    private final String FEE_DESC = "Permit Application Fee";
    private final boolean ASK_FOR_SHIPPING_ADDRESS = false;
    
    // The values for these variables are stored in squarePayments.properties
    private String SANDBOX_TOKEN;
    private String TEST_LOCATION;    
    private String REDIRECT_URL;
    
    private String generatedCheckoutId;
    private String idempotencyKey;
    
    public SquareCheckoutCoordinator(){
        
    }
    
    /**
     * Sends the user to a Square hosted checkout page for taking payments by creating a Checkout 
     * object and sending it to Square's Checkout API.
     * 
     * @throws IOException 
     */        
    public void sendToCheckout() throws IOException{
        
        SANDBOX_TOKEN = getResourceBundle(Constants.SQUARE_PAYMENTS_PARAMS).getString("sandbox_token");
        TEST_LOCATION = getResourceBundle(Constants.SQUARE_PAYMENTS_PARAMS).getString("test_location");
        REDIRECT_URL = getResourceBundle(Constants.SQUARE_PAYMENTS_PARAMS).getString("redirect_url");        
    
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setAccessToken(SANDBOX_TOKEN);

        OAuth oauth2 = (OAuth) defaultClient.getAuthentication("oauth2");
        oauth2.setAccessToken(SANDBOX_TOKEN);

        CheckoutApi checkoutInstance = new CheckoutApi();
        checkoutInstance.setApiClient(defaultClient);
        
        CreateCheckoutRequest body = new CreateCheckoutRequest();
        idempotencyKey = getIdempotencyKey();
        body.setIdempotencyKey(idempotencyKey);       
        body.setOrder(configureOrderRequest());
        body.setAskForShippingAddress(ASK_FOR_SHIPPING_ADDRESS);
        body.setRedirectUrl(REDIRECT_URL);
        
        try {
            CreateCheckoutResponse result = checkoutInstance.createCheckout(TEST_LOCATION, body);            
            generatedCheckoutId = result.getCheckout().getId();
            String checkoutURL = result.getCheckout().getCheckoutPageUrl();
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(checkoutURL);
            
        } catch (ApiException e) {
            System.out.println("Exception when calling CheckoutApi.createCheckout " + e);
        }
    }
    /**
     * Configures a CreateOrderRequest object with relevant fee amounts, quantities, and 
     * descriptions, to be set on CreateCheckoutRequest object that will be sent to Checkout API. 
     * @return 
     */
    public CreateOrderRequest configureOrderRequest(){
        
        // Set application fee
        Money price = new Money();
        price.setAmount(APPLICATION_FEE);
        price.setCurrency(Money.CurrencyEnum.USD);
        
        /* 
        Configure fee description and quantity for application fee line item on Checkout webpage,
        to be set on Order object
        */
        OrderLineItem lineItem = new OrderLineItem();
        lineItem.setName(FEE_DESC);
        lineItem.setQuantity(FEE_QUANTITY);
        lineItem.setBasePriceMoney(price);
        List<OrderLineItem> lineItems = new ArrayList<>();
        lineItems.add(lineItem);
        
        /*
        Configure List<CreateOrderRequestLineItem> to be set on CreateOrderRequest object. This
        must be set for API call to work, but this line item will not be shown to the user.
        */
        CreateOrderRequestLineItem orderRequestLineItem = new CreateOrderRequestLineItem();
        orderRequestLineItem.setQuantity("1");        
        List<CreateOrderRequestLineItem> orderRequestLineItems = new ArrayList<>();
        orderRequestLineItems.add(orderRequestLineItem);       
        
        // Configure Order object
        Order order = new Order();
        order.setLocationId(TEST_LOCATION);
        order.setLineItems(lineItems);

        /* 
        Set Order and List<CreateOrderRequestLineItem> on CreateOrderRequest, which is the object
        to be set on CreateCheckoutRequest.
        */
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setOrder(order);
        orderRequest.setLineItems(orderRequestLineItems);
        return orderRequest;
    }
    
    /**
     * Verifies the Checkout transaction to prevent order spoofing.
     * @param transactionId - viewParam appended to redirect URL provided by Checkout API 
     * @param checkoutId - viewParam appended to redirect URL by Checkout API
     */
    public void verifyTransaction(String transactionId, String checkoutId) {
        RetrieveTransactionResponse result = null;
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        OAuth oauth2 = (OAuth) defaultClient.getAuthentication("oauth2");
        oauth2.setAccessToken(SANDBOX_TOKEN);
        
        TransactionsApi transactionClient = new TransactionsApi();
        
        try {
            result = transactionClient.retrieveTransaction(TEST_LOCATION, transactionId);
        } catch (ApiException e) {
            System.out.println("Exception when calling TransactionsApi.retrieveTransaction: " + e);
        }
        
        Long transactionAmount = null;
        boolean isVerifiedTransaction;
        
        /* 
        Checks that transaction amount (obtained using transactionId) and checkoutId (from 
        viewParam appended to redirect URL) are equal to expected values to verify transaction
        */
        if(result != null){
            transactionAmount = result.getTransaction().getTenders().get(0).getAmountMoney()
                    .getAmount();
            
            if(generatedCheckoutId.equals(checkoutId) 
                && transactionAmount.equals(APPLICATION_FEE)){
                isVerifiedTransaction = true;
            }
            else {
                isVerifiedTransaction = false;
                logUnverifiedTransaction(transactionAmount, checkoutId);
            }
        }
        else {
            System.out.println("No transaction response was received for the following "
                    + "transactionId: " + transactionId + ". Could not retrieve transactionAmount.");
        }        
    }
    /**
     * Prints the reason(s) that a checkout is unverified to the console. 
     * 
     * @param transactionAmount - Transaction amount retrieved from Square's Transactions API, to be
     * checked against the value of system supplied application fee.
     * @param checkoutId - CheckoutId received as viewParam appended to redirect URL after user has 
     * completed the checkout page, to be checked against the checkoutId generated when sending 
     * Checkout object to Checkout API.
     */
    public void logUnverifiedTransaction (Long transactionAmount, String checkoutId) {
        boolean correctTransactionAmount = transactionAmount.equals(APPLICATION_FEE);
        boolean correctCheckoutId = generatedCheckoutId.equals(checkoutId);
        
        if (!correctTransactionAmount){
            System.out.println("Invalid Transaction: Transaction amount from transactionId and "
                    + "system supplied application fee are not equal.");
        }
        
        if (!correctCheckoutId){
            System.out.println("Invalid Transaction: CheckoutId viewParam and system supplied "
                    + "checkoutId are not equal.");
        }
    }
    
    /**
     * Creates an idempotency key, which must be unique for every order sent to Square Checkout for 
     * a given location (based on location ID).
     * @return idempotencyKey - a unique String 
     */
    public String getIdempotencyKey(){
        UUID idempotencyUUID = UUID.randomUUID();
        String idempotencyKey = idempotencyUUID.toString();
        System.out.println(idempotencyKey);
        return idempotencyKey;
    }
}
