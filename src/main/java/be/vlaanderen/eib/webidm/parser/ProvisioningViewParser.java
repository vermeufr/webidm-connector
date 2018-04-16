package be.vlaanderen.eib.webidm.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import be.vlaanderen.eib.webidm.model.Account;

public class ProvisioningViewParser {
	
	private static Logger LOG = LoggerFactory.getLogger(ProvisioningViewParser.class);

	public Account parseView(JsonNode view) {
		
		Account account = new Account();
		Iterator<Entry<String, JsonNode>> attribuutIterator = view.fields();
		while (attribuutIterator.hasNext()) {
			
			Entry<String, JsonNode> attribuut = attribuutIterator.next();
			JsonNodeType nodeType = attribuut.getValue().getNodeType();
			
			LOG.debug("Volgende node: <" + attribuut.getKey() + "> , type: " + nodeType);
			Object waarde = parseAttribuut(attribuut.getValue());
			account = aanpassen(account, attribuut.getKey(), waarde);
		}
		
		return account;
	}
	
	private Account aanpassen(Account account, String attribuuteNaam, Object attribuutWaarde) {
		

		account.voegAttribuutToe(attribuuteNaam, attribuutWaarde);

		
		return account;
	}
	
	@SuppressWarnings("incomplete-switch")
	private Object parseAttribuut(JsonNode attribuutWaarde) {
		
		Object resultaat = null;
		switch (attribuutWaarde.getNodeType()) {
		
			case STRING:
				
				resultaat = attribuutWaarde.asText();
				break;
				
			case ARRAY:
				
				List<Object> waardes = new ArrayList<>();
				int aantalElementen = attribuutWaarde.size();
				int index = 0;
				while (index < aantalElementen) {
					JsonNode element = attribuutWaarde.get(index);
					Object waarde = parseAttribuut(element);
					waardes.add(waarde);
					++index;
				}
				resultaat = waardes;
				
				break;
				
			case OBJECT:
				
				Map<String, Object> hetObject = new HashMap<>();
				Iterator<Entry<String, JsonNode>> attribuutIterator = attribuutWaarde.fields();
				while (attribuutIterator.hasNext()) {
					Entry<String, JsonNode> attribuut = attribuutIterator.next();
					Object waarde = parseAttribuut(attribuut.getValue());
					hetObject.put(attribuut.getKey(), waarde);
				}
				resultaat = hetObject;
				
				break;
		}
		
		return resultaat;
	}
}
