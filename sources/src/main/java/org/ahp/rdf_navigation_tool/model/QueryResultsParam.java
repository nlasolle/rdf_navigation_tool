package org.ahp.rdf_navigation_tool.model;

import java.util.ArrayList;
import java.util.List;

public class QueryResultsParam {
	private List<Integer> conditionsIds = new ArrayList<Integer>();
	private List<Integer> negativeConditionsIds = new ArrayList<Integer>();
	private String lang = "";
	
	/**
	 * @return the conditionsIds
	 */
	public List<Integer> getConditionsIds() {
		return conditionsIds;
	}
	/**
	 * @param conditionsIds the conditionsIds to set
	 */
	public void setConditionsIds(List<Integer> conditionsIds) {
		this.conditionsIds = conditionsIds;
	}
	
	/**
	 * @return the negativeConditionsIds
	 */
	public List<Integer> getNegativeConditionsIds() {
		return negativeConditionsIds;
	}
	/**
	 * @param negativeConditionsIds the negativeConditionsIds to set
	 */
	public void setNegativeConditionsIds(List<Integer> negativeConditionsIds) {
		this.negativeConditionsIds = negativeConditionsIds;
	}
	/**
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}
	/**
	 * @param lang the lang to set
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}
	
	
}
