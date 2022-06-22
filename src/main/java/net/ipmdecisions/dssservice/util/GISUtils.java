/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of IPM Decisions DSS Service.
 * IPM Decisions DSS Service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * IPM Decisions DSS Service is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with IPM Decisions DSS Service.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.ipmdecisions.dssservice.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.ipmdecisions.dssservice.services.DSSService;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class GISUtils {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GISUtils.class);
	
    private FeatureCollection allCountryBoundaries = null;

    public FeatureCollection getCountryBoundaries(){
        if(this.allCountryBoundaries == null)
        {
            try {

                Path path = Paths.get(System.getProperty("net.ipmdecisions.dssservice.COUNTRY_BOUNDARIES_FILE"));
            	Stream<String> lines = Files.lines(path);
                String all = lines.collect(Collectors.joining("\n"));
                lines.close();
                this.allCountryBoundaries = (FeatureCollection) GeoJSONFactory.create(all);
            } catch (IOException | NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        return this.allCountryBoundaries;
    }
    
    public Feature getCountryBoundary(String countryCode)
    {
        FeatureCollection countryBoundaries = this.getCountryBoundaries();
        for(Feature feature:countryBoundaries.getFeatures())
        {
            if(feature.getProperties().get("ISO_A3").equals(countryCode))
            {
                return feature;
            }
        }
        return null;
    }
    
    public FeatureCollection getCountryBoundaries(Set<String> countryCodes)
    {
        List<Feature> matching = new ArrayList<>();
        FeatureCollection countryBoundaries = this.getCountryBoundaries();
        for(Feature feature:countryBoundaries.getFeatures())
        {
            if(countryCodes.contains((String) feature.getProperties().get("ISO_A3")))
            {
                matching.add(feature);
            }
        }
        return new GeoJSONWriter().write(matching);
    }
    
    /**
     * This is for now only used in able to run tests
     * @param countryBoundaries 
     */
    public void setCountryBoundaries(FeatureCollection countryBoundaries)
    {
        this.allCountryBoundaries = countryBoundaries;
    }
    
    public boolean isGeoJsonStringEmpty(String geoJsonString) {
    	try
    	{
	    	ObjectMapper oMapper = new ObjectMapper();
	    	JsonNode node = oMapper.readTree(geoJsonString);
	    	return node.isEmpty();
    	}
    	catch(JsonProcessingException ex) {
    		return false;
    	}
    }
}
