

package example;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
        
     public class RequestClass {
        String body;
        
        public String getBody() {
            return body;
        }
        public void setBody(String body) {
            this.body = body;
        }
        public RequestClass(String body) {
            this.body = body;
        }

        public RequestClass() {
        }
    }