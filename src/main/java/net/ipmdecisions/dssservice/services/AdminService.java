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
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.annotations.GZIP;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import net.ipmdecisions.dssservice.entity.DSS;
import net.ipmdecisions.dssservice.controller.DSSController;


/**
 * Endpoints for admin operations
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */
@Path("rest")
public class AdminService {
	
	// If this ever needs to be an EJB, simply annotate with @EJB
	// and remove the init in the constructor for this class
	private DSSController DSSController;
	
	public AdminService()
	{
		this.DSSController = new DSSController();
	}

	@GET
    @Path("admin/heartbeat")
    @Produces("application/json;charset=UTF-8")
    public Response heartbeat() {
        return Response.ok().entity("Alive and well").build();
	}
	
	@POST
	@Path("admin/dss/add")
	@GZIP
	@Consumes("application/x-yaml;charset=UTF-8")
	@Produces("application/x-yaml;charset=UTF-8")
	public Response addDSS(String DSSYAMLFile,@QueryParam("dryRun") String dryRunStr)
	{
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
			DSS matchingDSS = this.DSSController.getDSSById(DSSToAdd.getId());
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
				yamlMapper.writeValue(
						new File(System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH") + this.DSSController.getDSSFileName(DSSToAdd) + ".yaml") , 
						DSSToAdd
						);
			}
			return Response.ok().entity(yamlMapper.writeValueAsString(DSSToAdd)).build();
		}
		catch(IOException ex)
		{
			return Response.serverError().entity(ex.getMessage()).build();
		}
	}
}
