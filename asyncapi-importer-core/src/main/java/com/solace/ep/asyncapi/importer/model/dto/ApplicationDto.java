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

package com.solace.ep.asyncapi.importer.model.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 
 * Internal Format for Event Portal Application Objects
 * Application objects may contain versions
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class ApplicationDto extends AbstractDtoObject {
    
    private String applicationType = "standard";

    private String brokerType = "solace";

    private String type = "application";

    private List<ApplicationVersionDto> applicationVersions;

    public List<ApplicationVersionDto> getApplicationVersions() {
        if ( this.applicationVersions == null ) {
            this.applicationVersions = new ArrayList<>();
        }
        return this.applicationVersions;
    }

    @Override
    public int getNumberOfVersions()
    {
        return ( this.getApplicationVersions() == null ) ? 0 : this.getApplicationVersions().size();
    }
}
