package com.camel.typeconversion;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;

public class MainApp extends CamelTestSupport {

    public static void main(String args[]) throws Exception{
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file://src/data/?fileName=copybook_string.txt&noop=true")
                        //We can remove this line and the conversion will still automatically happen as we have
                        //specified the requested converted type in the getBody() method for the exchange object below.
                        .convertBodyTo(ConvertedObject.class)
                        .process(exchange -> {
                            log.info("Converted Object is " + exchange.getIn().getBody(ConvertedObject.class).toString());
                        });
            }
        });
        context.start();
        Thread.sleep(10000);
        context.stop();

    }

}
