package org.ahp.rdf_navigation_tool.service;

import java.io.File;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.ahp.rdf_navigation_tool.RDFConfiguration;
import org.ahp.rdf_navigation_tool.model.DateOptions;
import org.ahp.rdf_navigation_tool.model.QueryExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Class used to interact with the .yml configuration file
 * @author Nicolas Lasolle
 *
 */
@Service(value = "ConfigurationService")
public class ConfigurationService implements InitializingBean {

	private static final String CONFIGURATION_FILE = "./config/application.yml";

	@Autowired
	private ApplicationContext context;

	@Value("${custom.initialResource}")
	private String initialResource;
	
	@Value("${custom.labels.individual}")
	private String individualLabel;

	@Value("${custom.labels.property}")
	private String propertyLabel;

	@Value("${custom.labels.language}")
	private String lang;

	@Value("${custom.sparqlEndpoint}")
	private String endpoint;

	@Value("${custom.dateProperty}")
	private String dateProperty;

	@Value("${custom.dateOptions.min}")
	private int minDate;

	@Value("${custom.dateOptions.max}")
	private int maxDate;

	@Value("${custom.dateOptions.initialMin}")
	private int initialMinDate;

	@Value("${custom.dateOptions.initialMax}")
	private int initialMaxDate;
	
	@Value("${custom.dateOptions.step}")
	private int step;

	@Value("${custom.itemLink}")
	private String itemLink;
	
	@Value("${custom.displayedProperties}")
	private String [] displayedProperties;

	@Value("${custom.literalDisplayedProperties}")
	private String [] literalDisplayedProperties;

	QueryExecutor queryExecutor;


	public void afterPropertiesSet() throws Exception {
		RDFConfiguration config = context.getBean(RDFConfiguration.class);

		/* One instance of queryExecutor is instantiated as a Spring @Bean
		 * This instance is accessible through Spring Application Context 
		 * and is shared by the different controllers and services of the application
		 */
		queryExecutor = config.queryExecutor(); 

	}

	/**
	 * Get the SPARQL endpoint url
	 * @return the url of the SPARQL endpoint
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * Get the label used for RDF individuals
	 * @return the label used
	 */
	public String getIndividualLabel() {
		return individualLabel;
	}

	/**
	 * Get the label used for RDF property
	 * @return the label used
	 */
	public String getPropertyLabel() {
		return propertyLabel;
	}

	/**
	 * Get the language to be used with SPARQL queries
	 * @return the language tag value
	 */
	public String getLang() {
		return lang;
	}

