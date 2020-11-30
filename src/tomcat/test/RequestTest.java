package tomcat.test;

import tomcat.connector.Request;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class RequestTest {

    private static final String validRequest = "GET /index.html HTTP/1.1";

    public static void main(String[] args) {
        InputStream input = new ByteArrayInputStream(validRequest.getBytes());
        Request request = new Request(input);
        request.parse();
        System.out.println("/index.html".equals(request.getRequestURI()));
    }

}
