

package example;

import main.java.example.Reservations;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
        
     public class ResponseClass {
        PaginatedScanList <Reservations> resrvations;

        public PaginatedScanList <Reservations> getReservations() {
            return this.resrvations;
        }

        public void setReservations(PaginatedScanList <Reservations> resrvations) {
            this.resrvations = resrvations;
        }

        public ResponseClass(PaginatedScanList <Reservations> resrvations) {
            this.resrvations = resrvations;
        }
    }