	public void setEndpoint(String endpoint) {
		// Create an ObjectMapper mapper for YAML
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		// Parse the YAML file
		ObjectNode root = null;
		try {
			root = (ObjectNode) mapper.readTree(new File(CONFIGURATION_FILE));
			root.put("custom.endpoint", endpoint);
			mapper.writer().writeValue(new File(CONFIGURATION_FILE), root);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Update the value 
	 * @param lang
	 */
	public void setLang(String lang) {
		// Create an ObjectMapper mapper for YAML
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		// Parse the YAML file
		ObjectNode root = null;
		try {
			root = (ObjectNode) mapper.readTree(new File(CONFIGURATION_FILE));
			JsonNode node = root.get("custom.labels.language");
			root.replace(lang, node);
			//root.put("custom.labels.language", lang);
			mapper.writer().writeValue(new File(CONFIGURATION_FILE), root);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Return the date property label
	 */
	public String getDatePropertyLabel(String langParam) {

		String query = "SELECT (STR(?value) AS ?label)\n"
				+ "WHERE {\n"
				+ dateProperty + " " + propertyLabel + " ?value .\n"
				+ "FILTER(LANG(?value) = \"" + langParam + "\""
				+ "|| LANG(?value) = \"\")}";

		query = QueryUtilsService.addPrefixes(query);
		System.out.println("----- Date retrieval query-----\n" + query);
		QueryExecution qExec = queryExecutor.executeRemoteSelectQuery(query, endpoint);
		Iterator<?> results = qExec.execSelect();

		//Format results
		while(results.hasNext())
		{
			QuerySolution soln = (QuerySolution) results.next();

			RDFNode value = soln.get("?label");
			if(value != null) {
				String[] words =  value.toString().split("(?<!(^|[A-ZÀ-Ö]))(?=[A-ZÀ-Ö])|(?<!^)(?=[A-ZÀ-Ö][a-z]')");
				String label =  StringUtils.capitalize(words[0]);
				
				for(int i = 1; i < words.length; i++) {
					label+= " " + words[i].toLowerCase();
				}
				
				qExec.close();
				return label;
			}
		}
		
		qExec.close();
		return "Date";
	}

	/**
	 * Retrieve value for date filtering options
	 * @return an object to be passed to the REST controller
	 */
	public DateOptions getDateFilteringOptions() {
		DateOptions options = new DateOptions(minDate, maxDate, initialMinDate, initialMaxDate, step);
		return options;
	}

	/**
	 * Return the labels associated with displayed properties
	 */
	public Map<String, String> getDisplayedPropertiesLabel(String lang) {
		Map<String, String> labels = new HashMap<String, String>();

		String selectClause = "SELECT";
		String whereClause		= "\nWHERE {\n";
		String query;

		for(String displayedProperty : displayedProperties) {
			String propertyId = displayedProperty.substring(displayedProperty.indexOf(":") + 1);
			selectClause += " (STR(?"+ propertyId + "LabelValue) AS ?" + propertyId + "Label)";
			whereClause += displayedProperty + " " + propertyLabel + " ?"+ propertyId + "LabelValue .\n"
					+ "FILTER(LANG(?"+ propertyId + "LabelValue) = \"" + lang + "\")\n";		
		}


		for(String literalProperty : literalDisplayedProperties) {
			String propertyId = literalProperty.substring(literalProperty.indexOf(":") + 1);
			selectClause += " (STR(?"+ propertyId + "LabelValue) AS ?" + propertyId + "Label)";
			whereClause += literalProperty + " " + propertyLabel + " ?"+ propertyId + "LabelValue .\n"
					+ "FILTER(LANG(?"+ propertyId + "LabelValue) = \"" + lang + "\")\n";		
		}

		
		query = QueryUtilsService.addPrefixes("") + "\n" + selectClause + whereClause + "}";

		System.out.println("---- Properties label query ----\n" + query);

		QueryExecution qExec = queryExecutor.executeRemoteSelectQuery(query, endpoint);
		Iterator<?> results = qExec.execSelect();

		//Format results
		while(results.hasNext())
		{
			QuerySolution soln = (QuerySolution) results.next();

			for(String displayedProperty : displayedProperties) {
				String propertyId = displayedProperty.substring(displayedProperty.indexOf(":") + 1);
				
				RDFNode value = soln.get(propertyId + "Label");
				
				if(value != null) {
					String[] words =  value.toString().split("(?<!(^|[A-ZÀ-Ö]))(?=[A-ZÀ-Ö])|(?<!^)(?=[A-ZÀ-Ö][a-z])");
					String label =  StringUtils.capitalize(words[0]);
					
					for(int i = 1; i < words.length; i++) {
						label+= " " + words[i].toLowerCase();
					}
					//label =  label.substring(0, 1).toUpperCase() + label.substring(1);

					labels.put(propertyId, label);
				}
			}
			
			for(String literalProperty : literalDisplayedProperties) {
				String propertyId = literalProperty.substring(literalProperty.indexOf(":") + 1);
				
				RDFNode value = soln.get(propertyId + "Label");
				
				if(value != null) {
					String[] words =  value.toString().split("(?<!(^|[A-ZÀ-Ö]))(?=[A-ZÀ-Ö])|(?<!^)(?=[A-ZÀ-Ö][a-z])");
					String label =  StringUtils.capitalize(words[0]);
					
					for(int i = 1; i < words.length; i++) {
						label+= " " + words[i].toLowerCase();
					}
					//label =  label.substring(0, 1).toUpperCase() + label.substring(1);

					labels.put(propertyId, label);
				}
			}
		}
		
		qExec.close();
		return labels;
	}
	
	/**
	 * Return the properties and their labels
	 */
	public Map<String, String> getProperties(String lang) {
		
		Map<String, String> properties = new HashMap<String, String>();
		
		String query = "SELECT DISTINCT ?p (STR(?value) AS ?label)\n"
				+ "WHERE {\n"
				+ "?s ?p ?o .\n"
				+ "?p " + propertyLabel + " ?value .\n"
				+ "FILTER(LANG(?value) = \"" + lang + "\""
				+ "|| LANG(?value) = \"\")}";

		query = QueryUtilsService.addPrefixes(query);
		System.out.println("---- Properties retrieval query -----\n" + query);
		QueryExecution qExec = queryExecutor.executeRemoteSelectQuery(query, endpoint);
		Iterator<?> results = qExec.execSelect();
		
		//Format results
		while(results.hasNext())
		{
			QuerySolution soln = (QuerySolution) results.next();

			RDFNode value = soln.get("?label");
			if(value != null) {
				String[] words =  value.toString().split("(?<!(^|[A-ZÀ-Ö]))(?=[A-ZÀ-Ö])|(?<!^)(?=[A-ZÀ-Ö][a-z]')");
				String label =  StringUtils.capitalize(words[0]);
				
				for(int i = 1; i < words.length; i++) {
					label+= " " + words[i].toLowerCase();
				}
				
				//Reduce label if it is too long for the UI
				if(label.length()>30) {
					label = label.substring(0,29);
				}
				
				properties.put(soln.get("?p").toString(), label);
			}
		}
		
		qExec.close();
		
		return properties;
	}
	
	/**
	 * Return all the resources, with  their label, matching the given type
	 */
	public Map<String, String> getResources(String type, String lang) {
		
		Map<String, String> objects = new HashMap<String, String>();
		
		String query = "SELECT DISTINCT ?s (STR(?value) AS ?label)\n"
				+ "WHERE {\n"
				+ "?s ?p ?o .\n"
				+ "?s a <" + type + "> .\n"
				+ "?s " + individualLabel + " ?value .\n"
				+ "FILTER(LANG(?value) = \"" + lang + "\""
				+ "|| LANG(?value) = \"\")}";

		query = QueryUtilsService.addPrefixes(query);
		
		System.out.println("---- Object retrieval query ----\n" + query);
		QueryExecution qExec = queryExecutor.executeRemoteSelectQuery(query, endpoint);
		Iterator<?> results = qExec.execSelect();

		//Format results
		while(results.hasNext())
		{
			QuerySolution soln = (QuerySolution) results.next();

			RDFNode value = soln.get("?label");
			
			if(value != null) {
				objects.put(soln.get("?s").toString(), value.toString());
			}
		}
		
		qExec.close();
		
		return objects;
	}
	
	/**
	 * Return all rdfs:Class used in the RDF graph
	 */
	public Map<String, String> getTypes(String lang) {
		
		Map<String, String> types = new HashMap<String, String>();
		
		String query = "SELECT DISTINCT ?C (STR(?value) AS ?label)\n"
				+ "WHERE {\n"
				+ "?s a ?C .\n"
				+ "?C " + propertyLabel + " ?value .\n"
				+ "FILTER(LANG(?value) = \"" + lang + "\""
				+ "|| LANG(?value) = \"\")}";

		query = QueryUtilsService.addPrefixes(query);
		System.out.println("---- Class retrieval query ----" + query);
		QueryExecution qExec = queryExecutor.executeRemoteSelectQuery(query, endpoint);
		Iterator<?> results = qExec.execSelect();

		//Format results
		while(results.hasNext())
		{
			QuerySolution soln = (QuerySolution) results.next();

			RDFNode value = soln.get("?label");
			if(value != null) {
				String[] words =  value.toString().split("(?<!(^|[A-ZÀ-Ö]))(?=[A-ZÀ-Ö])|(?<!^)(?=[A-ZÀ-Ö][a-z]')");
				String label =  StringUtils.capitalize(words[0]);
				
				for(int i = 1; i < words.length; i++) {
					label+= " " + words[i].toLowerCase();
				}
				
				//Reduce label if it is too long for the UI
				if(label.length()>30) {
					label = label.substring(0,29);
				}
				
				types.put(soln.get("?C").toString(), label);
			}
		}
		
		qExec.close();
		
		types = types.entrySet().stream()
                .sorted(Entry.comparingByValue())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
		return types;
	}

	/**
	 * Return the link for iri dereferencing 
	 * @return
	 */
	public String getItemLink() {
		return itemLink;
	}

	/**
	 * Return the initial resource for process initialization
	 * @return
	 */
	public String getInitialResource() {
		return initialResource;
	}


}
