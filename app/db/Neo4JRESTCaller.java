package db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.text.MessageFormat;

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
	
	
	
	public List<LinkedHashMap<String,Object>> getNodesWithLabelAndAttributes(String label, Map<String,String> attributes ) throws JsonParseException, JsonMappingException, IOException {
		String postURI = serverRootURI+ "transaction/commit";
		String query = makeCypherQuery(label,attributes);
		String jsonStr = makePOSTCall(postURI,query);
		return objectifyNodeJSON(jsonStr);
	}
	
	// this code is in dummy data as well.. might want to consolidate later
	private static String statementWrapper = "'{' \"statements\": [ {0} ] \" '}' ";
	private static String matchQuery = "'{' \"statement\": \"match (n:{0} {1}) return id(n)\" '}'";
	private static String createNodeAttributes = "  '{' {0} '}'";
	
	private String makeCypherQuery(String label,Map<String,String >attributes) { 
		String attString = getAttributeClauses(attributes);
		// now query is in the statementString variable.
		Object[] params = new Object[] { label,attString };
		String queries = MessageFormat.format(matchQuery,params);
		params = new Object[] {queries};
		String wrappedQueries = MessageFormat.format(statementWrapper, params);
		return wrappedQueries;
	}
	
	
	// turn each pair in map into name: \\\"value\\\", and separate by commas.
	private String getAttributeClauses(Map<String, String> atts) {
		ArrayList<String> pairs = new ArrayList<String>();
		MessageFormat form = new MessageFormat(createNodeAttributes);
		Set<String> keys = atts.keySet();
		for (String key : keys) {
			String val = atts.get(key);
			String attString = form.format(new Object[] { key, val });
			pairs.add(attString);
		}
		// put all in a list
		String attListString = String.join(",", pairs);
		form = new MessageFormat(createNodeAttributes);
		return form.format(new Object[] { attListString });
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
