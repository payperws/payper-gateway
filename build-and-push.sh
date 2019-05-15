#!/usr/bin/env bash
mvn clean install
echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin
docker push payper/payper-gateway:latest