#!/bin/bash
scriptDir=$(dirname $0)

IMAGE_NAME=quay.io/ibmcase/eda-demo-order-ms
#./mvnw package -DskipTests
./mvnw clean package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t ${IMAGE_NAME} .

if [ -n "$1" ]; then
   docker push ${IMAGE_NAME}
fi