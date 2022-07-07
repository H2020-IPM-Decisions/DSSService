/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of IPMDecisionsDSSService.
 * IPMDecisionsDSSService is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * IPMDecisionsDSSService is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with IPMDecisionsDSSService.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.ipmdecisions.dssservice.entity;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.time.Instant;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import javax.validation.constraints.NotNull;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@JsonSchemaInject(strings = {
    @JsonSchemaString(path = "$id", value="https://platform.ipmdecisions.net/api/dss/rest/schema/fieldobservation"),
    }
)
@JsonSchemaTitle("Field observation")
@JsonSchemaExamples("TODO")
@JsonSchemaDescription("Version 0.1. The schema describes the field observation format for the IPM Decisions platform. See an example here: TODO")
public class FieldObservation {
    
    @NotNull
    @JsonSchemaTitle("Location  of the observation. In GeoJson format.")
    @JsonSchemaInject(strings = {
        @JsonSchemaString(path = "$ref", value="https://platform.ipmdecisions.net/api/dss/rest/schema/geojson")
        }
    )
    private Object location;
    @NotNull
    @JsonSchemaTitle("Time (yyyy-MM-dd'T'HH:mm:ssXXX)")
    @JsonPropertyDescription("The timestamp of the field observation. Format: \"yyyy-MM-dd'T'HH:mm:ssXXX\", e.g. 2020-04-09T18:00:00+02:00")
    private Instant time;
    @NotNull
    @JsonSchemaTitle("Pest")
    @JsonPropertyDescription("The EPPO code for the observed pest. See https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes")
    private String pestEPPOCode;
    @NotNull
    @JsonSchemaTitle("Crop")
    @JsonPropertyDescription("The EPPO code for the crop in which the pest was observed. See https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes")
    private String cropEPPOCode;
    //@JsonSchemaTitle("Quantification")
    //@JsonPropertyDescription("Json formatted quantification data for the observation. ")
    //private String quantification;

    

    /**
     * @return the time
     */
    public Instant getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(Instant time) {
        this.time = time;
    }

    /**
     * @return the pestEPPOCode
     */
    public String getPestEPPOCode() {
        return pestEPPOCode;
    }

    /**
     * @param pestEPPOCode the pestEPPOCode to set
     */
    public void setPestEPPOCode(String pestEPPOCode) {
        this.pestEPPOCode = pestEPPOCode;
    }

    /**
     * @return the cropEPPOCode
     */
    public String getCropEPPOCode() {
        return cropEPPOCode;
    }

    /**
     * @param cropEPPOCode the cropEPPOCode to set
     */
    public void setCropEPPOCode(String cropEPPOCode) {
        this.cropEPPOCode = cropEPPOCode;
    }

    /**
     * @return the quantification
     *
    public String getQuantification() {
        return quantification;
    }*/

    /**
     * @param quantification the quantification to set
     *
    public void setQuantification(String quantification) {
        this.quantification = quantification;
    }*/

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
