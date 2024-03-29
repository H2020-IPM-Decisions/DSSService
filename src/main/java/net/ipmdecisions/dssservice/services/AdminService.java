/*
 * Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
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


import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.annotations.GZIP;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.wnameless.json.flattener.JsonFlattener;

import net.ipmdecisions.dssservice.entity.DSS;
import net.ipmdecisions.dssservice.entity.DSSModel;
import net.ipmdecisions.dssservice.entity.DSSModel.Output.WarningStatusInterpretation;
import net.ipmdecisions.dssservice.util.MD5Encrypter;
import net.ipmdecisions.dssservice.controller.DSSController;


/**
 * Endpoints for admin operations
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */
@PermitAll
@Path("rest")
public class AdminService {
	
	@Context
    private HttpServletRequest httpServletRequest;
	
	
	
	// If this ever needs to be an EJB, simply annotate with @EJB
	// and remove the init in the constructor for this class
	private DSSController DSSController;
	
	public AdminService()
	{
		this.DSSController = new DSSController();
	}
	
	private Response unauthorizedResponse()
	{
		return Response.status(Status.UNAUTHORIZED).entity("You are not authorized to access this resource").build();
	}
	
	private boolean isAuthorized()
	{
		return httpServletRequest.getHeader("ipmdss_admin_token") != null
				&& MD5Encrypter.getMD5HexString(httpServletRequest.getHeader("ipmdss_admin_token"))
				.equals(System.getProperty("net.ipmdecisions.dssservice.IPMDSS_ADMIN_TOKEN_MD5"));
	}

	@GET
    @Path("admin/heartbeat")
    @Produces("application/json;charset=UTF-8")
    public Response heartbeat() {
		if( !this.isAuthorized() ) { return this.unauthorizedResponse(); }
        return Response.ok().entity("Alive and well").build();
	}
	
