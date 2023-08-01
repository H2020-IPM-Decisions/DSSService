[Back](index.md)
# 1. Create a DSS model request based on metadata
So, you have found a model that you want to run. Given the metadata provided, let's go through the steps needed to automatically create a request and send it to the DSS.

Let's use the VIPS/PSILARTEMP (Carrot rust fly temperature) model as an example. The relevant metadata is provided below

``` json
{
            "name": "Carrot rust fly temperature model",
            "id": "PSILARTEMP",
            "version": "1.0",
            "type_of_decision": "Short-term tactical",
            "type_of_output": "Risk indication",
            "description_URL": "https://www.vips-landbruk.no/forecasts/models/PSILARTEMP/",
            "citation": null,
            "keywords": "none",
            "pests": [
                "PSILRO"
            ],
            "crops": [
                "DAUCS"
            ],
            "authors": [
                {
                    "name": "Berit Nordskog",
                    "email": "berit.nordskog@nibio.no",
                    "organization": "NIBIO"
                }
            ],
            "execution": {
                "type": "ONTHEFLY",
                "endpoint": "https://coremanager.vips.nibio.no/models/PSILARTEMP/run/ipmd",
                "form_method": "post",
                "content_type": "application/json",
                "input_schema": "{\n  \"type\":\"object\",\n  \"properties\": {\n    \"modelId\": {\"type\": \"string\", \"pattern\":\"^PSILARTEMP$\", \"title\": \"Model Id\", \"default\":\"PSILARTEMP\", \"description\":\"Must be PSILARTEMP\"},\n    \"configParameters\": {\n      \"title\":\"Configuration parameters\",\n      \"type\": \"object\",\n      \"properties\": {\n        \"timeZone\": {\"type\": \"string\", \"title\": \"Time zone (e.g. Europe/Oslo)\", \"default\":\"Europe/Oslo\", \"options\":{\"infoText\":\"The time zone information is used when hourly temperature values need to be converted to daily.\"}},\n        \"timeStart\": {\"type\":\"string\",\"format\": \"date\", \"title\": \"Start date of calculation (YYYY-MM-DD)\"},\n        \"timeEnd\": {\"type\":\"string\",\"format\": \"date\", \"title\": \"End date of calculation (YYYY-MM-DD)\"}\n      },\n      \"required\": [\"timeZone\",\"timeStart\",\"timeEnd\"]\n    },\n    \"weatherData\": {\n      \"$ref\": \"https://ipmdecisions.nibio.no/api/wx/rest/schema/weatherdata\"\n    }\n  },\n  \"required\": [\"modelId\",\"configParameters\"]\n}\n",
                "input_schema_categories": {
                    "hidden": [
                        "modelId"
                    ],
                    "system": null,
                    "user_init": null,
                    "triggered": null,
                    "internal": null
                }
            },
            "input": {
                "weather_parameters": [
                    {
                        "parameter_code": 1002,
                        "interval": 86400
                    }
                ],
                "field_observation": null,
                "weather_data_period_start": {
                    "determined_by": "INPUT_SCHEMA_PROPERTY",
                    "value": "configParameters.timeStart"
                },
                "weather_data_period_end": {
                    "determined_by": "INPUT_SCHEMA_PROPERTY",
                    "value": "configParameters.timeEnd"
                }
            },
            "valid_spatial": {
                "countries": [
                    "NOR"
                ],
                "geoJSON": "{}"
            },
            "output": {
                "warning_status_interpretation": "Green warning indicates that the flight period has not yet begun.\nYellow warning indicates that the flight period is beginning and that flies can be coming into the field.\nRed warning indicates peak flight period.\nGrey warning indicates that the flight period of the 1st generation is over.\nBe aware that in areas with field covers (plastic, single or double non-woven covers, etc.) with early crops the preceding season (either on the current field or neighboring fields), the flight period can start earlier due to higher soil temperature under the covers.",
                "result_parameters": [
                    {
                        "id": "TMDD5C",
                        "title": "Accumulated day degrees",
                        "description": "The accumulated day degrees with a base temperature of 5 degrees celcius",
                        "chart_info": {
                            "default_visible": true,
                            "unit": "&deg;C",
                            "color": "#0033cc",
                            "chart_type": "spline"
                        }
                    },
                    {
                        "id": "THRESHOLD_1",
                        "title": "Threshold for start of flight period",
                        "description": "When the accumulated day degrees exceed this threshold, the flight period is starting up",
                        "chart_info": {
                            "default_visible": true,
                            "unit": "&deg;C",
                            "color": "#ffff00",
                            "chart_type": "spline"
                        }
                    },
                    {
                        "id": "THRESHOLD_2",
                        "title": "Threshold for peak flight period",
                        "description": "When the accumulated day degrees exceed this threshold, you enter the peak flight period",
                        "chart_info": {
                            "default_visible": true,
                            "unit": "&deg;C",
                            "color": "#ff0000",
                            "chart_type": "spline"
                        }
                    },
                    {
                        "id": "THRESHOLD_3",
                        "title": "Threshold for end of 1st generation flight period",
                        "description": "When the accumulated day degrees exceed this threshold, the 1st generation flight period is over",
                        "chart_info": {
                            "default_visible": true,
                            "unit": "&deg;C",
                            "color": "#999999",
                            "chart_type": "spline"
                        }
                    }
                ],
                "chart_groups": [
                    {
                        "id": "G1",
                        "title": "Day degrees",
                        "result_parameter_ids": [
                            "TMDD5C",
                            "THRESHOLD_1",
                            "THRESHOLD_2",
                            "THRESHOLD_3"
                        ]
                    }
                ],
                "chart_heading": "Accumulated day degrees"
            },
            "description": {
                "other": "The warning system model «Carrot rust fly temperature» is based on a Finnish temperature-based model (Markkula et al, 1998; Tiilikkala & Ojanen, 1999; Markkula et al, 2000). The model determines the start of the flight period for the 1st and 2nd generation of carrot rust fly based on accumuleted degree-days (day-degrees) over a base temperature of 5,0 °C. VIPS uses the model for the 1st generation only. \nStandard air temperature (temperature measured 2 m above ground) is used in the model. Degree-days are defined for this model as the sum of the difference between a base temperature of 5,0 °C and the mean temperature for all days with a temperature >5,0 °C, in other words (daily mean temperature – 5,0 °C) from 1 March (beginning when the ground has thawed).\n",
                "created_by": "",
                "age": "",
                "peer_review": "",
                "case_studies": null
            }
        }
```
Here's the input_schema pretty printed

