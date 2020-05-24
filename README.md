<img src="https://ipmdecisions.net/media/phdj3qhd/output-onlinepngtools.png" width="200px;"/>

# IPM Decisions DSS Service
This service is part of the <a href="https://ipmdecisions.net/" target="new">Horizon 2020 IPM Decisions project</a>.
The service provides the system with sufficient information for a client to be able to connect to and get information from any registered DSS (Decision Support System)

The source code for this service can be found here: <a href="https://github.com/H2020-IPM-Decisions/WeatherService" target="new">https://github.com/H2020-IPM-Decisions/WeatherService</a>

## 1. A catalogue of DSSs and their models available to the platform
The catalogue is a list of DSSs available to the platform. Each DSS may contain one or more models. So even we are describing a standalone model, 
in our data structure we have a DSS and one model.  
The catalogue is searchable, primarily using crops, pests and (TODO) geography as criteria.
This is part of the DSSService

## 2. A standard for result data returned from DSS models
In order to present result data from potentially several models to the user (e.g. farmer or advisor), the data must be provided
from the models in a uniform way. The data structure is described in a Json schema. This - along with the validation service for
such data - is part of the MetaDataService

## 3. A standard for field observations sent as input data to DSS models
Field observations of pests and diseases are required as inputs in some DSS models. The common properties of such data is
described in a Json schema. This is part of the MetaDataService
