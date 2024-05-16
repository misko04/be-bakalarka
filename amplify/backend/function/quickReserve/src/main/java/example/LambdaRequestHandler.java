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
import main.java.example.Reservations;
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


public class LambdaRequestHandler implements RequestHandler<RequestClass, Map<String, Object> >{   
    private DynamoDBMapper mapper;
    private static final String secret = "8ea51b9fe482f246dea82b2635025a5c7ad4d8819ea42f30bf44b29663b9b810";

    public LambdaRequestHandler() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClient.builder().build();
        this.mapper = new DynamoDBMapper(dynamoDBClient);
    }

    public Map<String, Object> handleRequest(RequestClass request,Context context){
        LambdaLogger logger = context.getLogger();

        logger.log("String found: " + request.getBody());
        logger.log("header:" + request.getHeaders());

        String date = JsonPath.read(request.getBody(), "$.date");

        String jwtToken = JsonPath.read(request.getHeaders(), "$.Authorization");

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


        String spotId = getFreeSpot("empty",date);

        logger.log("String found: " + request.getBody());
        logger.log("header:" + request.getHeaders());

        Map<String, Object> res = new HashMap<>();
        
        String body =  "{" +
                        "\"saved\":\"" + saveReservationToDynamoDB(spotId,userId,date) + "\"" +
                        "}";
        res.put("body", body);

        return res;
    } 

    public boolean saveReservationToDynamoDB(String spotId, String userId, String date) {
      
            Reservations reservations = new Reservations();
    
            reservations.setsId(spotId);
            reservations.setuId(userId);
            reservations.setDate(date);
            if(!seeSameDate(userId,date)){
                mapper.save(reservations);
                return true;
            }
        return false;
    }

    public String getFreeSpot(String variant, String date) {
        List<ParkingSpot> spots = getParkingSpotsByVariant(variant);

        for (ParkingSpot spot : spots) {     
                if (!getSpotReservations(spot.getId(),date)) {
                    return spot.getId();
            }
        }

        return null;
    }

    // private List<ParkingSpot> getParkingSpotsByVariant(String variant) {
    //     DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
    //     scanExpression.addFilterCondition(
    //         "variant",
    //             new Condition().withComparisonOperator(ComparisonOperator.EQ)
    //                     .withAttributeValueList(new AttributeValue(variant)));


    //     PaginatedScanList<ParkingSpot> spots = mapper.scan(ParkingSpot.class, scanExpression);

    //     return spots;
    // }

    private List<ParkingSpot> getParkingSpotsByVariant(String variant) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        scanExpression.addFilterCondition(
            "variant",
            new Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue(variant))
        );
    
        PaginatedScanList<ParkingSpot> spots = mapper.scan(ParkingSpot.class, scanExpression);

        List<ParkingSpot> spotList = new ArrayList<>(spots);

        spotList.sort(Comparator.comparingInt(ParkingSpot::getNumber).reversed());
    
        return spotList;
    }


    public boolean seeSameDate(String userId,String date) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition(
                "uId",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(userId))
        );
    
        PaginatedScanList<Reservations> result = mapper.scan(Reservations.class, scanExpression);
        if(result.isEmpty()){
            return false;
        }
        else{
            for(Reservations r : result){
                if(r.getDate().toString().equals(date.toString())){
                    return true;
                }
            }
            return false;
        
        }
    }

    public boolean getSpotReservations(String spotId,String date) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition(
                "sId",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(spotId))
        );
    
        PaginatedScanList<Reservations> result = mapper.scan(Reservations.class, scanExpression);
        if(result.isEmpty()||spotId.equals(null)){
            return false;
        }
        else{
            for(Reservations r : result){
                if(r.getDate().toString().equals(date.toString())){
                    return true;
                }
            }
            return false;
        }
    }

    // private static Claims decodeJWT(String jwtToken) {
        
    //     SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

    //     Jws<Claims> jws = Jwts.parserBuilder()
    //             .setSigningKey(key)
    //             .build()
    //             .parseClaimsJws(jwtToken);

    //     return jws.getBody();
    // }
}