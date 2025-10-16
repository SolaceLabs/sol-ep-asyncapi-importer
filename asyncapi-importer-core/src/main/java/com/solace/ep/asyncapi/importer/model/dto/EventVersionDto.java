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

import com.solace.cloud.ep.designer.model.EventVersion;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * EventVersionDto represents an EP Event for import.
 * `epEventVersion` is the EP matched Event version or 
 * latest Event version if it exists
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class EventVersionDto extends AbstractVersionDto {
    
    private String eventId;

    private String schemaVersionId;

    private String schemaPrimitiveType;

    private DeliveryDescriptor deliveryDescriptor;

    private String type = "eventVersion";

    private SchemaVersionDto schemaVersionDto;

    private String topicAddressPattern;

    private EventVersion epEventVersion;

    public DeliveryDescriptor getDeliveryDescriptor() {
        if (this.deliveryDescriptor == null) {
            this.deliveryDescriptor = new DeliveryDescriptor();
        }
        return this.deliveryDescriptor;
    }

    @Data
    public static class TopicAddressLevel {
        
        private String name;

        private String addressLevelType;

        private Boolean hasEnum = false;

        private String enumVersionId;

        private EnumVersionDto enumVersionDto;

    }

    @Data
    public static class TopicAddress {

        private String id;

        private List<TopicAddressLevel> addressLevels;
        
        private String addressType;

        private String type = "address";

        public List<TopicAddressLevel> getAddressLevels() {
            if (this.addressLevels == null) {
                this.addressLevels = new ArrayList<>();
            }
            return this.addressLevels;
        }
    }

    @Data
    public static class DeliveryDescriptor {

        private String brokerType = "solace";

        private TopicAddress address;

        public TopicAddress getAddress() {
            if (this.address == null) {
                this.address = new TopicAddress();
            }
            return this.address;
        }
    }
}
