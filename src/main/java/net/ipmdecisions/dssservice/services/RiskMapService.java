/*
 * Copyright (c) 2024 NIBIO <http://www.nibio.no/>. 
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
import java.util.HashMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import net.ipmdecisions.dssservice.entity.RiskMaps;

/**
 * Web services for getting information about available risk maps following the 
 * VIPS risk maps WMS standard: https://gitlab.nibio.no/VIPS/documentation/-/blob/master/grid_models.md 
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest")
public class RiskMapService {
    
    /**
     * List all Risk maps available in the platform
     * @return a list of all risk map providers and their risk maps offered through the platform
     * @responseExample application/json {
   "risk_map_providers": [
       {
           "id": "nibio",
           "name": "NIBIO",
           "country": "Norway",
           "address": "Postboks 115",
           "postal_code": "1431",
           "city": "Ås",
           "email": "berit.nordskog@nibio.no",
           "url": "https://www.nibio.no/",
           "risk_maps": [
               {
                   "id": "SEPTREFHUM_EU",
                   "title": "Septoria Reference Humidity Model",
                   "wms_url": "https://testvips.nibio.no/cgi-bin/SEPTREFHUM_EU",
                   "platform_validated": true
               }
           ]
       }
   ]
}
     * 
     */
    @GET
    @Path("risk_maps/list")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(RiskMaps[].class)
    public Response listRiskMaps()
    {
        try
        {
            return Response.ok().entity(this.getRiskMaps()).build();
        }
        catch(IOException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    private RiskMaps getRiskMaps() throws IOException
    {
        File riskMapsFile = new File(System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH") + "/risk_maps/risk_maps.yaml");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.convertValue(mapper.readValue(riskMapsFile, HashMap.class), new TypeReference<RiskMaps>(){});
    }
    
}
