package com.camel.RegistryAndBeans;

import com.camel.typeconversion.ConvertedObject;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.language.Bean;

public class HelloBean {

    //This annotation tells camel this is a candidate for a bean method
    @Handler
    //As we have a TypeConverted for ConvertedObject Camel will convert the string passed in to a ConvertedObject
    //by default.
    //By using the @Bean annotation camel will lookup the guidGenerator in the registry and call its only method which
    //returns a Random number
    //First value if un annotated will be bound to Body... Have annotated it however just for completion
    public String hello(@Body ConvertedObject obj, @Bean(ref = "guidGenerator", method = "generate") int refNum){
        return "Hello " + obj.getName() + " your unique reference number is " + refNum;
    }

    @Handler
    public String goodbye(String str){
        return "Goodbye " + str;
    }
}