``` json
{
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
			"required": ["timeZone", "timeStart", "timeEnd"]
		},
		"weatherData": {
			"$ref": "https://ipmdecisions.nibio.no/api/wx/rest/schema/weatherdata"
		}
	},
	"required": ["modelId", "configParameters"]
}
```
In order to run the model, you need to create Json data that conforms to this schema, and send it over HTTP to the endpoint. This schema can be transformed into a self-validation HTML form automatically, using tranformation tools like [JSON Editor](https://github.com/json-editor/json-editor). This editor also generates the conforming JSON that you  need. You can even [tinker with it online](https://json-editor.github.io/json-editor/).

Here's a sample of conforming Json (The period has been made artificially short for illustration purposes)

``` json
{
  "modelId": "PSILARTEMP",
  "configParameters": {
    "timeZone": "Europe/Oslo",
    "timeStart": "2020-05-01",
    "timeEnd": "2020-05-03"
  },
  "weatherData": {
    "timeStart": "2020-04-30T22:00:00Z",
    "timeEnd": "2020-05-02T22:00:00Z",
    "interval": 86400,
    "weatherParameters": [
        1002
    ],
    "locationWeatherData": [
        {
            "longitude": 10.781989,
            "latitude": 59.660468,
            "altitude": 94.0,
            "data": [
                [
                    5.7
                ],
                [
                    8.2
                ],
                [
                    8.5
                ]
            ],
            "length": 3,
            "width": 1
        }
    ]
  }
}
```
According to the metadata, the client should send this to the model endpoint (which in this case is [https://coremanager.vips.nibio.no/models/PSILARTEMP/run/ipmd](https://coremanager.vips.nibio.no/models/PSILARTEMP/run/ipmd)) using the HTTP POST method. See the DSS Postman test suite for more details. 

What is returned, is a set of result objects. It conforms to [the modeloutput schema](https://ipmdecisions.nibio.no/api/dss/rest/schema/modeloutput), and in our example it looks like this. The parameters are (or should be ) documented in the model metadata (see above).

``` json
{
	"timeStart": "2020-04-30T22:00:00Z",
	"timeEnd": "2020-05-02T22:00:00Z",
	"interval": 86400,
	"resultParameters": [
		"TMDD5C",
		"THRESHOLD_1",
		"THRESHOLD_2",
		"TMD5C",
		"TMD",
		"THRESHOLD_3"
	],
	"locationResult": [{
		"longitude": null,
		"latitude": null,
		"altitude": null,
		"data": [
			[
				0.7,
				260.0,
				360.0,
				0.7,
				5.7,
				560.0
			],
			[
				3.9,
				260.0,
				360.0,
				3.2,
				8.2,
				560.0
			],
			[
				7.4,
				260.0,
				360.0,
				3.5,
				8.5,
				560.0
			]
		],
		"warningStatus": [
			2,
			2,
			2
		],
		"length": 3,
		"width": 6
	}]
}
```
## Create a DSS model request with field observations
Looking at the VIPS's observation based carrot rust fly model (PSILAROBSE), we have this input_schema:

```json
{
  "type": "object",
  "properties": {
    "modelId": {
      "type": "string",
      "pattern": "^PSILAROBSE$",
      "title": "Model Id",
      "default": "PSILAROBSE",
      "description": "Must be PSILAROBSE"
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
        "startDateCalculation": {
          "type": "string",
          "format": "date",
          "default": "{CURRENT_YEAR}-03-01",
          "title": "Start date of calculation (YYYY-MM-DD)"
        },
        "endDateCalculation": {
          "type": "string",
          "format": "date",
          "default": "{CURRENT_YEAR}-09-01",
          "title": "End date of calculation (YYYY-MM-DD)"
        },
        "fieldObservations": {
          "title": "Field observations",
          "type": "array",
          "items": {
            "type": "object",
            "title": "Field observation",
            "properties": {
              "fieldObservation": {
                "title": "Generic field observation information",
                "$ref": "https://platform.ipmdecisions.net/api/dss/rest/schema/fieldobservation"
              },
              "quantification": {
                "$ref": "#/definitions/fieldObs_PSILRO"
              }
            }
          }
        }
      },
      "required": [
        "timeZone",
        "startDateCalculation",
        "endDateCalculation"
      ]
    }
  },
  "required": [
    "modelId",
    "configParameters"
  ],
  "definitions": {
    "fieldObs_PSILRO": {
      "title": "Psila rosae quantification",
      "properties": {
        "trapCountCropEdge": {
          "title": "Insect trap count at the edge of the field",
          "type": "integer"
        },
        "trapCountCropInside": {
          "title": "Insect trap count inside the field",
          "type": "integer"
        }
      },
      "required": [
        "trapCountCropEdge",
        "trapCountCropInside"
      ]
    }
  }
}

```
The field observations definition is a bit complicated, because it consists of some common, standard parts, but also parts that vary between pests. Common parts are:
* Time - when was the observation made?
* Location - where was the observation made?
* What has been observed?
* In which crop was it observed?

Here's the [schema for the common parts](https://platform.ipmdecisions.net/api/dss/rest/schema/fieldobservation):

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "title": "Field observation",
  "type": "object",
  "additionalProperties": true,
  "description": "Version 0.9. The schema describes the field observation format for the IPM Decisions platform. See an example here: \"fieldObservations\": [\n      {\n        \"fieldObservation\": {\n          \"location\": {\n            \"type\": \"Point\",\n            \"coordinates\": [\n              10.781989,\n              59.660468\n            ]\n          },\n          \"time\": \"2023-05-28T18:00:00+02:00\",\n          \"pestEPPOCode\": \"PSILRO\",\n          \"cropEPPOCode\": \"DAUCS\"\n        },\n        \"quantification\": {\n          \"trapCountCropEdge\": 2,\n          \"trapCountCropInside\": 55\n        }\n      }\n    ]",
  "$id": "https://platform.ipmdecisions.net/api/dss/rest/schema/fieldobservation",
  "properties": {
    "time": {
      "type": "string",
      "format": "date-time",
      "description": "The timestamp of the field observation. Format: \"yyyy-MM-dd'T'HH:mm:ssXXX\", e.g. 2020-04-09T18:00:00+02:00",
      "title": "Time (yyyy-MM-dd'T'HH:mm:ssXXX)"
    },
    "pestEPPOCode": {
      "type": "string",
      "description": "The EPPO code for the observed pest. See https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes",
      "title": "Pest"
    },
    "cropEPPOCode": {
      "type": "string",
      "description": "The EPPO code for the crop in which the pest was observed. See https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes",
      "title": "Crop"
    },
    "location": {
      "title": "Location  of the observation. In GeoJson format.",
      "$ref": "https://geojson.org/schema/GeoJSON.json"
    }
  },
  "required": [
    "time",
    "pestEPPOCode",
    "cropEPPOCode",
    "location"
  ]
}
```
Please note that the pests and crops are referred by [EPPO codes](https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes)

In addition to this, most field observations carry some kind of quantification information: 
Number of leaves infected, number of eggs per plant, trap countings, etc. Since this is 
different for most pests, and also is expressed differently in different models, we have to 
include a wild card in the system. So we have added the optional property "quantification" to the 
"Field observation" property, which in turn may refer to a definition elsewhere.  

Pasting the schema into the [online version of json-editor](https://json-editor.github.io/json-editor/) allows us to create a request like the one below:

```json
{
  "modelId": "PSILAROBSE",
  "configParameters": {
    "timeZone": "Europe/Oslo",
    "startDateCalculation": "2023-05-01",
    "endDateCalculation": "2023-06-05",
    "fieldObservations": [
      {
        "fieldObservation": {
          "location": {
            "type": "Point",
            "coordinates": [
              10.781989,
              59.660468
            ]
          },
          "time": "2023-05-28T18:00:00+02:00",
          "pestEPPOCode": "PSILRO",
          "cropEPPOCode": "DAUCS"
        },
        "quantification": {
          "trapCountCropEdge": 2,
          "trapCountCropInside": 55
        }
      }
    ]
  }
}
```
Sending this request to the model endpoint returns the result below. You can test this using Postman, importing the collection at `../postman_tests/IPM Decisions DSS examples.postman_collection.json`

```json
{
    "timeStart": "2023-04-30T22:00:00Z",
    "timeEnd": "2023-06-04T22:00:00Z",
    "interval": 86400,
    "resultParameters": [
        "TRAP_COUNT_CROP_EDGE",
        "TRAP_COUNT_THRESHOLD",
        "TRAP_COUNT_CROP_INSIDE"
    ],
    "locationResult": [
        {
            "longitude": null,
            "latitude": null,
            "altitude": null,
            "data": [
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    2.0,
                    1.0,
                    55.0
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    null
                ]
            ],
            "warningStatus": [
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                4,
                4,
                4,
                4,
                4,
                4,
                4,
                4,
                4
            ],
            "length": 36,
            "width": 3
        }
    ]
}
```