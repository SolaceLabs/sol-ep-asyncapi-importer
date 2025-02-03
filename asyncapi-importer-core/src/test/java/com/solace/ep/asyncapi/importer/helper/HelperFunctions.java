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
package com.solace.ep.asyncapi.importer.helper;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.solace.cloud.ep.designer.ApiClient;
import com.solace.cloud.ep.designer.api.ApplicationsApi;
import com.solace.cloud.ep.designer.api.EnumsApi;
import com.solace.cloud.ep.designer.api.EventsApi;
import com.solace.cloud.ep.designer.api.SchemasApi;
import com.solace.cloud.ep.designer.auth.HttpBearerAuth;
import com.solace.cloud.ep.designer.model.AddressLevel;
import com.solace.cloud.ep.designer.model.Application;
import com.solace.cloud.ep.designer.model.ApplicationVersion;
import com.solace.cloud.ep.designer.model.ApplicationVersionsResponse;
import com.solace.cloud.ep.designer.model.ApplicationsResponse;
import com.solace.cloud.ep.designer.model.Event;
import com.solace.cloud.ep.designer.model.EventResponse;
import com.solace.cloud.ep.designer.model.EventVersion;
import com.solace.cloud.ep.designer.model.EventVersionsResponse;
import com.solace.cloud.ep.designer.model.EventsResponse;
import com.solace.cloud.ep.designer.model.SchemaObject;
import com.solace.cloud.ep.designer.model.SchemaVersion;
import com.solace.cloud.ep.designer.model.SchemaVersionsResponse;
import com.solace.cloud.ep.designer.model.SchemasResponse;
import com.solace.cloud.ep.designer.model.TopicAddressEnum;
import com.solace.cloud.ep.designer.model.TopicAddressEnumVersion;
import com.solace.cloud.ep.designer.model.TopicAddressEnumVersionsResponse;
import com.solace.cloud.ep.designer.model.TopicAddressEnumsResponse;
import com.solace.cloud.ep.designer.model.VersionedObjectStateChangeRequest;
import com.solace.ep.asyncapi.importer.ConfigManager;
import com.solace.ep.asyncapi.importer.EpNewVersionStrategy;
import com.solace.ep.asyncapi.importer.client.EventPortalClientApi;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to facilitate step-wise, stateful testing of AsyncAPI import capabilities
 * The purpose of these methods contained here is to provide a mechanism to move
 * specific objects from "Draft" to "Released" life-cycle state. The state
 * of the objects has an impact on processing:
 * - If an object is in Draft state, then a change will update that version
 * - If an object is in Release state or greater, a change will result in a new version
 * ---------------
 * - These methods will not trigger on Maven build.
 * - Relies on ConfigManager class, which in turn requires that 
 *   src/test/resources/config.properties is configured with token + Application Domain
*/
@Slf4j
public class HelperFunctions {

