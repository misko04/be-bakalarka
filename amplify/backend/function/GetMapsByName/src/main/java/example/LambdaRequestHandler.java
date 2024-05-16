package example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;

import main.java.example.Maps;
import java.util.ArrayList;
import java.util.List;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.example.Reservations;
import java.util.Map;
import java.util.HashMap;
import com.jayway.jsonpath.JsonPath;


public class LambdaRequestHandler implements RequestHandler<RequestClass, Map<String, Object> >{   

    private DynamoDBMapper mapper;

    public LambdaRequestHandler() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClient.builder().build();
        this.mapper = new DynamoDBMapper(dynamoDBClient);
    }

    // public ResponseClass handleRequest(RequestClass request,Context context){
    //     String name = request.getName();
    //     LocalDate date = request.getDate();
    //     return new ResponseClass(getDynamoDBData(date,name));
    // }

    public Map<String, Object> handleRequest(RequestClass request, Context context){
        
        String date = JsonPath.read(request.getQueryStringParameters(), "$.date");
        String id = JsonPath.read(request.getQueryStringParameters(), "$.mapId");

        String name = getMapById(id).getName();

        Map<String, Object> res = new HashMap<>();
        
        String body =  getDynamoDBData(date,name);
        res.put("body", body);

        return res;
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

    public String getDynamoDBData(String date, String name) {
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        String updatedJsonStr = " ";
        List<Maps> maps = getMapByName(name);
        for (Maps map : maps) {
            rootNode.put("name", map.getName());
            rootNode.put("rows", map.getRows());
            rootNode.put("cols", map.getColumns());
        }
        try {
            List<OfficeEntry> officeEntries = getAllOfficeEntriesByPN(name);
            ArrayNode layoutArray = JsonNodeFactory.instance.arrayNode();
            ObjectNode blocksObject = JsonNodeFactory.instance.objectNode();

            for (OfficeEntry officeEntry : officeEntries) {
                ObjectNode layoutItem = JsonNodeFactory.instance.objectNode();
                layoutItem.put("i", officeEntry.getId());
                layoutItem.put("x", officeEntry.getX());
                layoutItem.put("y", officeEntry.getY());
                layoutItem.put("w", officeEntry.getWidth());
                layoutItem.put("h", officeEntry.getHeight());
                layoutItem.put("maxW", officeEntry.getMaxW());

                layoutArray.add(layoutItem);

                ObjectNode blockItem = JsonNodeFactory.instance.objectNode();
                blockItem.put("type", "OfficeEntry");
                blockItem.put("w", officeEntry.getWidth());
                blockItem.put("h", officeEntry.getHeight());
                blockItem.put("rotation", officeEntry.getRotation());
                blockItem.put("directEntrance", officeEntry.isDirectEntrance());
                blockItem.put("staircase", officeEntry.isStaircase());
                blockItem.put("elevator", officeEntry.isElevator());

                blocksObject.set(officeEntry.getId(), blockItem);

            }

            List<ParkingSpot> parkingSpots = getAllParkingSpotsByPN(name);
            List<Reservations> reservations = getAllReservations();

            for (ParkingSpot parkingSpot : parkingSpots) {
                ObjectNode layoutItem = JsonNodeFactory.instance.objectNode();
                layoutItem.put("i", parkingSpot.getId());
                layoutItem.put("x", parkingSpot.getX());
                layoutItem.put("y", parkingSpot.getY());
                layoutItem.put("w", parkingSpot.getWidth());
                layoutItem.put("h", parkingSpot.getHeight());
                layoutItem.put("maxW", parkingSpot.getMaxW());

                layoutArray.add(layoutItem);

                ObjectNode blockItem = JsonNodeFactory.instance.objectNode();
                blockItem.put("type", "ParkingSpot");
                blockItem.put("w", parkingSpot.getWidth());
                blockItem.put("h", parkingSpot.getHeight());
                blockItem.put("name", parkingSpot.getNumber());
                blockItem.put("reversed", parkingSpot.isReversed());
                blockItem.put("rotation", parkingSpot.getRotation());
                blockItem.put("variant", "empty");
                if (reservations != null) {
                    for (Reservations reservation : reservations) {
                        if (reservation.getsId().equals(parkingSpot.getId())) {
                            if (reservation.getDate().equals("0001-01-01")) {
                                blockItem.put("variant", "permanent");
                            } else if (reservation.getDate().equals(date)) {
                                blockItem.put("variant", "occupied");
                            }
                        }
                    }
                }
                blocksObject.set(parkingSpot.getId(), blockItem);
            }

            List<Filler> fillers = getAllFillersByPN(name);
            for (Filler filler : fillers) {
                ObjectNode layoutItem = JsonNodeFactory.instance.objectNode();
                layoutItem.put("i", filler.getId());
                layoutItem.put("x", filler.getX());
                layoutItem.put("y", filler.getY());
                layoutItem.put("w", filler.getWidth());
                layoutItem.put("h", filler.getHeight());
                layoutItem.put("maxW", filler.getMaxW());

                layoutArray.add(layoutItem);

                ObjectNode blockItem = JsonNodeFactory.instance.objectNode();
                blockItem.put("type", "Filler");
                blockItem.put("w", filler.getWidth());
                blockItem.put("h", filler.getHeight());
                blockItem.put("rotation", filler.getRotation());

                blocksObject.set(filler.getId(), blockItem);
            }
             
            rootNode.set("layout", layoutArray);
            rootNode.set("blocks", blocksObject);

            updatedJsonStr = rootNode.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        

        return rootNode.toString();
    }


    public ArrayList<Maps> getMapByName(String name) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition(
                "name",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(name))
        );

        PaginatedScanList<Maps> result = mapper.scan(Maps.class, scanExpression);
        return new ArrayList<>(result);
    }


    public ArrayList<OfficeEntry> getAllOfficeEntriesByPN(String name) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition(
                "name",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(name))
        );

        PaginatedScanList<OfficeEntry> result = mapper.scan(OfficeEntry.class, scanExpression);
        return new ArrayList<>(result);
    }


    public ArrayList<ParkingSpot> getAllParkingSpotsByPN(String name) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition(
                "name",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(name))
        );

        PaginatedScanList<ParkingSpot> result = mapper.scan(ParkingSpot.class, scanExpression);
        return new ArrayList<>(result);
    }

    public ArrayList<Reservations> getAllReservations() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        PaginatedScanList<Reservations> result = mapper.scan(Reservations.class, scanExpression);

        if (result.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(result);
    }


    public ArrayList<Filler> getAllFillersByPN(String name) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition(
                "name",
                new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(name))
        );

        PaginatedScanList<Filler> result = mapper.scan(Filler.class, scanExpression);
        return new ArrayList<>(result);
    }
    
}