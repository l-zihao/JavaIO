package tomcat.connector;

public enum HttpStatus {

    OK(200, "OK"),
    NOT_FOUND(404, "File Not Found");

    private int statusCode;
    private String reason;

    HttpStatus(int statusCode, String reason) {
        this.statusCode = statusCode;
        this.reason = reason;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReason() {
        return reason;
    }

}