	/**
	 * Add or update DSS meta data. Please remember to update any version info, as
	 * A DSS with same id and version as an existing one will be rejected.
	 * @param DSSYAMLFile in the POST body, provide YAML for the DSS
	 * @param dryRunStr true to test the endpoint without changing the state of the application. Null or false otherwise
	 * @return the updated/new DSS meta data in YAML format
	 */
	@POST
	@Path("admin/dss/add")
	@GZIP
	@Consumes("application/x-yaml;charset=UTF-8")
	@Produces("application/x-yaml;charset=UTF-8")
	public Response addDSS(String DSSYAMLFile,@QueryParam("dryRun") String dryRunStr)
	{
		if( !this.isAuthorized() ) { return this.unauthorizedResponse(); }
		Boolean dryRun = dryRunStr == null ? false : dryRunStr.equals("true");
		try
		{
			// Validation first
			MetaDataService metaDataService = new MetaDataService();
			Response validationResponse = metaDataService.validateDSSYAMLFile(DSSYAMLFile);
			// Forward any errors to client
			if(validationResponse.getStatus() != 200)
			{
				return validationResponse;
			}
			// Check that isValid == true
			ObjectMapper jsonMapper = new ObjectMapper();
			Map validationMap = (Map)validationResponse.getEntity();
			if(validationMap.get("isValid") == null || validationMap.get("isValid").equals("true"))
			{
				return Response.status(Status.BAD_REQUEST).entity(Map.of("errorMessage",validationMap.get("errorMessage"))).build();
			}

			// Check if a file with a DSS of the same ID exists. If so, archive it - and use the same filename for the new one
			// However, if same ID and version - issue error
			ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
			DSS DSSToAdd = yamlMapper.readValue(DSSYAMLFile, new TypeReference<DSS>(){});
			DSS matchingDSS = this.DSSController.getDSSById(DSSToAdd.getId(), null, null, null);
			if(matchingDSS != null)
			{
				if(matchingDSS.getVersion().equals(DSSToAdd.getVersion()))
				{
					return Response.status(Status.CONFLICT).entity("The DSS " + matchingDSS.getName() + " already exists in "
							+ "the given version (" + matchingDSS.getVersion() + "). We won't overwrite it. Please check your "
									+ "input data.").build();
				}
				else if(!dryRun)
				{
					// Archive the file
					this.DSSController.archiveDSSFile(matchingDSS);
				}
			}
			// Store the DSS file 
			if(!dryRun)
			{
				if(System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH") != null)
				{
					String path = System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH") 
							+ (System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH").endsWith("/") ? "" : "/");
					yamlMapper.writeValue(
							new File(path + this.DSSController.getDSSFileName(DSSToAdd) + ".yaml") , 
							DSSToAdd
							);
				}
				else
				{
					return Response.serverError().entity("ERROR: path for DSS files is not set").build();
				}
			}
			return Response.ok().entity(yamlMapper.writeValueAsString(DSSToAdd)).build();
		}
		catch(IOException ex)
		{
			return Response.serverError().entity(ex.getMessage()).build();
		}
	}
	
	/**
	 * Generate a starting point for translation. Returns the keys and "default" column as CSV,
	 * with comma as separator and double quotes for strings. So don't use double quotes in the text!
	 * @param DSSId
	 * @return CSV file with translation for the given DSS and models regardless of validation status
	 */
	@GET
	@Path("admin/dss/{DSSId}/i18n/csv")
	@Produces("text/csv;charset=UTF-8")
	public Response geti18nCSVForDSS(
			@PathParam("DSSId") String DSSId
	)
	{
		return this.geti18nCSVForDSS(DSSId, null);
	}
	
	/**
	 * Generate a starting point for translation. Returns the keys and "default" column as CSV,
	 * with comma as separator and double quotes for strings. So don't use double quotes in the text!
	 * @param DSSId
	 * @param platformValidated true or false
	 * @return CSV file with translation for the given DSS and models filtered by validation status
	 */
	@GET
	@Path("admin/dss/{DSSId}/i18n/csv/platform_validated/{platformValidated}")
	@Produces("text/csv;charset=UTF-8")
	public Response geti18nCSVForDSS(
			@PathParam("DSSId") String DSSId,
			@PathParam("platformValidated") Boolean platformValidated
	)
	{
		try
		{
			// Assuming none existing per now
			// Generate the default
			DSS  dss = this.DSSController.getDSSById(DSSId, platformValidated, null, null);
			if(dss == null)
			{
				return Response.status(Status.NOT_FOUND).entity("A DSS with id=" + DSSId + " was not found.").build();
			}
			// And now... all the properties that need to be extracted
			// e.g. no.nibio.vips.2_0.models.PSILARTEMP.name=Carrot rust fly temperature model
			String basePath = dss.getId() + "." + dss.getVersion().replace(".","_");
			Properties props = new Properties();
			
			props.setProperty(basePath + ".name", dss.getName());
			for(DSSModel model:dss.getModels())
			{
				String modelPath = basePath + ".models." + model.getId();
				props.setProperty(modelPath + ".name", this.makeCSVCompatibleValue(model.getName()));
				props.setProperty(modelPath + ".purpose", model.getPurpose());
				props.setProperty(modelPath + ".description", this.makeCSVCompatibleValue(model.getDescription()));
				for(int i=0; i<model.getOutput().getWarning_status_interpretation().length;i++)
				{
					props.setProperty(modelPath + ".output.warning_status_interpretation." + i + ".explanation", 
							this.makeCSVCompatibleValue(model.getOutput().getWarning_status_interpretation()[i].getExplanation()));
					props.setProperty(modelPath + ".output.warning_status_interpretation." + i + ".recommended_action", 
							this.makeCSVCompatibleValue(model.getOutput().getWarning_status_interpretation()[i].getRecommended_action()));
				}
				
				props.setProperty(modelPath + ".output.chart_heading", this.makeCSVCompatibleValue(model.getOutput().getChart_heading()));
				for(DSSModel.Output.ChartGroup cg : model.getOutput().getChart_groups())
				{
					if(cg.getId() != null && !cg.getId().isBlank())
					{
						props.setProperty(modelPath + ".output.chart_groups." + cg.getId() + ".title", this.makeCSVCompatibleValue(cg.getTitle()));
					}

				}
				for(DSSModel.Output.ResultParameter rp : model.getOutput().getResult_parameters())
				{
					if(rp.getId() != null && !rp.getId().isBlank()) {
						props.setProperty(modelPath + ".output.result_parameters." + rp.getId() + ".title", this.makeCSVCompatibleValue(rp.getTitle()));
						props.setProperty(modelPath + ".output.result_parameters." + rp.getId() + ".description", this.makeCSVCompatibleValue(rp.getDescription()));
					}
				}
				
				// Finding and adding translatable items in the input_schema
				Map<String, Object> inputSchemaProperties = JsonFlattener.flattenAsMap(model.getExecution().getInput_schema());
				Set<String> propsOfInterest = Set.of("title", "description", "infoText");
				
				for(String key:inputSchemaProperties.keySet())
				{
					for(String propOfInterest:propsOfInterest)
					{
						if(key.endsWith("." + propOfInterest)) {
							props.setProperty(modelPath + ".execution.input_schema." + key, (String) inputSchemaProperties.get(key));
						}
					}
				}

			}
			
			String separator = ";";
			
			List<String> keys = props.keySet().stream().map(p->(String)p).collect(Collectors.toList());
			// Alphanumeric sort gives us pretty much the correct ordering
			Collections.sort(keys);
			// Heading
			String heading = "\"KEY\"" + separator + "\"default\"\n";
			String retVal = keys.stream()
					.reduce(heading, (hitherto, key) ->  hitherto + "\"" + key + "\"" + separator + "\"" + props.get(key) + "\"\n");
					
			StringWriter sw = new StringWriter();
			props.store(sw,"Auto generated new config file for translation. (c) NIBIO");
			return Response.ok().entity(retVal).build();
		}
		catch(IOException ex)
		{
			return Response.serverError().entity(ex.getMessage()).build();
		}
	}
	
	private String makeCSVCompatibleValue(String theVal)
	{
		//return theVal != null ? theVal.replace("\n", "\\n").replace("\"", "\\\"") : "";
		return theVal != null ? theVal.replace("\"", "\\\"") : "";
	}
}
