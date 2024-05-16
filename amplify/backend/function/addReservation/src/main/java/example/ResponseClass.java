package example;
        
     public class ResponseClass {
        boolean saveReservationToDynamoDB;

        public boolean getSaveResevation() {
            return this.saveReservationToDynamoDB;
        }

        public void setSaveResevation(boolean saveReservationToDynamoDB) {
            this.saveReservationToDynamoDB = saveReservationToDynamoDB;
        }

        public ResponseClass(boolean saveReservationToDynamoDB) {
            this.saveReservationToDynamoDB = saveReservationToDynamoDB;
        }
    }
