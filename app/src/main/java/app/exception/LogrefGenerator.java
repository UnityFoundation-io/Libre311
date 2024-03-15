package app.exception;

import java.security.SecureRandom;

public class LogrefGenerator {

    private static final String UPPERCASE_ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generate(int length) {
        StringBuilder identifier = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(UPPERCASE_ALPHANUMERIC.length());
            char randomChar = UPPERCASE_ALPHANUMERIC.charAt(randomIndex);
            identifier.append(randomChar);
        }

        return identifier.toString();
    }
}