#!/bin/bash

if [ ! -f .env ]; then

    # ------------------!!!EDIT!!!----------------

    CLIENT_READONLY_CLIENT_ID="someClientReadonly"
    CLIENT_READONLY_CLIENT_SECRET="someClientReadonlySecret"

    CLIENT_TRUSTSTORE_KEYSTORE_JAVA_NAME="authorization-server.com.truststore.p12"
    CLIENT_TRUSTSTORE_KEYSTORE_JAVA_PASS="somePass"

    # ------------------------------------------

    echo CLIENT_READONLY_CLIENT_ID=${CLIENT_READONLY_CLIENT_ID} >>.env
    echo CLIENT_READONLY_CLIENT_SECRET=${CLIENT_READONLY_CLIENT_SECRET} >>.env

    echo CLIENT_TRUSTSTORE_KEYSTORE_JAVA_NAME=${CLIENT_TRUSTSTORE_KEYSTORE_JAVA_NAME} >>.env
    echo CLIENT_TRUSTSTORE_KEYSTORE_JAVA_PASS=${CLIENT_TRUSTSTORE_KEYSTORE_JAVA_PASS} >>.env


    cd CAForNginx && sh generate_CA.sh && cd ../
    cd nginx && bash generate_cert.bash && cd ../

    docker compose up -d --no-recreate --wait


else

    docker compose down || true
    docker compose up -d || true

fi
