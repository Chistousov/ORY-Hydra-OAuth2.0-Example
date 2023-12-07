#!/bin/bash

if [ ! -f .env ]; then

    # ------------------!!!EDIT!!!----------------

    IP_AUTHORIZATION_SERVER="192.168.0.101"

    HYDRA_INTROSPECT_USER="user_introspect"
    HYDRA_INTROSPECT_PASSWORD="hUq7Mw3fr4lFjnHQtoJucgDdAV58NbAOvuGN2OfB"

    # ------------------------------------------

    echo IP_AUTHORIZATION_SERVER=${IP_AUTHORIZATION_SERVER} >> .env

    echo HYDRA_INTROSPECT_USER=${HYDRA_INTROSPECT_USER} >>.env
    echo HYDRA_INTROSPECT_PASSWORD=${HYDRA_INTROSPECT_PASSWORD} >>.env

    cd CAForNginx && sh generate_CA.sh && cd ../
    cd nginx && bash generate_cert.bash && cd ../

    docker compose up -d --no-recreate --wait


else

    docker compose down || true
    docker compose up -d || true

fi
