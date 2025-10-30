package com.solace.ep.asyncapi.importer.model.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class EventApiDto extends AbstractDtoObject {

    private String brokerType = "solace";

    private String type = "eventapi";

    private List<EventApiVersionDto> eventApiVersions;

    public List<EventApiVersionDto> getEventApiVersions() {
        if ( this.eventApiVersions == null ) {
            this.eventApiVersions = new ArrayList<>();
        }
        return this.eventApiVersions;
    }

    @Override
    public int getNumberOfVersions()
    {
        return ( this.getEventApiVersions() == null ) ? 0 : this.getEventApiVersions().size();
    }

}
