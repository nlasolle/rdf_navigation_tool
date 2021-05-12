package org.ahp.rdf_navigation_tool.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

/**
 * Application process state.
 * @author Nicolas Lasolle.
 *
 */
public class State {
	private List<Pair<Integer, String>> expressions = new ArrayList<Pair<Integer,String>>();
	
	/*List of natural expressions representing the expression with various language
	 A map key represents a language tag ("fr", "en", etc.) */
	
	private Map<String,List<String>> naturalExpressions = new HashMap<>();
	
	//Each SPARQL query is associated to one or several expressions.
	private List<QueryState> queries = new ArrayList<QueryState>(); 

	public QueryState findQueryState(List<Integer> conditionsIds, List<Integer> negativeConditionsIds){
		for(QueryState queryState:queries) {
			for(int i = 0; i< conditionsIds.size(); i++){
				if(queryState.getConditionsIds().contains(conditionsIds.get(i))) {
					if(i == conditionsIds.size() - 1) {
						if(negativeConditionsIds.isEmpty()) {
							return queryState;
						}
						
						for(int j = 0; j < negativeConditionsIds.size(); j++) {
							if(queryState.getNegativeConditions().contains(negativeConditionsIds.get(j))) {
								if(j == negativeConditionsIds.size() - 1) {
									return queryState;
								}
							} else {
								break;
							}
						}
					}
				} else {
					break;
				}
			}
			
			if(conditionsIds.isEmpty()) {
				for(int j = 0; j < negativeConditionsIds.size(); j++) {
					if(queryState.getNegativeConditions().contains(negativeConditionsIds.get(j))) {
						if(j == negativeConditionsIds.size() - 1) {
							return queryState;
						}
					} else {
						break;
					}
				}
			}
		}

		return null;
	}

	public List<QueryResults> findQueryResults(int id){
		for(QueryState query:queries) {

			if(query.getConditionsIds().size() == 1 && query.getConditionsIds().get(0) == id) {
				return query.getResults();
			} 
		}

		return null;
	}

	/**
	 * @return the expressions
	 */
	public List<Pair<Integer, String>> getExpressions() {
		return expressions;
	}

	/**
	 * @param expressions the expressions to set
	 */
	public void setExpressions(List<Pair<Integer, String>> expressions) {
		this.expressions = expressions;
	}

	public List<QueryState> getQueries() {
		return queries;
	}

	public void setQueriesState(List<QueryState> queries) {
		this.queries = queries;
	}

	public void addQueryState(QueryState queryState) {
		this.queries.add(queryState);
	}

	/**
	 * @return the naturalExpressions
	 */
	public Map<String, List<String>> getNaturalExpressions() {
		return naturalExpressions;
	}

	/**
	 * @param naturalExpressions the naturalExpressions to set
	 */
	public void setNaturalExpressions(Map<String, List<String>> naturalExpressions) {
		this.naturalExpressions = naturalExpressions;
	}
	
	/**
	 * We add a condition for a specific language
	 * @param lang
	 * @param expression
	 */
	public void addNaturalExpression(String lang, String expression) {
		//Tests if the language tag exists in the list
		if(naturalExpressions.containsKey(lang)) {
			//Check that we don't add the same expression twice
			if(!naturalExpressions.get(lang).contains(expression)) {
				naturalExpressions.get(lang).add(expression);
			}
		} else {
			List<String> expressions = new ArrayList<>();
			expressions.add(expression);
			naturalExpressions.put(lang, expressions);
		}
	}
	
	/**
	 * Save the list of conditions for the given language (replace it if it already exists)
	 * @param lang
	 * @param expression
	 */
	public void addNaturalExpressions(String lang, List<String> expressions) {
		naturalExpressions.put(lang, expressions);
	}

	/**
	 * Add a triple form expression to the list
	 * @param expression
	 */
	public void addExpression(String expression) {
		this.expressions.add(new Pair<Integer, String>(this.expressions.size(), expression));
		
	}

}
