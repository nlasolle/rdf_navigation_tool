package org.ahp.rdf_navigation_tool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store SPARQL query execution results.
 * @author Nicolas Lasolle
 *
 */
public class QueryResults {
	
	//URI of the RDF resource
	private String uri;
	
	//The date property
	private String date;
	
	//Number of properties to be displayed is unknown and can be parameterized by users.
	private Map<String, String> properties = new HashMap<String, String>();
	
	private List<Integer> conditions = new ArrayList<Integer>();
	
	public QueryResults(String uri){
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	/**
	 * Add a property value. 
	 * @param property
	 * @param value
	 * @return true if the operation was successful.
	 */
	public boolean addPropertyValue(String property, String value) {
		if(properties.containsKey(property)) {
			return false;
		}
		
		properties.put(property, value);
		
		return true;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return the conditions
	 */
	public List<Integer> getConditions() {
		return conditions;
	}

	/**
	 * @param conditions the conditions to set
	 */
	public void setConditions(List<Integer> conditions) {
		this.conditions = conditions;
	}
	
	/**
	 * Add an id to the list of matching conditions for this result
	 * @param id the condition id
	 */
	public void addConditions(int id) {
		this.conditions.add(id);
	}
	
}
