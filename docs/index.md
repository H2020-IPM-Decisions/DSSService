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
