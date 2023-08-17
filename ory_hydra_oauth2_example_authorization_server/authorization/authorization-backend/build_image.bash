#!/bin/bash

# export HTTP_PROXY="http://proxyuser:proxypass@192.168.20.4:8822/"
# export HTTPS_PROXY="http://proxyuser:proxypass@192.168.20.4:8822/"
# export NO_PROXY="localhost,127.0.0.1"

export REPO_IMAGE="chistousov"
export PROJECT_NAME="ory-hydra-oauth2-example-authorization-server-backend"
export VERSION="1.0.0"

docker pull paketobuildpacks/builder:0.3.280-base

./gradlew clean test
./gradlew bootBuildImage --builder=paketobuildpacks/builder:0.3.280-base

# publish in docker hub 
# docker login
# docker push $REPO_IMAGE/$PROJECT_NAME:$VERSION
# docker logout
# docker rmi $REPO_IMAGE/$PROJECT_NAME:$VERSION
