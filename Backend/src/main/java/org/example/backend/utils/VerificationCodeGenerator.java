package org.example.backend.utils;

import java.util.Random;

public class VerificationCodeGenerator {

    public static String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(10000000));
    }

}
