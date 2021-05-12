package org.ahp.rdf_navigation_tool;

import java.io.FileNotFoundException;

import org.ahp.rdf_navigation_tool.model.JenaQueryExecutor;
import org.ahp.rdf_navigation_tool.model.QueryExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Configuration class which is used to instantiate
 * the SPARQL query engine (using spring @Bean) 
 * @author Nicolas Lasolle
 *
 */
@Configuration
public class RDFConfiguration {

	@Bean
	@Scope("singleton")
	public QueryExecutor queryExecutor() throws FileNotFoundException {
		QueryExecutor queryExecutor;

		queryExecutor = new JenaQueryExecutor();
		return queryExecutor;
	}
}
