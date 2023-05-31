package net.ipmdecisions.dssservice.entity;

import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;

import javax.validation.constraints.NotNull;

public class FieldObservation extends FieldObservationNoLocation{
    @NotNull
    @JsonSchemaTitle("Location  of the observation. In GeoJson format.")
    @JsonSchemaInject(strings = {
            @JsonSchemaString(path = "$ref", value="https://geojson.org/schema/GeoJSON.json")
    }
    )
    private Object location;

    /**
     * @return the location
     */
    public Object getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Object location) {
        this.location = location;
    }
}
