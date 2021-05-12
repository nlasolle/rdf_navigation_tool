package org.ahp.rdf_navigation_tool.controller;

import java.util.Map;

import javax.ws.rs.PathParam;

import org.ahp.rdf_navigation_tool.model.DateOptions;
import org.ahp.rdf_navigation_tool.service.ConfigurationService;
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
 * Controller used to access configuration values
 * @author Nicolas Lasolle
 *
 */
@RestController
public class ConfigurationController {
	
	@Autowired
	private ConfigurationService configurationService;
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/endpoint", method = RequestMethod.GET)
	public ResponseEntity<String> getEndpoint() {
		
		String endpoint = configurationService.getEndpoint();
		System.out.println("endpoint " + endpoint);
		return new ResponseEntity<String>(endpoint, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/initial", method = RequestMethod.GET)
	public ResponseEntity<String> getInitialResource() {
		
		String endpoint = configurationService.getInitialResource();
		return new ResponseEntity<String>(endpoint, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/endpoint", method = RequestMethod.PUT)
	public String setEndpoint(@RequestBody String endpoint) {
		
		configurationService.setEndpoint(endpoint);
		return "Endpoint property updated with value " + endpoint;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/language", method = RequestMethod.GET)
	public ResponseEntity<String> getLang() {
		
		String lang = configurationService.getLang();
		return new ResponseEntity<String>(lang, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/itemLink", method = RequestMethod.GET)
	public ResponseEntity<String> getItemLink() {
		
		String lang = configurationService.getItemLink();
		return new ResponseEntity<String>(lang, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/language", method = RequestMethod.PUT)
	public String setLang(@RequestBody String lang) {
		
		configurationService.setLang(lang);
		return "Language property updated with value " + lang;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/date-property-label", method = RequestMethod.GET)
	public String getDatePropertyLabel(@PathParam(value = "lang") String lang) {
		
		String label = configurationService.getDatePropertyLabel(lang);
		
		return label;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/date-options", method = RequestMethod.GET)
	public ResponseEntity<DateOptions> getDateFilteringOptions() {
		
		DateOptions options = configurationService.getDateFilteringOptions();
		
		return new ResponseEntity<DateOptions>(options, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/properties-labels", method = RequestMethod.GET)
	public ResponseEntity<Map<String,String>> getDisplayedPropertiesLabel(@PathParam(value = "lang") String lang) {
		
		Map<String,String> labels = configurationService.getDisplayedPropertiesLabel(lang);
		
		return new ResponseEntity<Map<String,String>>(labels, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/ontology-properties", method = RequestMethod.GET)
	public ResponseEntity<Map<String,String>> getOntologyProperties(@PathParam(value = "lang") String lang) {
		
		Map<String,String> labels = configurationService.getProperties(lang);
		
		return new ResponseEntity<Map<String,String>>(labels, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/resources-labels", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<Map<String,String>> getResourcesLabels(@RequestBody String type, @PathParam(value = "lang") String lang) {
		
		Map<String,String> labels = configurationService.getResources(type, lang);
		
		return new ResponseEntity<Map<String,String>>(labels, HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/configuration-service/types", method = RequestMethod.GET)
	public ResponseEntity<Map<String,String>> getTypes(@PathParam(value = "lang") String lang) {
		
		Map<String,String> labels = configurationService.getTypes(lang);
		
		return new ResponseEntity<Map<String,String>>(labels, HttpStatus.OK);
	}
	
}
