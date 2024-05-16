package example;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger; 
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
import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.example.Reservations;
import main.java.example.Maps;
import main.java.example.ParkingSpot;
import example.RequestClass;
import example.ResponseClass;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Comparator;
import javax.crypto.SecretKey;
import java.util.Base64;
import example.RequestClass;
import example.ResponseClass;




public class LambdaRequestHandler implements RequestHandler<RequestClass, Map<String, Object> >{   
    private DynamoDBMapper mapper;
    
    public LambdaRequestHandler() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClient.builder().build();
        this.mapper = new DynamoDBMapper(dynamoDBClient);
    }

    public Map<String, Object> handleRequest(RequestClass request, Context context){
        String date = JsonPath.read(request.getQueryStringParameters(), "$.date");
         String jwtToken = JsonPath.read(request.getHeaders(), "$.Authorization");
         LambdaLogger logger = context.getLogger();
        
                String[] chunks = jwtToken.split("\\.");
                Base64.Decoder decoder = Base64.getUrlDecoder();
        
                String header = new String(decoder.decode(chunks[0]));
                String payload = new String(decoder.decode(chunks[1]));
                ObjectMapper mapperStr = new ObjectMapper();
                String userId = null;
                try{
                JsonNode payloadJson = mapperStr.readTree(payload);
                userId = payloadJson.get("id").asText();
                }catch(Exception e){
                    logger.log("Error: " + e);
                }
        
        List<Reservations> reservations = getReservations(userId,date);

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

    public PaginatedScanList<Reservations> getReservations(String userId, String date) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        Map<String, Condition> filterConditions = new HashMap<>();
    
        filterConditions.put(
                "uId",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(userId))
        );
    
        
        filterConditions.put(
                "date",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.GE)
                        .withAttributeValueList(new AttributeValue(date))
        );
    
        scanExpression.setScanFilter(filterConditions);
        PaginatedScanList<Reservations> result = mapper.scan(Reservations.class, scanExpression);
        return result.isEmpty() ? null : result;
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