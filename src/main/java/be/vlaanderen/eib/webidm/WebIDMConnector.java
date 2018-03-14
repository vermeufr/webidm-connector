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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.TestOp;

import be.vlaanderen.eib.webidm.query.WebIDMFilterTranslator;
import be.vlaanderen.eib.webidm.query.WebIDMQuery;

@ConnectorClass(displayNameKey = "webidm.connector.display", configurationClass = WebIDMConfiguration.class)
public class WebIDMConnector implements Connector, SchemaOp, SearchOp<Filter>, SyncOp, TestOp {

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
	public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
		
		LOG.info("Filter translator maken");
		
		return new WebIDMFilterTranslator();
	}
	
	@Override
	public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {
		
		LOG.info("Filter translator maken");
		
		if (ObjectClass.ACCOUNT.is(objectClass.getObjectClassValue())) {
			
			ConnectorObjectBuilder coBuilder = new ConnectorObjectBuilder();
			coBuilder.setObjectClass(objectClass);
			coBuilder.setUid("dc797290-703a-41d3-b4bb-6b3aa4cd5291");
			coBuilder.setName("test");
			
			AttributeBuilder attributeBuilder = new AttributeBuilder();
			attributeBuilder.setName("idd_cn");
			attributeBuilder.addValue("Hermans, Erwin");
			coBuilder.addAttribute(attributeBuilder.build());
			attributeBuilder = new AttributeBuilder();
			attributeBuilder.setName("idd_dn");
			attributeBuilder.addValue("vo-idv=dc797290-703a-41d3-b4bb-6b3aa4cd5291,ou=gid,dc=vlaanderen,dc=be");
			coBuilder.addAttribute(attributeBuilder.build());
			
			handler.handle(coBuilder.build());
		}
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
		objClassBuilder.addAllAttributeInfo(maakAttributenInfo());
		
		return objClassBuilder.build();
	}
	
	private Collection<AttributeInfo> maakAttributenInfo() {
		
		Set<AttributeInfo> attributenInfo = new HashSet<>();
		
		AttributeInfoBuilder aib = new AttributeInfoBuilder();
		aib.setName("idd_cn");
		aib.setType(String.class);
		aib.setMultiValued(false);
		aib.setReadable(true);
		aib.setReturnedByDefault(true);
		attributenInfo.add(aib.build());
		aib = new AttributeInfoBuilder();
		aib.setName("idd_dn");
		aib.setType(String.class);
		aib.setMultiValued(false);
		aib.setReadable(true);
		aib.setReturnedByDefault(true);
		attributenInfo.add(aib.build());
		aib = new AttributeInfoBuilder();
		aib.setName("idd_federatie-applicatie12-autorisatiedata");
		aib.setType(String.class);
		aib.setMultiValued(true);
		aib.setReadable(true);
		aib.setReturnedByDefault(true);
		attributenInfo.add(aib.build());
		
		return attributenInfo;
	}

	@Override
	public void test() {
		
		LOG.info("Test connector");
	}

	@Override
	public void sync(ObjectClass objectClass, SyncToken token, SyncResultsHandler handler, OperationOptions options) {
		
		LOG.info("Live sync");
	}

	@Override
	public SyncToken getLatestSyncToken(ObjectClass objectClass) {
		
		LOG.info("Geef laatste sync token");
		
		return null;
	}

}
