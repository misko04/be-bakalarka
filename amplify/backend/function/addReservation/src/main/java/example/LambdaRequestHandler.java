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
import example.RequestClass;
import example.ResponseClass;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import javax.swing.plaf.synth.Region;

import io.jsonwebtoken.security.Keys;
import java.util.Comparator;
import javax.crypto.SecretKey;
import java.util.Base64;
import main.java.example.Reservations;
import main.java.example.Reservations;
import example.RequestClass;
import example.ResponseClass;

public class LambdaRequestHandler implements RequestHandler<RequestClass, Map<String, Object> >{   
    private DynamoDBMapper mapper;

    public LambdaRequestHandler() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClient.builder().build();
        this.mapper = new DynamoDBMapper(dynamoDBClient);
    }

    public Map<String, Object> handleRequest(RequestClass request,Context context){
        LambdaLogger logger = context.getLogger();
        logger.log("String found: " + request.getBody());

        String date = JsonPath.read(request.getBody(), "$.date");
        String spotId = JsonPath.read(request.getBody(), "$.spotId");
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
        
        logger.log("String found: " + request.getBody());

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
            if(!seeSameDate(userId,date)&&!getSpotReservations(spotId,date)) {
                mapper.save(reservations);
                return true;
            }
            
            return false;
    }
    
    public boolean seeSameDate(String userId, String date) {
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
        if(result.isEmpty()){
            return false;
        }
        return true;
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
        if(result.isEmpty()){
            return false;
        }
        else{
            for(Reservations r : result){
                if(r.getDate().equals(date)){
                    return true;
                }
            }
            return false;
        }
    }

    
}
