

package example;
import java.util.Map;        
     public class ResponseClass {
        String resrvationNum;

        public String getReservation() {
            return this.resrvationNum;
        }

        public void setReservation(String resrvationNum) {
            this.resrvationNum = resrvationNum;
        }

        public ResponseClass(String resrvationNum) {
            this.resrvationNum = resrvationNum;
        }
    }