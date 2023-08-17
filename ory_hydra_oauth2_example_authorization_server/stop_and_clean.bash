#!/bin/bash

docker compose -f docker-compose.yaml -f docker-compose.prod.yaml down -v || true
rm -rf .env || true

rm -rf CAForNginx/ca.key || true
rm -rf CAForNginx/ca.crt || true
rm -rf CAForNginx/ca.srl || true

rm -rf nginx/cert/ || true

rm -rf nginx/confs/htpasswd_introspect || true

rm -rf authorization-server.com.truststore.p12 || true
