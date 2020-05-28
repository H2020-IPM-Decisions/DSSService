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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import net.ipmdecisions.dssservice.entity.DSS;
import net.ipmdecisions.dssservice.entity.DSSModel;
import net.ipmdecisions.dssservice.util.GISUtils;
import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.geojson.Point;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

/**
 * Web service for listing and querying the DSS Catalogue
 *
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest")
public class DSSService {

    /**
     * List all DSSs and models available in the platform
     *
     * @return a list of all DSSs and models available in the platform
     */
    @GET
    @Path("dss")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listDSSs() {
        try {
            return Response.ok().entity(this.getDSSListObj()).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    /**
     * Returns a list of models that are applicable to the given crop
     *
     * @param cropCode EPPO code for the crop
     * https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes
     * @pathExample /rest/dss/crop/SOLTU
     * @return a list of models that are applicable to the given crop
     */
    @GET
    @Path("dss/crop/{cropCode}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForCrops(@PathParam("cropCode") String cropCode) {
        try {
            List<DSS> retVal = new ArrayList<>();
            List<DSS> allDSSs = this.getDSSListObj();
            for (DSS currentDSS : allDSSs) {
                List<DSSModel> qualifyingModels
                        = currentDSS.getModels().stream()
                                .filter(model -> model.getCrops().contains(cropCode))
                                .collect(Collectors.toList());
                if (qualifyingModels.size() > 0) {
                    currentDSS.setModels(qualifyingModels);
                    retVal.add(currentDSS);
                }
            }
            return Response.ok().entity(retVal).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    /**
     * Returns a list of models that are applicable to the given pest
     *
     * @param pestCode EPPO code for the pest
     * https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes
     * @pathExample /rest/dss/pest/PSILRO
     * @return a list of models that are applicable to the given pest
     */
    @GET
    @Path("dss/pest/{pestCode}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForPests(@PathParam("pestCode") String pestCode) {
        try {
            List<DSS> retVal = new ArrayList<>();
            this.getDSSListObj().forEach((currentDSS) -> {
                List<DSSModel> qualifyingModels
                        = currentDSS.getModels().stream()
                                .filter(model -> model.getPests().contains(pestCode))
                                .collect(Collectors.toList());
                if (qualifyingModels.size() > 0) {
                    currentDSS.setModels(qualifyingModels);
                    retVal.add(currentDSS);
                }
            });
            return Response.ok().entity(retVal).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    /**
     *
     * @return A list of EPPO codes
     * (https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes) for all pests
     * that the DSS models in the platform deals with in some way.
     * @responseExample application/json ["PSILRO","PHYTIN","SEPTAP"]
     */
    @GET
    @Path("pest")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(String[].class)
    public Response getAllPests() {
        try {
            List<String> retVal = new ArrayList<>();
            for (DSS dss : this.getDSSListObj()) {
                dss.getModels().forEach((model) -> {
                    retVal.addAll(model.getPests());
                });
            }
            return Response.ok().entity(retVal).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }

    }

    /**
     * @return A list of EPPO codes
     * (https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes) for all crops
     * that the DSS models in the platform
     *
     * @responseExample application/json ["DAUCS","SOLTU","APUGD"]
     */
    @GET
    @Path("crop")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(String[].class)
    public Response getAllCrops() {
        try {
            List<String> retVal = new ArrayList<>();
            for (DSS dss : this.getDSSListObj()) {
                dss.getModels().forEach((model) -> {
                    retVal.addAll(model.getCrops());
                });
            }
            return Response.ok().entity(retVal).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    /**
     * Get all information about a specific DSS
     *
     * @param DSSId the id of the DSS
     * @return the requested DSS
     * @pathExample /rest/model/no.nibio.vips
     */
    @GET
    @Path("dss/{DSSId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS.class)
    public Response getDSS(@PathParam("DSSId") String DSSId) {
        try {
            Optional<DSS> matchingDSS = this.getDSSListObj().stream().filter(dss -> dss.getId().equals(DSSId)).findFirst();
            if (matchingDSS.isPresent()) {
                return Response.ok().entity(matchingDSS.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("errorMessage", "Could not find DSS with id " + DSSId)).build();
            }
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }

    }

    /**
     * Get all information about a specific DSS model
     *
     * @param DSSId The id of the DSS containing the model
     * @param ModelId The id of the DSS model requested
     * @return The requested DSS model
     * @pathExample /rest/model/no.nibio.vips/PSILARTEMP
     */
    @GET
    @Path("model/{DSSId}/{ModelId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSSModel.class)
    public Response getDSSModel(@PathParam("DSSId") String DSSId, @PathParam("ModelId") String ModelId) {
        try {
            Optional<DSS> matchingDSS = this.getDSSListObj().stream().filter(dss -> dss.getId().equals(DSSId)).findFirst();
            if (matchingDSS.isPresent()) {
                DSS actualDSS = matchingDSS.get();
                Optional<DSSModel> matchingDSSModel = actualDSS.getModels().stream().filter(model -> model.getId().equals(ModelId)).findFirst();
                if (matchingDSSModel.isPresent()) {
                    return Response.ok().entity(matchingDSSModel.get()).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity(Map.of("errorMessage", "Could not find DSS Model with id " + ModelId + " in DSS with id " + DSSId)).build();
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("errorMessage", "Could not find DSS with id " + DSSId)).build();
            }
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }

    }

    /**
     * Search for DSS models that have been validated for the specific location.
     * The location can by any valid Geometry, such as Point or Polygon.
     * Example geoJson input
     * <pre>
     * {
        "type": "FeatureCollection",
        "features": [
          {
            "type": "Feature",
            "properties": {},
            "geometry": {
              "type": "Point",
              "coordinates": [
                12.01629638671875,
                59.678835236960765
              ]
            }
          }
        ]
      }
     * 
     * </pre>
     *
     * @param geoJson valid GeoJSON https://geojson.org/
     * @return A list of all the matching DSS models
     */
    @POST
    @Path("dss/location")
    @Consumes("application/json")
    @Produces("application/json")
    @TypeHint(DSS[].class)
    public Response listModelsForLocation(String geoJson) {
        try {
            FeatureCollection clientFeatures = (FeatureCollection) GeoJSONFactory.create(geoJson);
            GeoJSONReader reader = new GeoJSONReader();
            // Get all geometries in request
            List<Geometry> clientGeometries = new ArrayList<>();
            for (Feature feature : clientFeatures.getFeatures()) {
                Geometry geom = reader.read(feature.getGeometry());
                clientGeometries.add(geom);
            }
            // Loop through all weather data sources
            // Return only data sources with geometries intersecting with the client's
            // specified geometries
            GISUtils gisUtils = new GISUtils();
            
            List<DSS> retVal = new ArrayList<>();
            for (DSS dss : this.getDSSListObj()) {
                List<DSSModel> matchingModels = dss.getModels().stream()
                        .filter(model -> {
                            String modelGeoJsonStr = "";
                            try
                            {
                                modelGeoJsonStr  = model.getValid_spatial().getGeoJSON();
                            }
                            catch(NullPointerException ex)
                            {
                                return false;
                            }
                            // We do a brute force search for the string "Sphere" in the geoJSON string
                            // to bypass any issues in deserialization of that custom type, which is 
                            // short for creating a polygon that covers the entire globe
                            if (modelGeoJsonStr.contains("\"Sphere\"")) {
                                return true;
                            }
                             // Get all geometries in current model
                            // Country boundaries 
                            List<Feature> modelFeatures = Arrays.asList(gisUtils.getCountryBoundaries(new HashSet<>(Arrays.asList(
                                    model.getValid_spatial().getCountries()
                            ))).getFeatures());
                            try
                            {
                                if(!modelGeoJsonStr.isBlank())
                                {
                                    modelFeatures.addAll(Arrays.asList((
                                            (FeatureCollection) GeoJSONFactory.create(modelGeoJsonStr)
                                    ).getFeatures()));
                                }
                            }catch(RuntimeException ex) {}
                            
                            // Match with all geometries in request. If found, add data source to
                            // list of matching data sources
                            List<Geometry> modelGeometries = modelFeatures.stream()
                                    .map(f -> {
                                        return reader.read(f.getGeometry());
                                    })
                                    .filter(modelGeometry -> {
                                        Boolean matching = false;
                                        for (Geometry clientGeometry : clientGeometries) {
                                            if (modelGeometry.intersects(clientGeometry)) {
                                                matching = true;
                                            }
                                        }
                                        return matching;
                                    })
                                    .collect(Collectors.toList());
                            // The number of matching geometries for this weather data source
                            // Used as filter criteria
                            return modelGeometries.size() > 0;
                        })
                        .collect(Collectors.toList());
                if(matchingModels.size() > 0)
                {
                    dss.setModels(matchingModels);
                    retVal.add(dss);
                }
                
            }

            return Response.ok().entity(retVal).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    /**
     * Search for models that are valid for the specific point. 
     * @param latitude in decimal degrees (WGS84)
     * @param longitude in decimal degrees (WGS84)
     * @return A list of all the matching DSS models
     * @pathExample /rest/dss/location/point/?latitude=59.678835236960765&longitude=12.01629638671875
     */
    @GET
    @Path("dss/location/point")
    @Produces("application/json")
    @TypeHint(DSS[].class)
    public Response listModelsForPoint(
            @QueryParam("latitude") Double latitude, 
            @QueryParam("longitude") Double longitude
            )
    {

        // Generate GeoJSON for a point and call the general method
        double[] coordinate = new double[2];
        coordinate[0] = longitude;
        coordinate[1] = latitude;
        Point point = new Point(coordinate);
        List<Feature> features = new ArrayList<>();
        Map<String, Object> properties = new HashMap<>();
        features.add(new Feature(point,properties));
        GeoJSONWriter writer = new GeoJSONWriter();
        return this.listModelsForLocation(writer.write(features).toString());
    }
    
    private File[] getFilesWithExtension(String path, String extension) throws IOException {
        File directory = new File(path);
        if (!directory.isDirectory()) {
            throw new IOException(path + " is not a directory");
        }
        return directory.listFiles((dir, name) -> name.endsWith(extension));
    }

    /**
     * Pulls YAML files from set path and creates a list of all DSSs (only
     * HashMap)
     *
     * @return
     * @deprecated
     * @throws IOException
     */
    @Deprecated
    private List<Map> getDSSList() throws IOException {
        List<Map> DSSList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        File[] DSSInfoFiles = this.getFilesWithExtension(System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH"), ".yaml");
        for (File f : DSSInfoFiles) {
            DSSList.add(mapper.readValue(f, HashMap.class));
        }
        return DSSList;
    }

    /**
     * Pulls YAML files from set path and creates a list of all DSSs This should
     * be replaced by a decent database
     *
     * @return
     * @throws IOException
     */
    private List<DSS> getDSSListObj() throws IOException {
        List<DSS> DSSList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        File[] DSSInfoFiles = this.getFilesWithExtension(System.getProperty("net.ipmdecisions.dssservice.DSS_LIST_FILES_PATH"), ".yaml");
        for (File f : DSSInfoFiles) {

            DSSList.add(mapper.convertValue(mapper.readValue(f, HashMap.class), new TypeReference<DSS>() {
            }));
        }
        return DSSList;
    }
}
