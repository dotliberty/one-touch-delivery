package dot.liberty.auth.util;

import java.util.Random;

public final class VerificationCodeUtils {

    private static final Random random = new Random();

    private VerificationCodeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String generateVerificationCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

}
