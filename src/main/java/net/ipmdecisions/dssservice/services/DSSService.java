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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import net.ipmdecisions.dssservice.controller.DSSController;
import net.ipmdecisions.dssservice.entity.DSS;
import net.ipmdecisions.dssservice.entity.DSSModel;
import net.ipmdecisions.dssservice.util.GISUtils;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.geojson.Point;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Web services for listing and querying the DSS Catalogue.
 * For a more thorough description of the concepts, please read <a href="https://github.com/H2020-IPM-Decisions/DSSService/blob/develop/docs/index.md" target="new">the user guide</a>
 * {@code @copyright} 2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest")
public class DSSService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DSSService.class);
	
	// If this ever needs to be an EJB, simply annotate with @EJB
	// and remove the init in the constructor for this class
	private final DSSController DSSController;
	
	public DSSService() {
		this.DSSController = new DSSController();
	}

    /**
     * List all DSSs and models available in the platform
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     *
     * @return a list of all DSSs and models available in the platform, regardless of validation
     */
    @GET
    @Path("dss")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listDSSs(
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
        	
        	List<DSS> allDSSs = this.DSSController.getDSSListObj(null,language, executionType);
            return Response.ok().entity(allDSSs).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    /**
     * Outputs a
     *  @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     *      * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     * @return CSV summary of the models
     *
     * @responseExample
     * Source name,DSS Model name, Crop(s), Pest(s), Purpose
     * IPM Decisions,Hutton Criteria Late Blight Model,Solanum tuberosum,Phytophthora infestans,Estimates risk of late blight
     * VIPS,Cabbage fly flight model (Scandinavia),Brassica sp.,Delia radicum,Estimates risk of flight and egg laying in crop
     */
    @GET
    @Path("dss/summary/csv")
    @Produces("text/csv;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listDSSsSummaryCSV(
            @QueryParam("language") String language,
            @QueryParam("executionType") String executionType
    ) {
        try {
            List<DSS> allDSSs = this.DSSController.getDSSListObj(null,language, executionType);
            String output = "Source name,DSS Model name, Crop(s), Pest(s), Purpose\n";

            // Collecting all EPPO Codes in one go
            Set<String> allEPPOCodesInDSSs = new HashSet<>();
            for(DSS dss:allDSSs) {
                String name = dss.getName();
                for (DSSModel model : dss.getModels()) {
                    if(model.getCrops() != null) {
                        allEPPOCodesInDSSs.addAll(model.getCrops());
                    }
                    if(model.getPests() != null) {
                        allEPPOCodesInDSSs.addAll(model.getPests());
                    }
                }
            }

            Map<String,String> allEPPOCodesMapped = this.DSSController.getOrganismNamesFromEPPO(new ArrayList<>(allEPPOCodesInDSSs));

            for(DSS dss:allDSSs){
                String name = dss.getName();
                for(DSSModel model:dss.getModels())
                {
                    String cropList = model.getCrops() != null ? String.join("/", model.getCrops().stream()
                            .map(eppoCode->allEPPOCodesMapped.get(eppoCode))
                            .collect(Collectors.toList()))
                            : "";
                    String pestList = model.getPests() != null ? String.join("/", model.getPests().stream()
                            .map(eppoCode->allEPPOCodesMapped.get(eppoCode))
                            .collect(Collectors.toList()))
                            : "";

                    output += name + "," + model.getName() + "," +
                            cropList + "," + pestList + "," +
                            model.getPurpose() + "\n";
                }
            }
            return Response.ok().entity(output).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    /**
     * List all DSSs and models available in the platform
     *
     * @param platformValidated true or false
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     *
     * @return a list of all DSSs and models available in the platform that are either validated or not
     */
    @GET
    @Path("dss/platform_validated/{platformValidated}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listDSSsByValidation(
    		@PathParam("platformValidated") Boolean platformValidated,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
        	List<DSS> allDSSs = this.DSSController.getDSSListObj(platformValidated, language, executionType);
            return Response.ok().entity(allDSSs).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    

    /**
     * Returns a list of models that are applicable to the given crop
     *
     * @param cropCode <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO code</a> for the crop
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     *
     * @pathExample /rest/dss/crop/SOLTU
     * @return a list of ALL models that are applicable to the given crop, regardless of validation status
     */
    @GET
    @Path("dss/crop/{cropCode}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForCrop(
    		@PathParam("cropCode") String cropCode,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
    	return this.listModelsForCropByValidation(cropCode, null, language, executionType);
    }
    
    /**
     * Returns a list of models that are applicable to the given crop
     *
     * @param cropCode <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO code</a> for the crop
     * @param platformValidated true or false
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     *
     * @pathExample /rest/dss/crop/SOLTU/platform_validated/true
     * @return a list of models that are applicable to the given crop, filtered on validation status
     */
    @GET
    @Path("dss/crop/{cropCode}/platform_validated/{platformValidated}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForCropByValidation(
    		@PathParam("cropCode") String cropCode,
    		@PathParam("platformValidated") Boolean platformValidated,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
            List<DSS> retVal = new ArrayList<>();
            List<DSS> allDSSs = this.DSSController.getDSSListObj(platformValidated, language, executionType);
            for (DSS currentDSS : allDSSs) {
                List<DSSModel> qualifyingModels
                        = currentDSS.getModels().stream()
                                .filter(model -> model.getCrops() != null && model.getCrops().contains(cropCode))
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
     * Returns a list of DSS models that are applicable to the given crops
     *
     * @param cropCodesStr comma separated <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO codes</a> for the crops
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     *
     * @pathExample /rest/dss/crops/SOLTU,DAUCS
     * @return a list of models that are applicable to the given crops, regardless of validation status
     */
    @GET
    @Path("dss/crops/{cropCodes}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForCrops(
    		@PathParam("cropCodes") String cropCodesStr,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
    	return this.listModelsForCropsByValidation(cropCodesStr, null, language, executionType);
    }
    
    /**
     * Returns a list of DSS models that are applicable to the given crops
     *
     * @param cropCodesStr comma separated <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO codes</a> for the crops
     * @param platformValidated true or false
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     *
     * @pathExample /rest/dss/crops/SOLTU,DAUCS/platform_validated/true
     * @return a list of models that are applicable to the given crops filtered by the models' validation status
     */
    @GET
    @Path("dss/crops/{cropCodes}/platform_validated/{platformValidated}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForCropsByValidation(
    		@PathParam("cropCodes") String cropCodesStr,
    		@PathParam("platformValidated") Boolean platformValidated,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
        	List<String> cropCodes = Arrays.asList(cropCodesStr.split(","));
            List<DSS> retVal = new ArrayList<>();
            List<DSS> allDSSs = this.DSSController.getDSSListObj(platformValidated, language, executionType);
            for (DSS currentDSS : allDSSs) {
                List<DSSModel> qualifyingModels
                        = currentDSS.getModels().stream()
                                .filter(model -> { 
                                	if(model.getCrops() == null)
                                	{
                                		return false;
                                	}
                                	
                                	List<String> intersection = model.getCrops().stream()
                                			.distinct()
                                			.filter(cropCodes::contains)
                                			.collect(Collectors.toList());
                                	return intersection.size() > 0;
                                	}
                                )
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
     * @param pestCode <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO code</a> for the pest
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     *
     * @pathExample /rest/dss/pest/PSILRO
     * @return a list of models that are applicable to the given pest, regardless of validation status
     */
    @GET
    @Path("dss/pest/{pestCode}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForPest(
    		@PathParam("pestCode") String pestCode,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
    	return this.listModelsForPestByValidation(pestCode, null, language, executionType);
    }

    /**
     * Returns a list of models that are applicable to the given pest
     *
     * @param pestCode <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO code</a> for the pest
     * @param platformValidated true or false
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     *
     * @pathExample /rest/dss/pest/PSILRO/platform_validated/true
     * @return a list of models that are applicable to the given pest filtered by their validation status
     */
    @GET
    @Path("dss/pest/{pestCode}/platform_validated/{platformValidated}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForPestByValidation(
    		@PathParam("pestCode") String pestCode,
    		@PathParam("platformValidated") Boolean platformValidated,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
            List<DSS> retVal = new ArrayList<>();
            this.DSSController.getDSSListObj(platformValidated, language, executionType).forEach((currentDSS) -> {
                List<DSSModel> qualifyingModels
                        = currentDSS.getModels().stream()
                                .filter(model -> model.getPests() != null && model.getPests().contains(pestCode))
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
     * Returns a list of models that are applicable to the given crop-pest combination
     *
     * @param cropCode <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO code</a> for the crop
     * @param pestCode <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO code</a> for the pest
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     *
     * @pathExample /rest/dss/crop/DAUCS/pest/PSILRO
     * @return a list of models that are applicable to the given crop-pest combination, regardless of validation status
     */
    @GET
    @Path("dss/crop/{cropCode}/pest/{pestCode}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForCropPestCombination(
    		@PathParam("cropCode") String cropCode, 
    		@PathParam("pestCode") String pestCode,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
    	return this.listModelsForCropPestCombinationByValidation(cropCode, pestCode, null, language, executionType);
    }
    
    /**
     * Returns a list of models that are applicable to the given crop-pest combination
     *
     * @param cropCode <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO code</a> for the crop
     * @param pestCode <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO code</a> for the pest
     * @param platformValidated true or false
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     * 
     * @pathExample /rest/dss/crop/DAUCS/pest/PSILRO/platform_validated/true
     * @return a list of models that are applicable to the given crop-pest combination by their validation status
     */
    @GET
    @Path("dss/crop/{cropCode}/pest/{pestCode}/platform_validated/{platformValidated}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS[].class)
    public Response listModelsForCropPestCombinationByValidation(
    		@PathParam("cropCode") String cropCode, 
    		@PathParam("pestCode") String pestCode,
    		@PathParam("platformValidated") Boolean platformValidated,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
            List<DSS> retVal = new ArrayList<>();
            this.DSSController.getDSSListObj(platformValidated, language, executionType).forEach((currentDSS) -> {
                List<DSSModel> qualifyingModels
                        = currentDSS.getModels().stream()
                                .filter(model -> model.getPests() != null && model.getCrops() != null && model.getPests().contains(pestCode) && model.getCrops().contains(cropCode))
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
     * @return A list of <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO codes</a>
     * for all pests that the DSS models in the platform deal with in some way.
     * @responseExample application/json ["PSILRO","PHYTIN","SEPTAP"]
     */
    @GET
    @Path("pest")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(String[].class)
    public Response getAllPests() {
        try {
            Set<String> retVal = new HashSet<>();
            for (DSS dss : this.DSSController.getDSSListObj(true)) {
                dss.getModels().stream()
                	.filter(model -> model.getPests() != null)
                	.forEach(model -> retVal.addAll(model.getPests()));
            }
            return Response.ok().entity(retVal).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }

    }

    /**
     * @return A list of <a href="https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes">EPPO codes</a> for all crops
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
        	Set<String> retVal = new HashSet<>();
            for (DSS dss : this.DSSController.getDSSListObj(true)) {
                dss.getModels().stream()
            	.filter(model -> model.getCrops() != null)
            	.forEach(model -> retVal.addAll(model.getCrops()));
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
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     * 
     * @return the requested DSS and its models, regardless of validation
     * @pathExample /rest/model/no.nibio.vips
     */
    @GET
    @Path("dss/{DSSId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS.class)
    public Response getDSS(
    		@PathParam("DSSId") String DSSId,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
    	return this.getDSS(DSSId, null, language, executionType);
    }

    /**
     * Get all information about a specific DSS
     *
     * @param DSSId the id of the DSS
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     * @param platformValidated true or false
     * @return the requested DSS and its models, filtered by their validation status
     * @pathExample /rest/model/no.nibio.vips/platform_validated/true
     */
    @GET
    @Path("dss/{DSSId}/platform_validated/{platformValidated}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSS.class)
    public Response getDSS(
    		@PathParam("DSSId") String DSSId,
    		@PathParam("platformValidated") Boolean platformValidated,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
        	DSS matchingDSS = this.DSSController.getDSSById(DSSId, platformValidated, language, executionType);
            if (matchingDSS != null) {
                return Response.ok().entity(matchingDSS).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("errorMessage", "Could not find DSS with id " + DSSId)).build();
            }
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }

    }
    
    /**
     * Get all information about a specific DSS in YAML format
     *
     * @param DSSId the id of the DSS
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     * 
     * @return the requested DSS
     * @pathExample /rest/model/no.nibio.vips
     */
    @GET
    @Path("dss/{DSSId}/yaml")
    @Produces("application/x-yaml;charset=UTF-8")
    @TypeHint(DSS.class)
    public Response getDSSAsYAML(
    		@PathParam("DSSId") String DSSId,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
            Optional<DSS> matchingDSS = this.DSSController.getDSSListObj(null, language, executionType).stream().filter(dss -> dss.getId().equals(DSSId)).findFirst();
            if (matchingDSS.isPresent()) {
            	ObjectMapper YAMLWriter = new ObjectMapper(new YAMLFactory());
                return Response.ok().entity(YAMLWriter.writeValueAsString(matchingDSS.get())).build();
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
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     * 
     * @return The requested DSS model
     * @pathExample /rest/model/no.nibio.vips/PSILARTEMP
     */
    @GET
    @Path("model/{DSSId}/{ModelId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSSModel.class)
    public Response getDSSModel(
    		@PathParam("DSSId") String DSSId, 
    		@PathParam("ModelId") String ModelId,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
            Optional<DSS> matchingDSS = this.DSSController.getDSSListObj(null, language, executionType).stream().filter(dss -> dss.getId().equals(DSSId)).findFirst();
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
     * Get the input Json schema for a specific DSS model
     *
     * @param DSSId The id of the DSS containing the model
     * @param ModelId The id of the DSS model requested
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     * 
     * @return The input Json schema for the DSS model
     * @pathExample /rest/model/no.nibio.vips/PSILARTEMP/input_schema
     * @responseExample application/json {
  "type": "object",
  "properties": {
    "modelId": {
      "type": "string",
      "pattern": "^PSILARTEMP$",
      "title": "Model Id",
      "default": "PSILARTEMP",
      "description": "Must be PSILARTEMP"
    },
    "configParameters": {
      "title": "Configuration parameters",
      "type": "object",
      "properties": {
        "timeZone": {
          "type": "string",
          "title": "Time zone (e.g. Europe/Oslo)",
          "default": "Europe/Oslo"
        },
        "timeStart": {
          "type": "string",
          "format": "date",
          "title": "Start date of calculation (YYYY-MM-DD)"
        },
        "timeEnd": {
          "type": "string",
          "format": "date",
          "title": "End date of calculation (YYYY-MM-DD)"
        }
      },
      "required": [
        "timeZone",
        "timeStart",
        "timeEnd"
      ]
    },
    "weatherData": {
      "$ref": "https://platform.ipmdecisions.net/api/wx/rest/schema/weatherdata"
    }
  },
  "required": [
    "modelId",
    "configParameters"
  ]
}
     */
    @GET
    @Path("model/{DSSId}/{ModelId}/input_schema")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSSModel.class)
    public Response getDSSModelInputSchema(
    		@PathParam("DSSId") String DSSId, 
    		@PathParam("ModelId") String ModelId,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
            Optional<DSS> matchingDSS = this.DSSController.getDSSListObj(null, language, executionType).stream().filter(dss -> dss.getId().equals(DSSId)).findFirst();
            if (matchingDSS.isPresent()) {
                DSS actualDSS = matchingDSS.get();
                Optional<DSSModel> matchingDSSModel = actualDSS.getModels().stream().filter(model -> model.getId().equals(ModelId)).findFirst();
                if (matchingDSSModel.isPresent()) {
                    return Response.ok().entity(matchingDSSModel.get().getExecution().getInput_schema()).build();
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
     * Provide Json schema only for the parts that should be viewed in the platform's HTML UI form
     * @param DSSId The id of the DSS containing the model
     * @param ModelId The id of the DSS model requested
     * @param language two-letter code for language (<a href="https://www.loc.gov/standards/iso639-2/php/code_list.php">ISO-639-1</a>)
     * @param executionType filter for types of models. Example values are ONTHEFLY and LINK. See DSSModel.Execution for more
     * 
     * @return The input Json schema for the DSS model
     * @pathExample /rest/model/no.nibio.vips/PSILARTEMP/input_schema/ui_form
     * @responseExample application/json {
  "type": "object",
  "properties": {
    
    "configParameters": {
      "title": "Configuration parameters",
      "type": "object",
      "properties": {
        "timeZone": {
          "type": "string",
          "title": "Time zone (e.g. Europe/Oslo)",
          "default": "Europe/Oslo"
        },
        "timeStart": {
          "type": "string",
          "format": "date",
          "title": "Start date of calculation (YYYY-MM-DD)"
        },
        "timeEnd": {
          "type": "string",
          "format": "date",
          "title": "End date of calculation (YYYY-MM-DD)"
        }
      },
      "required": [
        "timeZone",
        "timeStart",
        "timeEnd"
      ]
    }
  },
  "required": [
    "configParameters"
  ]
}
     */
    @GET
    @Path("model/{DSSId}/{ModelId}/input_schema/ui_form")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(DSSModel.class)
    public Response getDSSModelUIFormSchema(
    		@PathParam("DSSId") String DSSId, 
    		@PathParam("ModelId") String ModelId,
    		@QueryParam("language") String language,
    		@QueryParam("executionType") String executionType
    		) {
        try {
            Optional<DSS> matchingDSS = this.DSSController.getDSSListObj(null, language, executionType).stream().filter(dss -> dss.getId().equals(DSSId)).findFirst();
            if (matchingDSS.isPresent()) {
                DSS actualDSS = matchingDSS.get();
                Optional<DSSModel> matchingDSSModel = actualDSS.getModels().stream().filter(model -> model.getId().equals(ModelId)).findFirst();
                if (matchingDSSModel.isPresent()) {
                	ObjectMapper om = new ObjectMapper();
                	JsonNode inputSchema = om.readTree(matchingDSSModel.get().getExecution().getInput_schema());
                	String[] hideThese = matchingDSSModel.get().getExecution().getInput_schema_categories().getHidden();
                	if(hideThese != null)
                	{
	                	for(String hideThis:hideThese)
	                	{
	                		//System.out.println("hideThis=" + hideThis);
	                		String[] hideThisPath = hideThis.split("\\.");
	                		String pathToHideThisParent = "/properties";
	                		for(int i=0;i<hideThisPath.length-1;i++)
	                		{
	                			pathToHideThisParent += "/" + hideThisPath[i];
	                			pathToHideThisParent += "/properties";
	                		}
	                		
	                		//System.out.println("pathToHideThisParent=" + pathToHideThisParent);
	                		ObjectNode parent = (ObjectNode) inputSchema.at(pathToHideThisParent);
	                		parent.remove(hideThisPath[hideThisPath.length-1]);
	                		// For getting the "required" attribute
	                		
	                		// Remove any "required" statements for hidden fields
	                		String pathToHideThisRequired = pathToHideThisParent.substring(0, pathToHideThisParent.lastIndexOf("/properties")) + "/required";
	                		ArrayNode required = (ArrayNode) inputSchema.at(pathToHideThisRequired);
	                		if(required != null)
	                		{
		                		for(int i=0;i<required.size();i++)
		                		{
		                			//System.out.println(required.get(i).asText() + "/" + hideThisPath[hideThisPath.length-1]);
		                			if(required.get(i).asText().equals(hideThisPath[hideThisPath.length-1]))
		                			{
		                				required.remove(i);
		                				break;
		                			}
		                		}
	                		}
	                	}
	                	
                	}
                	// Weather data part will always be hidden
                	if(inputSchema.findParent("weatherData") != null)
                	{
                		((ObjectNode)inputSchema.findParent("weatherData")).remove("weatherData");
                	}
                	
                    return Response.ok().entity(inputSchema).build();
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
     * @param geoJson valid <a href="https://geojson.org/">GeoJSON</a>
     * @return A list of all the matching DSS models
     */
    @POST
    @Path("dss/location")
    @Consumes("application/json")
    @Produces("application/json")
    @TypeHint(DSS[].class)
    public Response listModelsForLocation(
    		String geoJson, // Sent as POST data (in the request body)
    		@QueryParam("platformValidated") Boolean platformValidated,
            @QueryParam("executionType") String executionType,
            @QueryParam("language") String language
    		) {
        try {
            FeatureCollection clientFeatures = (FeatureCollection) GeoJSONFactory.create(geoJson);
            GeoJSONReader reader = new GeoJSONReader();
            // Get all geometries in request
            List<Geometry> clientGeometries = new ArrayList<>();
            for (Feature feature : clientFeatures.getFeatures()) {
                Geometry geom = reader.read(feature.getGeometry());
                clientGeometries.add(geom);
            }
            // Loop through all DSS models
            // Return only data sources with geometries intersecting with the client's
            // specified geometries
            GISUtils gisUtils = new GISUtils();
            
            List<DSS> retVal = new ArrayList<>();
            LOGGER.debug("platformValidated is " + platformValidated);
            for (DSS dss : this.DSSController.getDSSListObj(platformValidated, language, executionType)) {

                List<DSSModel> matchingModels = dss.getModels().stream()
                        .filter(model -> {
                            String modelGeoJsonStr = "";
                            try
                            {
                                modelGeoJsonStr  = model.getValid_spatial().getGeoJSON();
                            }
                            catch(NullPointerException ex)
                            {
                                //return false; // We should pass, check for matching countries
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
                                if(!modelGeoJsonStr.isBlank() && ! gisUtils.isGeoJsonStringEmpty(modelGeoJsonStr))
                                {
                                    modelFeatures.addAll(Arrays.asList(
                                    		((FeatureCollection) GeoJSONFactory.create(modelGeoJsonStr)).getFeatures()
                                    		));
                                }
                            }catch(RuntimeException ex) {LOGGER.debug(ex.getMessage());}
                            
                            // Match with all geometries in request. If found, add data source to
                            // list of matching data sources
                            List<Geometry> modelGeometries = modelFeatures.stream()
                                    .map(f -> reader.read(f.getGeometry()))
                                    .filter(modelGeometry -> {
                                        boolean matching = false;
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
            @QueryParam("longitude") Double longitude,
            @QueryParam("executionType") String executionType,
            @QueryParam("platformValidated") Boolean platformValidated,
            @QueryParam("language") String language
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
        return this.listModelsForLocation(writer.write(features).toString(), platformValidated, executionType, language);
    }

    /**
     * Lists all the countries for which DSS models exist
     * @param platformValidated true or false (default). If true, return information only for
     *                          DSS models that have platform_validated set to true in the metadata
     * @return
     */
    @GET
    @Path("countries")
    @Produces("application/json")
    public Response getDSSCountries(
            @QueryParam("platformValidated") Boolean platformValidated
    )
    {
        try {
            List<DSS> allDSSs = this.DSSController.getDSSListObj(platformValidated);
            Set<String> countries = new HashSet<>();
            for(DSS dss:allDSSs)
            {
                for(DSSModel dssModel:dss.getModels())
                {
                    if(dssModel.getValid_spatial() != null && dssModel.getValid_spatial().getCountries() != null)
                    {
                        countries.addAll(Arrays.asList(dssModel.getValid_spatial().getCountries()));
                    }
                }
            }
            return Response.ok().entity(countries).build();
        } catch (IOException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    /**
     *
     * @param logoFileName The file name of the logo.
     * @return
     */
    @GET
    @Path("dss/logo/{logoFileName}")
    public Response getDSSLogo(@PathParam("logoFileName") String logoFileName){
        try
        {
            InputStream logo = this.DSSController.getLogo(logoFileName);
            if(logo != null) {
                Response.ResponseBuilder response = Response.ok().entity(logo);
                // Check file ending to decide image mime type
                String mimeType = "image/jpg";
                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                mimeType = fileNameMap.getContentTypeFor(logoFileName);
                response.header("Content-Type", mimeType);
                return response.build();
            }
            else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        catch(NullPointerException | IllegalArgumentException ex)
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
