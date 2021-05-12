package org.ahp.rdf_navigation_tool.controller;
import java.util.List;

import org.ahp.rdf_navigation_tool.model.QueryResults;
import org.ahp.rdf_navigation_tool.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring controller class to manage context associated with subject
 * @author Nicolas Lasolle
 *
 */
@RestController
public class ChronologyApplicationController {
	
	@Autowired
	private ExecutionService executionService;
	
	@CrossOrigin
	@RequestMapping(value = "/query", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<List<QueryResults>> getQueryExecutionResults(@RequestBody String query) {
		
		List<QueryResults> results = executionService.executeQuery(query);
		
		return new ResponseEntity<List<QueryResults>>(results, HttpStatus.OK);
	}
	
}
