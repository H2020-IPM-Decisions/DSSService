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

package net.ipmdecisions.dssservice.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;




/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SchemaUtils {
    
    public static final String JSON_V4_SCHEMA_IDENTIFIER = "http://json-schema.org/draft-04/schema#";
    public static final String JSON_SCHEMA_IDENTIFIER_ELEMENT = "$schema";
    
    /**
     * Validates the Json data against the Json schema at the given URL
     * @param schemaURL
     * @param jsonNode
     * @return
     * @throws IOException
     * @throws ProcessingException
     * @throws SchemaValidationException
     */
    public Boolean isJsonValid(URL schemaURL, JsonNode jsonNode) throws IOException, ProcessingException, SchemaValidationException
    {
        JsonSchema schemaNode = this.getSchemaNode(JsonLoader.fromURL(schemaURL));
        return this.isJsonValid(schemaNode, jsonNode);
    }
    
    /**
     * Validates the Json data against the Json schema (given as a string)
     * @param schema
     * @param jsonNode
     * @return
     * @throws IOException
     * @throws ProcessingException
     * @throws SchemaValidationException
     */
    public Boolean isJsonValid(String schema, JsonNode jsonNode) throws IOException, ProcessingException, SchemaValidationException
    {
        JsonSchema schemaNode = this.getSchemaNode(JsonLoader.fromString(schema));
        return this.isJsonValid(schemaNode, jsonNode);
    }
    
    /**
     * Validates the Json data against the given Json schema
     * @param schemaNode
     * @param jsonNode
     * @return
     * @throws ProcessingException
     * @throws SchemaValidationException
     */
    public Boolean isJsonValid(JsonSchema schemaNode, JsonNode jsonNode) throws ProcessingException, SchemaValidationException
    {
        ProcessingReport report = schemaNode.validate(jsonNode);
        String processingMessage = "";
        for(ProcessingMessage m:report)
        {
            if(m.getLogLevel().equals(LogLevel.ERROR))
            {
            	processingMessage += m.getLogLevel().toString().toUpperCase() + ": " + m.getMessage();
                System.out.println(m.getMessage() + ": " + m.getLogLevel());
                //System.out.println(m.toString());
            }
        }
        if(report.isSuccess())
        {
        	return true;
        }
        else
        {
        	throw new SchemaValidationException(processingMessage);
        }
    }
    
    /**
     * Validates the Json data against the given Json schema
     * @param schemaNode
     * @param jsonNode
     * @return
     * @throws ProcessingException
     * @throws SchemaValidationException
     */
    public Boolean isJsonValid(JsonNode schemaNode, JsonNode jsonNode) throws ProcessingException, SchemaValidationException
    {
        JsonSchema s = this.getSchemaNode(jsonNode);
        return this.isJsonValid(s, jsonNode);
    }
    
    /**
     * Create a JsonSchema from the Json structure
     * @param jsonNode
     * @return
     * @throws ProcessingException 
     */
    public JsonSchema getSchemaNode(JsonNode jsonNode) throws ProcessingException
    {
        JsonNode schemaIdentifier = jsonNode.get(SchemaUtils.JSON_SCHEMA_IDENTIFIER_ELEMENT);
        if(schemaIdentifier == null)
        {
            ((ObjectNode)jsonNode).put(SchemaUtils.JSON_SCHEMA_IDENTIFIER_ELEMENT, SchemaUtils.JSON_V4_SCHEMA_IDENTIFIER);
            
        }
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        return factory.getJsonSchema(jsonNode);
    }
    
    public Boolean isValidJsonSchema(JsonNode jsonNode) throws SchemaValidationException
    {
    	JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    	SyntaxValidator syntaxValidator = factory.getSyntaxValidator();
    	if(syntaxValidator.schemaIsValid(jsonNode))
    	{
    		return true;
    	}
    	ProcessingReport pReport = syntaxValidator.validateSchema(jsonNode);
    	throw new SchemaValidationException(pReport.toString());
    }
    
    
    public JsonNode getJsonFromInputStream(InputStream inputStream) throws IOException
    {
        
            JsonFactory f = new MappingJsonFactory();
            JsonParser jp = f.createParser(inputStream);
            JsonNode all = jp.readValueAsTree();
            return all;
    }
    
    public JsonNode getJsonFromString(String inputString) throws IOException
    {
    	JsonFactory f = new MappingJsonFactory();
    	JsonParser jp = f.createParser(inputString);
    	JsonNode all = jp.readValueAsTree();
    	return all;
    }
    
    public String getSchemaAsString(String url)
    {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String schema = "";
            String line;
            while((line = in.readLine()) != null)
            {
                schema += line;
            }
            return schema;
        } catch (IOException  ex) {
            return ex.getMessage();
        }
    }
}
