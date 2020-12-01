# IPM Decisions DSS API User Guide

## Use the API test suite for understanding the APIs
In the repo folder `/postman_tests` you will find the test suite for the API. It must be run using the [Postman API Client](https://www.postman.com/product/api-client/). The test suite contains requests to the API, and serve as a good guide for understanding how to make the requests. In addition, some example requests to available DSSs are provided in the sample suite in the same folder.

## Create a DSS model request based on metadata
So, you have found a model that you want to run. Given the metadata provided, let's go through the steps needed to automatically create a request and send it to the DSS.

Let's use the VIPS/PSILARTEMP (Carrot rust fly temperature) model as an example. The relevant metadata is provided below

```
{
        "name": "Carrot rust fly temperature model",
        "id": "PSILARTEMP",
        "version": "1.0",
        "type_of_decision": "Short-term tactical",
        "type_of_output": "Risk indication",
        "description_URL": "https://www.vips-landbruk.no/forecasts/models/PSILARTEMP/",
        
        "pests": [
          "PSILRO"
        ],
        "crops": [
          "DAUCS"
        ],
        "execution": {
          "type": "ONTHEFLY",
          "endpoint": "https://coremanager.vips.nibio.no/models/PSILARTEMP/run/ipmd",
          "form_method": "post",
          "content_type": "application/json",
          "input_schema": "{\n  \"type\":\"object\",\n  \"properties\": {\n    \"modelId\": {\"type\": \"string\", \"pattern\":\"^PSILARTEMP$\", \"title\": \"Model Id\", \"default\":\"PSILARTEMP\", \"description\":\"Must be PSILARTEMP\"},\n    \"configParameters\": {\n      \"title\":\"Configuration parameters\",\n      \"type\": \"object\",\n      \"properties\": {\n        \"timeZone\": {\"type\": \"string\", \"title\": \"Time zone (e.g. Europe/Oslo)\", \"default\":\"Europe/Oslo\"},\n        \"timeStart\": {\"type\":\"string\",\"format\": \"date\", \"title\": \"Start date of calculation (YYYY-MM-DD)\"},\n        \"timeEnd\": {\"type\":\"string\",\"format\": \"date\", \"title\": \"End date of calculation (YYYY-MM-DD)\"}\n      },\n      \"required\": [\"timeZone\",\"timeStart\",\"timeEnd\"]\n    },\n    \"weatherData\": {\n      \"$ref\": \"https://ipmdecisions.nibio.no/api/wx/rest/schema/weatherdata\"\n    }\n  },\n  \"required\": [\"modelId\",\"configParameters\"]\n}\n"
        },
        "input": {
          "weather": [
            {
              "parameter_code": 1002,
              "interval": 86400
            }
          ],
          "field_observation": null
        },
        "valid_spatial": {
          "countries": [
            "NOR"
          ],
          "geoJSON": "{}"
        },
          "result_parameters": [
            {
              "id": "TMDD5C",
              "title": "Accumulated day degrees",
              "description": "The accumulated day degrees with a base temperature of 5 degrees celcius"
            }
          ]
        }
      }
```
Here's the input_schema pretty printed

```
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

```
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

```
{
    "timeStart": "2020-04-30T22:00:00Z",
    "timeEnd": "2020-05-02T22:00:00Z",
    "interval": 86400,
    "resultParameters": [
        "TMDD5C",
        "THRESHOLD_1",
        "THRESHOLD_2",
        "TMD5C",
        "WARNING_STATUS",
        "TMD",
        "THRESHOLD_3"
    ],
    "locationResult": [
        {
            "longitude": null,
            "latitude": null,
            "altitude": null,
            "data": [
                [
                    0.7,
                    260.0,
                    360.0,
                    0.7,
                    2.0,
                    5.7,
                    560.0
                ],
                [
                    3.9,
                    260.0,
                    360.0,
                    3.2,
                    2.0,
                    8.2,
                    560.0
                ],
                [
                    7.4,
                    260.0,
                    360.0,
                    3.5,
                    2.0,
                    8.5,
                    560.0
                ]
            ],
            "length": 3,
            "width": 7
        }
    ]
}
```
## Create a DSS model request with field observations
Looking at the VIPS's observation based carrot rust fly model (PSILAROBSE), we have this input_schema:
```
      {
        "type":"object",
        "properties": {
          "modelId": {"type": "string", "pattern":"^PSILAROBSE$", "title": "Model Id", "default":"PSILAROBSE", "description":"Must be PSILAROBSE"},
          "configParameters": {
            "title":"Configuration parameters",
            "type": "object",
            "properties": {
              "timeZone": {"type": "string", "title": "Time zone (e.g. Europe/Oslo)", "default":"Europe/Oslo"},
              "startDateCalculation": {"type":"string","format": "date", "title": "Start date of calculation (YYYY-MM-DD)"},
              "endDateCalculation": {"type":"string","format": "date", "title": "End date of calculation (YYYY-MM-DD)"},
              "fieldObservations": {
                "title": "Field observations",
                "type": "array",
                "items": {
                  "$ref": "https://ipmdecisions.nibio.no/api/dss/rest/schema/fieldobservation"
                }
              },
              "fieldObservationQuantifications": {
                "title": "Field observation quantifications",
                "type": "array",
                "items": {
                  "oneOf": [
                    {
                      "$ref": "#/definitions/fieldObs_PSILRO"
                    }
                  ]
                }
              }
            },
            "required": ["timeZone","startDateCalculation","endDateCalculation"]
          }
        },
        "required": ["modelId","configParameters"],
        "definitions": {
          "fieldObs_PSILRO": {
            "title": "Psila rosae quantification", 
            "properties": {
              "trapCountCropEdge":{"title":"Insect trap count at the edge of the field","type":"integer"},
              "trapCountCropInside":{"title":"Insect trap count inside the field","type":"integer"}
            },
            "required": ["trapCountCropEdge","trapCountCropInside"]
          }
        }
      }
```
The field observations definition is a bit complicated, because it consists of some common, standard parts, but also parts that vary between pests. Common parts are:
* Time - when was the observation made?
* Location - where was the observation made?
* What has been observed?
* In which crop was it observed?

Here's the [schema for the common parts](https://ipmdecisions.nibio.no/api/dss/rest/schema/fieldobservation):
```
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "title": "Field observation",
  "type": "object",
  "additionalProperties": false,
  "description": "Version 0.1. The schema describes the field observation format for the IPM Decisions platform. See an example here: TODO",
  "$id": "https://ipmdecisions.nibio.no/api/dss/rest/schema/fieldobservation",
  "properties": {
    "location": {
      "title": "Location  of the observation. In GeoJson format.",
      "$ref": "https://ipmdecisions.nibio.no/schemas/geojson.json"
    },
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
      "title": "Pest"
    }
  },
  "required": [
    "location",
    "time",
    "pestEPPOCode",
    "cropEPPOCode"
  ]
}
```
Please note that the pests and crops are referred by [EPPO codes](https://www.eppo.int/RESOURCES/eppo_databases/eppo_codes)

In addition to this, most field observations carry some kind of quantification information: Number of leaves infected, number of eggs per plant, trap countings, etc. Since this is different for most pests, and also is expressed differently in different models, we have to include a wild card in the system. So we have added the property "fieldObservationQuantifications". These quantifications are given as an array, so that they correspond item-by-item with the array of fieldObservations. Each quantification has its own schema definition, given in the "definition" section. 

Pasting the schema into the [online version of json-editor](https://json-editor.github.io/json-editor/) allows us to create a request like the one below:
```
{
  "modelId": "SEPAPIICOL",
  "configParameters": {
    "timeZone": "Europe/Oslo",
    "startDateCalculation": "2020-05-01",
    "endDateCalculation": "2020-05-10",
    "fieldObservations": [
      {
        "location": {
          "type": "Point",
          "coordinates": [
            "11.025635",
            "59.715791"
          ]
        },
        "time": "2020-05-05T12:00:00Z",
        "pestEPPOCode": "SEPTAP",
        "cropEPPOCode": "APUGD"
      }
    ],
    "fieldObservationQuantifications": [
      {
        "trapCountCropEdge": 22,
        "trapCountCropInside": 2
      }
    ]
  }
}
```
Sending this request to the model endpoint returns the result below. You can test this using Postman, importing the collection at `/postman_tests/IPM Decisions DSS examples.postman_collection.json`
```
{
    "timeStart": "2020-04-30T22:00:00Z",
    "timeEnd": "2020-05-09T22:00:00Z",
    "interval": 86400,
    "resultParameters": [
        "TRAP_COUNT_CROP_EDGE",
        "TRAP_COUNT_THRESHOLD",
        "WARNING_STATUS",
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
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    1.0,
                    null
                ],
                [
                    null,
                    1.0,
                    1.0,
                    null
                ],
                [
                    22.0,
                    1.0,
                    4.0,
                    2.0
                ],
                [
                    null,
                    1.0,
                    4.0,
                    null
                ],
                [
                    null,
                    1.0,
                    4.0,
                    null
                ],
                [
                    null,
                    1.0,
                    4.0,
                    null
                ],
                [
                    null,
                    1.0,
                    4.0,
                    null
                ],
                [
                    null,
                    1.0,
                    4.0,
                    null
                ]
            ],
            "length": 10,
            "width": 4
        }
    ]
}
```