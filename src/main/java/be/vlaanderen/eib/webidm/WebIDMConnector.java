/*
 * Copyright (c) 2010-2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.vlaanderen.eib.webidm;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;

import be.vlaanderen.eib.webidm.query.WebIDMQuery;

@ConnectorClass(displayNameKey = "webidm.connector.display", configurationClass = WebIDMConfiguration.class)
public class WebIDMConnector implements Connector, SchemaOp, SearchOp<WebIDMQuery>, TestOp {

    private static final Log LOG = Log.getLog(WebIDMConnector.class);

    private WebIDMConfiguration configuration;
    private WebIDMConnection connection;

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(Configuration configuration) {
    	
        this.configuration = (WebIDMConfiguration)configuration;
        if (this.connection == null) {
        	this.connection = new WebIDMConnection(this.configuration);
        }
    }

    @Override
    public void dispose() {
    	
        configuration = null;
        if (connection != null) {
            connection.dispose();
            connection = null;
        }
    }

	@Override
	public FilterTranslator<WebIDMQuery> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
		
		LOG.info("Filter translator maken");
		
		return null;
	}

	@Override
	public void executeQuery(ObjectClass objectClass, WebIDMQuery query, ResultsHandler handler,
								OperationOptions options) {
		
		LOG.info("Query uitvoeren");
	}

	@Override
	public Schema schema() {
		
		LOG.info("Schema maken");
		
		SchemaBuilder builder = new SchemaBuilder(WebIDMConnector.class);
		builder.defineObjectClass(maakVirtualIdentityClass());
		
		return builder.build();
	}
	
	private ObjectClassInfo maakVirtualIdentityClass() {
		
		ObjectClassInfoBuilder objClassBuilder = new ObjectClassInfoBuilder();
		objClassBuilder.setType("__ACCOUNT__");
		
		AttributeInfoBuilder aib = new AttributeInfoBuilder();
		aib.setName("vo-id");
		aib.setType(String.class);
		aib.setMultiValued(false);
		aib.setReadable(true);
		aib.setReturnedByDefault(true);
		
		objClassBuilder.addAttributeInfo(aib.build());
		
		return objClassBuilder.build();
	}

	@Override
	public void test() {
		
		LOG.info("Test connector");
	}
}
