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

package net.ipmdecisions.dssservice.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import net.ipmdecisions.dssservice.entity.DSS;

public class DSSController {
	
	/**
     * Pulls YAML files from set path and creates a list of all DSSs This should
     * be replaced by a decent database
     *
     * @return
     * @throws IOException
     */
    public List<DSS> getDSSListObj(Boolean platformValidated) throws IOException {
        List<DSS> DSSList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        File[] DSSInfoFiles = this.getFilesWithExtension(System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH"), ".yaml");
        for (File f : DSSInfoFiles) {

            DSSList.add(mapper.convertValue(mapper.readValue(f, HashMap.class), new TypeReference<DSS>(){}));
        }
        // If the platformValidated is set, filter models with this as the criterium
        
        if(platformValidated != null)
        {
        	for(DSS dss:DSSList)
        	{
        		dss.setModels(dss.getModels().stream()
        				.filter(model->model.getPlatform_validated().equals(platformValidated))
        				.collect(Collectors.toList()));
        	}
        }
        return DSSList;
    }
    
    private File[] getFilesWithExtension(String path, String extension) throws IOException {
        File directory = new File(path);
        if (!directory.isDirectory()) {
            throw new IOException(path + " is not a directory");
        }
        return directory.listFiles((dir, name) -> name.endsWith(extension));
    }
    
    public DSS getDSSById(String DSSid) throws IOException
    {
        Optional<DSS> matchingDSS = this.getDSSListObj(true).stream().filter(dss -> dss.getId().equals(DSSid)).findFirst();
        if (matchingDSS.isPresent()) {
            return matchingDSS.get();
        } else {
            return null;
        }
    }
    
    /**
     * Archive a file: Giving it a unique name and making sure it doesn't end in ".yaml"
     * @param dss
     */
    public void archiveDSSFile(DSS dss) throws IOException
    {
    	// Find the file with this DSS
    	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    	File[] DSSInfoFiles = this.getFilesWithExtension(System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH"), ".yaml");
    	for (File f : DSSInfoFiles) {
    		DSS matchingDSS = mapper.convertValue(mapper.readValue(f, HashMap.class), new TypeReference<DSS>(){});
            if(dss.getId().equals(matchingDSS.getId()) && dss.getVersion().equals(matchingDSS.getVersion()))
            {
            	String archiveFileName = this.getDSSFileName(dss) + ".yaml_bak";
            	f.renameTo(new File(f.getParent() + "/" + archiveFileName));
            	break;
            }
        }
    }
    
    public String getDSSFileName(DSS dss)
    {
    	return dss.getId().replace(".", "_") + "_" + dss.getVersion().replace(".", "_");
    }
}
