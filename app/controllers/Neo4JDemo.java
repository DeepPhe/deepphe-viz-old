package controllers;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import db.DatamodelUtility;
import db.Patient;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.neo4jdemo;

public class Neo4JDemo extends Controller {

//    
//    public Result connect() {
////    	return ok(neo4jdemo.render(true));
//
//    	String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
//    	ClientConfig config = new ClientConfig();
//
//	    Client client = ClientBuilder.newClient(config);
//	    HttpAuthenticationFeature authFeature =
//	            HttpAuthenticationFeature.basic("neo4j", "neo4jpass");
//	         
//	    client.register(authFeature);
//	    
//	    WebTarget target = client.target(SERVER_ROOT_URI);
//	    
//	    Response response = target.
//	              request().
//	              accept(MediaType.APPLICATION_JSON_TYPE).
//	              get(Response.class);
//    	System.out.println(response);
//    	
//    	Boolean isOK = false;
//    	isOK = response.getStatus()==200;
//    	response.close();
//        return ok(neo4jdemo.render(isOK));
//        
//    }
	
	
   public Result demo() {
	   
	    String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
	   	String username = "neo4j";
	   	String password = "neo4jpass";
	   
	   	try {
			DatamodelUtility caller = new DatamodelUtility(SERVER_ROOT_URI, username, password);
			List<Patient> patients = caller.getPatients();
			System.out.println(patients.size());
			return ok(neo4jdemo.render(patients));
		} catch (Exception e) {
			e.printStackTrace();
			return ok(index.render(e.getMessage()));
		}
	    
    }
//    
//    public Result listAll() {
//    	
////    	return ok(neo4jdemo.render(true));
////    	
//    	String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
//    	final String txUri = SERVER_ROOT_URI + "transaction/commit";
//    	
//    	ClientConfig config = new ClientConfig();
//    	Client client = ClientBuilder.newClient(config);
//	    HttpAuthenticationFeature authFeature =
//	            HttpAuthenticationFeature.basic("neo4j", "neo4jpass");
//	    client.register(authFeature);
// 	    WebTarget target = client.target(txUri);
// 	    
// 	    String query = "MATCH (n) RETURN n,labels(n) LIMIT 100";
//    	String payload = "{\"statements\" : [ {\"statement\" : \"" +query + "\"} ]}";
//    	
//    	String bean =
//    			target.request(MediaType.APPLICATION_JSON_TYPE)
//    			    .post(Entity.entity(payload,MediaType.APPLICATION_JSON_TYPE),
//    			        String.class);
//
//    	JsValue json = Json.parse(bean);
//    	
//    	System.out.println(bean);
//    	return ok(neo4jdemo.render(true));
//    }
//    

}
