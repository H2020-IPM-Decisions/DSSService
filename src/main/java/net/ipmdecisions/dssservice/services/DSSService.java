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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import net.ipmdecisions.dssservice.entity.DSS;
import net.ipmdecisions.dssservice.entity.DSSModel;

/**
 * Web service for listing and querying the DSS Catalogue
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest")
public class DSSService {
    
   
    /**
     * List all DSSs and models available in the platform
     * @return a list of all DSSs and models available in the platform
     */
    @GET
    @Path("list")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listDSSs()
    {
        try
        {
            return Response.ok().entity(this.getDSSListObj()).build();
        }
        catch(IOException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    /**
     * Returns a list of models that are applicable to the given crop
     * @param cropCode EPPO code for the crop https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes
     * @pathExample /rest/list/crop/SOLTU
     * @return a list of models that are applicable to the given crop
     */
    @GET
    @Path("list/crop/{cropCode}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForCrops(@PathParam("cropCode") String cropCode)
    {
        try
        {
            List<DSS> retVal = new ArrayList<>();
            List<DSS> allDSSs = this.getDSSListObj();
            for(DSS currentDSS: allDSSs)
            {
                List<DSSModel> qualifyingModels = 
                        currentDSS.getModels().stream()
                        .filter(model->model.getCrops().contains(cropCode))
                        .collect(Collectors.toList());
                if(qualifyingModels.size() > 0)
                {
                    currentDSS.setModels(qualifyingModels);
                    retVal.add(currentDSS);
                }
            }
            return Response.ok().entity(retVal).build();
        }
        catch(IOException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    /**
     * Returns a list of models that are applicable to the given pest
     * @param pestCode EPPO code for the pest https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes
     * @pathExample /rest/list/pest/PSILRO
     * @return a list of models that are applicable to the given pest
     */
    @GET
    @Path("list/pest/{pestCode}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForPests(@PathParam("pestCode") String pestCode)
    {
        try
        {
            List<DSS> retVal = new ArrayList<>();
            this.getDSSListObj().forEach((currentDSS) -> {
                List<DSSModel> qualifyingModels = 
                        currentDSS.getModels().stream()
                                .filter(model->model.getPests().contains(pestCode))
                                .collect(Collectors.toList());
                if (qualifyingModels.size() > 0) {
                    currentDSS.setModels(qualifyingModels);
                    retVal.add(currentDSS);
                }
            });
            return Response.ok().entity(retVal).build();
        }
        catch(IOException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    private File[] getFilesWithExtension(String path, String extension) throws IOException
    {
        File directory = new File(path);
        if(!directory.isDirectory())
        {
            throw new IOException(path + " is not a directory");
        }
        return directory.listFiles((dir,name) -> name.endsWith(extension));
    }
    
    /**
     * Pulls YAML files from set path and creates a list of all DSSs (only HashMap)
     * @return
     * @deprecated
     * @throws IOException 
     */
    @Deprecated
    private List<Map> getDSSList() throws IOException
    {
        List<Map> DSSList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        File[] DSSInfoFiles = this.getFilesWithExtension(System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH"), ".yaml");
        for(File f:DSSInfoFiles)
        {
            DSSList.add(mapper.readValue(f, HashMap.class));
        }
        return DSSList;
    }
    
    /**
     * Pulls YAML files from set path and creates a list of all DSSs
     * This should be replaced by a decent database
     * @return
     * @throws IOException 
     */
    private List<DSS> getDSSListObj() throws IOException
    {
        List<DSS> DSSList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        File[] DSSInfoFiles = this.getFilesWithExtension(System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH"), ".yaml");
        for(File f:DSSInfoFiles)
        {
            
            DSSList.add(mapper.convertValue(mapper.readValue(f, HashMap.class), new TypeReference<DSS>(){}));
        }
        return DSSList;
    }
}
