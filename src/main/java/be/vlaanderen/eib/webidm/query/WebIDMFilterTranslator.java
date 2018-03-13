package be.vlaanderen.eib.webidm.query;

import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;

public class WebIDMFilterTranslator implements FilterTranslator<Filter> {

	@Override
	public List<Filter> translate(Filter filter) {
		
		ArrayList<Filter> filters = new ArrayList<Filter>();
		filters.add(filter);
		
		return filters;
	}

}
