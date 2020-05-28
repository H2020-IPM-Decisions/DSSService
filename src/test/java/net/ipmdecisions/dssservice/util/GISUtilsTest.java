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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;


/**
 *
 * @author treinar
 */
public class GISUtilsTest {
    
    public GISUtilsTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
        
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getCountryBoundaries method, of class GISUtils.
     */
    @Test
    public void testGetCountryBoundaries_0args() {
        //System.setProperty("net.ipmdecisions.dssservice.COUNTRY_BOUNDARIES_FILE", "/home/treinar/prosjekter/ipm_decisions/Sourcecode/geo-countries/data/countries.geojson");
        System.out.println("getCountryBoundaries");
        GISUtils instance = new GISUtils();
        instance.setCountryBoundaries(this.getCountryBoundaries());
        FeatureCollection result = instance.getCountryBoundaries();
        assertNotNull(result);
        
    }

    /**
     * Test of getCountryBoundary method, of class GISUtils.
     */
    @Test
    public void testGetCountryBoundary() {
        System.out.println("getCountryBoundary");
        String countryCode = "NOR";
        GISUtils instance = new GISUtils();
        instance.setCountryBoundaries(this.getCountryBoundaries());
        Feature result = instance.getCountryBoundary(countryCode);
        assertNotNull(result);
        
    }

    /**
     * Test of getCountryBoundaries method, of class GISUtils.
     */
    @Test
    public void testGetCountryBoundaries_Set() {
        
        System.out.println("getCountryBoundaries");
        String[] cc = {"NOR","SWE"};
        String geoJson = "{\n" +
            "  \"type\": \"FeatureCollection\",\n" +
            "  \"features\": [\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {},\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"Point\",\n" +
            "        \"coordinates\": [\n" +
            "          10.780913829803467,\n" +
            "          59.66215936204673\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        FeatureCollection fc = (FeatureCollection) GeoJSONFactory.create(geoJson);
        GeoJSONReader reader = new GeoJSONReader();
        Geometry p = reader.read(fc.getFeatures()[0].getGeometry());
        Set<String> countryCodes = new HashSet<>(Arrays.asList(cc));
        GISUtils instance = new GISUtils();
        instance.setCountryBoundaries(this.getCountryBoundaries());
        FeatureCollection result = instance.getCountryBoundaries(countryCodes);
        
        for(Feature feature:result.getFeatures())
        {
            if(feature.getProperties().get("ISO_A3").equals("NOR"))
            {
                Geometry mpol = reader.read(feature.getGeometry());
                assertTrue(mpol.intersects(p));
                
            }
        }
        
        assertNotNull(result);
        
    }
    
    private FeatureCollection countryBoundaries;
    
    /**
     * Pulls a minimized (only Norway and Sweden included) set of country 
     * polygons for testing purposes
     * @return 
     */
    private FeatureCollection getCountryBoundaries(){
        
        if(this.countryBoundaries == null)
        {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/geojson/countries_NOR_SWE.geojson")));
                String all = "";
                String line;
                while((line = br.readLine()) != null)
                {
                    all += line;
                }

                this.countryBoundaries = (FeatureCollection) GeoJSONFactory.create(all);
            } catch (IOException ex) {
                
            }
        }
        return this.countryBoundaries;
    }
}
