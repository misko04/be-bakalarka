

package example;

import main.java.example.Reservations;
import java.util.List;
import java.util.ArrayList;
        
     public class ResponseClass {
        List<Reservations> reservations;

        public List<Reservations> getReservations() {
            return reservations;
        }

        public void setReservations(List<Reservations> reservations) {
            this.reservations = reservations;
        }

        public ResponseClass(List<Reservations> reservations) {
            this.reservations = reservations;
        }
    }