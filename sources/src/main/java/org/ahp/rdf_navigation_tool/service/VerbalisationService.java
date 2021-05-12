package org.ahp.rdf_navigation_tool.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ahp.rdf_navigation_tool.RDFConfiguration;
import org.ahp.rdf_navigation_tool.model.QueryExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Manage the transformation from triple path to natural language expressions
 * @author Nicolas Lasolle
 *
 */
@Service(value = "VerbalisationService")
public class VerbalisationService implements InitializingBean {

	@Autowired
	private ApplicationContext context;
	
	@Value("${custom.labels.individual}")
	private String individualLabel;

	@Value("${custom.labels.property}")
	private String propertyLabel;

	@Value("${custom.sparqlEndpoint}")
	private String endpoint;
	
	QueryExecutor queryExecutor;


	public void afterPropertiesSet() throws Exception {
		RDFConfiguration config = context.getBean(RDFConfiguration.class);

		/* One instance of queryExecutor is instantiated as a Spring @Bean
		 * This instance is accessible through Spring Application Context 
		 * and is shared by the different controllers and services of the application
		 */
		queryExecutor = config.queryExecutor(); 
		//transformationManager = config.sparqlManager();

	}
	
	/**
	 * Return the list of natural language expressions matching the given 
	 * @param triples
	 * @return
	 */
	public List<String> getNaturalLanguageExpressions(List<Triple> triples, String lang) {
		List<String> results = new ArrayList<String>();

		for(Triple triple : triples) {
			results.add(getVerbalisation(triple, lang));
		}

		return results;
	}

	/**
	 * Construct the natural language expression by using label defined in the RDF database
	 * @param condition expression of the form "?s ?p ?o". 
	 * @return the natural language expression
	 */
	public String getVerbalisation(Triple condition, String lang) {
		String predicateLabel = "", objectLabel = "";


		if(condition.getPredicate().isURI()) {
			predicateLabel = getPropertyLabel("<" + condition.getPredicate().toString() + ">", lang);

			/* If not label has been found associated with the given property, we try to extract the property name */
			if(predicateLabel.isEmpty()) {
				String predicate = condition.getPredicate().toString();
				if(predicate.contains("#")) {
					predicateLabel = predicate.substring(predicate.lastIndexOf("#") + 1, predicate.length());
				} else {
					predicateLabel = predicate.substring(predicate.lastIndexOf("/") + 1, predicate.length());
				}

			}

		} else if(condition.getPredicate().isLiteral() || condition.getPredicate().isVariable()) {
			predicateLabel = condition.getPredicate().toString();
		} else {
			predicateLabel = getPropertyLabel(condition.getPredicate().toString(), lang);
		}


		if(condition.getObject().isURI()) {
			objectLabel = getIndividualLabel("<" + condition.getObject().toString() + ">", lang);
		} else if(condition.getObject().isLiteral() || condition.getObject().isVariable()) {
			objectLabel = condition.getObject().toString().replaceAll("@en", "");
		} else {
			objectLabel = getIndividualLabel(condition.getObject().toString(), lang);
		}


		return predicateLabel + " " + objectLabel;

	}

	public String getIndividualLabel(String individual, String lang) {
		String query = 
				"SELECT (STR(?value) as ?label) WHERE {\n"
						+ individual + " " + individualLabel + " ?value .\n" 
						+ "FILTER(LANG(?value) = \"" + lang + "\""
						+ " || LANG(?value) = \"\")}";
		
		query = QueryUtilsService.addPrefixes(query);
		System.out.println("---- Individual label query -----\n" + query);

		//Execute the query
		QueryExecution qExec = queryExecutor.executeRemoteSelectQuery(query, endpoint);
		Iterator<?> results = qExec.execSelect();

		//Format results
		while(results.hasNext())
		{
			QuerySolution soln = (QuerySolution) results.next();

			if(!soln.get("?label").toString().isEmpty()) {
				qExec.close();
				return soln.get("?label").toString();
			}
		}

		qExec.close();
		return "";

	}

	/**
	 * Get the verbalisation for the given language
	 * @param expression
	 * @return
	 */
	public String getVerbalisation(String expression, String lang) {
		String naturalExpression = "";
		int i1 = expression.indexOf(" ");
		int i2 = expression.indexOf(" ", i1 + 1);
		
				
		String property = expression.substring(i1, i2);
		String individual = expression .substring(i2 + 1);
		
		String propertyLabel = getPropertyLabel(property, lang);
		
		if(propertyLabel.equals("")) {
			return null;
		}
		
		String individualLabel = 
				individual.startsWith("\"") ? individual : getIndividualLabel(individual, lang);
				
		if(individualLabel.equals("")) {
			return null;
		}
		
		naturalExpression += propertyLabel + " " + individualLabel;

		return naturalExpression;
	}
	
	public String getPropertyLabel(String property, String lang) {
		String query =  "SELECT (STR(?value) as ?label) WHERE {\n"
						+ property + " " + propertyLabel + " ?value .\n" 
						+ "FILTER(LANG(?value) = \"" + lang + "\""
						+ " || LANG(?value) = \"\")}";
		
		System.out.println("---- Property label query -----\n" + query);
		query = QueryUtilsService.addPrefixes(query);

		QueryExecution qExec = queryExecutor.executeRemoteSelectQuery(query, endpoint);
		Iterator<?> results = qExec.execSelect();

		
		//Format results
		while(results.hasNext())
		{
			QuerySolution soln = (QuerySolution) results.next();

			if(!soln.get("?label").toString().isEmpty()) {
				String[] words =  soln.get("?label").toString().split("(?<!(^|[A-ZÀ-Ö]))(?=[A-ZÀ-Ö])|(?<!^)(?=[A-ZÀ-Ö][a-z])");
				String label =  StringUtils.capitalize(words[0]);
				
				for(int i = 1; i < words.length; i++) {
					label+= " " + words[i].toLowerCase();
				}
				
				qExec.close();
				return label;
			}
		}

		qExec.close();
		return "";
	}
}
