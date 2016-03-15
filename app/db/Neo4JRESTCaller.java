package db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Neo4JRESTCaller {
	private static final String NODES_SUFFIX = "/nodes";
	private static final String INCOMING_RELATIONS_SUFFIX = "/relationships/in";
	private static final String OUTGOING_RELATIONS_SUFFIX = "/relationships/out";
	
	String serverRootURI = "http://localhost:7474/db/data/";

	String username = "neo4j";
	String password = "neo4j";
	
	
	public Neo4JRESTCaller(String serverRootURI, String username, String password) {
		super();
		this.serverRootURI = serverRootURI;
		this.username = username;
		this.password = password;
	}


	public List<LinkedHashMap<String, Object>>	getNodesWithLabel(String label) throws JsonParseException, JsonMappingException, IOException{
		String restURI = serverRootURI + "label/" + label + NODES_SUFFIX;
		
    	String jsonStr = makeRESTCall(restURI);
	   
        return objectifyNodeJSON(jsonStr);
        
	}
	
	
	
	public List<LinkedHashMap<String, Object>>	getIncomingNodesWithRelationshipType(String id, String relationType) throws JsonParseException, JsonMappingException, IOException{
		 List<LinkedHashMap<String, Object>> out = new ArrayList<LinkedHashMap<String, Object>>();
//		http://localhost:7474/db/data/node/10354/relationships/in
		String restURI = serverRootURI + "node/" + id + INCOMING_RELATIONS_SUFFIX;
		
    	String jsonStr = makeRESTCall(restURI);
	   
	    List<LinkedHashMap<String, Object>> rows = objectifyRelationshipJSON(jsonStr);
	    for(LinkedHashMap<String,Object> datamap:rows){
	    	restURI = (String) datamap.get("start");
	    	jsonStr = makeRESTCall(restURI);
	    	
	    	out.addAll(objectifyNodeJSON(jsonStr));
	    }
	    
	    return out;
	}

	public List<LinkedHashMap<String, Object>>	getOutgoingNodesWithRelationshipType(String id, String relationType) throws JsonParseException, JsonMappingException, IOException{
		 List<LinkedHashMap<String, Object>> out = new ArrayList<LinkedHashMap<String, Object>>();
//		http://localhost:7474/db/data/node/10354/relationships/out
		String restURI = serverRootURI + "node/" + id + OUTGOING_RELATIONS_SUFFIX + "/" + relationType;
		System.out.println(restURI);
		String jsonStr = makeRESTCall(restURI);
	   
	    List<LinkedHashMap<String, Object>> rows = objectifyRelationshipJSON(jsonStr);
	    for(LinkedHashMap<String,Object> datamap:rows){
	    	restURI = (String) datamap.get("end");
	    	jsonStr = makeRESTCall(restURI);
	    	
	    	out.addAll(objectifyNodeJSON(jsonStr));
	    }
	    
	    return out;
	}

	

	private String makeRESTCall(String restURI) {
		ClientConfig config = new ClientConfig();

	    Client client = ClientBuilder.newClient(config);
	    HttpAuthenticationFeature authFeature =
	            HttpAuthenticationFeature.basic(username, password);
	         
	    client.register(authFeature);
	    
	    WebTarget target = client.target(restURI);
	    
	    Response response = target.
	              request().
	              accept(MediaType.APPLICATION_JSON_TYPE).
	              get(Response.class);
    	
	    String jsonStr = response.readEntity(String.class);
		return jsonStr;
	}
	
	
	public String makePOSTCall(String restURI,String query) {
		ClientConfig config = new ClientConfig();

	    Client client = ClientBuilder.newClient(config);
	    HttpAuthenticationFeature authFeature =
	            HttpAuthenticationFeature.basic(username, password);
	         
	    client.register(authFeature);
	    
	    WebTarget target = client.target(restURI);
	    
	    Response response = target.
	              request(MediaType.APPLICATION_JSON_TYPE).
	               post(Entity.entity(query,MediaType.APPLICATION_JSON_TYPE));
    	
	    String jsonStr = response.readEntity(String.class);
		return jsonStr;
	}
	
	public List<LinkedHashMap<String, Object>> objectifyRelationshipJSON(String jsonStr) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> dataMap = new HashMap<String,Object>(); 
        Object[] rows = new Object[]{};
        rows = mapper.readValue(jsonStr, Object[].class);
        
        List<LinkedHashMap<String, Object>> out = new ArrayList<LinkedHashMap<String, Object>>();
        
        for(Object o:rows){
	    LinkedHashMap<String, Object> rowmap = (LinkedHashMap<String, Object>) o;
        	
        	
        	LinkedHashMap<String, Object> datamap = (LinkedHashMap<String, Object>) rowmap.get("data");
        	LinkedHashMap<String, Object> metadatamap = (LinkedHashMap<String, Object>) rowmap.get("metadata");
        	for(String key:metadatamap.keySet()){
        		datamap.put(key, metadatamap.get(key));
        	}
        	
        	datamap.put("start",rowmap.get("start"));
        	datamap.put("end",rowmap.get("end"));
        	
        	out.add(datamap);
        	
        }
        
        return out;
	}
	
	public List<LinkedHashMap<String, Object>> objectifyNodeJSON(String jsonStr) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> dataMap = new HashMap<String,Object>(); 
        Object[] rows = new Object[]{};
        if(jsonStr.trim().startsWith("{")){
        	LinkedHashMap<String, Object> rowmap = (LinkedHashMap<String, Object>) mapper.readValue(jsonStr, Map.class);
        	rows = new Object[]{rowmap};
        }
        else
        	rows = mapper.readValue(jsonStr, Object[].class);
        
        List<LinkedHashMap<String, Object>> out = new ArrayList<LinkedHashMap<String, Object>>();
        
        for(Object o:rows){
        	LinkedHashMap<String, Object> rowmap = (LinkedHashMap<String, Object>)o;
        	
        	LinkedHashMap<String, Object> datamap = (LinkedHashMap<String, Object>) rowmap.get("data");
        	LinkedHashMap<String, Object> metadatamap = (LinkedHashMap<String, Object>) rowmap.get("metadata");
        	
        	for(String key:metadatamap.keySet()){
        		datamap.put(key, metadatamap.get(key));
        	}
        	out.add(datamap);
        	
        }
        
        return out;
	}

}
