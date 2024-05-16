

package example;

import java.util.Map;
        
     public class RequestClass {
        Map<String, String> queryStringParameters;

        public Map<String, String> getQueryStringParameters() {
            return queryStringParameters;
        }
    
        public void setQueryStringParameters(Map<String, String> queryStringParameters) {
            this.queryStringParameters = queryStringParameters;
        }

        public RequestClass(Map<String, String> queryStringParameters) {
            this.queryStringParameters = queryStringParameters;
           
        }

        public RequestClass() {
        }
    }