#!/bin/bash

if [ ! -f .env ]; then

    # ------------------!!!EDIT!!!----------------

    IP_AUTHORIZATION_SERVER="192.168.0.101"
    IP_RESOURCE_SERVER="192.168.0.104"
    
    CLIENT_WRITE_AND_READ_CLIENT_ID="3f684231-e17a-4d1b-b39a-eb26af9f7d27"
    CLIENT_WRITE_AND_READ_CLIENT_SECRET="6bf9Le7LuurMd5in4B61Bt43kp"

    # ------------------------------------------

    echo IP_AUTHORIZATION_SERVER=${IP_AUTHORIZATION_SERVER} >> .env
    echo IP_RESOURCE_SERVER=${IP_RESOURCE_SERVER} >> .env

    echo CLIENT_WRITE_AND_READ_CLIENT_ID=${CLIENT_WRITE_AND_READ_CLIENT_ID} >>.env
    echo CLIENT_WRITE_AND_READ_CLIENT_SECRET=${CLIENT_WRITE_AND_READ_CLIENT_SECRET} >>.env

    cd CAForNginx && sh generate_CA.sh && cd ../
    cd nginx && bash generate_cert.bash && cd ../

    docker compose up -d --no-recreate --wait


else

    docker compose down || true
    docker compose up -d || true

fi
