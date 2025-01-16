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

package com.solace.ep.asyncapi.accessor.v3;

import com.google.gson.JsonObject;
import com.solace.ep.asyncapi.accessor.v3.solace.EpFieldConstants;

public class AsyncApiInfo extends AbstractAccessor {
    
    /**
     * Public constructor - requires the asyncapi 'info' object as JsonObject
     * @param info
     */
    public AsyncApiInfo( JsonObject info ) {
        super(null, info);
        if (info == null) {
            throw new IllegalArgumentException( "AsyncApi [info] block cannot be null" );
        }
    }

    public String getEpApplicationVersion() throws Exception {
        return getStringFieldByName(EpFieldConstants.EP_APPLICATION_VERSION);
    }

    public String getEpApplicationVersionId() throws Exception {
        return getStringFieldByName(EpFieldConstants.EP_APPLICATION_VERSION_ID);
    }

    public String getEpApplicationId() throws Exception {
        return getStringFieldByName(EpFieldConstants.EP_APPLICATION_ID);
    }

    public String getEpStateName() throws Exception {
        return getStringFieldByName(EpFieldConstants.EP_STATE_NAME);
    }

    public String getEpStateId() throws Exception {
        return getStringFieldByName(EpFieldConstants.EP_STATE_ID);
    }

    public String getEpApplicationDomainId() throws Exception {
        return getStringFieldByName(EpFieldConstants.EP_APPLICATION_DOMAIN_ID);
    }

    public String getEpApplicationDomainName() throws Exception {
        return getStringFieldByName(EpFieldConstants.EP_APPLICATION_DOMAIN_NAME);
    }
    
    public String getDescription() throws Exception {
        return getStringFieldByName(AsyncApiFieldConstants.INFO_DESCRIPTION);
    }

    public String getTitle() throws Exception {
        return getStringFieldByName(AsyncApiFieldConstants.INFO_TITLE);
    }

    public String getVersion() throws Exception {
        return getStringFieldByName(AsyncApiFieldConstants.INFO_VERSION);
    }

}
