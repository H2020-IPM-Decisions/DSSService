/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of IPMDecisionsDSSService.
 * IPMDecisionsWeatherService is free software: you can redistribute it and/or modify
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
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@JsonSchemaInject(
		strings = {
		    @JsonSchemaString(path = "$id", value="https://platform.ipmdecisions.net/api/dss/rest/schema/modeloutput"),
		    }
)
@JsonSchemaTitle("Model output")
@JsonSchemaExamples("TODO")
@JsonSchemaDescription("Version 0.1. The schema describes the model output format for the IPM Decisions platform. See an example here: TODO")
/**
 * 
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ModelOutput {
    @NotNull
    @JsonSchemaTitle("Time start (yyyy-MM-dd'T'HH:mm:ssXXX)")
    @JsonPropertyDescription("The timestamp of the first result. Format: \"yyyy-MM-dd'T'HH:mm:ssXXX\", e.g. 2020-04-09T18:00:00+02:00")
    private Instant timeStart;
    @NotNull
    @JsonSchemaTitle("Time end (yyyy-MM-dd'T'HH:mm:ssXXX)")
    @JsonPropertyDescription("The timestamp of the last result. Format: \"yyyy-MM-dd'T'HH:mm:ssXXX\", e.g. 2020-04-09T18:00:00+02:00")
    private Instant timeEnd;
    @NotNull
    @Positive
    @JsonSchemaTitle("Sampling frequency (seconds)")
    @JsonPropertyDescription("The sampling frequency in seconds. E.g. 3600 = hourly values")
    private Integer interval; 
    @NotNull
    @Size(min=1)
    @JsonSchemaTitle("Result parameter codes")
    @JsonPropertyDescription("Codes for the result parameters. They are unique to each model, and must be described in the DSS catalogue metadata property output->result_parameters).")
    private String[] resultParameters;
    @JsonSchemaTitle("Result data")
    @JsonPropertyDescription("The result data per location.")
    private List<LocationResult> locationResult;
    @JsonSchemaTitle("Message")
    @JsonPropertyDescription("Any message output from the model: Informations, warnings and error messages")
    private String message;
    @JsonSchemaTitle("Message type")
    @JsonPropertyDescription("0 = info, 1 = warning, 2 = error")
    private Integer messageType;

    /**
     * @return the timeStart
     */
    public Instant getTimeStart() {
        return timeStart;
    }
    
    
    /**
     * @param timeStart the timeStart to set
     */
    public void setTimeStart(Instant timeStart) {
        this.timeStart = timeStart;
    }

    /**
     * @return the timeEnd
     */
    public Instant getTimeEnd() {
        return timeEnd;
    }
    

    /**
     * @param timeEnd the timeEnd to set
     */
    public void setTimeEnd(Instant timeEnd) {
        this.timeEnd = timeEnd;
    }

    /**
     * @return the interval
     */
    public Integer getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * @return the resultParameters
     */
    public String[] getResultParameters() {
        return resultParameters;
    }

    /**
     * @param resultParameters the resultParameters to set
     */
    public void setResultParameters(String[] resultParameters) {
        this.resultParameters = resultParameters;
    }

    /**
     * @return the locationResult
     */
    public List<LocationResult> getLocationResult() {
        return locationResult;
    }

    /**
     * @param locationResult the locationResult to set
     */
    public void setLocationResult(List<LocationResult> locationResult) {
        this.locationResult = locationResult;
    }
    
    public void addLocationResult(LocationResult locationResult) {
        if (this.locationResult == null)
        {
            this.locationResult = new ArrayList<>();
        }
        this.locationResult.add(locationResult);
    }


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public Integer getMessageType() {
		return messageType;
	}


	public void setMessageType(Integer messageType) {
		this.messageType = messageType;
	}
}
