package su.aspectvisuals.client.account.api;

public class ApiException extends RuntimeException {
    private final ApiError error;

    public ApiException(ApiError error) {
        super(error.message());
        this.error = error;
    }

    public ApiError error() {
        return error;
    }
}
