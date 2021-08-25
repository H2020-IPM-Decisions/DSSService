#IPM DSS developer guide

##Building and deploying with Docker

To see your current images, run `sudo docker images`

###Build the image

The Dockerfile inside the repo root folder is the build description. To build it, run e.g.:
`sudo docker build --tag ipmdecisions/dss_api:ALPHA-04 .`

###Run/test the image
To run it locally:
`sudo docker run --publish 18080:8080 --detach --name ipmdss ipmdecisions/dss_api:ALPHA-04`

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