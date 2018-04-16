package be.vlaanderen.eib.webidm.model;

import java.util.HashMap;
import java.util.Map;

public class Account {

	private static final String ACCOUNT_ID = "accountID";
	private Map<String, Object> attributes = new HashMap<>();
	
	public Account() {
		
	}
	
	public Account(Map<String, Object> attributes) {
		
		this.attributes = attributes;
	}
	
	public void voegAttribuutToe(String naam, Object waarde) {
		
		attributes.put(naam, waarde);
	}
	
	public String geefAccountId() {
		
		return (String) attributes.get(ACCOUNT_ID);
	}
}
