package org.ahp.rdf_navigation_tool.model;

import java.util.ArrayList;
import java.util.List;

public class QueryState {
	private String query;
	private List<Integer> conditions = new ArrayList<Integer>();
	private List<Integer> negativeConditions = new ArrayList<Integer>();
	private List<QueryResults> results = new ArrayList<QueryResults>();
	
	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}
	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}
	
	/**
	 * @return the results
	 */
	public List<QueryResults> getResults() {
		return results;
	}
	/**
	 * @param results the results to set
	 */
	public void setResults(List<QueryResults> results) {
		this.results = results;
	}
	
	/**
	 * @return the conditions ids related to this query for the current application state
	 */
	public List<Integer> getConditionsIds() {
		return conditions;
	}
	/**
	 * @param conditions the expressions to set
	 */
	public void setConditions(List<Integer> conditions) {
		this.conditions = conditions;
	}
	
	/**
	 * Add a condition
	 * @param id
	 * @return true if the operation was successful
	 */
	public boolean addConditionId(int id) {
		if(conditions.contains(id) ||  negativeConditions.contains(id)) {
			return false;
		}
		
		conditions.add(id);
		return true;
	}
	/**
	 * @return the negativeConditions
	 */
	public List<Integer> getNegativeConditions() {
		return negativeConditions;
	}
	/**
	 * @param negativeConditions the negativeConditions to set
	 */
	public void setNegativeConditions(List<Integer> negativeConditions) {
		this.negativeConditions = negativeConditions;
	}
	
	/**
	 * Add a negative condition
	 * @param id
	 * @return true if the operation was successful
	 */
	public boolean addNegativeConditionId(int id) {
		if(conditions.contains(id) || negativeConditions.contains(id)) {
			return false;
		}
		
		negativeConditions.add(id);
		return true;
	}
}
