package org.ahp.rdf_navigation_tool.model;

/**
 * Represents a RDF triple
 * Example: ahpo:henriPoincaré a ahpo:Person
 * @author Nicolas Lasolle
 *
 */
public class Triple {
	private String subject;
	private String predicate;
	private String object;
	
	public Triple (String subject, String predicate, String object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	public Triple() {
		
	}
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getPredicate() {
		return predicate;
	}
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	
	@Override
	public String toString() {
		return "{" +
				"\rSubject: " + subject +
				"\rPredicate: " + predicate +
				"\rObject: " + object + "\n}"
				;
	}

	
	
}
