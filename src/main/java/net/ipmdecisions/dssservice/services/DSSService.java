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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest")
public class DSSService {
    
    @GET
    @Path("test")
    @Produces("text/plain;charset=UTF-8")
    public Response test()
    {
        return Response.ok().entity("Hello world").build();
    }
    
    @GET
    @Path("list")
    @Produces("application/json;charset=UTF-8")
    //@Produces("text/plain;charset=UTF-8")
    public Response listDSSs()
    {
        try
        {
            return Response.ok().entity(this.getDSSList()).build();
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
}
