package controllers;

import java.util.List;

import db.DatamodelUtility;
import db.Patient;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.neo4jdemo;
import views.html.patientview;

public class PatientView extends Controller {

   public Result show(String patient) {
	   
	    String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
	   	String username = "neo4j";
	   	String password = "neo4jpass";
	   
	   	try {
			DatamodelUtility caller = new DatamodelUtility(SERVER_ROOT_URI, username, password);
			Patient p = caller.getPatient(patient);
			return ok(patientview.render(p));
		} catch (Exception e) {
			e.printStackTrace();
			return ok(index.render(e.getMessage()));
		}
	    
    }
}
