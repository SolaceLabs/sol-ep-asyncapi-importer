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

import lombok.Getter;

public class AsyncApiMessage extends AbstractAccessor {

    @Getter
    private String messageName;
    
    /**
     * Constructor for AsyncApiMessage
     * @param name - The message name
     * @param rootSpec - The root AsyncApi as JsonObject
     * @param leafSpec - The message as JsonObject
     */
    public AsyncApiMessage( final String name, final JsonObject rootSpec, final JsonObject leafSpec )
    {
        super(rootSpec, leafSpec);
        this.messageName = name;
    }

    public String getName() throws Exception
    {
        return getStringFieldByName("name");
    }

    public String getTitle() throws Exception
    {
        return getStringFieldByName("title");
    }

    public String getSummary() throws Exception
    {
        return getStringFieldByName("summary");
    }

    /**
     * Returns field 'contentType' from the message. If 'contentType' is not found,
     * then returns 'defaultContentType' field from the spec root.
     * Returns NULL if neither values if found.
     * @return
     * @throws Exception
     */
    public String getContentType() throws Exception
    {
        final String messageContentType = getStringFieldByName("contentType");
        if (messageContentType != null && ! messageContentType.isBlank()) {
            return messageContentType;
        }
        return getStringFieldByReference("#/defaultContentType");
    }
}
