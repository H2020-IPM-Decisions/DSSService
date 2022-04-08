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

package net.ipmdecisions.dssservice.entity;

import com.webcohesion.enunciate.metadata.DocumentationExample;
import java.util.List;

/**
 * Represents a pest prediction model belonging to a DSS. The data in this object
 * should be sufficient for the platform to describe to the client (farmer, advisor)
 * what the model does and how it does it. It should also provide the platform 
 * with enough information/meta data for the platform to be able to execute
 * the model.
 * 
 * @copyright 2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class DSSModel {
    private String name, id, version, purpose, description, type_of_decision, type_of_output, description_URL, citation, keywords;
    private Boolean platform_validated;
    private List<String> pests, crops;
    private List<Author> authors;
    private Execution execution;
    private Input input;
    private Valid_Spatial valid_spatial;
    private Output output;
    
    /**
     * Describes the output returned by the DSS model. The output must conform
     * to the Json schema https://ipmdecisions.nibio.no/api/dss/rest/schema/modeloutput
     */
    public static class Output {
        private WarningStatusInterpretation[]  warning_status_interpretation;
        private ResultParameter[] result_parameters;
        private ChartGroup[] chart_groups;
        private String chart_heading;
        
        public static class WarningStatusInterpretation {
        	private String explanation, recommended_action;

			/**
			 * @return the explanation
			 */
			public String getExplanation() {
				return explanation;
			}

			/**
			 * @param explanation the explanation to set
			 */
			public void setExplanation(String explanation) {
				this.explanation = explanation;
			}

			/**
			 * @return the recommended_action
			 */
			public String getRecommended_action() {
				return recommended_action;
			}

			/**
			 * @param recommended_action the recommended_action to set
			 */
			public void setRecommended_action(String recommended_action) {
				this.recommended_action = recommended_action;
			}
        	
        }
        
        public static class ChartGroup {
        	private String id, title;
        	private String[] result_parameter_ids;
			/**
			 * @return the title
			 */
			public String getTitle() {
				return title;
			}
			/**
			 * @param title the title to set
			 */
			public void setTitle(String title) {
				this.title = title;
			}
			/**
			 * @return the id
			 */
			public String getId() {
				return id;
			}
			/**
			 * @param id the id to set
			 */
			public void setId(String id) {
				this.id = id;
			}
			/**
			 * @return the result_parameter_ids
			 */
			public String[] getResult_parameter_ids() {
				return result_parameter_ids;
			}
			/**
			 * @param result_parameter_ids the result_parameter_ids to set
			 */
			public void setResult_parameter_ids(String[] result_parameter_ids) {
				this.result_parameter_ids = result_parameter_ids;
			}
        }
        
        /**
         * A result or intermediary from a DSS model. These are distinct for 
         * each DSS model
         */
        public static class ResultParameter {
            private String id, title, description;
            private Chart_info chart_info;

            static class Chart_info {
            	private boolean default_visible;
            	private String unit, color, chart_type;
            	
				public boolean isDefault_visible() {
					return default_visible;
				}
				
				public void setDefault_visible(boolean default_visible) {
					this.default_visible = default_visible;
				}
				
				@DocumentationExample("&deg;C")
				public String getUnit() {
					return unit;
				}
				
				public void setUnit(String unit) {
					this.unit = unit;
				}

				@DocumentationExample("#00ccff")
				public String getColor() {
					return color;
				}

				public void setColor(String color) {
					this.color = color;
				}

				public String getChart_type() {
					return chart_type;
				}

				public void setChart_type(String chart_type) {
					this.chart_type = chart_type;
				}
            }
            
            /**
             * @return The Id, which is combined with the DSS id and model id as 
             * name space to create a unique ID. For example no.nibio.vips.PSILARTEMP.TMDD5C
             */
            @DocumentationExample("TMDD5C")
            public String getId() {
                return id;
            }

            /**
             * @param id the id to set
             */
            public void setId(String id) {
                this.id = id;
            }

            /**
             * @return The parameter title, e.g. "Accumulated day degrees" or "Calculated RISK value"
             */
            @DocumentationExample("Accumulated day degrees")
            public String getTitle() {
                return title;
            }

            /**
             * @param title the title to set
             */
            public void setTitle(String title) {
                this.title = title;
            }

            /**
             * @return Optionally, a description of the parameter
             */
            @DocumentationExample("A thorough description of the parameter goes here.")
            public String getDescription() {
                return description;
            }

            /**
             * @param description the description to set
             */
            public void setDescription(String description) {
                this.description = description;
            }

			public Chart_info getChart_info() {
				return chart_info;
			}

			public void setChart_info(Chart_info chart_info) {
				this.chart_info = chart_info;
			}
        }

        /**
         * @return A thorough description of how to interpret the GRAY(0)/BLUE(0)/GREEN(2)/YELLOW(3)/RED(4) warning status codes
         * @documentationExample [
                                        		{
                                        			"explanation": "The flight period of the 1st generation is over",
                                        			"recommended_action": "Consult your advisory service"
                                        		},
                                        		{
                                        			"explanation": "",
                                        			"recommended_action": ""
                                        		},
                                        		{
                                        			"explanation": "The flight period has not yet begun",
                                        			"recommended_action": "Consult your advisory service"
                                        		},
                                        		{
                                        			"explanation": "The flight period is beginning, flies can be coming into the field",
                                        			"recommended_action": "Consult your advisory service"
                                        		},
                                        		{
                                        			"explanation":"Peak flight period",
                                        			"recommended_action":"Consult your advisory service"
                                        		}
                                        	]
         */
        public WarningStatusInterpretation[] getWarning_status_interpretation() {
            return warning_status_interpretation;
        }

        /**
         * @param warning_status_interpretation the warning_status_interpretation to set
         */
        public void setWarning_status_interpretation(WarningStatusInterpretation[] warning_status_interpretation) {
            this.warning_status_interpretation = warning_status_interpretation;
        }

        /**
         * @return The result_parameters returned by the DSS model
         */
        public ResultParameter[] getResult_parameters() {
            return result_parameters;
        }

        /**
         * @param result_parameters the result_parameters to set
         */
        public void setResult_parameters(ResultParameter[] result_parameters) {
            this.result_parameters = result_parameters;
        }

        @DocumentationExample("Day degree chart")
		public String getChart_heading() {
			return chart_heading;
		}

		public void setChart_heading(String chart_heading) {
			this.chart_heading = chart_heading;
		}

		/**
		 * @return the chart_groups
		 */
		@DocumentationExample("[{\"id\": \"G1\",\"title\":\"Day degrees\",\"result_parameter_ids\": [\"TMDD5C\",\"FOOBAR\"]},{\"id\": \"G2\",\"title\":\"Thresholds\",\"result_parameter_ids\": [\"THRESHOLD_1\",\"THRESHOLD_2\",\"THRESHOLD_3\"]}]")
		public ChartGroup[] getChart_groups() {
			return chart_groups;
		}

		/**
		 * @param chart_groups the chart_groups to set
		 */
		public void setChart_groups(ChartGroup[] chart_groups) {
			this.chart_groups = chart_groups;
		}
    }
    
    /**
     * Where is the model considered valid to run? This can be specified 
     * either by a list of countries (using ISO-3166-1 Three-letter country codes 
     * https://en.wikipedia.org/wiki/ISO_3166-1#Current_codes) OR by specifying
     * a custom polygon using GeoJSON https://geojson.org/
     * 
     * Or both!
     * Check out: https://github.com/datasets/geo-countries / https://datahub.io/core/geo-countries 
     * 
     * 
     * 
     */
    public static class Valid_Spatial {
        private String[] countries;
        private String geoJSON;

        /**
         * 
         * @return a list of countries (using ISO-3166-1 Three-letter country codes 
         * https://en.wikipedia.org/wiki/ISO_3166-1#Current_codes)
         */
        @DocumentationExample(value = "NOR", value2="SWE")
        public String[] getCountries() {
            return countries;
        }

        /**
         * @param countries the countries to set
         */
        public void setCountries(String[] countries) {
            this.countries = countries;
        }

        /**
         * @return the geoJSON
         */
        public String getGeoJSON() {
            return geoJSON;
        }

        /**
         * @param geoJSON the geoJSON to set
         */
        public void setGeoJSON(String geoJSON) {
            this.geoJSON = geoJSON;
        }
    }
    
    /**
     * Represents a person responsible for the prediction model
     */
    static class Author {
        private String name, email, organization;

        /**
         * @return The name of the author
         */
        @DocumentationExample("Berit Nordskog")
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return The author's email
         */
        @DocumentationExample("acme@foobar.com")
        public String getEmail() {
            return email;
        }

        /**
         * @param email the email to set
         */
        public void setEmail(String email) {
            this.email = email;
        }

        /**
         * @return The Organization of the author (may differ from the DSSs organization)
         */
        @DocumentationExample("NIBIO")
        public String getOrganization() {
            return organization;
        }

        /**
         * @param organization the organization to set
         */
        public void setOrganization(String organization) {
            this.organization = organization;
        }
    }
    
    /**
     * Meta data about how to run the model
     */
    public static class Execution {
        private String type, endpoint, form_method, content_type, input_schema;
        private InputSchemaCategories input_schema_categories;
        
        /**
         * @return The type of execution. As of now, the only valid value is
         * ONTHEFLY, meaning that it can be run directly from the platform
         */
        @DocumentationExample("ONTHEFLY")
        public String getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * @return The URL (to a web service) for the platform to execute the model
         */
        @DocumentationExample("https://coremanager.vips.nibio.no/models/PSILARTEMP/run/ipmd")
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * @param endpoint the endpoint to set
         */
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        /**
         * @return The HTTP method that the platform should use to access the endpoint.
         * Values are [get, post]
         */
        @DocumentationExample("post")
        public String getForm_method() {
            return form_method;
        }

        /**
         * @param form_method the form_method to set
         */
        public void setForm_method(String form_method) {
            this.form_method = form_method;
        }

        /**
         * @return The Media content type of the input data being submitted
         * Defined by IANA: https://www.iana.org/assignments/media-types/media-types.xhtml . 
         * Example: application/json
         */
        @DocumentationExample("application/json")
        public String getContent_type() {
            return content_type;
        }

        /**
         * @param content_type the content_type to set
         */
        public void setContent_type(String content_type) {
            this.content_type = content_type;
        }

        /**
         * @return Json schema (https://json-schema.org/) that describes the model's 
        input result_parameters. The platform can 
        use it to build and validate forms for the client
        in addition to see if data such as weather data are part of the input.
        Must be used together with the input property, that further describes
        commonly defined types of input data such as weather data and field observations
        <h2>Default value templates</h2>
        <p>We have added a simple templating system for expressing default values. Templates are enclosed in curly
        brackets {}. The first template added is for CURRENT_YEAR. This is an integer that can be changed by simple
        arithmetics like addition and substractin. E.g. for the user to enter a sowing date of last year (e.g. winter
        wheat),it would be expressed like this: "default": "{CURRENT_YEAR-1}-09-15" </p>
        * @jsonExampleOverride {
        "type":"object",
        "properties": {
          "modelId": {"type": "string", "pattern":"^PSILARTEMP$", "title": "Model Id", "default":"PSILARTEMP", "description":"Must be PSILARTEMP"},
          "configParameters": {
            "title":"Configuration parameters",
            "type": "object",
            "properties": {
              "timeZone": {"type": "string", "title": "Time zone (e.g. Europe/Oslo)", "default":"Europe/Oslo"},
              "timeStart": {"type":"string","format": "date", "default": "{CURRENT_YEAR}-03-01", "title": "Start date of calculation (YYYY-MM-DD)"},
              "timeEnd": {"type":"string","format": "date", "default": "{CURRENT_YEAR}-09-01", "title": "End date of calculation (YYYY-MM-DD)"}
            },
            "required": ["timeZone","timeStart","timeEnd"]
          },
          "weatherData": {
            "$ref": "https://ipmdecisions.nibio.no/api/wx/rest/schema/weatherdata"
          }
        },
        "required": ["modelId","configParameters"]
      }
         */
        
        public String getInput_schema() {
            return input_schema;
        }

        /**
         * @param input_schema the input_schema to set
         */
        public void setInput_schema(String input_schema) {
            this.input_schema = input_schema;
        }

		/**
		 * @return the input_schema_categories
		 */
		public InputSchemaCategories getInput_schema_categories() {
			return input_schema_categories;
		}

		/**
		 * @param input_schema_categories the input_schema_categories to set
		 */
		public void setInput_schema_categories(InputSchemaCategories input_schema_categories) {
			this.input_schema_categories = input_schema_categories;
		}

    }
    
    /**
     * Description of this model's requirement for weather data and field observations
     * Must be used in conjunction with the model's input_schema
     */
    static class Input {
        private List<WeatherParameter> weather_parameters;
        private FieldObservation field_observation;
        private DSSDate[] weather_data_period_start, weather_data_period_end;

        /**
         * @return Specification of the weather result_parameters required by the model
         */
        public List<WeatherParameter> getWeather_parameters() {
            return weather_parameters;
        }

        /**
         * @param weather_parameters the weather to set
         */
        public void setWeather_parameters(List<WeatherParameter> weather_parameters) {
            this.weather_parameters = weather_parameters;
        }

        /**
         * @return Specification of the field observations required by the model
         */
        public FieldObservation getField_observation() {
            return field_observation;
        }

        /**
         * @param field_observation the field_observation to set
         */
        public void setField_observation(FieldObservation field_observation) {
            this.field_observation = field_observation;
        }

		public DSSDate[] getWeather_data_period_start() {
			return weather_data_period_start;
		}

		public void setWeather_data_period_start(DSSDate[] weather_data_period_start) {
			this.weather_data_period_start = weather_data_period_start;
		}

		public DSSDate[] getWeather_data_period_end() {
			return weather_data_period_end;
		}

		public void setWeather_data_period_end(DSSDate[] weather_data_period_end) {
			this.weather_data_period_end = weather_data_period_end;
		}
        
    }
    
    static class DSSDate {
    	private String determined_by, value;
    	
    	public String getDetermined_by()
    	{
    		return determined_by;
    	}
    	public void setDetermined_by(String determined_by)
    	{
    		this.determined_by = determined_by;
    	}
    	
    	public String getValue()
    	{
    		return value;
    	}
    	
    	public void setValue(String value)
    	{
    		this.value = value;
    	}
    }
    
    /**
     * List of species for which a field observation required by the model. Not 
     * to be confused with the fieldObservation property in an input_schema
     */
    static class FieldObservation{
        private List<String> species;

        /**
         * @return EPPO code for the observation's species. 
         * See https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes
         */
        @DocumentationExample(value="SEPTAP")
        public List<String> getSpecies() {
            return species;
        }

        /**
         * @param species the species to set
         */
        public void setSpecies(List<String> species) {
            this.species = species;
        }
    }
    
    /**
     * Description of the weather result_parameters needed by the model
     */
    static class WeatherParameter {
        private int parameter_code;
        private int interval;

        /**
         * @return The parameter, as defined here: https://ipmdecisions.nibio.no/api/wx/rest/parameter
         */
        @DocumentationExample(value="1002", value2="2001")
        public int getParameter_code() {
            return parameter_code;
        }

        /**
         * @param parameter_code the parameter_code to set
         */
        public void setParameter_code(int parameter_code) {
            this.parameter_code = parameter_code;
        }

        /**
         * @return The sampling frequency, typically whether this is hourly or
         * daily data. Measured in seconds between each timestamp. So 
         * hourly == 3600 and daily = 86400
         * 
         */
        @DocumentationExample(value="3600", value2="3600")
        public int getInterval() {
            return interval;
        }

        /**
         * @param interval the interval to set
         */
        public void setInterval(int interval) {
            this.interval = interval;
        }
    }

    public static class InputSchemaCategories {
		private String[] hidden, system, user_init, triggered, internal;
	
		/**
		 * @return the internal
		 */
		public String[] getInternal() {
			return internal != null ? internal : new String[0];
		}
	
		/**
		 * @param internal the internal to set
		 */
		public void setInternal(String[] internal) {
			this.internal = internal;
		}
	
		/**
		 * @return the triggered
		 */
		public String[] getTriggered() {
			return triggered!= null ? triggered : new String[0];
		}
	
		/**
		 * @param triggered the triggered to set
		 */
		public void setTriggered(String[] triggered) {
			this.triggered = triggered;
		}
	
		/**
		 * @return the user_init
		 */
		public String[] getUser_init() {
			return user_init!= null ? user_init : new String[0];
		}
	
		/**
		 * @param user_init the user_init to set
		 */
		public void setUser_init(String[] user_init) {
			this.user_init = user_init;
		}
	
		/**
		 * @return the system
		 */
		public String[] getSystem() {
			return system!= null ? system : new String[0];
		}
	
		/**
		 * @param system the system to set
		 */
		public void setSystem(String[] system) {
			this.system = system;
		}
	
		/**
		 * @return the hidden
		 */
		public String[] getHidden() {
			return hidden!= null ? hidden : new String[0];
		}
	
		/**
		 * @param hidden the hidden to set
		 */
		public void setHidden(String[] hidden) {
			this.hidden = hidden;
		}
	
	}

	/**
     * @return The name of the model. E.g. "DOWNCAST" or "Nærstad's model"
     */
    @DocumentationExample("Carrot rust fly temperature model")
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The id for the model, unique for this DSS. E.g. "PSILARTEMP"
     * The DSS ID + Version + Model Id + Version is used to keep track of the 
     * usage of the DSS as it changes over time
     */
    @DocumentationExample("PSILARTEMP")
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The id for the model, unique for this DSS. E.g. "1.01"
     * The DSS ID + Version + Model Id + Version is used to keep track of the 
     * usage of the DSS as it changes over time
     */
    @DocumentationExample("1.0")
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return The type of decision this model supports. E.g. "Short-term tactical"
     * TODO: get all the different categories here
     */
    @DocumentationExample("Short-term tactical")
    public String getType_of_decision() {
        return type_of_decision;
    }

    /**
     * @param type_of_decision the type_of_decision to set
     */
    public void setType_of_decision(String type_of_decision) {
        this.type_of_decision = type_of_decision;
    }

    /**
     * @return What kind of output do you get? A risk indication? Specific 
     * advice for how to spray or treat otherwise? TODO: Needs more systematic
     * categorization OR to be free form text
     */
    @DocumentationExample("Risk indication")
    public String getType_of_output() {
        return type_of_output;
    }

    /**
     * @param type_of_output the type_of_output to set
     */
    public void setType_of_output(String type_of_output) {
        this.type_of_output = type_of_output;
    }

    /**
     * @return A URL to the DSS's own description of the model (or other type 
     * of publication)
     */
    @DocumentationExample("https://www.vips-landbruk.no/forecasts/models/PSILARTEMP/")
    public String getDescription_URL() {
        return description_URL;
    }

    /**
     * @param description_URL the description_URL to set
     */
    public void setDescription_URL(String description_URL) {
        this.description_URL = description_URL;
    }

    /**
     * @return A description of the model
     * @jsonExampleOverride "The warning system model «Carrot rust fly temperature» is based on a Finnish temperature-based model (Markkula et al, 1998; Tiilikkala & Ojanen, 1999; Markkula et al, 2000). The model determines the start of the flight period for the 1st and 2nd generation of carrot rust fly based on accumulated degree-days (day-degrees) over a base temperature of 5,0 °C. VIPS uses the model for the 1st generation only. "
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return DOI https://www.doi.org/ for publications related to the model
     */
    public String getCitation() {
        return citation;
    }

    /**
     * @param citation the citation to set
     */
    public void setCitation(String citation) {
        this.citation = citation;
    }

    /**
     * @return Meta data about how to run the model
     */
    public Execution getExecution() {
        return execution;
    }

    /**
     * @param execution the execution to set
     */
    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    /**
     * @return Description of this model's requirement for weather data and field observations
     * Must be used in conjunction with the model's input_schema
     */
    public Input getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(Input input) {
        this.input = input;
    }

    /**
     * @return The authors responsible for this model
     */
    public List<Author> getAuthors() {
        return authors;
    }

    /**
     * @param authors the authors to set
     */
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    /**
     * @return EPPO codes (https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes) 
     * for the pests that this model deals with.
     */
    @DocumentationExample("PSILRO")
    public List<String> getPests() {
        return pests;
    }

    /**
     * @param pests the pests to set
     */
    public void setPests(List<String> pests) {
        this.pests = pests;
    }

    /**
     * @return EPPO codes (https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes) 
     * for the crops that this model deals with.
     */
    @DocumentationExample("DAUCS")
    public List<String> getCrops() {
        return crops;
    }

    /**
     * @param crops the crops to set
     */
    public void setCrops(List<String> crops) {
        this.crops = crops;
    }

    /**
     * @return Keywords to associate with this model. AKA hashtags (#)
     * E.g. #regression #dacuscarota #norway
     */
    @DocumentationExample("#regression #dacuscarota #norway")
    public String getKeywords() {
        return keywords;
    }

    /**
     * @param keywords the keywords to set
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * @return The output from the DSS model
     */
    public Output getOutput() {
        return output;
    }

    /**
     * @param output the output to set
     */
    public void setOutput(Output output) {
        this.output = output;
    }

    /**
     * Where is the model considered valid to run? This can be specified 
     * either by a list of countries (using ISO-3166-1 Three-letter country codes 
     * https://en.wikipedia.org/wiki/ISO_3166-1#Current_codes) OR by specifying
     * a custom polygon using GeoJSON https://geojson.org/
     *
     * @return the valid_spatial
     */
    public Valid_Spatial getValid_spatial() {
        return valid_spatial;
    }

    /**
     * @param valid_spatial the valid_spatial to set
     */
    public void setValid_spatial(Valid_Spatial valid_spatial) {
        this.valid_spatial = valid_spatial;
    }

	/**
	 * @return the platform_validated
	 */
	public Boolean getPlatform_validated() {
		return platform_validated != null ? this.platform_validated : false;
	}

	/**
	 * @param platform_validated the platform_validated to set
	 */
	public void setPlatform_validated(Boolean platform_validated) {
		this.platform_validated = platform_validated;
	}

	/**
	 * @return the purpose
	 * @DocumentationExample("Estimates risk of transmission of virus by aphid vectors")
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * @param purpose the purpose to set
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
}
