package dot.liberty.auth.exception;

public class EmailAlreadyVerifiedException extends RuntimeException {
    public EmailAlreadyVerifiedException() {
        super("Email already verified");
    }
}
