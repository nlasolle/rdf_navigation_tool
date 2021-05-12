package org.ahp.rdf_navigation_tool.service;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class used to parse, validate and extract components from a SPARQL query.
 * @author Nicolas Lasolle
 *
 */
public class QueryParser {
	private static final Logger logger = LoggerFactory.getLogger(QueryParser.class);

	private Query query;

	/**
	 * Instantiate a SPARQL query parser with a given SPARL query string representation.
	 * An exception is thrown if the query is invalid.
	 * 
	 * @param queryString
	 */
	public QueryParser(String queryString){
		parse(queryString);
	}


	/**
	 * Instantiate a SPARQL query parser with an object representation of a SPARQL query.
	 * An exception is thrown if the query is invalid.
	 * 
	 * @param queryString
	 */
	public QueryParser(Query query) {
		this.query = query;
	}


	/**
	 * Create a query from a given query string representation
	 * @param queryString
	 * @throws QueryException
	 */
	public void parse(String queryString){
		query = QueryFactory.create(queryString);
	}

	/**
	 * Extract triples patterns from a query
	 * @return
	 */
	public  List<Triple>  extractTriplesPath() {
		logger.info("Starting query conditions extraction.");

		final List<Triple> triples = new ArrayList<Triple>();


		ElementWalker.walk(query.getQueryPattern(),
				new ElementVisitorBase() {

			@Override
			public void visit(ElementPathBlock el) {
				ListIterator<TriplePath> triplesIterator = el.getPattern().iterator();

				for( ;  triplesIterator.hasNext() ;) {
					TriplePath path = triplesIterator.next();

					triples.add(new Triple(path.getSubject(), 
							path.getPredicate(), 
							path.getObject()));
				}
			}});
		logger.info("Ending query conditions extraction.");
		return triples;
	}

	/**
	 * Get the list of variables requested in the SELECT clause.
	 * @return variables as strings
	 */
	public List<String> extractVars(){
		return query.getResultVars();
	}

	/**
	 * Get the list of sorting conditions.
	 * @return
	 */
	public List<SortCondition> extractOrderBy(){
		return query.getOrderBy();
	}

	/**
	 * Get the list of grouping conditions.
	 * @return
	 */
	public VarExprList extractGroupBy(){
		return query.getGroupBy();
	}

}
