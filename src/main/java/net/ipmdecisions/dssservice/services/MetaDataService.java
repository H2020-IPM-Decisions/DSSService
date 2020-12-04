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

package net.ipmdecisions.dssservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.ipmdecisions.dssservice.entity.FieldObservation;
import net.ipmdecisions.dssservice.entity.ModelOutput;
import net.ipmdecisions.dssservice.util.SchemaProvider;
import net.ipmdecisions.dssservice.util.SchemaUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.HttpRequest;

/**
 * Provides schemas and validation thereof
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest")
public class MetaDataService {
    @Context
    private HttpRequest httpRequest;
    @Context
    private HttpServletRequest httpServletRequest;
    
    private final JsonSchemaGenerator schemaGen;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public MetaDataService()
    {
        super();
        // Documentation found here: https://github.com/mbknor/mbknor-jackson-jsonSchema
        
        Map<String,String> customFormatMapping = new HashMap<>(); 
        customFormatMapping.put(Instant.class.getName(), "date-time");
        Map<Class<?>,Class<?>> customClassMapping = new HashMap<>();
        customClassMapping.put(Instant.class, String.class);    
        JsonSchemaConfig config = JsonSchemaConfig.create(
            JsonSchemaConfig.vanillaJsonSchemaDraft4().autoGenerateTitleForProperties(), 
            Optional.empty(), 
            JsonSchemaConfig.nullableJsonSchemaDraft4().useOneOfForOption(), 
            JsonSchemaConfig.nullableJsonSchemaDraft4().useOneOfForNullables(), 
            JsonSchemaConfig.vanillaJsonSchemaDraft4().usePropertyOrdering(), 
            JsonSchemaConfig.vanillaJsonSchemaDraft4().hidePolymorphismTypeProperty(), 
            JsonSchemaConfig.vanillaJsonSchemaDraft4().disableWarnings(), 
            JsonSchemaConfig.vanillaJsonSchemaDraft4().useMinLengthForNotNull(), 
            JsonSchemaConfig.vanillaJsonSchemaDraft4().useTypeIdForDefinitionName(), 
            customFormatMapping, 
            JsonSchemaConfig.vanillaJsonSchemaDraft4().useMultipleEditorSelectViaProperty(), 
            new HashSet<>(), 
            customClassMapping, 
            new HashMap<>(),
            null,
            false,
            null
        );
        
        schemaGen = new JsonSchemaGenerator(objectMapper,config);
    }

    /**
     * This returns the generic schema for field observations, containing the common
     * properties for field observations. These are location (GeoJson), time 
     * (ISO-8859 datetime), EPPO Code for the pest and crop. In addition, quantification
     * information must be provided. This is specified in a custom schema, which
     * must be a part of the input_schema property in the DSS model metadata. So 
     * in the configParameters property of the input_schema for a model, we may find:
     * <pre>
        "fieldObservations": {
            "title": "Field observations",
            "type": "array",
            "items": {
                "$ref": "https://ipmdecisions.nibio.no/api/dss/rest/schema/fieldobservation"
            }
        },
        "fieldObservationQuantifications": {
            "title": "Field observation quantifications",
            "type": "array",
            "items": {
              "oneOf": [
                {
                  "$ref": "#/definitions/fieldObs_SEPTAP"
                }
              ]
            }
        }
     * </pre>
     * 
     * Both fieldObservations and fieldObservationQuantifications are arrays, so
     * for each fieldObservation object you must have a corresponding 
     * quantification object. So fieldObservations[0] corresponds to 
     * fieldObservationQuantifications[0] and so on.
     * 
     * The actual contents of the field observation quantification object is
     * structured by an entry in the definitions section of the input_schema. So
     * in our specific example, you will find:
     * 
     * <pre>
     *  "definitions": {
          "fieldObs_SEPTAP": {
            "title": "Septoria apiicola quantification", 
            "properties": {
              "observed":{
                "title":"observed", 
                "type":"boolean"
              }
            }
        }
     * </pre>
     * 
     * Which is, admittedly, a very simple quantification. For the form builder, 
     * the EPPO code for the pest is specified in the fieldObs_SEPTAP (after the 
     * underscore). This should be sufficient for the form to build the correct
     * quantification fields when the user selects the pest (if observations
     * of more than one pest species are required/possible)
     * 
     * @return The generic schema for field observations
     */
    @GET
    @Path("schema/fieldobservation")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getFieldObservationSchema()
    {
        JsonNode schema = schemaGen.generateJsonSchema(FieldObservation.class);
        return Response.ok().entity(schema).build();
    }
    
    
    /**
     * 
     * @return The Json Schema for the platform's standard for DSS model output
     */
    @GET
    @Path("schema/modeloutput")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getModelOutputSchema()
    {
    	ObjectMapper m = new ObjectMapper();
        JsonNode schema = schemaGen.generateJsonSchema(ModelOutput.class);
        // Manually add some properties
        JsonNode warningStatus = schema.findValue("warningStatus");
        for(JsonNode jn:warningStatus.get("oneOf"))
        {
        	if(jn.get("type").asText().equals("array"))
        	{
        		ObjectNode items = (ObjectNode) jn.get("items");
        		items.put("minimum","0");
        		items.put("maximum","4");
        	}
        }
        
        return Response.ok().entity(schema).build();
    }
    
    /**
     * Validate model output against this schema: https://ipmdecisions.nibio.no/api/dss/rest/schema/modeloutput
     * @param modelOutputData
     * @return <code>{"isValid":"true"}</code> if the data is valid, <code>{"isValid":"false"}</code> otherwise
     * @responseExample application/json {"isValid":"true"}
     */
    @POST
    @Path("schema/modeloutput/validate")
    @GZIP
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateModelOutputData(JsonNode modelOutputData)
    {
        try 
        {
            SchemaUtils sUtils = new SchemaUtils();
            boolean isValid; 
            URL schemaURL = new URL("https://ipmdecisions.nibio.no/api/dss/rest/schema/modeloutput");
            isValid = sUtils.isJsonValid(schemaURL, modelOutputData);
            return Response.ok().entity(Map.of("isValid", isValid)).build();
        } catch (IOException | ProcessingException  ex) 
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    /**
     * Validate DSS YAML description file, using this Json schema: https://ipmdecisions.nibio.no/api/dss/rest/schema/dss
     * @param modelOutputData
     * @return <code>{"isValid":"true"}</code> if the data is valid, <code>{"isValid":"false"}</code> otherwise
     * @responseExample application/json {"isValid":"true"}
     */
    @POST
    @Path("schema/dss/yaml/validate")
    @GZIP
    @Consumes("application/x-yaml;charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateDSSYAMLFile(String DSSYAMLFile)
    {
        try
        {
            ObjectMapper YAMLReader = new ObjectMapper(new YAMLFactory());
            JsonNode j = YAMLReader.readTree(DSSYAMLFile);
            JsonNode schema = SchemaProvider.getDSSSchema();
            SchemaUtils sUtils = new SchemaUtils();
            boolean isValid = sUtils.isJsonValid(schema.toString(), j);
            return Response.ok().entity(Map.of("isValid", isValid)).build();
        }
        catch(ProcessingException | IOException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
        
    }
    
    
    @GET
    @Path("schema/dss")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getDSSSchema()
    {
        try
        {
            return Response.ok().entity(SchemaProvider.getDSSSchema()).build();
        }
        catch(IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    /*
    This takes some more work due to the polymorphism of the FieldObservation Object
    @POST
    @Path("schema/fieldobservation/validate")
    @GZIP
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateFieldObservationData(JsonNode fieldObservationData)
    {
        try
        {
            JsonNode schema = schemaGen.generateJsonSchema(FieldObservation.class);
            SchemaUtils sUtils = new SchemaUtils();
            // If we don't create a string from the schema, it will always pass with a well formed but non conforming JSON document
            // Don't ask me why!!!
            boolean isValid = sUtils.isJsonValid(schema.toString(), fieldObservationData); 
            return Response.ok().entity(Map.of("isValid", isValid)).build();
        }
        catch(ProcessingException | IOException ex)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }
*/
}
