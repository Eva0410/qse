#!/bin/bash
cd ..

### Clear Cache
echo "Clearing cache"
sync; echo 1 > /proc/sys/vm/drop_caches


### Build Docker Image
image=shacl:QSE-Exact
docker build . -t $image

echo "------------------ A3: WIKIDATA WITH MAX CARDINALITY CONSTRAINTS ------------------"

container=QSE_WIKIDATA_Exact_With_Max_Cardinality

echo "About to run docker container: ${container}"

docker run -m 750GB -d --name $container -e "JAVA_TOOL_OPTIONS=-Xmx550g" --mount type=bind,source=/user/cs.aau.dk/iq26og/data/,target=/app/data --mount type=bind,source=/user/cs.aau.dk/iq26og/git3/shacl/,target=/app/local $image /app/local/config/wd-max-card/wikiDataConfig.properties

docker ps

# Get the status of the current docker container
status=$(docker container inspect -f '{{.State.Status}}' $container)

echo "Status of the ${container} is ${status}"

### Keep it in sleep for 1 minutes while this container is running
while :
do
  status=$(docker container inspect -f '{{.State.Status}}' $container)
  if [ $status == "exited" ]; then
    break
  fi
  docker stats --no-stream | cat >>   "${container}-Docker-Stats.csv"
  echo "Sleeping for 1 minutes : $(date +%T)"
  sleep 1m
done

status=$(docker container inspect -f '{{.State.Status}}' $container)

echo "Status of the ${container} is ${status}" ### Container exited

