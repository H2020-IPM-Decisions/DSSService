#IPM DSS developer guide

##Prerequisites
### Credentials for the EPPO web services
The IPM Decisions DSS API uses the [EPPO web services](https://data.eppo.int/documentation/rest) for some backend operations, such as evaluating EPPO codes sent in by clients. In order to do this, credentials need to be provided for each request. This is in the form of an authtoken that is passed to the service endpoints. To configure this: follow these two steps:

1. [Register with the EPPO database](https://data.eppo.int/user/register) to get your own authtoken
2. Make it available for the application<br>
    a. Running locally (not in Docker)
Make it available in your Java Application server. For Wildfly, put it in `[WILDFLY_HOME]/standalone/configuration/standalone[-full].xml`:

``` xml
<system-properties>
[...]
    <property name="net.ipmdecisions.dssservice.EPPO_AUTHTOKEN" value="***YOUR_AUTHTOKEN_HERE***"/>
</system-properties>
```
b. provide to the Docker build
Simply use the `-e EPPO_AUTHTOKEN=[XXX]` as specified in the `docker run` command in the "Run/test the image" paragraph below

##Building and deploying with Docker

To see your current images, run `sudo docker images`

###Build the image

The Dockerfile inside the repo root folder is the build description. To build it, run e.g.:
`sudo docker build --tag ipmdecisions/dss_api:ALPHA-04 .`

###Run/test the image
To run it locally:
`sudo docker run --publish 18080:8080 --detach -e EPPO_AUTHTOKEN=***YOUR AUTHTOKEN_HERE*** --name ipmdss ipmdecisions/dss_api:ALPHA-04`

**Please note that you need to have a valid EPPO_AUTHTOKEN (See the "Prerequisites" paragraph above)**

Test it with Postman (url = [http://localhost:18080/DSSService](http://localhost:18080/DSSService)). If the tests run OK, then you can proceed to push the image. If not, you need to rebuild the image:
1. First, you need to stop the running container

```
sudo docker stop ipmdss
```

2. Then, remove the container. First find the container id

```
sudo docker ps -a -q --filter ancestor=ipmdecisions/dss_api:ALPHA-04
```
3. Then, remove it

```
sudo docker rm [CONTAINER_ID]
```

4. Then, remove the image

```
sudo docker rmi ipmdecisions/dss_api:ALPHA-04
```

5. Also, make sure you remove any ancestors as well, use sudo docker images to reveal them (check for recent ones)
6. Then you can rebuild the image (see above). Consider adding the `--no-cache` tag if you need a complete rebuild

###Login to the container’s console (e.g. for troubleshooting)
```
Sudo docker exec -it <containername> bash
```

###Push the image
```
sudo docker push ipmdecisions/dss_api:ALPHA-04
```