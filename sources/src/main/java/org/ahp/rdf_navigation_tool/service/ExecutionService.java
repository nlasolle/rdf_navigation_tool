package org.ahp.rdf_navigation_tool.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ahp.rdf_navigation_tool.RDFConfiguration;
import org.ahp.rdf_navigation_tool.model.QueryExecutor;
import org.ahp.rdf_navigation_tool.model.QueryResults;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Query execution service.
 * @author Nicolas Lasolle
 *
 */
@Service(value = "ExecutionService")
public class ExecutionService implements InitializingBean {

	@Autowired
	private ApplicationContext context;
	QueryExecutor queryExecutor;

	@Value("${custom.sparqlEndpoint}")
	private String endpoint;
	
	public void afterPropertiesSet() throws Exception {
		RDFConfiguration config = context.getBean(RDFConfiguration.class);

		/* One instance of queryExecutor is instantiated as a Spring @Bean
		 * This instance is accessible through Spring Application Context 
		 * and is shared by the different controllers and services of the application
		 */
		queryExecutor = config.queryExecutor(); 

	}

	/**
	 * Return results of a SPARQL query with the form [{id, variable, value}, ...].
	 * @param the SPARQL query to be executed.
	 * @return list of results
	 */
	public List<QueryResults> executeQuery(String query) {
		List<QueryResults> formattedResults = new ArrayList<QueryResults>();
		
		QueryExecution qExec = queryExecutor.executeRemoteSelectQuery(query, endpoint);
		Iterator<?> results = qExec.execSelect();
		
		//Format and save results
		for ( ; results.hasNext() ; )
		{
			QuerySolution soln = (QuerySolution) results.next() ;
			Iterator<String> varNames = soln.varNames();

			//If this resource has already been created
			if(!formattedResults.isEmpty() &&
					soln.get("l").toString().equals(formattedResults.get(formattedResults.size() - 1).getUri())) {
				QueryResults result = formattedResults.get(formattedResults.size() - 1);

				for(;varNames.hasNext();) {
					String var = varNames.next();
					if (!var.equals("date") && !var.equals("l")){
						if(result.getProperties().containsKey(var)) {
							String previousValue = result.getProperties().get(var);
							String value = soln.get(var).toString();
							
							if(value.contains("@")){
								value = value.substring(0, value.indexOf("@"));
							}

							if(!previousValue.contains(value)) {
								result.getProperties().put(var, previousValue + ", "
										+ value); 
							}
						} else {
							
							String value = soln.get(var).toString();
							
							if(value.contains("@")){
								value = value.substring(0, value.indexOf("@"));
							}

							
							result.addPropertyValue(var, value);
						}

					}
				}
			} else {
				QueryResults result = new QueryResults(soln.get("l").toString());
				for(;varNames.hasNext();) {
					String var = varNames.next();
					if(var.equals("date")) {
						result.setDate(soln.get(var).toString());
					} else if(!var.equals("l")){
						
						String value = soln.get(var).toString();
						
						if(value.contains("@")){
							value = value.substring(0, value.indexOf("@"));
						}

						result.addPropertyValue(var, value);
					}

				}
				
				Map<String, String> properties = result.getProperties();
				/*properties = 
					    properties.entrySet().stream()
					    .sorted(Comparator.comparing(Map.Entry::getKey))
					    .collect(Collectors.toMap(Map.Entry::getKey,
								Map.Entry::getValue, (oldValue, newValue) -> oldValue,
								LinkedHashMap::new));*/
				properties = new TreeMap<String, String>(properties);
				result.setProperties(properties);
					      

				formattedResults.add(result);
			}
		}
		
		qExec.close();
		return formattedResults;
	}
}
