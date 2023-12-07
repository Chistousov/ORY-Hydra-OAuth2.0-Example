#!/bin/bash

if [ ! -f .env ]; then

    # ------------------!!!EDIT!!!----------------

    USER_DATA_POSTGRESQL_PASSWORD="cklGS7BNMT6Io9Yd8FKzg4ZmWLXjQnA24JbXNHbG"

    HYDRA_POSTGRESQL_PASSWORD="7pj3gK8arVwk6A1BbUD2XysfIYmKdEk0DL8BMRNx"

    HYDRA_SECRETS_COOKIE="OT9Z8I2NcBp01rP4FwQG7JEt6nuXeJ0BDpf4Bjwc"
    HYDRA_SECRETS_SYSTEM="cIsKS4VzJCDpXlwm2PNTb7v60GHh1iEYZPiiPpRS"
    
    HYDRA_INTROSPECT_USER="user_introspect"
    HYDRA_INTROSPECT_PASSWORD="hUq7Mw3fr4lFjnHQtoJucgDdAV58NbAOvuGN2OfB"

    # ------------------------------------------

    HYDRA_DEPENDS_ON_MIGRATE="service_completed_successfully"

    echo HYDRA_DEPENDS_ON_MIGRATE=${HYDRA_DEPENDS_ON_MIGRATE} >>.env

    echo USER_DATA_POSTGRESQL_PASSWORD=${USER_DATA_POSTGRESQL_PASSWORD} >>.env
   
    echo HYDRA_POSTGRESQL_PASSWORD=${HYDRA_POSTGRESQL_PASSWORD} >>.env

    echo HYDRA_SECRETS_COOKIE=${HYDRA_SECRETS_COOKIE} >>.env
    echo HYDRA_SECRETS_SYSTEM=${HYDRA_SECRETS_SYSTEM} >>.env

    echo HYDRA_INTROSPECT_USER=${HYDRA_INTROSPECT_USER} >>.env
    echo HYDRA_INTROSPECT_PASSWORD=${HYDRA_INTROSPECT_PASSWORD} >>.env
    
    htpasswd -bcB -C 10 nginx/confs/htpasswd_introspect ${HYDRA_INTROSPECT_USER} ${HYDRA_INTROSPECT_PASSWORD}

    cd CAForNginx && sh generate_CA.sh && cd ../
    cd nginx && bash generate_cert.bash && cd ../

    docker compose -f docker-compose.yaml -f docker-compose.prod.yaml up -d --no-recreate --wait

    echo "Create OAuth 2.0 client for only read"
    code_client_readonly=$(docker compose -f docker-compose.yaml -f docker-compose.prod.yaml exec ory-hydra-oauth2-example-authorization-server-hydra \
    hydra create client \
    --endpoint http://127.0.0.1:4445 \
    --grant-type authorization_code,refresh_token \
    --response-type code,id_token \
    --format json \
    --scope openid --scope offline --scope read \
    --redirect-uri https://client-readonly.com/api/login/oauth2/code/client-readonly)

    echo "Create OAuth 2.0 client for read and write"
    code_client_read_and_write=$(docker compose -f docker-compose.yaml -f docker-compose.prod.yaml exec ory-hydra-oauth2-example-authorization-server-hydra \
    hydra create client \
    --endpoint http://127.0.0.1:4445 \
    --grant-type authorization_code,refresh_token \
    --response-type code,id_token \
    --format json \
    --scope openid --scope offline --scope read --scope write \
    --redirect-uri https://client-write-and-read.com/api/login/oauth2/code/client-write-and-read)

    # OAuth 2.0 Clients
    echo CLIENT_READONLY_CLIENT_ID=$(echo $code_client_readonly | jq -r '.client_id') >> .env_readonly
    echo CLIENT_READONLY_CLIENT_SECRET=$(echo $code_client_readonly | jq -r '.client_secret') >> .env_readonly

    echo CLIENT_WRITE_AND_READ_CLIENT_ID=$(echo $code_client_read_and_write | jq -r '.client_id') >> .env_read_and_write
    echo CLIENT_WRITE_AND_READ_CLIENT_SECRET=$(echo $code_client_read_and_write | jq -r '.client_secret') >> .env_read_and_write

else

    docker compose -f docker-compose.yaml -f docker-compose.prod.yaml down || true
    docker compose -f docker-compose.yaml -f docker-compose.prod.yaml up -d || true

fi
