package org.ahp.rdf_navigation_tool.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.PathParam;

import org.ahp.rdf_navigation_tool.model.QueryResults;
import org.ahp.rdf_navigation_tool.model.QueryResultsParam;
import org.ahp.rdf_navigation_tool.service.StateService;
import org.ahp.sqtruleapplication.exceptions.MoreRequestException;
import org.ahp.sqtruleapplication.exceptions.RuleException;
import org.ahp.sqtruleapplication.exceptions.SPARQLException;
import org.ahp.sqtruleapplication.exceptions.XMLFileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Manage application process change
 * @author Nicolas Lasolle
 *
 */
@RestController
public class StatesController {
	
	private static final int AND_MODE = 1;
	
	@Autowired
	private StateService stateService;
	
	@CrossOrigin
	@RequestMapping(value = "/init-process", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> initializeApplicationProcess(@RequestBody Map<String, String> jsonBody) {
		
		List<String> naturalLanguageexpressions= stateService.initApplicationProcess(jsonBody.get("iri"), jsonBody.get("lang"));
		
		return new ResponseEntity<List<String>>(naturalLanguageexpressions, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/resource-details", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<Map<String, String>> getResourceDetails(@RequestBody String iri, @PathParam(value = "lang") String lang) {
		
		Map<String,String> details = stateService.getResourceDetails(iri, lang);
		
		return new ResponseEntity<Map<String,String>>(details, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/execute-queries", method = RequestMethod.GET)
	public String executeQueries() {
		
		stateService.executeCurrentStateQueries();
		
		return "All queries executed for current state";
	}
	
	@CrossOrigin
	@RequestMapping(value = "/get-results", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<QueryResults>> getResults(@RequestParam int mode, @RequestBody QueryResultsParam param) {
		
		List<QueryResults> results;
		if(mode == AND_MODE) {
			results = stateService.getAndModeQueryResults(param.getConditionsIds(), param.getNegativeConditionsIds(), param.getLang());
			results.size();
		} else {
			results = stateService.getOrModeQueryResults(param.getConditionsIds());
		}
		
		return new ResponseEntity<List<QueryResults>>(results, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/init-sqtrl-process", method = RequestMethod.GET, consumes = MediaType.TEXT_PLAIN_VALUE)
	public String initSQTRLProcess() {

		try {
			stateService.initSQTRLProcess();
		} catch (SPARQLException | XMLFileException | RuleException e) {
			e.printStackTrace();
		}
		
		return "SQTRL process initialized";
	}
	
	@CrossOrigin
	@RequestMapping(value = "/get-more", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getMore(@PathParam(value = "lang") String lang) {

		List<String> expressions = new ArrayList<>();
		
		try {
			expressions = stateService.getMore(lang);
		} catch (SPARQLException | XMLFileException | MoreRequestException e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<List<String>>(expressions, HttpStatus.OK);
	}

	@CrossOrigin
	@RequestMapping(value = "/add-condition", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public boolean addManualCondition(@RequestBody Map<String, String> jsonBody) {
		return stateService.addCondition(jsonBody.get("property"), 
				jsonBody.get("propertyLabel"), 
				jsonBody.get("resource"), 
				jsonBody.get("resourceLabel"),
				jsonBody.get("lang"));
	}
	
	@CrossOrigin
	@RequestMapping(value = "/get-translated-expressions", method = RequestMethod.GET)
	public List<String> getTranslatedExpressions(@PathParam(value = "lang") String lang) {
		return stateService.getNaturalExpressions(lang);
	}
}
