package example;
        
import java.util.Map;    
import java.util.List;

public class RequestClass {
       
        String body;
        Map<String, String> headers;
        private Map<String, List<String>> multiValueHeaders;


        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public Map<String, List<String>> getMultiValueHeaders() {
            return multiValueHeaders;
        }
    

        public void setMultiValueHeaders(Map<String, List<String>> multiValueHeaders) {
            this.multiValueHeaders = multiValueHeaders;
        }

        public RequestClass(String body, Map<String, String> headers, Map<String, List<String>> multiValueHeaders) {
            this.body = body;
            this.headers = headers;
            this.multiValueHeaders = multiValueHeaders;
        }
        public RequestClass() {
        }
    }
