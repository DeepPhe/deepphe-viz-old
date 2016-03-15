package controllers;

import java.io.IOException;
import java.util.List;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;


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

import db.DataCreatorUtility;
import db.Patient;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.neo4jdemo;

public class DummyData extends Controller {

    	   
    String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
    String username = "neo4j";
    String password = "neo4jpass";

   
   public static String statementWrapper = "'{' \"statements\": [ {0} ] \" '}' ";

    public static String deleteString = "{ \"statement\" : \" match (n) optional match (n)-[r]-() delete n,r\" }";
		   
   
   
   public static String createNodeQuery = "'{' \"statement\": \"create (n:{0} {1}) return id(n)\" '}'";  // {0} is node type, {1} is the map of attributes
   public static String createAttribute   = "{0}: \\\"{1}\\\" ";
   public static String createNodeAttributes = "  '{' {0} '}'";
   
   public static String createRelationQuery = "'{' \"statement\": \"match (dx:{0}),(d:{1}) where dx.name=\\\"{2}\\\" and d.name=\\\"{3}\\\" create (d)-[:{4}]->(dx) return id(d);\"'}' ";
   
   
   
   public Result populate() {
	   
	   	try {
		    System.err.println("======= STARTING POPULATE=====");
	   		DataCreatorUtility caller = new DataCreatorUtility(SERVER_ROOT_URI, username, password);
	   		String p1 = createPatient("George");
	   		String p2 = createPatient("Harry");
	   		String p3 = createDocument("doc1","2015-12-15 09:00");
	   		String p4 = createHasSubjectQuery("Harry","doc1");
	   		String p5 = createDiagnosis("Malignant Breast Neoplasm","[left breast]","II");
	   		String p6 = createHasDiagnosisQuery("doc1","Malignant Breast Neoplasm");
	   		String[] allStatements = new String[]{p1,p2,p3,p4,p5,p6};
	   		String statementString = String.join(",",allStatements);
	   		// split with a comma
	   		Object[] params = new Object[]{statementString};
	   		String queries = MessageFormat.format(statementWrapper,params);
	   		String result=caller.executeQueries(queries);
			return ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			return ok(index.render(e.getMessage()));
		}
	    
    }

    public Result clear() {
	try {
	    DataCreatorUtility caller = new DataCreatorUtility(SERVER_ROOT_URI, username, password);

	    System.err.println("delete string is .."+deleteString);
	    Object[] params = new Object[]{deleteString};
	    String queries = MessageFormat.format(statementWrapper,params);
	    String result=caller.executeQueries(queries);
	    System.err.println("result..."+result);
	    return ok(result);
	} catch (Exception e) {
	    e.printStackTrace();
	    return ok(index.render(e.getMessage()));
	}
	
    }

   
   private String createPatient(String name) {
	   // put "name" and name into a map for attributes
	   HashMap<String,String> atts = new HashMap<String,String>();
	   atts.put("name",name);
	   return createNodeQuery("Patient",atts);
   }
   
   private String createNodeQuery(String type,Map<String,String> atts) {
	   //convert the attributes into a formatted query
	   String attributes = getAttributeClauses(atts);
	   // format the whole thing wih createNodeQuery
	   return formatQuery(createNodeQuery,new Object[]{type,attributes});
   }
   
   
   private String createDocument(String name,String date) {
	   HashMap<String,String> atts = new HashMap<String,String>();
	   atts.put("name",name);
	   atts.put("date",date);
	   return createNodeQuery("Document",atts);
   }
   
   private String createDiagnosis(String name,String sites,String stage) {
	   HashMap<String,String> atts = new HashMap<String,String>();
	   atts.put("name",name);
	   atts.put("bodySites",sites);
	   atts.put("Stage",stage);
	   return createNodeQuery("Diagnosis",atts);
   }
   
   private String createHasSubjectQuery(String p,String d) {
	   return formatQuery(createRelationQuery, new Object[]{"Patient","Document",p,d,"hasSubject"});
   }
  
   private String createHasDiagnosisQuery(String dx,String d) {
	   return formatQuery(createRelationQuery,new Object[]{"Diagnosis","Document",d,dx,"hasDiagnosis"});
   }
   
  // turn each pair in map into  name: \\\"value\\\", and separate by commas.
  private String getAttributeClauses(Map<String,String> atts) {
	  ArrayList<String> pairs  =new ArrayList<String>();
	  MessageFormat form = new MessageFormat(createAttribute);
	  Set<String> keys = atts.keySet();
	  for (String key: keys) {
		  String val = atts.get(key);
		  String attString  = form.format(new Object[]{key,val});
		  pairs.add(attString);
	  }	  
	  // put all in a list
	 String attListString =  String.join(",",pairs);
	 form = new MessageFormat(createNodeAttributes);
	 return form.format(new Object[]{attListString});
  }
  
  private String formatQuery(String template,Object[] params) {
	  	MessageFormat form = new MessageFormat(template);
	  	String q = form.format(params);
	   	System.err.println(q);
	   	return q;	  
  }
 

}