    // EVENT - BOTH - InventoryHeld
    @Test
    public void releaseInventoryHeldEvent() {
        try {
            updateLatestEventVersionToReleased("InventoryHeld");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }

    // APP - Fulfillment
    @Test
    public void releaseFulfillmentApp() {
        try {
            updateLatestApplicationVersionToReleased("Fulfillment");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }

    @Test
    public void releaseShippingServiceApp() {
        try {
            updateLatestApplicationVersionToReleased("ShippingService");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }

    // EVENT - ONLINE STORE - OrderCreated
    @Test
    public void releaseOrderCreatedEvent() {
        try {
            updateLatestEventVersionToReleased("OrderCreated");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }

    // APP - OnlineStore
    @Test
    public void releaseOnlineStoreApp() {
        try {
            updateLatestApplicationVersionToReleased("OnlineStore");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }
    
    // #######  SCHEMAS  ########  

    // SCHEMA - FULLFILLMENT - Invoice
    @Test
    public void updateInvoiceSchema() {
        try {
            updateLatestSchemaVersionToReleased("Invoice");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }

    // SCHEMA - BOTH - Inventory
    @Test
    public void updateInventorySchema() {
        try {
            updateLatestSchemaVersionToReleased("Inventory");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }

    // SCHEMA - FULLFILLMENT - Payment
    @Test
    public void updatePaymentSchema() {
        try {
            updateLatestSchemaVersionToReleased("Payment");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }

    // SCHEMA - BOTH - Customer
    @Test
    public void updateCustomerSchema() {
        try {
            updateLatestSchemaVersionToReleased("Customer");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }

    // SCHEMA - ONLINE STORE - Order
    @Test
    public void updateOrderSchema() {
        try {
            updateLatestSchemaVersionToReleased("Order");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }



    // #######  ENUMS  ####### 

    // InvoiceStatus Enum
    @Test
    public void updateInvoiceStatusEnum() {
        try {
            updateLatestEventVersionToReleased("IMP_invoiceStatus");
        } catch (Exception exc) {
            fail(exc.getMessage());
        } 
    }

    // RegionID Enum
    @Test
    public void updateRegionIdEnum() {
        try {
            updateLatestEventVersionToReleased("IMP_regionId");
        } catch (Exception exc) {
            fail(exc.getMessage());
        } 
    }

    // CustomerStatus Enum
    @Test
    public void updateCustomerStatusEnum() {
        try {
            updateLatestEventVersionToReleased("IMP_customerStatus");
        } catch (Exception exc) {
            fail(exc.getMessage());
        } 
    }

    // InventoryStatus Enum
    @Test
    public void updateInventoryStatusEnum() {
        try {
            updateLatestEventVersionToReleased("IMP_inventoryStatus");
        } catch (Exception exc) {
            fail(exc.getMessage());
        } 
    }
    
    // OrderStatus Enum
    @Test
    public void updateOrderStatusEnum() {
        try {
            updateLatestEventVersionToReleased("IMP_orderStatus");
        } catch (Exception exc) {
            fail(exc.getMessage());
        } 
    }
    

    // #######  EVENTS  ####### 

    // EVENT - BOTH - CustomerUpdated
    @Test
    public void updateCustomerUpdatedEvent() {
        try {
            updateLatestEventVersionToReleased("CustomerUpdated");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }

    // EVENT - ONLINE STORE - CustomerCreated
    @Test
    public void updateCustomerCreatedEvent() {
        try {
            updateLatestEventVersionToReleased("CustomerCreated");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }
    
    // EVENT - ONLINE STORE - OrderCreated
    @Test
    public void updateOrderCreatedEvent() {
        try {
            updateLatestEventVersionToReleased("OrderCreated");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }

    // EVENT - ONLINE STORE - OrderUpdated
    @Test
    public void updateOrderUpdatedEvent() {
        try {
            updateLatestEventVersionToReleased("OrderUpdated");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }
    
    // EVENT - BOTH - InventoryHeld
    @Test
    public void updateInventoryHeldEvent() {
        try {
            updateLatestEventVersionToReleased("InventoryHeld");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }
    
    // EVENT - FULLFILLMENT - InventoryPulled
    @Test
    public void updateInventoryPulledEvent() {
        try {
            updateLatestEventVersionToReleased("InventoryPulled");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }
    
    // EVENT - FULFILLMENT - InvoiceCreated
    @Test
    public void updateInvoiceCreatedEvent() {
        try {
            updateLatestEventVersionToReleased("InvoiceCreated");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }
    
    // EVENT - FULFILLMENT - InvoiceFinalized
    @Test
    public void updateInvoiceFinalizedEvent() {
        try {
            updateLatestEventVersionToReleased("InvoiceFinalized");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }
    
    // EVENT - FULLFILLMENT - PaymentReceived
    @Test
    public void updatePaymentReceivedEvent() {
        try {
            updateLatestEventVersionToReleased("PaymentReceived");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }
    


    // #######  APPLICATIONS  #######

    // APP - OnlineStore
    @Test
    public void updateOnlineStoreApp() {
        try {
            updateLatestApplicationVersionToReleased("OnlineStore");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }
    
    // APP - Fulfillment
    @Test
    public void updateFulfillmentApp() {
        try {
            updateLatestApplicationVersionToReleased("Fulfillment");
        } catch (Exception exc) {
            fail(exc.getMessage());
        }
    }



    // #############  ALL OBJECTS  #############

    // ALL SCHEMAS
    @Test
    public void updateAllLatestSchemaVersionsToReleased() throws Exception
    {
        EventPortalClientApi epClientApi = getEventPortalClientApi();
        ApiClient apiClient = epClientApi.getApiClient();
        SchemasApi schemasApi = new SchemasApi(apiClient);
        try {
            SchemasResponse response = schemasApi.getSchemas(100, 1, null, null, epClientApi.getAppDomainId(), null, null, null, null, null);
            for (SchemaObject schemaObject : response.getData()) {
                updateLatestSchemaVersionToReleased(schemaObject.getName());
            }
        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            fail(exc.getLocalizedMessage());
        }
    }

    // ALL ENUMS
    @Test
    public void updateAllLatestEnumVersionsToRelease(
    ) throws Exception
    {
        EventPortalClientApi epClientApi = getEventPortalClientApi();
        ApiClient apiClient = epClientApi.getApiClient();
        EnumsApi enumsApi = new EnumsApi(apiClient);
        try {
            TopicAddressEnumsResponse response = enumsApi.getEnums(100, 1, null, epClientApi.getAppDomainId(), null, null, null, null, null);
            for (TopicAddressEnum enumObject : response.getData()) {
                updateLatestEnumVersionToReleased(enumObject.getName());
            }
        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            fail(exc.getLocalizedMessage());
        }
    }

    // ALL EVENTS
    @Test
    public void updateAllLatestEventVersionsToReleased(
    ) throws Exception
    {
        EventPortalClientApi epClientApi = getEventPortalClientApi();
        EventsApi eventsApi = new EventsApi(epClientApi.getApiClient());
        EventsResponse response = eventsApi.getEvents(100, 1, null, null, null, epClientApi.getAppDomainId(), null, null, null, null);
        for (Event event : response.getData()) {
            updateLatestEventVersionToReleased(event.getName());
        }
    }

    // ALL APPLICATIONS
    @Test
    public void updateAllLatestApplicationVersionsToReleased(
    ) throws Exception
    {
        EventPortalClientApi epClientApi = getEventPortalClientApi();
        ApiClient apiClient = epClientApi.getApiClient();
        ApplicationsApi applicationsApi = new ApplicationsApi(apiClient);
        try {
            ApplicationsResponse response = applicationsApi.getApplications(100, 1, null, epClientApi.getAppDomainId(), null, null, null, null);
            for (Application appObject : response.getData()) {
                updateLatestApplicationVersionToReleased(appObject.getName());
            }
        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            fail(exc.getLocalizedMessage());
        }
    }



    public static void updateLatestSchemaVersionToReleased(
        final String schemaName ) throws Exception
    {
        EventPortalClientApi epClientApi = getEventPortalClientApi();
        ApiClient apiClient = epClientApi.getApiClient();
        SchemaObject schemaObject = epClientApi.getSchemaObjectByName(schemaName);
        SchemasApi schemasApi = new SchemasApi(apiClient);
        try {
            if (schemaObject == null) {
                log.info("Schema Object [{}] not found", schemaName);
                return;
            } else {
                SchemaVersionsResponse response = schemasApi.getSchemaVersions(1, 1, Set.of(schemaObject.getId()), null, null);
                if (response.getData().size() == 0) {
                    log.info("Version not found for schema [{}]", schemaName);
                    return;
                }
                SchemaVersion version = response.getData().get(0);
                if (version.getStateId().contentEquals("1")) {
                    schemasApi.updateSchemaVersionState(version.getId(), getVersionUpdateRequest());
                }
            }
        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            return;
        }
    }

    public static void updateLatestEnumVersionToReleased(
        final String enumName ) throws Exception
    {
        EventPortalClientApi epClientApi = getEventPortalClientApi();
        ApiClient apiClient = epClientApi.getApiClient();
        TopicAddressEnum enumObject = epClientApi.getTopicAddressEnumByName(enumName);
        EnumsApi enumsApi = new EnumsApi(apiClient);
        try {
            if (enumObject == null) {
                log.info("Enum Object [{}] not found", enumName);
                return;
            } else {
                TopicAddressEnumVersionsResponse response = enumsApi.getEnumVersions(1, 1, Set.of(enumObject.getId()), null);
                if (response.getData().size() == 0) {
                    log.info("Version not found for Enum [{}]", enumName);
                    return;
                }
                TopicAddressEnumVersion version = response.getData().get(0);
                if (version.getStateId().contentEquals("1")) {
                    enumsApi.updateEnumVersionState(version.getId(), getVersionUpdateRequest());
                }
            }
        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            return;
        }
    }

    public static void updateLatestEventVersionToReleased(
        final String eventName ) throws Exception
    {
        EventPortalClientApi epClientApi = getEventPortalClientApi();
        ApiClient apiClient = epClientApi.getApiClient();
        Event eventObject = epClientApi.getEventByName(eventName);
        EventsApi eventsApi = new EventsApi(apiClient);
        try {
            if (eventObject == null) {
                log.info("Event Object [{}] not found", eventName);
                return;
            } else {
                EventVersionsResponse response = eventsApi.getEventVersions(1, 1, Set.of(eventObject.getId()), null, null, null, null);
                if (response.getData().size() == 0) {
                    log.info("Version not found for Event [{}]", eventName);
                    return;
                }
                EventVersion version = response.getData().get(0);

                SchemasApi schemasApi = new SchemasApi(apiClient);
                EnumsApi enumsApi = new EnumsApi(apiClient);
                if (version.getSchemaVersionId() != null) {
                    schemasApi.updateSchemaVersionState(version.getSchemaVersionId(), getVersionUpdateRequest());
                }
                for (AddressLevel level : version.getDeliveryDescriptor().getAddress().getAddressLevels()) {
                    if (level.getEnumVersionId() != null) {
                        enumsApi.updateEnumVersionState(level.getEnumVersionId(), getVersionUpdateRequest());
                    }
                }

                if (version.getStateId().contentEquals("1")) {
                    eventsApi.updateEventVersionState(version.getId(), getVersionUpdateRequest());
                }
            }
        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            return;
        }
    }

    public static void updateLatestApplicationVersionToReleased(
        final String applicationName ) throws Exception
    {
        EventPortalClientApi epClientApi = getEventPortalClientApi();
        ApiClient apiClient = epClientApi.getApiClient();
        Application applicationObject = epClientApi.getApplicationByName(applicationName);
        ApplicationsApi applicationsApi = new ApplicationsApi(apiClient);
        try {
            if (applicationObject == null) {
                log.info("Application Object [{}] not found", applicationName);
                return;
            } else {
                ApplicationVersionsResponse response = applicationsApi.getApplicationVersions(1, 1, Set.of(applicationObject.getId()), null, null, null);
                if (response.getData().size() == 0) {
                    log.info("Version not found for Application [{}]", applicationName);
                    return;
                }
                ApplicationVersion version = response.getData().get(0);


                List<String> eventNames = new ArrayList<>();
                EventsApi eventsApi = new EventsApi(apiClient);
                EventVersionsResponse evResponse = eventsApi.getEventVersions(100, 1, null, Set.copyOf(version.getDeclaredProducedEventVersionIds()), null, null, null);
                for(EventVersion ev : evResponse.getData()) {
                    EventResponse eResponse = eventsApi.getEvent(ev.getEventId());
                    eventNames.add(eResponse.getData().getName());
                }

                for (String name : eventNames) {
                    updateLatestEventVersionToReleased(name);
                }

                if (version.getStateId().contentEquals("1")) {
                    applicationsApi.updateApplicationVersionState(version.getId(), getVersionUpdateRequest());
                }
            }
        } catch (Exception exc) {
            System.out.println(exc.getLocalizedMessage());
            return;
        }
    }

    private static VersionedObjectStateChangeRequest getVersionUpdateRequest() {
        VersionedObjectStateChangeRequest vRequest = new VersionedObjectStateChangeRequest();
        vRequest.setStateId("2");
        return vRequest;
    }

    private static EventPortalClientApi getEventPortalClientApi() throws Exception
    {
        String bearerToken, basePath, appDomainName;

        try {
            bearerToken = ConfigManager.getProperty("ep.bearer_token");
            basePath = ConfigManager.getProperty("ep.baseurl");
            appDomainName = ConfigManager.getProperty("ep.appdomain.name");

            EventPortalClientApi clientApi = new EventPortalClientApi(
                getApiClient(bearerToken, basePath), 
                appDomainName, 
                EpNewVersionStrategy.MAJOR
            );
            return clientApi;
        } catch (Exception exc) {
            System.out.println("Failed to create clientApi");
            exc.printStackTrace();
            throw exc;
        }
    }

    public static ApiClient getApiClient(
        final String bearerToken,
        final String basePath
    ) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(basePath);
        HttpBearerAuth apiToken = (HttpBearerAuth)apiClient.getAuthentication("APIToken");
        apiToken.setBearerToken(bearerToken);
        return apiClient;
    }
}
