/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
