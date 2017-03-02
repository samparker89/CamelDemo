package com.camel.RegistryAndBeans;

import java.util.Random;

public class GuidGenerator {

    private static final int MAX_RANDOM_NUMBER = 10000000;

    public static Integer generate(){
        Random random = new Random();
        return random.nextInt(MAX_RANDOM_NUMBER);
    }
}
