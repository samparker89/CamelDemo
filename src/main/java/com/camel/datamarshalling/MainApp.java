package com.camel.datamarshalling;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.model.dataformat.JaxbDataFormat;

import javax.jms.ConnectionFactory;

/**
 * A Camel Application
 * Using jaxb I have had to add an extra piece of configuration for the maven resources plugin to copy over the
 * jaxb.index which jaxb requires.
 */
public class MainApp {
    public static void main(String... args) throws Exception {
        CamelContext context = new DefaultCamelContext();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
        context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        context.addRoutes(new XMLRoute());
        context.addRoutes(new CSVRoute());
        context.start();
        Thread.sleep(10000);
        context.stop();
    }
}

class XMLRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        JaxbDataFormat jaxb = new JaxbDataFormat();
        jaxb.setContextPath(BusinessObject.class.getPackage().getName());

        //Notice the way we have to declare the file name as a parameter
        from("file://src/data/?fileName=myDataObject.xml&noop=true").
                unmarshal(jaxb).
                process(exchange -> {
                    //This is actually the only line we need to do the conversion. Camel will convert under the
                    //covers to the class specified as the argument to getBody(). We can remove the unmarshal(),
                    //the JaxbDataFormat object and the maven configuration for copying the jaxb.index.
                    BusinessObject busObj = exchange.getIn().getBody(BusinessObject.class);
                    log.info("Name is " + busObj.getName());
                    log.info("Address is " + busObj.getAddress());
                });
    }
}

class CSVRoute extends RouteBuilder{

    @Override
    public void configure() throws Exception {
        from("file://src/data/?fileName=csvMarshall.csv&noop=true").
                //Use bindy to convert CSV to POJO
                unmarshal().bindy(BindyType.Csv, CsvObject.class).
                //Calling split will split out the List<List<String>> and give us a simple List<String> of each line
                split(body()).
                process(exchange -> {
                    CsvObject bo = exchange.getIn().getBody(CsvObject.class);
                    exchange.getIn().setBody(bo.getSurname() + " " + bo.getForename() + " " + bo.getAge());
                }).
                //Notice the fileExist option at the end. This will append to end of file rather than replace.
                to("file://src/data/?fileName=csvMarshallOutPut.txt&noop=true&fileExist=Append");
    }

    @CsvRecord(separator = ",", crlf = "UNIX")
    public static class CsvObject {

        @DataField(pos = 1)
        private String forename;

        @DataField(pos = 2)
        private String surname;

        @DataField(pos = 3)
        private Integer age;

        String getForename() {
            return forename;
        }

        void setForename(String forename) {
            this.forename = forename;
        }

        String getSurname() {
            return surname;
        }

        void setSurname(String surname) {
            this.surname = surname;
        }

        Integer getAge() {
            return age;
        }

        void setAge(Integer age) {
            this.age = age;
        }
    }
}

