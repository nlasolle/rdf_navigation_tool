package org.ahp.rdf_navigation_tool.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ahp.rdf_navigation_tool.RDFConfiguration;
import org.ahp.rdf_navigation_tool.model.QueryExecutor;
import org.ahp.rdf_navigation_tool.model.QueryResults;
import org.ahp.rdf_navigation_tool.model.QueryState;
import org.ahp.rdf_navigation_tool.model.State;
import org.ahp.sqtruleapplication.exceptions.MoreRequestException;
import org.ahp.sqtruleapplication.exceptions.RuleException;
import org.ahp.sqtruleapplication.exceptions.SPARQLException;
import org.ahp.sqtruleapplication.exceptions.XMLFileException;
import org.ahp.sqtruleapplication.treatment.QueriesManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;

import org.apache.jena.rdf.model.RDFNode;
import org.javatuples.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Main service managing the application process. 
 * It deals with the states and SQTRL tool
 * @author Nicolas Lasolle
 *
 */
@Service(value = "StateService")
public class StateService implements InitializingBean{

	private static final double DEFAULT_MAX_COST = 15;

	@Autowired 
	private VerbalisationService verbalisationService;

	@Autowired 
	private ExecutionService executionService;

	@Autowired
	private ApplicationContext context;

	private QueryExecutor queryExecutor;

	private State state;

	@Value("${custom.pickedProperties}")
	private String [] pickedProperties;

	@Value("${custom.displayedProperties}")
	private String [] displayedProperties;

	@Value("${custom.literalDisplayedProperties}")
	private String [] literalDisplayedProperties;

	@Value("${custom.dateProperty}")
	private String dateProperty;

	@Value("${custom.labels.property}")
	private String propertyLabel;

	@Value("${custom.labels.individual}")
	private String individualLabel;

	@Value("${custom.sparqlEndpoint}")
	private String endpoint;

	@Value("${custom.transformationRulesFile}")
	private String transformationFile;

	@Value("${custom.schemaFile}")
	private String schemaFile;

	@Value("${custom.labels.language}")
	private String lang;

	@Value("${custom.prefixes}")
	private String [] prefixes;
	
	private QueriesManager sqtrlManager;
	private String initialQuery = "";

	public void afterPropertiesSet() throws Exception {
		RDFConfiguration config = context.getBean(RDFConfiguration.class);

		/* One instance of queryExecutor is instantiated as a Spring @Bean
		 * This instance is accessible through Spring Application Context 
		 * and is shared by the different controllers and services of the application
		 */
		queryExecutor = config.queryExecutor(); 
		sqtrlManager = new QueriesManager(endpoint);
		//transformationManager = config.sparqlManager();

	}

	/**
	 * Find the related individual within the RDF base and generates a process from it.
	 * Steps : 
	 * (1) Find the individual with the given iri.
	 * (2) Extract the relevant attributes (based on the list defined in the configuration file).
	 * (3) Send back the verbalized conditions.
	 * (4) Generates all first level states for these conditions.
	 * @param iri
	 */
	public List<String> initApplicationProcess(String iri, String lang) {
		List<Pair<Integer,String>> expressions = new ArrayList<Pair<Integer, String>>();
		List<String> conditions = getConditions(iri);


		state = new State();
		String query = "SELECT ?l WHERE {";

		int i = 0;

		for(String condition:conditions) {
			expressions.add(new Pair<Integer, String>(i, condition));
			query += condition + ".\n";
			i++;
		}
		query += "}";

		query = QueryUtilsService.addPrefixes(query);

		initialQuery = query;

		//Add this point, the initial query will just retrieve the uri associated with label.
		QueryParser parser = new QueryParser(query);

		List<Triple> triples = parser.extractTriplesPath();

		List<String> naturalExpressions = verbalisationService.getNaturalLanguageExpressions(triples, lang);

		state.addNaturalExpressions(lang, naturalExpressions);
		state.setExpressions(expressions);

		List<QueryState> initialStates = new ArrayList<QueryState>();

		for(Pair<Integer, String> expression : expressions) {
			QueryState queryState = new QueryState();
			List<Integer> conditionsIds = new ArrayList<>(1);

			conditionsIds.add(expression.getValue0());
			queryState.setQuery(generateQuery(expressions, conditionsIds, null, lang));
			queryState.setConditions(conditionsIds);
			initialStates.add(queryState);
		}

		state.setQueriesState(initialStates);

		return naturalExpressions;
	}

