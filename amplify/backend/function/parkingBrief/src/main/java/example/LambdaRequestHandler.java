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
import com.jayway.jsonpath.JsonPath;
import main.java.example.Maps;


public class LambdaRequestHandler implements RequestHandler<RequestClass, Map<String, Object>>{   

    private DynamoDBMapper mapper;
    
    public LambdaRequestHandler() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClient.builder().build();
        this.mapper = new DynamoDBMapper(dynamoDBClient);
        
    }

    public ArrayList<Maps> getAllMaps() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        PaginatedScanList<Maps> result = mapper.scan(Maps.class, scanExpression);

        return new ArrayList<>(result);
    }
    
    public Map<String, Object> handleRequest(RequestClass request,Context context){
        
        Map<String, Object> res = new HashMap<>();

        ArrayNode rootNode = JsonNodeFactory.instance.arrayNode();
        List<Maps> maps = getAllMaps();
        for (Maps map : maps) {
            ObjectNode item = JsonNodeFactory.instance.objectNode();
            item.put("id",  map.getId());
            item.put("name", map.getName());
            item.put("rows", map.getRows());
            item.put("cols", map.getColumns());
            rootNode.add(item);
        }
        String body =  rootNode.toString();
        res.put("body", body);

        return res;
    }
}