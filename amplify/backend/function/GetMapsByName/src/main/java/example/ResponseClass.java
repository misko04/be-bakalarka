

package example;


import com.fasterxml.jackson.databind.JsonNode;

        
     public class ResponseClass {
        String rootNode;

        public String getNode() {
            return this.rootNode;
        }

        public void setNode(String rootNode) {
            this.rootNode = rootNode;
        }

        public ResponseClass(String rootNode) {
            this.rootNode = rootNode;
        }
    }