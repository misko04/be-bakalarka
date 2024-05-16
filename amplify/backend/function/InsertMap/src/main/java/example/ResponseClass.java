

package example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
        
     public class ResponseClass {
        Boolean success;

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }   

        public ResponseClass(Boolean success) {
            this.success = success;
        }

    }