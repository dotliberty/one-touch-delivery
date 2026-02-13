package dot.liberty.auth.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Email not verified. Please check your inbox");
    }
}
