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
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Comparator;
import javax.crypto.SecretKey;
import java.util.Base64;
import example.RequestClass;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;


public class LambdaRequestHandler implements RequestHandler<RequestClass, Map<String, Object>>{   

    public Map<String, Object> handleRequest(RequestClass request, Context context){
        Map<String, Object> res = new HashMap<>();
         CognitoIdentityProviderClient client = CognitoIdentityProviderClient.builder()
            .region(Region.EU_CENTRAL_1)
            .build();
        String body = "";
        String payload = "";
        ObjectMapper mapperStr = new ObjectMapper();
        String header = "";
        try{
        String jwtToken = JsonPath.read(request.getHeaders(), "$.Authorization");
        LambdaLogger logger = context.getLogger();
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

        body =  listAllUsers(client, "eu-central-1_12JGW4S8I").toString();
        res.put("body", body);
        client.close();
        return res;
    }

    public String listAllUsers(CognitoIdentityProviderClient cognitoClient, String userPoolId) {
        ArrayNode rootNode = JsonNodeFactory.instance.arrayNode();
        try {
            ListUsersRequest usersRequest = ListUsersRequest.builder()
                    .userPoolId(userPoolId)
                    .build();

            ListUsersResponse response = cognitoClient.listUsers(usersRequest);
            response.users().forEach(user -> {
                ObjectNode item = JsonNodeFactory.instance.objectNode();
                item.put("userId",  user.username());
                item.put("email", user.attributes().stream().filter(attribute -> attribute.name().equals("email")).findFirst().get().value());
                rootNode.add(item);
            });
            return rootNode.toString();

        } catch (CognitoIdentityProviderException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return rootNode.toString();
        }
    }
}