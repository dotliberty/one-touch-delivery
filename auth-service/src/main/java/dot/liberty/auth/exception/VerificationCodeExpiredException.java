package dot.liberty.auth.exception;

public class VerificationCodeExpiredException extends RuntimeException {
    public VerificationCodeExpiredException() {
        super("Verification code expired");
    }
}
