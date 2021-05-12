package org.ahp.rdf_navigation_tool.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Class for SPARQL query string manipulation
 * @author Nicolas Lasolle
 *
 */
@Service(value = "QueryUtilsService")
public class QueryUtilsService implements InitializingBean{
	
	@Value("${custom.prefixes}")
	private String [] prefixes;
	
	private static String queryHeader = "";
	
	private static Map<String, String> prefixAssociations;
	
	/**
	 * Append prefixes to a SPARQL query string
	 * @param prefixes the list of prefixes (short + full name)
	 * @param query the current SPARQL query string
	 * @return the updated SPARQL query
	 */
	public static String addPrefixes(String query) {

		/*queryHeader += "PREFIX ahpo: <http://e-hp.ahp-numerique.fr/ahpo#>\n";
		queryHeader += "PREFIX dcterms: <http://purl.org/dc/terms/>\n";
		queryHeader += "PREFIX dbo: <http://dbpedia.org/ontology/>\n";
		queryHeader += "PREFIX dbp: <http://dbpedia.org/property/>\n";
		queryHeader += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
		queryHeader += "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n";
		queryHeader += "PREFIX rdf: <ttp://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";*/
		
		return queryHeader + query;
	}

	/**
	 * Add graph constraint for a SPARQL query string
	 * @param graphName the graph URI
	 * @param query the current SPARQL query
	 * @return the updated SPARQL query
	 */
	public static String addGraphConstraint(String graphURI, String query) {
		int startSearchIndex = query.indexOf("WHERE"); //Start search from the WHERE keyword 

		String queryBegin = query.substring(0, query.indexOf("{", startSearchIndex));
		String graphConstraint = "{\nGRAPH <" +graphURI + "> {\n";
		String queryEnd = query.substring(query.indexOf("{", startSearchIndex)+1);

		return queryBegin + graphConstraint + queryEnd + "\n}";
	}

	/**
	 * Add graph constraint for a SPARQL query string
	 * @param varName the variable name (ex : s, p, o, etc.)
	 * @param term the term (user input)
	 * @param query the current SPARQL query
	 * @return the updated SPARQL query
	 */
	public static String addContainsFiltering(String varName, String term, String query) {
		String queryUpdated = query.substring(0, query.lastIndexOf("}"));

		if(queryUpdated.trim().charAt(queryUpdated.trim().length()-1) == '.'){
			queryUpdated += " \n";
		} else {
			queryUpdated += " .\n";
		}


		queryUpdated += "FILTER(CONTAINS(STR(?" + varName + "), \""+ term + "\"))\n}";		
		return queryUpdated;
	}

	/**
	 * Add LIMIT constraint for a SPARQL query
	 * @param limit the limit to set
	 * @param query the current SPARQL query
	 * @return the updated SPARQL query
	 */
	public static String addLimit(int limit, String query) {

		return query + "\nLIMIT " + limit;
	}

	/**
	 * Add ORDER BY constraint for a SPARQL query
	 * @param varName the variable name (ex : s, p, o, etc.)
	 * @param query the current SPARQL query
	 * @return the updated SPARQL query
	 */
	public static String addOrdering(String varName, String query) {

		return query + "\nORDER BY " + "?" + varName;
	}
	
	/**
	 * Return the list of prefix and full namespaces
	 * @return a hash map containing the associations
	 */
	public static Map<String,String> getAssociations() {
		return prefixAssociations;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		prefixAssociations = new HashMap<>();
		
		for(String prefix : prefixes) {
			int i = prefix.indexOf(":");
			String shortName = prefix.substring(0, i);
			String namespace = prefix.substring(i+2, prefix.length()-1);
			prefixAssociations.put(shortName, namespace);
			
			queryHeader += "PREFIX " + shortName + ": <" + namespace + ">\n";
			
		}
	}

}