	/**
	 * Initializes the SPARQL query transformation process (in order to generate new conditions)
	 * @param query
	 * @throws SPARQLException
	 * @throws XMLFileException
	 * @throws RuleException
	 */
	public void initSQTRLProcess() throws SPARQLException, XMLFileException, RuleException {

		List<String> files = new ArrayList<>();
		files.add("resources/ttl/items.ttl");
		files.add("resources/ttl/ontology.ttl");

		sqtrlManager.parseRules(transformationFile, schemaFile);
		System.out.println("--------- Initial query for more process ------");
		System.out.println(initialQuery);

		//sqtrlManager.initMoreProcess(initialQuery, DEFAULT_MAX_COST);
	}

	/**
	 * Get conditions by finding values for the given resource for a set of properties.
	 * @param iri
	 * @return the conditions to be inserted into a SPARQL triple pattern in the where clause
	 */
	public List<String> getConditions(String iri) {
		List<String> conditions =  new ArrayList<String>();

		String selectClause = "SELECT ",
				whereClause = "WHERE {\n",
				query;

		int i = 0;
		for(String property : pickedProperties) {
			selectClause += "?o" + i + " ";
			whereClause += "OPTIONAL{<" + iri + "> " + property + " ?o" + i + "}.\n";
			i++;
		}

		query = selectClause + whereClause + "}";
		query = QueryUtilsService.addPrefixes(query);
		
		//Execute the query
		QueryExecution qExec = queryExecutor.executeRemoteSelectQuery(query, endpoint);
		Iterator<?> results = qExec.execSelect();

		//Format results
		while(results != null && results.hasNext())
		{
			QuerySolution soln = (QuerySolution) results.next();

			i = 0;
			for(String property:pickedProperties) {
				String condition = "";
				RDFNode value = soln.get("?o" + i);
				if(value != null) {
					condition = value.isURIResource() ?
							"?l " + property + " <" + value + ">" :
								"?l " + property + " \"" + value + "\"";

					if(!conditions.contains(condition)) {
						conditions.add(condition);
					}
				}
				i++;
			}

		}

		qExec.close();
		return conditions;

	}
	
	/**
	 * Retrieve the list of conditions expression using the given language
	 * @param lang a SPARQL language tag ("en", "fr", etc.)
	 * @return
	 */
	public List<String> getNaturalExpressions(String lang){
		List<String> translatedExpressions;
		
		//First, we check if the conditions have been saved for the given language
		translatedExpressions = state.getNaturalExpressions().get(lang);
		if(translatedExpressions != null) {
			//If a manual condition has been added
			if(translatedExpressions.size() != state.getExpressions().size()) {
				List<Pair<Integer, String>> expressions = state.getExpressions();
				
				//Add missing expressions
				for(int i = translatedExpressions.size(); i < state.getExpressions().size(); i++) {
					translatedExpressions.add(verbalisationService.getVerbalisation(expressions.get(i).getValue1(), lang));
					
				}
			
			}
			return translatedExpressions;
		}
		
		translatedExpressions = new ArrayList<String>();
		int i=0;
		
		//Otherwise, it is required to call the verbalisation service and save the condition translation for this language
		for(Pair<Integer, String> expression : state.getExpressions()) {
			String naturalExpression = verbalisationService.getVerbalisation(expression.getValue1(), lang);
			
			if(naturalExpression == null){
				//In this situation, no labels has been retrieved for the given language tag, we use the english version if it exists 
				if(state.getNaturalExpressions().containsKey("en")) {
					naturalExpression = state.getNaturalExpressions().get("en").get(i);
				}	
			} 
			
			translatedExpressions.add(naturalExpression);
			i++;
		}
		
		//Save the expressions for future uses
		state.addNaturalExpressions(lang, translatedExpressions);
		
		//verbalisationService.g
		return translatedExpressions;
	}
	
