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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;

import jdk.jfr.Description;
import net.ipmdecisions.dssservice.entity.DSS;
import net.ipmdecisions.dssservice.entity.DSSModel;

public class DSSController {
	
	/**
     * Pulls YAML files from set path and creates a list of all DSSs This should
     * be replaced by a decent database
     *
     * @return
     * @throws IOException
     */
    public List<DSS> getDSSListObj(Boolean platformValidated, String language) throws IOException {
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
        
        // i18n
        if(language != null)
        {
        	for(DSS dss:DSSList)
        	{
        		dss = this.getDSSTranslated(dss, language);
        	}
        }
        
        return DSSList;
    }
    
    public DSS getDSSTranslated(DSS dss, String language) throws IOException
    {
    	String basePath = dss.getId() + "." + dss.getVersion().replace(".","_");
    	//System.out.println("get " + language + " for " + dss.getId());
    	File file = new File(System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH") + "/i18n");
    	URL[] urls = {file.toURI().toURL()};
    	ClassLoader loader = new URLClassLoader(urls);
    	try
    	{
    		ResourceBundle bundle = ResourceBundle.getBundle(dss.getId(), new Locale(language), loader);
    		/*Enumeration<String> e = bundle.getKeys();
    		while(e.hasMoreElements())
    		{
    			String key = e.nextElement();
    			System.out.println(key + ": " + bundle.getString(key));
    		}*/
    		dss.setName(bundle.getString(basePath + ".name"));
    		for(DSSModel model:dss.getModels())
			{
    			String modelPath = basePath + ".models." + model.getId();
    			model.setName(bundle.getString(modelPath + ".name"));
    			DSSModel.Description d = model.getDescription();
    			d.setOther(bundle.getString(modelPath + ".description.other"));
    			d.setCreated_by(bundle.getString(modelPath + ".description.created_by"));
    			d.setAge(bundle.getString(modelPath + ".description.age"));
    			d.setAssumptions(bundle.getString(modelPath + ".description.assumptions"));
    			DSSModel.Output.WarningStatusInterpretation[] wsi = model.getOutput().getWarning_status_interpretation();
    			for(int i=0; i < wsi.length;i++)
				{
    				wsi[i].setExplanation(bundle.getString(modelPath + ".output.warning_status_interpretation." + i + ".explanation"));
    				wsi[i].setRecommended_action(bundle.getString(modelPath + ".output.warning_status_interpretation." + i + ".recommended_action"));
				}
    			model.getOutput().setChart_heading(bundle.getString(modelPath + ".output.chart_heading"));
    			for(DSSModel.Output.ChartGroup cg : model.getOutput().getChart_groups())
				{
    				cg.setTitle(bundle.getString(modelPath + ".output.chart_groups." + cg.getId() + ".title"));
				}
    			for(DSSModel.Output.ResultParameter rp : model.getOutput().getResult_parameters())
				{
    				rp.setTitle(bundle.getString(modelPath + ".output.result_parameters." + rp.getId() + ".title"));
    				rp.setDescription(bundle.getString(modelPath + ".output.result_parameters." + rp.getId() + ".description"));
				}
    			
    			// Flatten the current input schema
    			Map<String, Object> inputSchemaProperties = JsonFlattener.flattenAsMap(model.getExecution().getInput_schema());
    			// Replace the matching properties
    			Integer pathToInputSchemaLength = (modelPath + ".execution.input_schema.").length();
    			for(String key: Collections.list(bundle.getKeys()).stream()
    					.filter(k->k.startsWith(modelPath + ".execution.input_schema.")).collect(Collectors.toList())
    					) 
    			{
    				// Remove the path to input_schema
    				String inputSchemaPath = key.substring(pathToInputSchemaLength);
    				//System.out.println(inputSchemaPath);
    				if(inputSchemaProperties.containsKey(inputSchemaPath))
    				{
    					inputSchemaProperties.put(inputSchemaPath, bundle.getString(key));
    				}
    			}
    			
    			// De-flatten and set as input schema
    			model.getExecution().setInput_schema(JsonUnflattener.unflatten(inputSchemaProperties));
			}
    	}
    	catch(MissingResourceException ex)
    	{
    		System.out.println("WARNING: " + ex.getMessage());
    		
    	}
    	return dss;
    }
    
    /*public ResourceBundle getResourceBundleForDSS(String DSSId)
    {
    	
    }*/
    
    /**
     * Pulls YAML files from set path and creates a list of all DSSs This should
     * be replaced by a decent database
     *
     * @return
     * @throws IOException
     */
    public List<DSS> getDSSListObj(Boolean platformValidated) throws IOException {
    	return this.getDSSListObj(platformValidated, null);
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
