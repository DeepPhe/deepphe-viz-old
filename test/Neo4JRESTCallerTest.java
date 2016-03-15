import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import db.DatamodelUtility;
import db.Patient;

public class Neo4JRESTCallerTest {

	
	@Test
	public void testGetPatients() {
		String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
    	String username = "neo4j";
    	String password = "neo4jpass";
    
    	DatamodelUtility caller = new DatamodelUtility(SERVER_ROOT_URI, username, password);
    	
    	try {
			List<Patient> patients = caller.getPatients();
			assertTrue(patients.size()>0);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    	
	}
	
//	@Test
	public void fetchAllNodes() {
		String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
    	final String txUri = SERVER_ROOT_URI + "transaction/commit";
    	
    	ClientConfig config = new ClientConfig();
    	Client client = ClientBuilder.newClient(config);
	    HttpAuthenticationFeature authFeature =
	            HttpAuthenticationFeature.basic("neo4j", "neo4jpass");
	    client.register(authFeature);
 	    WebTarget target = client.target(txUri);
 	    
 	    String query = "MATCH (n) RETURN labels(n),n LIMIT 100";
 	    
    	String payload = "{\"statements\" : [ {\"statement\" : \"" +query + "\"} ]}";
    	
    	String jsonResultStr =
    			target.request(MediaType.APPLICATION_JSON_TYPE)
    			    .post(Entity.entity(payload,MediaType.APPLICATION_JSON_TYPE),
    			        String.class);
    	
    	System.out.println(jsonResultStr);
    	ObjectMapper mapper = new ObjectMapper();

    	 
         Map<String,Object> dataMap = new HashMap<String,Object>(); 
    	 try {
			dataMap = mapper.readValue(jsonResultStr, Map.class);
			List<Map<String, Object>> rows = (List<Map<String, Object>>) dataMap.get("results");
			dataMap = rows.get(0);
			rows = (List)dataMap.get("data");
			
//			for(Map<String, Object> row:rows){
//					List cols = (List) row.get("row");
//					if(cols.get(0).equals("Patient")){
//						
//					}
//					
//			}
			
			assertTrue(jsonResultStr!=null);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	

}
