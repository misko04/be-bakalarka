package example;

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
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import example.RequestClass;
import example.ResponseClass;
import main.java.example.Reservations;
import com.jayway.jsonpath.JsonPath;
import main.java.example.ParkingSpot;
import main.java.example.Maps;

public class LambdaRequestHandler implements RequestHandler<RequestClass, Map<String, Object>>{   
    private DynamoDBMapper mapper;
    
    public LambdaRequestHandler() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClient.builder().build();
        this.mapper = new DynamoDBMapper(dynamoDBClient);
    }

    public Map<String, Object> handleRequest(RequestClass request, Context context){
        
        List<Reservations> reservations = getAllReservations();

        Map<String, Object> res = new HashMap<>();

        ArrayNode rootNode = JsonNodeFactory.instance.arrayNode();
        for (Reservations reservation : reservations) {
            String spotId = reservation.getsId();
            String mapName = getSpot(spotId).getName();
            ObjectNode item = JsonNodeFactory.instance.objectNode();
            item.put("spotNumber",  getSpot(spotId).getNumber());
            item.put("spotId", spotId);
            item.put("date", reservation.getDate());
            item.put("mapName", mapName);
            item.put("mapId", getMap(mapName).getId());
            rootNode.add(item);
        }
        String body =  rootNode.toString();
        res.put("body", body);

        return res;
    }


     public List<Reservations> getAllReservations() {
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            PaginatedScanList<Reservations> result = mapper.scan(Reservations.class, scanExpression);
    
            return new ArrayList<>(result);
        }

        public ParkingSpot getSpot(String spotId) {
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
        public Maps getMap(String name) {
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            scanExpression.addFilterCondition(
                    "name",
                    new Condition()
                            .withComparisonOperator(ComparisonOperator.EQ)
                            .withAttributeValueList(new AttributeValue(name))
            );
    
            PaginatedScanList<Maps> result = mapper.scan(Maps.class, scanExpression);
            return result.get(0);
        }
}