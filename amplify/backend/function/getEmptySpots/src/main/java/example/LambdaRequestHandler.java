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
import main.java.example.ParkingSpot;
import main.java.example.Reservations;
import main.java.example.Maps;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Comparator;
import javax.crypto.SecretKey;
import java.util.Base64;
import example.RequestClass;



public class LambdaRequestHandler implements RequestHandler<RequestClass, Map<String, Object> >{   
    private DynamoDBMapper mapper;
    
    public LambdaRequestHandler() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClient.builder().build();
        this.mapper = new DynamoDBMapper(dynamoDBClient);
    }

    public Map<String, Object> handleRequest(RequestClass request, Context context){
        Map<String, Object> res = new HashMap<>();
        String body = "";
        String payload = "";
        ObjectMapper mapperStr = new ObjectMapper();
        String header = "";
        String mapId = "";
        String date = "";
        List<ParkingSpot> parkingSpots  = new ArrayList<>();
        List<ParkingSpot> pss  = new ArrayList<>();
        LambdaLogger logger = context.getLogger();
        ParkingSpot ps = new ParkingSpot();
        try{
        String jwtToken = JsonPath.read(request.getHeaders(), "$.Authorization");
        mapId = JsonPath.read(request.getQueryStringParameters(), "$.mapId");
        date = JsonPath.read(request.getQueryStringParameters(), "$.date");
        String[] chunks = jwtToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        header = new String(decoder.decode(chunks[0]));
        payload = new String(decoder.decode(chunks[1]));
        }catch(Exception e){
            res.put("statusCode", 401);
            res.put("body", "");
            return res;
        }
        String userId = null;

        try{
            JsonNode payloadJson = mapperStr.readTree(payload);
            userId = payloadJson.get("id").asText();
        }catch(Exception e){
            res.put("statusCode", 401);
            res.put("body", "");
            return res;
        }
        String mapName = getMapById(mapId).getName();
        List<ParkingSpot> spots = getEmptySpots(mapName);
        List<Reservations> reservations = getReservations(date);
        try{
            for(Reservations r : reservations){
                for(ParkingSpot s : spots){
                    if(!(r.getsId().equals(s.getId()))){
                        if(!(parkingSpots.contains(s))){
                            parkingSpots.add(s);
                        }
                    }
                    else{
                        pss.add(s);
                        logger.log("s: " + s);
                    }
                }
            }
        }
        catch(Exception e){
            parkingSpots = spots;
        }
        if(!(pss.isEmpty())){
            for(ParkingSpot s : pss){
                if(parkingSpots.contains(s)){
                    parkingSpots.remove(s);
                }
            }
        }
        ArrayNode rootNode = JsonNodeFactory.instance.arrayNode();
        for (ParkingSpot spot : parkingSpots) {
            String spotId = spot.getId();
            int spotNumber = spot.getNumber();
            ObjectNode item = JsonNodeFactory.instance.objectNode();
            item.put("spotNumber",  spotNumber);
            item.put("spotId", spotId);
            rootNode.add(item);
        }
        body =  rootNode.toString();
        res.put("body", body);

        return res;
    }



    public PaginatedScanList<ParkingSpot> getEmptySpots(String mapName) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        Map<String, Condition> filterConditions = new HashMap<>();
    
        filterConditions.put(
                "variant",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue("empty"))
        );
    
        
        filterConditions.put(
                "name",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(mapName))
        );
    
        scanExpression.setScanFilter(filterConditions);
        PaginatedScanList<ParkingSpot> result = mapper.scan(ParkingSpot.class, scanExpression);
        return result.isEmpty() ? null : result;
    }

    public Maps getMapById(String id) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition(
                "id",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(id))
        );

        PaginatedScanList<Maps> result = mapper.scan(Maps.class, scanExpression);
        return result.get(0);
    }

    public PaginatedScanList<Reservations> getReservations(String date) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition(
                "date",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(date))
        );
    
        PaginatedScanList<Reservations> result = mapper.scan(Reservations.class, scanExpression);
        return result.isEmpty() ? null : result;
    }
}