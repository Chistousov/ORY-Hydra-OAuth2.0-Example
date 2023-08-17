#!/bin/bash

users=(resource-server.com)

mkdir -p cert

echo 'generate cert for nginx'

cd cert

for i in "${users[@]}"; do
    echo ${i}

    mkdir -p $i
    
    cd $i

    if [ ! -f $i.crt ]; then
        # private key
        # закрытый ключ
        openssl genrsa -out $i.key 4096

        # Request for Certification (CSR)
        # запрос на сертификацию (CSR)
        openssl req -sha512 -new \
            -subj "/C=RU/ST=Stavropol region/L=Stavropol/O=Some ORG/OU=Some dep/CN=$i" \
            -key $i.key \
            -out $i.csr

        # v3 extension for the certificate
        # расширение v3 для сертификата
        cat >v3-$i.ext <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth,clientAuth
subjectAltName = @alt_names
[alt_names]
DNS.1=$i
DNS.2=ory-hydra-oauth2-example-resource-server-backend
DNS.3=localhost
IP.4=127.0.0.1
EOF
        # certificate generation
        # генерация сертификата
        openssl x509 -req -sha512 -days 999999 \
            -extfile v3-$i.ext \
            -CA ../../../CAForNginx/ca.crt -CAkey ../../../CAForNginx/ca.key -CAcreateserial \
            -in $i.csr \
            -out $i.crt
    else
        echo "$i.crt is exists"
    fi

    cd ../
    
done

cd ../
echo 'Done'
