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

import java.time.Instant;
import org.locationtech.jts.geom.Geometry;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import javax.validation.constraints.NotNull;

/**
 * "fieldObservations": {
             "$ref": "https://ipmdecisions.nibio.no/DSSService/rest/schema/fieldobservations"
          },
          "fieldObsQuantification":{
          },
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@JsonSchemaInject(strings = {
    @JsonSchemaString(path = "$id", value="https://ipmdecisions.nibio.no/DSSService/rest/schema/fieldobservation")
    }
)
@JsonSchemaTitle("Field observation")
@JsonSchemaExamples("TODO")
@JsonSchemaDescription("Version 0.1. The schema describes the field observation format for the IPM Decisions platform. See an example here: TODO")
public class FieldObservation {
    
    @JsonSchemaTitle("")
    private Geometry location;
    @NotNull
    private Instant time;
    private String pestEPPOCode, cropEPPOCode, quantification;
}
