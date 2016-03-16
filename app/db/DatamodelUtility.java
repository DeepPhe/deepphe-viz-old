package db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
			Patient p = new Patient();
			patientList.add(p);
			int pid = (int) datamap.get("id");
			String pname = (String) datamap.get("name");
			p.setId(pid);
			p.setName(pname);
			p.setDocuments(new ArrayList<Document>());
			List<LinkedHashMap<String,Object>> docrows = getIncomingNodesWithRelationshipType(p.getId()+"", "hasSubject");
			
			for(LinkedHashMap<String,Object> docdatamap:docrows){
				Document document = new Document();
				document.setId((int) docdatamap.get("id"));
				document.setName((String) docdatamap.get("name"));
				document.setText((String)docdatamap.get("text"));
				
				document.setDiagnoses(new ArrayList<Diagnosis>());
				document.setProcedures(new ArrayList<Procedure>());
				document.setMedications(new ArrayList<Medication>());
				
				document.setSubject(p);
				p.getDocuments().add(document);
				
				List<LinkedHashMap<String,Object>> drows = getOutgoingNodesWithRelationshipType(document.getId()+"", "hasDiagnosis");
			
				for(LinkedHashMap<String,Object> dmap:drows){
					Diagnosis di = new Diagnosis();
					di.setId((int) dmap.get("id"));
					di.setName((String) dmap.get("name"));
					
					document.getDiagnoses().add(di);
				}
				
				drows = getOutgoingNodesWithRelationshipType(document.getId()+"", "hasProcedure");
				
				for(LinkedHashMap<String,Object> dmap:drows){
					Procedure di = new Procedure();
					di.setId((int) dmap.get("id"));
					di.setName((String) dmap.get("name"));
					
					document.getProcedures().add(di);
				}
				
				drows = getOutgoingNodesWithRelationshipType(document.getId()+"", "hasMedication");
				
				for(LinkedHashMap<String,Object> dmap:drows){
					Medication di = new Medication();
					di.setId((int) dmap.get("id"));
					di.setName((String) dmap.get("name"));
					
					document.getMedications().add(di);
				}
			}
		}
		
		return patientList;
	}
	
	public Patient getPatient(String name) {
		Patient p = null;
		return p;
	}
	
}
