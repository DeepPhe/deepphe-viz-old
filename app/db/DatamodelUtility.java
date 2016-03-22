package db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class DatamodelUtility extends Neo4JRESTCaller{

	public DatamodelUtility(String serverRootURI, String username, String password) {
		super(serverRootURI, username, password);
	}
	
	public List<Patient> getPatients() throws JsonParseException, JsonMappingException, IOException{
		List<LinkedHashMap<String,Object>> rows = getNodesWithLabel("Patient");
		
		List<Patient> patientList = new ArrayList<Patient>();
		
		for(LinkedHashMap<String,Object> datamap:rows){
			int pid = (int) datamap.get("id");
			String pname = (String) datamap.get("name");
			Patient p = getPatient(pname,pid);
			
			patientList.add(p);
		}
		return patientList;
	}

	
	
	private Patient getPatient(String pname,int pid) throws JsonParseException, JsonMappingException, IOException {
		
			Patient p = new Patient();
			
			p.setName(pname);
			p.setId(pid);
			p.setDocuments(new ArrayList<Document>());
			List<LinkedHashMap<String,Object>> docrows = getIncomingNodesWithRelationshipType(p.getId()+"", "hasSubject");
			
			for(LinkedHashMap<String,Object> docdatamap:docrows){
				int id = (int) docdatamap.get("id");
				String name = (String) docdatamap.get("name");
				String text = (String) docdatamap.get("text");
				String date = (String) docdatamap.get("date");
				Document document = getDocument(name,id,text,date);
				document.setSubject(p);
				p.getDocuments().add(document);
			}
			return p;
	}
	
	public Patient getPatient(String name) throws JsonParseException, JsonMappingException, IOException {
		HashMap<String,String> atts = new HashMap<String,String>();
		atts.put("name",name);
		List<LinkedHashMap<String,Object>> docrows = getNodesWithLabelAndAttributes("Patient",atts);
		// should only be one thing in the results
		LinkedHashMap<String,Object> datamap = docrows.get(0);
		name = (String) datamap.get("name");
		int id = (int) datamap.get("id");
		
		return getPatient(name,id);
	}
	
	private Document getDocument(String dname,int id,String text,String date) throws JsonParseException, JsonMappingException, IOException {
				
		Document document = new Document();
		document.setId(id);
		document.setName(dname);
		document.setText(text);
		document.setDate(date);
		
		document.setDiagnoses(new ArrayList<Diagnosis>());
		document.setProcedures(new ArrayList<Procedure>());
		document.setMedications(new ArrayList<Medication>());
		document.setObservations(new ArrayList<Observation>());
		
		getDocumentDiagnoses(document);
		getDocumentProcedures(document);
		getDocumentMedications(document);
		getDocumentObservations(document);
	
		return document;
	}	
	
	private void getDocumentDiagnoses(Document document) throws IOException {
		List<LinkedHashMap<String,Object>> drows = getOutgoingNodesWithRelationshipType(document.getId()+"", "hasDiagnosis");
		
		for(LinkedHashMap<String,Object> dmap:drows){
			Diagnosis di = new Diagnosis();
			di.setId((int) dmap.get("id"));
			di.setName((String) dmap.get("name"));
			
			document.getDiagnoses().add(di);
		}
	}
	
	private void getDocumentProcedures(Document document) throws IOException {
		List<LinkedHashMap<String,Object>> drows = getOutgoingNodesWithRelationshipType(document.getId()+"", "hasProcedure");
		
		for(LinkedHashMap<String,Object> dmap:drows){
			Procedure di = new Procedure();
			di.setId((int) dmap.get("id"));
			di.setName((String) dmap.get("name"));
			
			document.getProcedures().add(di);
		}
	}
	
	private void getDocumentMedications(Document document) throws IOException {
		List<LinkedHashMap<String,Object>> drows = getOutgoingNodesWithRelationshipType(document.getId()+"", "hasMedication");
	
		for(LinkedHashMap<String,Object> dmap:drows){
			Medication di = new Medication();
			di.setId((int) dmap.get("id"));
			di.setName((String) dmap.get("name"));
			
			document.getMedications().add(di);
		}
	}
	
	private void getDocumentObservations(Document document) throws IOException {
		List<LinkedHashMap<String,Object>> drows = getOutgoingNodesWithRelationshipType(document.getId()+"", "hasObservation");
	
		for(LinkedHashMap<String,Object> dmap:drows){
			Observation di = new Observation();
			di.setId((int) dmap.get("id"));
			di.setName((String) dmap.get("name"));
			di.setValue((String) dmap.get("value"));
			
			document.getObservations().add(di);
		}
	}
}
