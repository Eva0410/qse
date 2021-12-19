#!/bin/bash

### Build Docker Image

docker build . -t shacl:QSE-LATEST

echo "------------------ LUBM ------------------"

### Run LUBM Docker Container
container=QSE_lubm_wd_maxCard
docker run -m 32GB -d --name QSE_lubm_wd_maxCard -e "JAVA_TOOL_OPTIONS=-Xmx17g" --mount type=bind,source=/srv/data/iq26og/data/,target=/app/data --mount type=bind,source=/srv/data/iq26og/git/shacl/,target=/app/local shacl:QSE-LATEST /app/local/config/wd-max-card/lubmConfig.properties

### Check the status of docker containers
docker ps

### Get the status of the current docker container
status=$(docker container inspect -f '{{.State.Status}}' $container)

echo "Status of the ${container} is ${status}"

### Keep it in sleep for 2 minutes while this container is running
until [ $status == "running" ]; do
  sleep 2m
done

echo "Status of the ${container} is ${status}" ### Container exited

echo "------------------ DBpedia ------------------"

### Run DBpedia Docker Container
container=QSE_dbpedia_wd_maxCard
docker run -m 32GB -d --name QSE_dbpedia_wd_maxCard -e "JAVA_TOOL_OPTIONS=-Xmx16g" --mount type=bind,source=/srv/data/iq26og/data/,target=/app/data --mount type=bind,source=/srv/data/iq26og/git/shacl/,target=/app/local shacl:QSE-LATEST /app/local/config/wd-max-card/dbpediaConfig.properties

docker ps

status=$(docker container inspect -f '{{.State.Status}}' $container)

echo "Status of the ${container} is ${status}"

until [ $status == "running" ]; do
  sleep 2m
done

echo "Status of the ${container} is ${status}" ### Container exited

echo "------------------ YAGO ------------------"

### Run YAGO Docker Container
container=QSE_yago_wd_maxCard
docker run -m 32GB -d --name QSE_yago_wd_maxCard -e "JAVA_TOOL_OPTIONS=-Xmx16g" --mount type=bind,source=/srv/data/iq26og/data/,target=/app/data --mount type=bind,source=/srv/data/iq26og/git/shacl/,target=/app/local shacl:QSE-LATEST /app/local/config/wd-max-card/yagoConfig.properties

docker ps

status=$(docker container inspect -f '{{.State.Status}}' $container)

echo "Status of the ${container} is ${status}"

until [ $status == "running" ]; do
  sleep 2m
done

echo "Status of the ${container} is ${status}" ### Container exited