	/**
	 * Generate state queries by finding all possible graph patterns using the state conditions
	 * @param state should contains a list of expressions otherwise the returned list will be empty.
	 * @return the list of SPARQL queries
	 */
	public List<QueryState> constructAllQueries(State state){
		List<QueryState> queryStates = new ArrayList<QueryState>();
		QueryState queryState;

		ArrayList<ArrayList<Integer>> combinations = generateCombinations(state.getExpressions().size());
		String selectClause = "SELECT DISTINCT ?l ?date ",
				whereClause = "WHERE {\n",
				propertiesPattern = "",
				query;

		int i = 0;

		//When retrieving a resource, we look for its label
		for(String property : displayedProperties) {
			String propertyVarName = property.substring(property.indexOf(":") + 1);
			selectClause += "?"  + propertyVarName + " ";

			propertiesPattern += "OPTIONAL {?l " + property + " ?O" + i + ".\n "
					+ "?O" + i + " " + individualLabel + " ?" + propertyVarName + "}.\n";
			i++;
		}

		for(String property : literalDisplayedProperties) {
			String propertyVarName = property.substring(property.indexOf(":") + 1);
			selectClause += "?"  + propertyVarName + " ";

			propertiesPattern += "OPTIONAL{?l " + property + " ?" + propertyVarName + "}.\n ";
			i++;
		}

		propertiesPattern+= "?l " + dateProperty + " ?date.\n";

		for(ArrayList<Integer> combination : combinations) {
			queryState = new QueryState();

			query = selectClause + whereClause;
			query = QueryUtilsService.addPrefixes(query);

			for(Integer id : combination) {
				query += state.getExpressions().get(id).getValue1() + ".\n";
				queryState.addConditionId(id);
			}

			query += propertiesPattern + "}";
			query+= "ORDER BY ?date ?l";
			queryState.setQuery(query);
			queryStates.add(queryState);
		}

		return queryStates;

	}

	/**
	 * Generate all initial queries (queries with only 1 condition, if 6 conditions are extracted from the document, 6 queries will be generated)
	 * @param expressions the list of triple path conditions with the associated id
	 * @param conditions the list of conditions to check
	 * @param negativeConditions the list of conditions to check the not exists pattern
	 * @return the SPARQL query
	 */
	public String generateQuery(List<Pair<Integer, String>> expressions, List<Integer> conditions, List<Integer> negativeConditions, String lang){

		String selectClause = "SELECT DISTINCT ?l (STR(?dateValue) AS ?date) ",
				whereClause = "WHERE {\n",
				propertiesPattern = "",
				query = "";

		int i = 0;

		query = QueryUtilsService.addPrefixes(query);

		//When retrieving a resource, we look for its label
		for(String property : displayedProperties) {
			String propertyVarName = property.substring(property.indexOf(":") + 1);
			selectClause += "?"  + propertyVarName + " ";

			propertiesPattern += "OPTIONAL{?l " + property + " ?O" + i + ".\n "
					+ "?O" + i + " " + individualLabel + " ?" + propertyVarName + ".\n"
					+ "FILTER(LANG(?" + propertyVarName + ") = \"" + lang + "\""
					+ " || LANG(?" + propertyVarName + ") = \"\")}.\n" ;

			i++;
		}

		for(String property : literalDisplayedProperties) {
			String propertyVarName = property.substring(property.indexOf(":") + 1);
			selectClause += "?"  + propertyVarName + " ";

			propertiesPattern += "OPTIONAL{?l " + property + " ?" + propertyVarName + ".\n "
					+ "FILTER(LANG(?" + propertyVarName + ") = \"" + lang + "\""
					+ " || LANG(?" + propertyVarName + ") = \"\")}.\n" ;
			i++;
		}

		propertiesPattern+= "?l " + dateProperty + " ?dateValue.\n";


		query += selectClause + whereClause;

		for(Integer id : conditions) {
			query += expressions.get(id).getValue1() + ".\n";
		}

		if(negativeConditions!= null && !negativeConditions.isEmpty()) {
			query += "FILTER NOT EXISTS {\n";

			for(Integer id : negativeConditions) {
				query += expressions.get(id).getValue1() + ".\n";
			}

			query += "}";
		}

		query += propertiesPattern + "}";
		query += "ORDER BY ?date ?l";

		return query;

	}

	private ArrayList<ArrayList<Integer>> generateCombinations(int n) {
		ArrayList<ArrayList<Integer>> combinations = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> values = new ArrayList<Integer>();
		for(int i = 1; i <= n; i++) {


			Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(n, i);
			while (iterator.hasNext()) {
				final int[] combination = iterator.next();
				values = new ArrayList<Integer>();

				for(int j = 0; j < combination.length; j++) {
					values.add(combination[j]);		
				}

				combinations.add(values);
			}
		}

		return combinations;
	}

