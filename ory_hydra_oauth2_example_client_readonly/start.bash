#!/bin/bash

if [ ! -f .env ]; then

    # ------------------!!!EDIT!!!----------------
    
    IP_AUTHORIZATION_SERVER="192.168.0.101"
    IP_RESOURCE_SERVER="192.168.0.104"

    CLIENT_READONLY_CLIENT_ID="6fe1db34-b439-4fcd-b748-c86da054f8b6"
    CLIENT_READONLY_CLIENT_SECRET="FF~3b967-z6f4mlgmq_swJ6By~"

    # ------------------------------------------

    echo IP_AUTHORIZATION_SERVER=${IP_AUTHORIZATION_SERVER} >> .env
    echo IP_RESOURCE_SERVER=${IP_RESOURCE_SERVER} >> .env

    echo CLIENT_READONLY_CLIENT_ID=${CLIENT_READONLY_CLIENT_ID} >>.env
    echo CLIENT_READONLY_CLIENT_SECRET=${CLIENT_READONLY_CLIENT_SECRET} >>.env

    cd CAForNginx && sh generate_CA.sh && cd ../
    cd nginx && bash generate_cert.bash && cd ../

    docker compose up -d --no-recreate --wait


else

    docker compose down || true
    docker compose up -d || true

fi
