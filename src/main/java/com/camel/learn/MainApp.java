package com.camel.learn;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.ConnectionFactory;

/**
 * A Camel Application
 */
public class MainApp {
    private static final String CAMEL_FILE_NAME_HEADER = "CamelFileName";

    private static final String INCOMING_ORDERS_ENDPOINT = "jms:incomingOrders";
    private static final String XML_ORDERS_ENDPOINT = "jms:xmlOrders";
    private static final String CSV_ORDERS_ENDPOINT = "jms:csvOrders";
    private static final String BAD_ORDERS_ENDPOINT = "jms:badOrders";
    private static final String CONTINUED_PROCESSING_ENDPOINT = "jms:continuedProcessing";
    private static final String ACCOUNTING_ENDPOINT = "jms:accounting";
    private static final String PRODUCTION_ENDPOINT = "jms:production";
    private static final String AUDIT_QUEUE = "jms:auditQueue";

    private static final int NUMBER_OF_PARALLEL_THREADS = 16;

    public static void main(String... args) throws Exception {
        CamelContext context = new DefaultCamelContext();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
        context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                //Process any files in the directory src/data
                from("file:src/data?noop=true").
                    process(exchange -> {
                        log.info(String.format("We have just downloaded %s", exchange.getIn().getHeader(CAMEL_FILE_NAME_HEADER)));
                    }).
                    to(INCOMING_ORDERS_ENDPOINT);

                from(INCOMING_ORDERS_ENDPOINT).
                    wireTap(AUDIT_QUEUE).
                    //Depending on the file extension, we will route to the specified end point
                    //(In this case a jms queue
                    choice().
                        when(header(CAMEL_FILE_NAME_HEADER).endsWith(".xml"))
                            .to(XML_ORDERS_ENDPOINT).
                        when(header(CAMEL_FILE_NAME_HEADER).regex("^.*(csv|cls)$"))
                            .to(CSV_ORDERS_ENDPOINT).
                        otherwise()
                            //By calling the stop method, it prevents the payload from being passed to the
                            //endpoint.
                            .to(BAD_ORDERS_ENDPOINT).stop()
                    .end()
                    //By closing the choice block above, we can also route the messages to a second endpoint.
                    .to(CONTINUED_PROCESSING_ENDPOINT);

                /*
                //ExecutorService to be used to increase thread size for parallel processing
                ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_PARALLEL_THREADS);

                from(XML_ORDERS_ENDPOINT).
                    //Filters out xml which contain test attribute and excludes them
                    filter(xpath("/order[not(@test)]")).
                    process(exchange -> {
                        log.info(String.format("Received xml order: %s", exchange.getIn().getHeader(CAMEL_FILE_NAME_HEADER)));
                    })
                    //Enables us to pass payload to multiple endpoints
                    //Stops processing should an exception be thrown.
                    .multicast().stopOnException()
                    //enable this processing in parallel rather than sequential
                    //Default number of threads is 10. By calling executorService and passing in the executor created above
                    //we can increase the thread size to whatever we like.
                    .parallelProcessing().executorService(executor)
                    .to(ACCOUNTING_ENDPOINT, PRODUCTION_ENDPOINT);
                 */

                /*
                List<String> preferredCustomers = new ArrayList<>();
                preferredCustomers.add("honda");
                preferredCustomers.add("ford");

                from(XML_ORDERS_ENDPOINT).
                    //Filters out xml which contain test attribute and excludes them
                    filter(xpath("/order[not(@test)]")).
                    //Using setHeader will set a header only within the scope of this route. It will be lost once the
                    //payload is passed to the endpoint
                    setHeader("customer", xpath("/order/@customer")).
                    process(exchange -> {
                        log.info(String.format("Received xml order: %s", exchange.getIn().getHeader(CAMEL_FILE_NAME_HEADER)));
                        String recipients = ACCOUNTING_ENDPOINT;
                        String customer = exchange.getIn().getHeader("customer", String.class);
                        //Assign the PRODUCTION_ENDPOINT to all preferred customers
                        if(preferredCustomers.contains(customer)){
                            recipients += "," + PRODUCTION_ENDPOINT;
                        }
                        exchange.getIn().setHeader("recipients", recipients);
                    }).
                    //This will send payload to all endpoints specified in the recipients header
                    recipientList(header("recipients"));
                    */

                //Simplified version of the above. Uses the RecipientListBean bean to obtain a recipientList.
                //This happens due to the @RecipientList annotation on the beans route() method.
                from(XML_ORDERS_ENDPOINT).
                    filter(xpath("/order[not(@test)]")).
                    bean(RecipientListBean.class);

                from(CSV_ORDERS_ENDPOINT).
                    process(exchange -> {
                        log.info(String.format("Received csv order: %s", exchange.getIn().getHeader(CAMEL_FILE_NAME_HEADER)));
                    });

                from(BAD_ORDERS_ENDPOINT).
                    process(exchange -> {
                        log.info(String.format("Received bad order: %s", exchange.getIn().getHeader(CAMEL_FILE_NAME_HEADER)));
                    });

                from(ACCOUNTING_ENDPOINT).
                    process(exchange -> {
                        log.info(String.format("Accounting received order %s", exchange.getIn().getHeader(CAMEL_FILE_NAME_HEADER)));
                    });

                from(PRODUCTION_ENDPOINT).
                    process(exchange -> {
                        log.info(String.format("Production received order %s", exchange.getIn().getHeader(CAMEL_FILE_NAME_HEADER)));
                    });

                from(AUDIT_QUEUE).
                    process(exchange -> {
                        log.info(String.format("Filename is %s. Message body is %s", exchange.getIn().getHeader(CAMEL_FILE_NAME_HEADER), exchange.getIn().getBody(String.class)));
                    });
            }
        });
        context.start();
        Thread.sleep(10000);
        context.stop();
    }
}