	/**
	 * Execute all SPARQL queries for the current state
	 */
	public void executeCurrentStateQueries() {
		List<QueryState> queryStates = state.getQueries();

		for(QueryState queryState : queryStates) {
			if(queryState.getConditionsIds().size() == 1) {
				List<QueryResults> results = executionService.executeQuery(queryState.getQuery());
				queryState.setResults(results);
			}
		}

	}

	/**
	 * Find the appropriate query in the state and send back associated results (already calculated)
	 * @param conditionsIds ids associated with the conditions expressions
	 * @param negativeConditionsIds ids associated with the negative conditions expressions
	 * @return the SPARQL query results 
	 */
	public List<QueryResults> getAndModeQueryResults(List<Integer> conditionsIds, List<Integer> negativeConditionsIds, String lang) {

		//Only one query result to retrieve in this situation
		//List<QueryResults> results = state.findQueryResults(expressionsIds);
		QueryState queryState = state.findQueryState(conditionsIds, negativeConditionsIds);

		if(queryState != null) {
			return queryState.getResults();
		} 
		queryState = new QueryState();
		queryState.setConditions(conditionsIds);
		queryState.setNegativeConditions(negativeConditionsIds);

		String query = generateQuery(state.getExpressions(),
				conditionsIds, negativeConditionsIds, lang);
		queryState.setQuery(query);

		List<QueryResults> results = executionService.executeQuery(query);
		queryState.setResults(results);
		return results;
	}

	/**
	 * Find the appropriate query in the state and send back associated results (already calculated)
	 * @param expressionsIds ids associated with the conditions expressions
	 * @return the SPARQL query results 
	 */
	public List<QueryResults> getOrModeQueryResults(List<Integer> expressionsIds) {

		//Need to aggregate the results
		List<QueryResults> finalResults = new ArrayList<>();
		List<QueryResults> queryResults; 

		for(Integer id: expressionsIds) {
			queryResults = state.findQueryResults(id);

			for(QueryResults queryResult: queryResults) {
				queryResult.setConditions(new ArrayList<Integer>());

				boolean found = false;
				for(QueryResults finalResult:finalResults) {

					if(finalResult.getUri().equals(queryResult.getUri())) {
						finalResult.addConditions(id);
						found = true;
						break;
					}
				}

				if(!found) {
					queryResult.addConditions(id);
					finalResults.add(queryResult);
				}

			}
		}

		return finalResults;
	}

	/**
	 * Call the SQTRL engine to generate a new list of filtering conditions
	 * @return
	 * @throws MoreRequestException 
	 * @throws XMLFileException 
	 * @throws SPARQLException 
	 */
	public List<String> getMore(String lang) throws SPARQLException, XMLFileException, MoreRequestException {
		List<String> newExpressions = new ArrayList<String>();

		if(sqtrlManager == null || !sqtrlManager.hasNextQuery()) {
			return newExpressions;
		}

		String transformedQuery = sqtrlManager.getNextQuery(DEFAULT_MAX_COST);

		transformedQuery = QueryUtilsService.addPrefixes(transformedQuery);

		QueryParser parser = new QueryParser(transformedQuery);
		List<Triple> triples = parser.extractTriplesPath();

		for(Triple triple : triples) {
			String expression = verbalisationService.getVerbalisation(triple, lang);
			if(!state.getNaturalExpressions().get(lang).contains(expression)) {
				String subject = triple.getSubject().toString();
				String predicate = triple.getPredicate().toString();
				String object = triple.getObject().toString();

				if(triple.getPredicate().isURI()) {
					predicate = "<" + triple.getPredicate() + ">";
				}

				if(triple.getObject().isURI()) {
					object = "<" + triple.getObject() + ">";
				}

				state.addExpression(subject + " " + predicate + " " + object);

				List<Pair<Integer, String>> expressions = state.getExpressions();
				//We generate the query of the
				QueryState queryState = new QueryState();
				queryState.addConditionId(expressions.size()-1);
				//executionService.executeQuery(queryState.getQuery());

				List<Integer> conditionsIds = new ArrayList<>(1);
				conditionsIds.add(expressions.size()-1);

				String query = generateQuery(expressions, conditionsIds, null, lang);
				queryState.setQuery(query);
	
				state.addQueryState(queryState);

				newExpressions.add(expression);
			}
		}

		return newExpressions;

	}

