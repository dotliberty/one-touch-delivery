package dot.liberty.auth.exception;

public class InvalidVerificationCodeException extends RuntimeException {
    public InvalidVerificationCodeException() {
        super("Invalid verification code");
    }
}
