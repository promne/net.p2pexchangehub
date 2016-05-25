package net.p2pexchangehub.core.util;

import java.security.SecureRandom;
import java.util.Random;

public class CodeGenerator {

    private static final char[] baseChars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

    private static final Random random = new SecureRandom();

    public static String generateCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(baseChars[random.nextInt(baseChars.length)]);
        }
        return sb.toString();
    }

}
