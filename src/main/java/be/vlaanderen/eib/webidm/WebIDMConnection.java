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

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumer;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.identityconnectors.common.logging.Log;

public class WebIDMConnection {

    private static final Log LOG = Log.getLog(WebIDMConnection.class);

    private WebIDMConfiguration configuration;
    private Endpoint endpoint;
    private CamelContext context;

    public WebIDMConnection(WebIDMConfiguration configuration) {
    	
        this.configuration = configuration;
        context = new DefaultCamelContext();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
		context.addComponent("test-jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		endpoint = context.getEndpoint("activemq:midpoint");
    }
    
    public void haalViewsOp() {
    	
    	Exchange exchange = null;
    	PollingConsumer consumer;
		try {
			consumer = endpoint.createPollingConsumer();
			do {
				exchange = consumer.receive(500);
				if (exchange != null) {
					System.out.println("View ontvangen");
				}
			} while (exchange != null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }

    public void dispose() {
        
    	try {
			context.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}