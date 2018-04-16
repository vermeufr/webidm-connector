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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.TestOp;

import be.vlaanderen.eib.webidm.query.WebIDMFilterTranslator;

@ConnectorClass(displayNameKey = "webidm.connector.display", configurationClass = WebIDMConfiguration.class)
public class WebIDMConnector implements Connector, SchemaOp, SearchOp<Filter>, SyncOp, DeleteOp, TestOp {

    private static final Log LOG = Log.getLog(WebIDMConnector.class);
    private static final int AANTAL_OBJECTEN = 100;

    private WebIDMConfiguration configuration;
    private WebIDMConnection connection;
    private static Integer vorigeTokenWaarde;
    private static Map<String, ConnectorObject> connectorObjecten = new HashMap<>();

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
		
		LOG.info("Query uitvoeren");
		
		if (ObjectClass.ACCOUNT.is(objectClass.getObjectClassValue())) {

			if (isEqualsFilter(query, "__UID__")) {
				Attribute uuidAttribute = ((EqualsFilter)query).getAttribute();
	        	String uuid = (String)uuidAttribute.getValue().get(0);
	        	ConnectorObject connectorObject = connectorObjecten.get(uuid);
	        	if (connectorObject != null) {
	        		handler.handle(connectorObject);
	        	}
			} else {
				for (ConnectorObject connObject : maakIDDObjecten(objectClass, 10, false)) {
					handler.handle(connObject);
				}
			}
		}
	}
	
	private boolean isEqualsFilter(Filter icfFilter, String icfAttrname) {
		
		return icfFilter != null && (icfFilter instanceof EqualsFilter) && icfAttrname.equals(((EqualsFilter)icfFilter).getName());
	}
	
	private List<ConnectorObject> maakIDDObjecten(ObjectClass objectClass, int aantal, boolean wijzigen) {
		

			int aantalTeMaken = 0;
			while (aantalTeMaken < aantal) {
				ConnectorObjectBuilder coBuilder = new ConnectorObjectBuilder();
				coBuilder.setObjectClass(objectClass);
				String uuid = "dc797290-703a-41d3-b4bb-6b3aa4cd5291" + "-" + (aantalTeMaken+1);
				coBuilder.setUid(uuid);
				String name = "TestVI" + "-" + (aantalTeMaken+1);
				coBuilder.setName(name);
				
				AttributeBuilder attributeBuilder = new AttributeBuilder();
				attributeBuilder.setName("idd_cn");
				attributeBuilder.addValue("Dierckx, Erwin" + "-" + (aantalTeMaken+1));
				coBuilder.addAttribute(attributeBuilder.build());
				attributeBuilder = new AttributeBuilder();
				attributeBuilder.setName("idd_dn");
				if (!wijzigen) {
					attributeBuilder.addValue("vo-idv=dc797290-703a-41d3-b4bb-6b3aa4cd5291,ou=gid,dc=vlaanderen,dc=be");
				} else {
					attributeBuilder.addValue("vo-idv=Dierckx,ou=gid,dc=vlaanderen,dc=be");
				}
				coBuilder.addAttribute(attributeBuilder.build());
				attributeBuilder = new AttributeBuilder();
				attributeBuilder.setName("idd_federatie-applicatie12-autorisatiedata");
				attributeBuilder.addValue("VO-RA");
				coBuilder.addAttribute(attributeBuilder.build());
				connectorObjecten.put(uuid, coBuilder.build());
				++aantalTeMaken;
			}

		List<ConnectorObject> objecten = new ArrayList<>(connectorObjecten.values());
		
		return objecten;
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
		
		connection.haalViewsOp();
		
		int aantal = 1;
		SyncDeltaType operatie = bepaalDeltaType(token);
		boolean wijzigen = operatie == SyncDeltaType.UPDATE;
		for (ConnectorObject connObject : maakIDDObjecten(objectClass, AANTAL_OBJECTEN, wijzigen)) {
			SyncDeltaBuilder deltaBuilder =  new SyncDeltaBuilder();
			deltaBuilder.setObjectClass(ObjectClass.ACCOUNT);
			deltaBuilder.setDeltaType(operatie);
			deltaBuilder.setObject(connObject);
			deltaBuilder.setUid(connObject.getUid());
			deltaBuilder.setToken(token);
			handler.handle(deltaBuilder.build());
			++aantal;
			if (aantal > AANTAL_OBJECTEN) {
				break;
			}
		}
		
	}
	
	private SyncDeltaType bepaalDeltaType(SyncToken token) {
		
		Integer waarde = (Integer) token.getValue();
		switch(waarde) {
		
			case 1:
				{
					return SyncDeltaType.CREATE;
				}
			case 2:
				{
					return SyncDeltaType.UPDATE;
				}
				
			case 3:
				{
					return SyncDeltaType.DELETE;
				}
			default:
			{
				return SyncDeltaType.CREATE;
			}
		}
	}
	
	@Override
	public SyncToken getLatestSyncToken(ObjectClass objectClass) {
		
		LOG.info("Geef laatste sync token");
		if (vorigeTokenWaarde == null || vorigeTokenWaarde.equals(3)) {
			vorigeTokenWaarde = new Integer("1");
		} else {
			++vorigeTokenWaarde;
		}
		
		
		return new SyncToken(vorigeTokenWaarde);
	}

	@Override
	public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
		
		LOG.info("Delete account");
	}

}
