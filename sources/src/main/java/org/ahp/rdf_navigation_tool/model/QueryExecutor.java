package org.ahp.rdf_navigation_tool.model;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.QueryExecution;


/**
 * General interface for SPARQL query execution
 * select, update, delete data
 * @author Nicolas Lasolle
 *
 */
public interface QueryExecutor {
	public Iterator<?> executeLocalSelectQuery(String queryString, boolean deduction);
	public void executeLocalUpdateQuery(String queryString) throws FileNotFoundException;
	public QueryExecution executeRemoteSelectQuery(String queryString, String endpoint);
	public void executeRemoteUpdateQuery(String queryString, String endpoint);
	public void loadDatabase(List<String> files);
	int getCount();
	
}
