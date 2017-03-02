package com.camel.velocity;

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
                from("file://src/data/?fileName=myDataObject.xml&noop=true")
                        .setHeader("Subject", constant("Thanks for your order"))
                        .setHeader("From", constant("donotreply@riders.com"))
                        .process(exchange -> {
                            EmailObject emailObject = new EmailObject();
                            emailObject.setName("Sam");
                            emailObject.setAmount(100);
                            emailObject.setPrice(50);
                            exchange.getIn().setBody(emailObject, EmailObject.class);
                            log.info(exchange.getIn().getBody(EmailObject.class).toString());
                        })
                        .to("velocity://email.vm")
                        .process(exchange -> {
                            log.info(exchange.getIn().getBody(String.class));
                        });
            }
        });
        context.start();
        Thread.sleep(10000);
        context.stop();

    }

    public static class EmailObject {
        private String name;
        private Integer amount;
        private Integer price;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }

        @Override
        public String toString() {
            return "EmailObject{" +
                    "name='" + name + '\'' +
                    ", amount=" + amount +
                    ", price=" + price +
                    '}';
        }
    }

}
