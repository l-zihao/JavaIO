package tomcat.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;

public class Response {

    private static final int BUFFER_SIZE = 1024;

    Request request;

    OutputStream output;

    public Response(OutputStream output){
        this.output = output;
    }

    public void setRequest(Request request){
        this.request = request;
    }

    public void sendStaticResource(){
        File file = new File(ConnectorUtils.WEB_ROOT, request.getRequestURI());

    }

    private void write(File resouce, HttpStatus status) throws FileNotFoundException{
        try(FileInputStream fis = new FileInputStream(resouce)){

        }
    }
}
