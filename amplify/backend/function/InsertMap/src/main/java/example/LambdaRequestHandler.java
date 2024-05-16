

package example;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import example.RequestClass;
import example.ResponseClass;
import java.time.LocalDate;
import main.java.example.Maps;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import main.java.example.OfficeEntry;
import main.java.example.ParkingSpot;
import main.java.example.Filler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.amazonaws.services.lambda.runtime.LambdaLogger; 
import java.util.Map;
import java.util.HashMap;
import com.jayway.jsonpath.JsonPath;


public class LambdaRequestHandler implements RequestHandler<RequestClass, Map<String, Object> >{   

    private DynamoDBMapper mapper;

    public LambdaRequestHandler() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClient.builder().build();
        this.mapper = new DynamoDBMapper(dynamoDBClient);
    }

    public Map<String, Object> handleRequest(RequestClass request,Context context){
        String map = request.getBody();
        LambdaLogger logger = context.getLogger();
        logger.log("String found: " + map);
        logger.log("String found: " + request);

        Map<String, Object> res = new HashMap<>();
        res.put("isBase64Encoded", false);
        res.put("statusCode", 200);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "*/*");
        res.put("headers", headers);
        res.put("multiValueHeaders", null);
        
        String body =  "{" +
        "\"saved\":\"" + insertMap(map) + "\"" +
        "}";
        res.put("body", body);

        return res;
    } 

    public Boolean insertMap(String map) {
        if(insertJsonToDynamoDB(map)){
            return true;
        }
        return false;
    }


    public boolean insertJsonToDynamoDB(String map) {
        ObjectMapper mapperStr = new ObjectMapper();
        try {
            JsonNode jsonFile = mapperStr.readTree(map);
        
        JsonNode name = jsonFile.get("name");
        JsonNode rows = jsonFile.get("rows");
        JsonNode cols = jsonFile.get("cols");
        insertMapsNames(name, rows, cols);
        JsonNode blocks = jsonFile.get("blocks");
        ArrayNode layout = (ArrayNode) jsonFile.get("layout");

        for (JsonNode entity : layout) {
            String id = entity.get("i").asText();
            JsonNode block = blocks.get(id);
            String type = block.get("type").asText();
            switch (type) {
                case "OfficeEntry":
                    OfficeEntry officeEntry = new OfficeEntry();
                    officeEntry.setId(id);
                    officeEntry.setRotation(block.get("rotation").asInt());
                    officeEntry.setDirectEntrance(block.get("directEntrance").asBoolean());
                    officeEntry.setStaircase(block.get("staircase").asBoolean());
                    officeEntry.setElevator(block.get("elevator").asBoolean());
                    officeEntry.setX(entity.get("x").asInt());
                    officeEntry.setY(entity.get("y").asInt());
                    officeEntry.setMaxW(entity.get("maxW").asInt());
                    officeEntry.setWidth(entity.get("w").asInt());
                    officeEntry.setHeight(entity.get("h").asInt());
                    officeEntry.setName(name.textValue());
                    mapper.save(officeEntry);
                    break;
                case "Filler":
                    Filler filler = new Filler();
                    filler.setRotation(block.get("rotation").asInt());
                    filler.setId(id);
                    filler.setWidth(entity.get("w").asInt());
                    filler.setHeight(entity.get("h").asInt());
                    filler.setX(entity.get("x").asInt());
                    filler.setY(entity.get("y").asInt());
                    filler.setMaxW(entity.get("maxW").asInt());
                    filler.setName(name.textValue());
                    mapper.save(filler);
                    break;
                
                case "ParkingSpot":
                    ParkingSpot parkingSpot = new ParkingSpot();
                    parkingSpot.setId(id);
                    parkingSpot.setNumber(block.get("name").asInt());
                    parkingSpot.setVariant(block.get("variant").asText());
                    parkingSpot.setReversed(block.get("reversed").asBoolean());
                    parkingSpot.setRotation(block.get("rotation").asInt());
                    parkingSpot.setX(entity.get("x").asInt());
                    parkingSpot.setY(entity.get("y").asInt());
                    parkingSpot.setWidth(entity.get("w").asInt());
                    parkingSpot.setHeight(entity.get("h").asInt());
                    parkingSpot.setMaxW(entity.get("maxW").asInt());
                    parkingSpot.setName(name.textValue());
                    mapper.save(parkingSpot);
                    break;
            
                default: System.out.println("Unknown type: " + type);
                        break;
            }
        }
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }  
    }
    public void insertMapsNames(JsonNode name, JsonNode rows, JsonNode cols) {
        Maps maps = new Maps();
        maps.setName(name.textValue());
        maps.setColumns(cols.asText());
        maps.setRows(rows.asText());
        mapper.save(maps);
    }
}