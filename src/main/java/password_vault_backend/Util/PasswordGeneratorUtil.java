package password_vault_backend.Util;

import java.security.SecureRandom;

public class PasswordGeneratorUtil {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}?";

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(int length, boolean useUpper, boolean useDigits, boolean useSymbols) {
        StringBuilder pool = new StringBuilder(LOWER);
        if (useUpper) pool.append(UPPER);
        if (useDigits) pool.append(DIGITS);
        if (useSymbols) pool.append(SYMBOLS);

        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(pool.length());
            password.append(pool.charAt(index));
        }
        return password.toString();
    }
}