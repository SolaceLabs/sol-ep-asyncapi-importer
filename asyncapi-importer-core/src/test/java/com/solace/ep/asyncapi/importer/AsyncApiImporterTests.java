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

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Conduct JUnit tests to import various AsyncApi spec files
 */
public class AsyncApiImporterTests {

    public static final String ASYNCAPI_SHIPPING_SVC_0_1_0 = "src/test/resources/asyncapi/ShippingService-0.1.0.yaml";
    public static final String ASYNCAPI_SHIPPING_SVC_0_1_1 = "src/test/resources/asyncapi/ShippingService-0.1.1.yaml";
    public static final String ASYNCAPI_SHIPPING_SVC_0_1_2 = "src/test/resources/asyncapi/ShippingService-0.1.2.yaml";
    public static final String ASYNCAPI_SHIPPING_SVC_0_2_0 = "src/test/resources/asyncapi/ShippingService-0.2.0.yaml";
    public static final String ASYNCAPI_SHIPPING_SVC_0_2_1 = "src/test/resources/asyncapi/ShippingService-0.2.1.yaml";
    public static final String ASYNCAPI_SHIPPING_SVC_0_2_2 = "src/test/resources/asyncapi/ShippingService-0.2.2.yaml";

    public static final String ASYNCAPI_SHIPPING_SVC_ALT_1_0_0 = "src/test/resources/asyncapi/ShippingServiceAlt-1.0.0.yaml";

    public static final String ASYNCAPI_FLIGHTSVC_0_1_0 = "src/test/resources/asyncapi/FlightSvc-0.1.0.yaml";
    public static final String ASYNCAPI_FLIGHTSVC_0_1_1 = "src/test/resources/asyncapi/FlightSvc-0.1.1.yaml";
    public static final String ASYNCAPI_FLIGHTSVC_0_1_2 = "src/test/resources/asyncapi/FlightSvc-0.1.2.yaml";
    public static final String ASYNCAPI_FLIGHTSVC_0_2_0 = "src/test/resources/asyncapi/FlightSvc-0.2.0.yaml";
    public static final String ASYNCAPI_FLIGHTSVC_0_2_1 = "src/test/resources/asyncapi/FlightSvc-0.2.1.yaml";

    @Test
    @Order(1)
    public void asyncApiImporter1()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_SHIPPING_SVC_0_1_0);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    @Order(2)
    public void asyncApiImporter2()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_SHIPPING_SVC_0_1_1);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    @Order(3)
    public void asyncApiImporter3()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_SHIPPING_SVC_0_1_2);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            exc.printStackTrace();
            System.out.println(exc.getMessage());
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    @Order(4)
    public void asyncApiImporter2_0()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_SHIPPING_SVC_0_2_0);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            exc.printStackTrace();
            System.out.println(exc.getMessage());
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    @Order(5)
    public void asyncApiImporter2_1()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_SHIPPING_SVC_0_2_1);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            exc.printStackTrace();
            System.out.println(exc.getMessage());
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    @Order(6)
    public void asyncApiImporter2_2()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_SHIPPING_SVC_0_2_2);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            exc.printStackTrace();
            System.out.println(exc.getMessage());
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    @Order(7)
    public void asyncApiImporter_Alt_1_0()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_SHIPPING_SVC_ALT_1_0_0);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            exc.printStackTrace();
            System.out.println(exc.getMessage());
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    @Order(8)
    public void importFlight_0_1_0()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_FLIGHTSVC_0_1_0);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
        }
    }
    
    @Test
    @Order(9)
    public void importFlight_0_1_1()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_FLIGHTSVC_0_1_1);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    @Order(10)
    public void importFlight_0_1_2()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_FLIGHTSVC_0_1_2);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    @Order(11)
    public void importFlight_0_2_0()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_FLIGHTSVC_0_2_0);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
        }
    }

    @Test
    @Order(12)
    public void importFlight_0_2_1()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String BASE_PATH = ConfigManager.getProperty("ep.baseurl");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile(ASYNCAPI_FLIGHTSVC_0_2_1);

        try {
            AsyncApiImporter asyncApiImporter = new AsyncApiImporter(APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, BASE_PATH, "MINOR");
            asyncApiImporter.execImportOperation();

        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
        }
    }
}
