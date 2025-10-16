package com.solace.ep.asyncapi.importer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.solace.ep.asyncapi.importer.helper.HelperFunctions;
import com.solace.ep.asyncapi.importer.model.dto.AbstractDtoObject;
import com.solace.ep.asyncapi.importer.model.dto.ApplicationDomainDto;
import com.solace.ep.asyncapi.importer.model.dto.DtoResultSet;
import com.solace.ep.asyncapi.importer.model.dto.EnumDto;
import com.solace.ep.asyncapi.importer.model.dto.EventDto;
import com.solace.ep.asyncapi.importer.model.dto.SchemaDto;

/**
 * Test aysyncapi import operations to 'test-importer' domain
 */
public class AsyncApi_test_importer {
    
    @Test
    @Order(100)
    public void importOnlineStore_00_init()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile("src/test/resources/asyncapi/test-importer/OnlineStore-00-INIT.yaml");

        try {
            AsyncApiImporter.execImportOperation(null, APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, null, "MAJOR");
        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
            return;
        }
        assertTrue(true);
    }

    @Test
    @Order(110)
    public void releaseInventoryHeldEvent() {
        HelperFunctions helperFunctions = new HelperFunctions();
        helperFunctions.releaseInventoryHeldEvent();
    }

    @Test
    @Order(120)
    public void importFulfillment_00_init()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile("src/test/resources/asyncapi/test-importer/Fullfillment-00-INIT.yaml");

        try {
            AsyncApiImporter.execImportOperation(null, APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, null, "MINOR");
        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
            return;
        }
        assertTrue(true);
    }

    @Test
    @Order(130)
    public void releaseFulfillmentApp() {
        HelperFunctions helperFunctions = new HelperFunctions();
        helperFunctions.releaseFulfillmentApp();
    }

    @Test
    @Order(140)
    public void importOnlineStore_01()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile("src/test/resources/asyncapi/test-importer/OnlineStore-01.yaml");

        try {
            AsyncApiImporter.execImportOperation(null, APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, null, "MAJOR");
        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
            return;
        }
        assertTrue(true);
    }

    @Test
    @Order(150)
    public void releaseFulfillmentAndOnlineStoreApps() {
        HelperFunctions helperFunctions = new HelperFunctions();
        helperFunctions.releaseFulfillmentApp();
        helperFunctions.releaseOnlineStoreApp();
    }

    @Test
    @Order(160)
    public void importFulfillment_01()
    {
        final String BEARER_TOKEN = ConfigManager.getProperty("ep.bearer_token");
        final String APP_DOMAIN_NAME = ConfigManager.getProperty("ep.appdomain.name");
        final String asyncApiSpecFile = AsyncApiV2ToDtoMapperTests.getAsyncApiFile("src/test/resources/asyncapi/test-importer/Fullfillment-01.yaml");

        try {
            AsyncApiImporter.execImportOperation(null, APP_DOMAIN_NAME, BEARER_TOKEN, asyncApiSpecFile, null, "MAJOR");
        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
            return;
        }
        assertTrue(true);
    }

}
