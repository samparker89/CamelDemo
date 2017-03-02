package com.camel.typeconversion;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;

/**
 * Converts from copybook fromat to POJO
 *
 * Had to add package in META-INF TypeConverter file so camel knows to scan for annotations
 */
@Converter
public class ConvertedObjectConverter {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertedObjectConverter.class);

    @Converter
    public static ConvertedObject toConvertedObject(String str, Exchange exchange){

        LOG.info("Converting String to ConvertedObject");

        if(str == null){
            throw new IllegalArgumentException();
        }

        return performConversion(str, exchange);

    }

    @Converter
    public static ConvertedObject toConvertedObject(File file, Exchange exchange){

        LOG.info("Converting File to ConvertedObject");

        if(file == null){
            throw new IllegalArgumentException();
        }
        return performConversion(file, exchange);

    }

    private static ConvertedObject performConversion(Object obj, Exchange exchange) {
        //Allows us to access the type converters loaded in to Camels context
        TypeConverter converter = exchange.getContext().getTypeConverter();

        String str = null;
        if(!(obj instanceof String)) {
            str = converter.convertTo(String.class, obj);
        } else {
            str = (String) obj;
        }

        String name = str.substring(0,9).trim();
        String s1 = str.substring(10,19).trim();
        String s2 = str.substring(20).trim();

        LOG.info(String.format("About to convert '%s' : '%s' : '%s'", name, s1, s2));

        BigDecimal price = new BigDecimal(s1);
        //Camel has various type converters. One of the is a String to Integer converter
        //Converter probably contains code under covers equivalent to Integer.valueOf(str);
        Integer amount = converter.convertTo(Integer.class, s2);

        return new ConvertedObject(name, amount, price);
    }

}
