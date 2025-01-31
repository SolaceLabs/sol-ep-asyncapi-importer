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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.solace.ep.asyncapi.importer.mapper.AsyncApiV2ToDto;
import com.solace.ep.asyncapi.accessor.v2.AsyncApiAccessor;
import com.solace.ep.asyncapi.importer.model.dto.DtoResultSet;

/**
 * Test
 */
public class AsyncApiV2ToDtoMapperTests {

    public static final String ASYNCAPI_SHIPPING_SVC_0_1_0 = "src/test/resources/asyncapi/ShippingService-0.1.0.yaml";

    @Test
    public void testDebug() {

        try {
            AsyncApiAccessor asyncApiAccessor = new AsyncApiAccessor( AsyncApiAccessor.parseAsyncApi(getAsyncApiFile(ASYNCAPI_SHIPPING_SVC_0_1_0)) );
            AsyncApiV2ToDto mapper = new AsyncApiV2ToDto(asyncApiAccessor, "fictionalId", "FictionalDomain" );
            DtoResultSet resultSet = mapper.mapAsyncApiToDto();

            assertTrue(resultSet.getMapEnums().size() > 0 );

        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Test
    public void testMapAsapioSalesOrder() {

        final String ASYNCAPI_ASAPIO_SALES_ORDER = "src/test/resources/asyncapi/asapio/sales-order.json";

        try {
            AsyncApiAccessor asyncApiAccessor = new AsyncApiAccessor( AsyncApiAccessor.parseAsyncApi(getAsyncApiFile(ASYNCAPI_ASAPIO_SALES_ORDER)) );
            AsyncApiV2ToDto mapper = new AsyncApiV2ToDto(asyncApiAccessor, "fictionalId", "FictionalDomain" );
            DtoResultSet resultSet = mapper.mapAsyncApiToDto();

            assertTrue(resultSet.getMapEnums().size() == 0 );
            assertTrue(resultSet.getMapApplications().size() ==1);
            assertTrue(resultSet.getMapSchemas().size() == 1);
            assertTrue(resultSet.getMapEvents().size() ==1);

        } catch (Exception exc) {
            fail(exc.getLocalizedMessage());
            exc.printStackTrace();
        }
    }

    public static String getAsyncApiFile(final String fileName) {

        Path path = Paths.get( fileName );  // Path to your file
        try {
            String content = Files.readString(path);
            // System.out.println(content);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
