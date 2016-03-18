package db;

public class Observation extends Node {
	
	String[] bodySites;
	String name;
	String value;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value=value;
	}
	
	public String[] getBodySites() {
		return bodySites;
	}
	public void setBodySites(String[] bodySites) {
		this.bodySites = bodySites;
	}
}
