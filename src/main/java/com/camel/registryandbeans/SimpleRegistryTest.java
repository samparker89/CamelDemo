package com.camel.registryandbeans;

import junit.framework.TestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleRegistryTest extends TestCase{

    private static final Logger LOG = LoggerFactory.getLogger(SimpleRegistryTest.class);

    private CamelContext context;
    private ProducerTemplate template;

    @Override
    protected void setUp() throws Exception {
        LOG.info("In setup");
        //This example uses Camels Simple Registry. If we integrated Camel with Spring Camel would automatically use
        //ApplicationContextRegistry which would lookup beans from Springs ApplicationContext.
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("helloBean", new HelloBean());
        registry.put("guidGenerator", new GuidGenerator());

        context = new DefaultCamelContext(registry);
        template = context.createProducerTemplate();

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:hello")
                        //By setting this header we can bypass the method lookup from the registry
                        .setHeader("CamelBeanMethodName", constant("hello"))
                        .process(exchange -> {
                            log.info("Before " + exchange.getIn().getBody(String.class));
                        })
                        //Using the bean we pass in the value from the exchange and Camel should do the conversion for us.
                        //The beans method is then invoked and the returned value is added to the camel exchange automatically.
                        //All below lines do the same. This is because HelloBean only has one method.
                        .bean("helloBean", "hello") // This would also allow us to skip the method lookup in the registry
                        //.bean("helloBean")
                        //.bean(HelloBean.class)
                        .process(exchange -> {
                            log.info("After " + exchange.getIn().getBody(String.class));
                        });
            }
        });
        context.start();
    };

    @Override
    protected void tearDown() throws Exception {
        template.stop();
        context.stop();
    }

    public void testHello() {
        //Object reply = template.requestBody("direct:hello", "World");
        String expected = "Hello SAM";
        Object actual = template.requestBody("direct:hello", "SAM       10        1");
        LOG.info("Result is " + actual);
        //assertEquals(expected, actual);
    }

}

