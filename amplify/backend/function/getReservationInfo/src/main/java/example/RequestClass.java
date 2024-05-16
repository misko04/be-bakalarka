

package example;

import java.util.Map;
        
     public class RequestClass {
        Map<String, String> queryStringParameters;
        Map<String, String> headers;

        public Map<String, String> getHeaders() {
            return headers;
        }
        
        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public Map<String, String> getQueryStringParameters() {
            return queryStringParameters;
        }
    
        public void setQueryStringParameters(Map<String, String> queryStringParameters) {
            this.queryStringParameters = queryStringParameters;
        }

        public RequestClass(Map<String, String> queryStringParameters, Map<String, String> headers) {
            this.queryStringParameters = queryStringParameters;
            this.headers = headers;
        }

        public RequestClass() {
        }
    }