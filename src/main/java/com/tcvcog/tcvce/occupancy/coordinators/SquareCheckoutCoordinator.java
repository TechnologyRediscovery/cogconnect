/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author Dominic Pimpinella
 */
public class SquareCheckoutCoordinator extends BackingBeanUtils implements Serializable{
    
    private final Long APPLICATION_FEE = 1500L;
    private final boolean ASK_FOR_SHIPPING_ADDRESS = false;
    private final String FEE_QUANTITY = "1";
    private final String FEE_DESC = "Permit Application Fee";
    
    private final String SANDBOX_TOKEN = "EAAAEZfNjdg56fhPorHJs2DiWe1QgK-m-RMfleLNzfOGovFUFzKsYu7BHGchDoOw";
    private final String TEST_LOCATION = "CBASEPH5WFkfvzm9OJ8TtXhBle0gAQ";
    
    private final String REDIRECT_URL = "http://localhost:8080/tcvce/public/services/occPermitApplicationFlow/occPermitConfirmPayment.xhtml";
    private String generatedCheckoutId;
    
    public SquareCheckoutCoordinator(){
        
    }
    
    public void createCheckout() throws IOException{
    
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        defaultClient.setAccessToken(SANDBOX_TOKEN);
        defaultClient.setAccessToken(SANDBOX_TOKEN);
        defaultClient.setAccessToken(SANDBOX_TOKEN);

        // Configure OAuth2 access token for authorization: oauth2
        OAuth oauth2 = (OAuth) defaultClient.getAuthentication("oauth2");
        oauth2.setAccessToken(SANDBOX_TOKEN);

        CheckoutApi apiInstance = new CheckoutApi();
        apiInstance.setApiClient(defaultClient);
        
        Money price = new Money();
        price.setAmount(APPLICATION_FEE);
        price.setCurrency(Money.CurrencyEnum.USD);
        
        OrderLineItem lineItem = new OrderLineItem();
        lineItem.setName(FEE_DESC);
        lineItem.setQuantity(FEE_QUANTITY);
        lineItem.setBasePriceMoney(price);
        List<OrderLineItem> lineItems = new ArrayList<>();
        lineItems.add(lineItem);
        
        
//         This needs to be set to work, but I have no idea why. CreateOrderRequestLineItem() is 
//        marked as deprecated at https://github.com/square/connect-java-sdk/blob/master/docs/CreateOrderRequestLineItem.md
//        The OrderLineItem above is displayed in the checkout when both lineItems and orderRequestLineItems are set in an 
//        Order and an CreateOrderRequest, respectively. If OrderLineItem is not set in Order, the checkout will display the 
//        orderRequestLineItems.

        CreateOrderRequestLineItem orderRequestLineItem = new CreateOrderRequestLineItem();
        orderRequestLineItem.setQuantity("1");
        
        List<CreateOrderRequestLineItem> orderRequestLineItems = new ArrayList<>();
        orderRequestLineItems.add(orderRequestLineItem);       
        
        Order order = new Order();
        order.setLocationId(TEST_LOCATION);
        order.setLineItems(lineItems);

        
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setOrder(order);
        orderRequest.setLineItems(orderRequestLineItems);
        
        CreateCheckoutRequest body = new CreateCheckoutRequest();
        body.setIdempotencyKey("3584305w1s28341122122");
        body.setOrder(orderRequest);
        body.setAskForShippingAddress(ASK_FOR_SHIPPING_ADDRESS);
        body.setRedirectUrl(REDIRECT_URL);
        
        try {
            CreateCheckoutResponse result = apiInstance.createCheckout(TEST_LOCATION, body);
            System.out.println(result);
            
            generatedCheckoutId = result.getCheckout().getId();
            String checkoutURL = result.getCheckout().getCheckoutPageUrl();
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(checkoutURL);
            
        } catch (ApiException e) {
            System.err.println("Exception when calling CheckoutApi.createCheckout");
            e.printStackTrace();
        }
    }
    
    public void verifyTransaction(String transactionId, String checkoutId) {
        RetrieveTransactionResponse result = null;
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        OAuth oauth2 = (OAuth) defaultClient.getAuthentication("oauth2");
        oauth2.setAccessToken(SANDBOX_TOKEN);
        
        TransactionsApi transactionClient = new TransactionsApi();
        
        try {
            result = transactionClient.retrieveTransaction(TEST_LOCATION, transactionId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TransactionsApi#retrieveTransaction");
            e.printStackTrace();
        }
        
        Long transactionAmount = result.getTransaction().getTenders().get(0).getAmountMoney().getAmount();
        if(generatedCheckoutId.equals(checkoutId) 
                && transactionAmount.equals(APPLICATION_FEE)){
            System.out.println("THIS TRANSACTION IS REAL");
        }
        
    }
}
