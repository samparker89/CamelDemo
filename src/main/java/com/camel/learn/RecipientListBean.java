package com.camel.learn;

import org.apache.camel.RecipientList;
import org.apache.camel.language.XPath;

public class RecipientListBean {
    @RecipientList
    public String[] route(@XPath("/order/@customer") String customer){
        return isGoldCustomer(customer) ? new String[] {"jms:accounting", "jms:production"} : new String[] {"jms:accounting"};
    }

    private boolean isGoldCustomer(String customer){
        return "honda".equals(customer);
    }
}