	/**
	 * Return the type label, the comment and the date of the given resource
	 * @param uri
	 * @return
	 */
	public Map<String, String> getResourceDetails(String uri, String lang) {
		Map<String, String> details = new HashMap<>();

		String query = "SELECT (STR(?value) as ?label) (STR(?description) as ?comment) ?date ?type (STR(?typeValue) as ?typeLabel) \n"
				+ "WHERE {\n"
				+ "OPTIONAL {<" + uri + "> " + individualLabel + " ?value\n"
				+ "FILTER(LANG(?value) = \"" + lang + "\""
				+ " || LANG(?value) = \"\")}\n" 
				+ "OPTIONAL {<" + uri + "> a ?type .\n"
				+ "?type " + propertyLabel + " ?typeValue\n"
				+ "FILTER(?typeValue != \"Thing\")\n"
				+ "FILTER(LANG(?typeValue) = \"" + lang + "\""
				+ " || LANG(?typeValue) = \"\")}\n" 
				+ "OPTIONAL {<" + uri + "> " + dateProperty + " ?date}\n"
				+ "OPTIONAL {<" + uri + "> rdfs:comment ?description\n"
				+ "FILTER(LANG(?description) = \"" + lang + "\""
				+ " || LANG(?description) = \"\")}\n"  
				+ "}";

		query = QueryUtilsService.addPrefixes(query);
		
		//Execute the query
		QueryExecution qExec = queryExecutor.executeRemoteSelectQuery(query, endpoint);
		Iterator<?> results = qExec.execSelect();

		//Format results
		while(results.hasNext())
		{
			QuerySolution soln = (QuerySolution) results.next();

			RDFNode value = soln.get("?label");
			if(value != null) {
				details.put("label", value.toString());
			}

			value = soln.get("?comment");
			
			if(value != null) {
				details.put("comment", value.toString());
			}
			
			value = soln.get("?date");
			if(value != null) {
				details.put("date", value.toString());
			}
			
			value = soln.get("?type");
			if(value != null) {
				details.put("type", value.toString());
			}
			
			value = soln.get("?typeLabel");
			if(value != null) {
				String[] words =  value.toString().split("(?<!(^|[A-ZÀ-Ö]))(?=[A-ZÀ-Ö])|(?<!^)(?=[A-ZÀ-Ö][a-z]')");
				String label =  StringUtils.capitalize(words[0]);
				details.put("typeLabel", label);
			}
		}

		qExec.close();
		return details;
	}

	/**
	 * Add a manual condition
	 * @param predicate property iri
	 * @param predicateLabel property label
	 * @param resource resource iri
	 * @param resourceLabel resource label
	 */
	public boolean addCondition(String predicate, String predicateLabel, String resource, String resourceLabel, String lang) {
		String naturalExpression = predicateLabel + " "+ resourceLabel;

		//iri should be encapsulated in <>
		if(resourceLabel != null) {
			resource = "<" + resource + ">";
		} else {
			resourceLabel = resource;
		}
		
		if(predicate.startsWith("<") || predicate.startsWith("http")) {

			for(Entry<String, String> entry : QueryUtilsService.getAssociations().entrySet()) {

				if(predicate.contains(entry.getValue())) {
					//The property name
					String name = predicate.replace(entry.getValue(), "").replace("<", "").replace(">", "");
					//Replace the property to save it with the short version
					predicate = entry.getKey() + ":" + name;
					break;
				}
			}
		}
		
		String expression = "?l " + predicate + " " + resource;
		
		//Check that the filtering condition does not exist in the list
		for(Pair<Integer, String> exp : state.getExpressions()) {	
			if(exp.getValue1().equals(expression)) {
				return false;
			}
		}

		//Add the newly formed expression to the list
		state.addNaturalExpression(lang, naturalExpression);
		state.addExpression(expression);

		//Retrieve and save results for expression matching this expression
		List<Pair<Integer, String>> expressions = state.getExpressions();

		QueryState queryState = new QueryState();
		queryState.addConditionId(expressions.size()-1);
		//executionService.executeQuery(queryState.getQuery());

		List<Integer> conditionsIds = new ArrayList<>(1);
		conditionsIds.add(expressions.size()-1);

		String query = generateQuery(expressions, conditionsIds, null, lang);
		queryState.setQuery(query);

		state.addQueryState(queryState);

		return true;
	}

}
