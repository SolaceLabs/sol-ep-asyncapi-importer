/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.solace.ep.asyncapi.importer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.solace.cloud.ep.designer.ApiClient;
import com.solace.cloud.ep.designer.auth.HttpBearerAuth;
import com.solace.cloud.ep.designer.model.ApplicationDomain;
import com.solace.cloud.ep.designer.model.Event;
import com.solace.cloud.ep.designer.model.SchemaObject;
import com.solace.cloud.ep.designer.model.SchemaVersion;
import com.solace.cloud.ep.designer.model.TopicAddressEnum;
import com.solace.cloud.ep.designer.model.TopicAddressEnumValue;
import com.solace.cloud.ep.designer.model.TopicAddressEnumVersion;
import com.solace.ep.asyncapi.importer.client.EventPortalClientApi;
// import com.solace.ep.asyncapi.importer.EpNewVersionStrategy;

/**
 * Unit test for simple App.
 */
public class TestEpClient
{
    private String BASE_PATH = "https://api.solace.cloud";
    private String BEARER_TOKEN = "ey";
    private String APP_DOMAIN_NAME = "CodeGen-SAP-IFlow";

    private EventPortalClientApi epClientApi;

    private EventPortalClientApi getEventPortalClientApi() throws Exception
    {
        try {
            BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
            BASE_PATH = ConfigManager.getProperty("ep.baseurl");
            APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");

            if (this.epClientApi == null) {
                EventPortalClientApi clientApi = new EventPortalClientApi(
                    getApiClient(), APP_DOMAIN_NAME, EpNewVersionStrategy.MINOR);
                this.epClientApi = clientApi;
            }
        } catch (Exception exc) {
            System.out.println("Failed to create clientApi");
            exc.printStackTrace();
            throw exc;
        }
        return this.epClientApi;
    }

    public ApiClient getApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(BASE_PATH);
        HttpBearerAuth apiToken = (HttpBearerAuth)apiClient.getAuthentication("APIToken");
        apiToken.setBearerToken(BEARER_TOKEN);
        return apiClient;
    }

    @Test
    public void testGetAppDomain() {

        try {
            ApplicationDomain applicationDomain = getEventPortalClientApi().getApplicationDomainByName(APP_DOMAIN_NAME);
            if ( applicationDomain != null ) {
                assertTrue( applicationDomain.getName().contentEquals("CodeGen-SAP-IFlow") );
            } else {
                fail("contents null");
            }

            System.out.println( applicationDomain == null ? "null" : applicationDomain.getId() );
            System.out.println( applicationDomain == null ? "null" : applicationDomain.getName() );
        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
            System.out.println(exc.getLocalizedMessage());
            exc.printStackTrace();
        }
    }

    @Test
    public void testGetSchemaObject() {

        try {
            SchemaObject schemaObject = getEventPortalClientApi().getSchemaObjectByName("Shipment");
            if (schemaObject != null ) {
                assertTrue (schemaObject.getName().contentEquals("Shipment"));
            } else {
                fail( "contents null" );
            }

            System.out.println( schemaObject == null ? "null" : schemaObject.getId() );
            System.out.println( schemaObject == null ? "null" : schemaObject.getName());

        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
            System.out.println(exc.getLocalizedMessage());
            exc.printStackTrace();
        }
    }

    @Test
    public void testGetSchemaVersionByContent() {

        final String SCHEMA_ID = "uq438iqj3fa";
        final String SCHEMA_CONTENT = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Shipment\",\"type\":\"object\",\"required\":[\"shipmentId\",\"status\",\"items\"],\"properties\":{\"shipment\":{\"description\":\"Identifier of the shipment\",\"type\":\"string\"},\"status\":{\"description\":\"Status of the shipment\",\"type\":\"string\"},\"items\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"quantity\":{\"description\":\"Quantity of the item available in inventory\",\"type\":\"integer\",\"minimum\":0},\"price\":{\"description\":\"Price of the item\",\"type\":\"number\",\"minimum\":0},\"name\":{\"description\":\"Name of the item\",\"type\":\"string\"},\"description\":{\"description\":\"Description of the item\",\"type\":\"string\"},\"id\":{\"description\":\"Unique identifier for the item\",\"type\":\"string\"},\"category\":{\"description\":\"Category of the item\",\"type\":\"string\"}},\"required\":[\"id\",\"name\",\"quantity\",\"price\"]}}}}";

        try {
            SchemaVersion schemaVersion = getEventPortalClientApi().getSchemaVersionByContent(SCHEMA_ID, SCHEMA_CONTENT);
            if (schemaVersion == null) {
                fail("Failed to find schema version for schema");
                return;
            }
            assertTrue( ! schemaVersion.getId().isEmpty() );
            System.out.println("Schema Version ID = " + schemaVersion.getId());

        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            exc.printStackTrace();
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    public void testGetTopicAddressEnumByName() {

        final String TOPIC_ADDRESS_ENUM_NAME = "dsb-shipmentStatus";

        try {
            TopicAddressEnum topicAddressEnum = getEventPortalClientApi().getTopicAddressEnumByName(TOPIC_ADDRESS_ENUM_NAME);
            if (topicAddressEnum == null) {
                fail("Did not find TopicAddressEnum name = " + TOPIC_ADDRESS_ENUM_NAME);
                return;
            }
            assertTrue( ! topicAddressEnum.getId().isEmpty() );
            System.out.println("TopicAddressEnum name = " + topicAddressEnum.getName() + "; Id = " + topicAddressEnum.getId());
            System.out.println("TopicAddressEnum app domain Id = " + topicAddressEnum.getApplicationDomainId());

        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            exc.printStackTrace();
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    public void testGetTopicAddressEnumVersionByContent() {

        final List<String> values = List.of("SHIPPED", "INITIATED", "CANCELLED");
        final String topicAddressEnumId = "ejqzw4v4kfr";

        try {
            TopicAddressEnumVersion topicAddressEnumVersion = getEventPortalClientApi().getTopicAddressEnumVersionByContent(topicAddressEnumId, values);
            if (topicAddressEnumVersion == null) {
                fail("Did not return TopicAddressEnumVersion as expected");
                return;
            }
            assertTrue( !topicAddressEnumVersion.getValues().isEmpty() );
            System.out.println("EnumVersionId: " + topicAddressEnumVersion.getId());
            for (TopicAddressEnumValue v : topicAddressEnumVersion.getValues()) {
                System.out.println("Value: " + v.getValue());
            }

        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            exc.printStackTrace();
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    public void testGetEventByName() {

        final String EVENT_NAME = "ShipmentEvent";

        try {
            Event solaceEvent = getEventPortalClientApi().getEventByName(EVENT_NAME);
            if (solaceEvent == null) {
                fail("Did not return " + EVENT_NAME + " as expected");
                return;
            }
            assertTrue(solaceEvent.getBrokerType().contentEquals("solace"));
            System.out.println("Event id/name from retrieved event: " + solaceEvent.getId() + "/" + solaceEvent.getName());

        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            exc.printStackTrace();
            fail(exc.getLocalizedMessage());
        }
    }
}
