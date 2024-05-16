package example;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import org.springframework.beans.factory.annotation.Autowired;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import main.java.example.Reservations;
import main.java.example.ParkingSpot;
import example.RequestClass;
import example.ResponseClass;
import com.jayway.jsonpath.JsonPath;

public class LambdaRequestHandler implements RequestHandler<RequestClass, Map<String, Object>>{   
    private DynamoDBMapper mapper;

    public LambdaRequestHandler() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClient.builder().build();
        this.mapper = new DynamoDBMapper(dynamoDBClient);
    }

    public Map<String, Object> handleRequest(RequestClass request,Context context){
        String date = "0001-01-01";
        String userId = JsonPath.read(request.getBody(), "$.userId");
        String spotId = JsonPath.read(request.getBody(), "$.spotId");

        Map<String, Object> res = new HashMap<>();
       
        
        String body =  "{" +
                        "\"saved\":\"" + saveReservationToDynamoDB(spotId,userId,date) + "\"" +
                        "}";
        res.put("body", body);

        return res;
    } 

    public boolean saveReservationToDynamoDB(String spotId, String userId, String date) {
      
            Reservations reservations = new Reservations();
            ParkingSpot parkingSpot = getParkingSpot(spotId);
    
            reservations.setsId(spotId);
            reservations.setuId(userId);
            reservations.setDate(date);
            parkingSpot.setVariant("permanent");
            try {
                mapper.save(reservations);
                mapper.save(parkingSpot);
                return true;
            } catch (Exception e) {
                return false;
            }
    }

    public ParkingSpot getParkingSpot(String spotId) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition(
                "id",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(spotId))
        );

        PaginatedScanList<ParkingSpot> result = mapper.scan(ParkingSpot.class, scanExpression);
        return result.get(0);
    }
}
