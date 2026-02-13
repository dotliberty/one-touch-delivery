package dot.liberty.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    private InvalidCredentialsException(String message) {
        super(message);
    }

    public static InvalidCredentialsException ifUserIsNotFounded() {
        return new InvalidCredentialsException("User not found");
    }

    public static InvalidCredentialsException byInvalidCredentials() {
        return new InvalidCredentialsException("Invalid credentials");
    }

    public static InvalidCredentialsException byInvalidToken() {
        return new InvalidCredentialsException("Invalid or expired token");
    }

}
