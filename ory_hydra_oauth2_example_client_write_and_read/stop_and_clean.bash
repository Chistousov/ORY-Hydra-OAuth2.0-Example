#!/bin/bash

docker compose down -v || true
rm -rf .env || true

rm -rf CAForNginx/ca.key || true
rm -rf CAForNginx/ca.crt || true
rm -rf CAForNginx/ca.srl || true

rm -rf nginx/cert/ || true

rm -rf authorization-server.com.truststore.p12 || true
