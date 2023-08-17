#!/bin/bash

if [ ! -f .env ]; then

    # ------------------!!!EDIT!!!----------------

    CLIENT_WRITE_AND_READ_CLIENT_ID="someClientReadAndWrite"
    CLIENT_WRITE_AND_READ_CLIENT_SECRET="someClientReadAndWriteSecret"

    CLIENT_TRUSTSTORE_KEYSTORE_JAVA_NAME="authorization-server.com.truststore.p12"
    CLIENT_TRUSTSTORE_KEYSTORE_JAVA_PASS="somePass"

    # ------------------------------------------

    echo CLIENT_WRITE_AND_READ_CLIENT_ID=${CLIENT_WRITE_AND_READ_CLIENT_ID} >>.env
    echo CLIENT_WRITE_AND_READ_CLIENT_SECRET=${CLIENT_WRITE_AND_READ_CLIENT_SECRET} >>.env

    echo CLIENT_TRUSTSTORE_KEYSTORE_JAVA_NAME=${CLIENT_TRUSTSTORE_KEYSTORE_JAVA_NAME} >>.env
    echo CLIENT_TRUSTSTORE_KEYSTORE_JAVA_PASS=${CLIENT_TRUSTSTORE_KEYSTORE_JAVA_PASS} >>.env


    cd CAForNginx && sh generate_CA.sh && cd ../
    cd nginx && bash generate_cert.bash && cd ../

    docker compose up -d --no-recreate --wait


else

    docker compose down || true
    docker compose up -d || true

fi
