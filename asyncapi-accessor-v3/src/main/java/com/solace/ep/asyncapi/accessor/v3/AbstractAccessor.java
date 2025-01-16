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

import java.util.StringTokenizer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAccessor {
    
    // Top level JsonObject for AsyncApi spec
    protected JsonObject rootSpec;

    // Level of current instantiated object.
    // e.g. info, channel, message object, etc.
    protected JsonObject leafSpec;

    /**
     * Use this constructor for any lower levels than root
     * rootSpec - The entire AsyncApi specification
     * leafSpec - Child Json object of interest
     */
    protected AbstractAccessor( final JsonObject rootSpec, final JsonObject leafSpec ) {
        this.rootSpec = rootSpec;
        this.leafSpec = leafSpec;
    }

    /**
     * Use this constructor for AsyncApiAccessor; Leaf nodes should use constructor
     * where the root and leaf can be specified independently.
     * rootSpec - The entire AsyncApi specification
     * leafSpec - Child Json object of interest
     * @param rootAndLeafSpec
     */
    protected AbstractAccessor( final JsonObject rootAndLeafSpec ) {
        this.rootSpec = rootAndLeafSpec;
        this.leafSpec = rootAndLeafSpec;
    }

    /**
     * Handle JsonObjects that may have $ref elements - resolves $ref if present
     * @param jsonObjectToResolve
     * @return
     * @throws Exception
     */
    public JsonObject getResolvedJsonObject( JsonObject jsonObjectToResolve ) throws Exception
    {
        if (jsonObjectToResolve == null) {
            return null;
        }
        if (jsonObjectToResolve.has("$ref")) {
            final String refPath = jsonObjectToResolve.get("$ref").getAsString();
            return getObjectFieldByReference(refPath);
        } else {
            return jsonObjectToResolve;
        }
    }

    /**
     * Return a referenced field as JsonObject
     * Reference path form: #/path/to/field - Reference will be resolved from AsyncApi root
     * Reference path form: path/to/field - Reference will be resolved at current object level
     * @param referencePath
     * @return
     * @throws Exception
     */
    public JsonObject getObjectFieldByReference( final String referencePath ) throws Exception {

        JsonObject node = null;
        final StringTokenizer t = new StringTokenizer(referencePath, "/");

        boolean firstToken = true;
        while ( t.hasMoreTokens() ) {
            String s = t.nextToken();
            if (firstToken) {
                if ( s.contentEquals("#")) {
                    node = this.rootSpec;
                    continue;
                } else {
                    node = this.leafSpec;
                }
                firstToken = false;
            }
            if ( !node.has(s) ) {
                throw new Exception(String.format(
                                    "Could not find element [%s] in reference path: %s",
                                    s,
                                    referencePath));
            }
            node = node.getAsJsonObject( s );
        }
        return node;
    }

    /**
     * Return a field of the current object by name as JsonObject
     * @param fieldName
     * @return
     * @throws Exception
     */
    public JsonObject getObjectFieldByName(final String fieldName) throws Exception
    {
        final JsonElement element = leafSpec.get(fieldName);

        if (element == null) {
            return null;
        }
        try {
            return element.getAsJsonObject();
        } catch (Exception exc) {
            log.error("AbstractAccessor.getObjectFieldByName - Error retrieving field [{}] as JSON Object", exc.getLocalizedMessage(), exc);
            throw exc;
        }
    }

    /**
     * Return a field of the current object by name as String
     * @param fieldName
     * @return
     * @throws Exception
     */
    public String getStringFieldByName( final String fieldName ) throws Exception
    {
        final JsonElement element = leafSpec.get(fieldName);

        if (element == null) {
            return null;
        }

        try {
            return element.getAsString();
        } catch (Exception exc) {
            log.error("AbstractAccessor.getObjectFieldByName - Error retrieving field [{}] as String", exc.getLocalizedMessage(), exc);
            throw exc;
        }
    }

    /**
     * Return a referenced field as String
     * Reference path form: #/path/to/field - Reference will be resolved from AsyncApi root
     * Reference path form: path/to/field - Reference will be resolved at current object level
     * @param referencePath
     * @return
     * @throws Exception
     */
    public String getStringFieldByReference( final String referencePath ) throws Exception
    {
        final JsonObject element = getObjectFieldByReference(referencePath);

        if (element == null) {
            return null;
        }

        try {
            return element.getAsString();
        } catch (Exception exc) {
            log.error("AbstractAccessor.getStringFieldByReference - Error retrieving field [{}] as String", exc.getLocalizedMessage(), exc);
            throw exc;
        }
    }
}
