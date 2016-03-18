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

	public static String createNodeQuery = "'{' \"statement\": \"create (n:{0} {1}) return id(n)\" '}'"; // {0}
																											// is
																											// node
																											// type,
																											// {1}
																											// is
																											// the
																											// map
																											// of
																											// attributes
	public static String createAttribute = "{0}: \\\"{1}\\\" ";
	public static String createNodeAttributes = "  '{' {0} '}'";

	public static String createRelationQuery = "'{' \"statement\": \"match (dx:{0}),(d:{1}) where dx.name=\\\"{2}\\\" and d.name=\\\"{3}\\\" create (d)-[:{4}]->(dx) return id(d);\"'}' ";

	public Result populate() {

		try {
			System.err.println("======= STARTING POPULATE=====");
			DataCreatorUtility caller = new DataCreatorUtility(SERVER_ROOT_URI, username, password);
			ArrayList<String> statements = new ArrayList<String>();
			statements.add(createPatient("George"));
			statements.add(createPatient("Harry"));
			statements.add(createDocument("doc1", "2015-12-15 09:00","document 1 text"));
			statements.add(createHasSubjectQuery("Harry", "doc1"));
			statements.add(createDiagnosis("Malignant Breast Neoplasm", "[left breast]", "II"));
			statements.add(createHasDiagnosisQuery("doc1", "Malignant Breast Neoplasm"));
			statements.add(createDocument("doc2", "2015-12-20 09:00","document 2 text"));
			statements.add(createHasSubjectQuery("Harry", "doc2"));
			statements.add(createObservation("PR-neg", "negative", "left breast"));
			statements.add(createHasObservation("doc2", "PR-neg"));
			statements.add(createObservation("Her2", "positive", ""));
			statements.add(createHasObservation("doc2", "Her2"));
			statements.add(createObservation("ER", "negative", ""));
			statements.add(createHasObservation("doc2", "ER"));
			statements.add(createDocument("doc3", "2015-12-16 12:00","document 3 text"));
			statements.add(createHasSubjectQuery("Harry", "doc3"));
			statements.add(createMedication("taxotere"));
			statements.add(createMedication("carboplatin"));
			statements.add(createMedication("pertuzumab"));
			statements.add(createHasMedication("doc3", "taxotere"));
			statements.add(createHasMedication("doc3", "carboplatin"));
			statements.add(createHasMedication("doc3", "pertuzumab"));

			String[] allStatements = new String[statements.size()];
			allStatements = statements.toArray(allStatements);
			String statementString = String.join(",", allStatements);
			// split with a comma
			Object[] params = new Object[] { statementString };
			String queries = MessageFormat.format(statementWrapper, params);
			String result = caller.executeQueries(queries);
			return ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			return ok(index.render(e.getMessage()));
		}

	}

	public Result clear() {
		try {
			DataCreatorUtility caller = new DataCreatorUtility(SERVER_ROOT_URI, username, password);

			System.err.println("delete string is .." + deleteString);
			Object[] params = new Object[] { deleteString };
			String queries = MessageFormat.format(statementWrapper, params);
			String result = caller.executeQueries(queries);
			System.err.println("result..." + result);
			return ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			return ok(index.render(e.getMessage()));
		}

	}

	private String createPatient(String name) {
		// put "name" and name into a map for attributes
		HashMap<String, String> atts = new HashMap<String, String>();
		atts.put("name", name);
		String res = createNodeQuery("Patient", atts);
		return res;
	}

	private String createNodeQuery(String type, Map<String, String> atts) {
		// convert the attributes into a formatted query
		String attributes = getAttributeClauses(atts);
		// format the whole thing wih createNodeQuery
		String res = formatQuery(createNodeQuery, new Object[] { type, attributes });
		return res;
	}

	private String createDocument(String name, String date,String text) {
		HashMap<String, String> atts = new HashMap<String, String>();
		atts.put("name", name);
		atts.put("date", date);
		atts.put("text", text);
		return createNodeQuery("Document", atts);
	}

	private String createDiagnosis(String name, String sites, String stage) {
		HashMap<String, String> atts = new HashMap<String, String>();
		atts.put("name", name);
		atts.put("bodySites", sites);
		atts.put("Stage", stage);
		return createNodeQuery("Diagnosis", atts);
	}

	private String createObservation(String name, String obs, String sites) {
		HashMap<String, String> atts = new HashMap<String, String>();
		atts.put("name", name);
		atts.put("value", obs);
		if (sites.length() > 0) {
			atts.put("bodySites", sites);
		}
		return createNodeQuery("Observation", atts);
	}

	private String createMedication(String name) {
		HashMap<String, String> atts = new HashMap<String, String>();
		atts.put("name", name);
		return createNodeQuery("Medication", atts);
	}

	private String createHasSubjectQuery(String p, String d) {
		return formatQuery(createRelationQuery, new Object[] { "Patient", "Document", p, d, "hasSubject" });
	}

	private String createHasDiagnosisQuery(String dx, String d) {
		return formatQuery(createRelationQuery, new Object[] { "Diagnosis", "Document", d, dx, "hasDiagnosis" });
	}

	private String createHasObservation(String d, String obs) {
		return formatQuery(createRelationQuery, new Object[] { "Observation", "Document", obs, d, "hasObservation" });
	}

	private String createHasMedication(String d, String med) {
		return formatQuery(createRelationQuery, new Object[] { "Medication", "Document", med, d, "hasMedication" });
	}

	// turn each pair in map into name: \\\"value\\\", and separate by commas.
	private String getAttributeClauses(Map<String, String> atts) {
		ArrayList<String> pairs = new ArrayList<String>();
		MessageFormat form = new MessageFormat(createAttribute);
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

	private String formatQuery(String template, Object[] params) {
		MessageFormat form = new MessageFormat(template);
		String q = form.format(params);
		System.err.println(q);
		return q;
	}

}
