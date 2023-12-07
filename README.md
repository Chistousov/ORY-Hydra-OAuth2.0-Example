# **ORY Hydra OAuth2 Example**

[Русский язык](README_RUS.md)

## Description

Example implementation of [OAuth 2.0 Authorization Code with PKCE](https://www.ory.sh/docs/oauth2-oidc/authorization-code-flow) using [ORY Hydra](https://www.ory.sh/hydra/).

Login Flow and Consent Flow are implemented using Spring boot 2(Java 11, WebFlux), Angular 17, PostgreSQL 15.

Everything has been tested: Karma, JUnit 5, Testcontainers.

## How to build example Login Flow Wrapper, Consent Flow Wrapper,  OAuth 2.0 Client(s), OAuth 2.0 Resource Server

Let's take Login Flow and Consent Flow Wrapper as an example.

### Build Frontend

path:

```bash
cd ory_hydra_oauth2_example_authorization_server/authorization/authorization-frontend/
```

If you need to perform testing (Karma), then:

```bash
cd ory_hydra_oauth2_example_authorization_server/authorization/authorization-frontend/ && \
npm i && \
bash test_and_report.bash
```

If you need to find out the code coverage of tests, then you need to open the following through the browser:

```text
ory_hydra_oauth2_example_authorization_server/authorization/authorization-frontend/coverage/authorization-frontend/index.html
```

To build a docker image, do the following (ATTENTION! You must install pack. The instructions are inside build_image.bash):

```bash

# install pack
# https://buildpacks.io/docs/tools/pack/#linux-script-install
# (curl -sSL "https://github.com/buildpacks/pack/releases/download/v0.29.0/pack-v0.29.0-linux.tgz" | sudo tar -C /usr/local/bin/ --no-same-owner -xzv pack)

cd ory_hydra_oauth2_example_authorization_server/authorization/authorization-frontend/ && \
bash build_image.bash
```

### Build Backend

path:

```bash
cd ory_hydra_oauth2_example_authorization_server/authorization/authorization-backend/
```

If you need to perform testing (JUnit 5), then (**ATTENTION!** Since Testcontainers is used for testing, you must have rights to run docker (sudo usermod -aG docker \[user\])):

```bash
cd ory_hydra_oauth2_example_authorization_server/authorization/authorization-backend/ && \
./gradlew -v && \
bash test_and_report.bash
```

If you need to find out the code coverage of tests (JaCoCo), then you need to open the following in the browser:

```text
ory_hydra_oauth2_example_authorization_server/authorization/authorization-backend/build/reports/jacoco/test/html/index.html
```

To build a docker image, do the following:

```bash
cd ory_hydra_oauth2_example_authorization_server/authorization/authorization-backend/ && \
bash build_image.bash
```

### Proxy

#### Docker (Debian)

```bash
mkdir -p /etc/systemd/system/docker.service.d

# set proxy
cat > /etc/systemd/system/docker.service.d/http-proxy.conf <<-EOF
[Service]
Environment="HTTP_PROXY=http://proxyuser:proxypass@192.168.20.4:8822/"
Environment="HTTPS_PROXY=http://proxyuser:proxypass@192.168.20.4:8822/"
Environment="NO_PROXY=localhost,127.0.0.1"
EOF

# restart docker
sudo systemctl daemon-reload
sudo systemctl restart docker

# check
systemctl show --property=Environment docker

```

#### APT (Docker image)

If you need to specify a proxy server (see ory_hydra_oauth2_example_authorization_server/authorization/authorization-frontend/build_image.bash or ory_hydra_oauth2_example_authorization_server/authorization/authorization-backend/build_image.bash), then uncomment HTTP_PROXY (HTTPS_PROXY or/and NO_PROXY) and edit HTTP_PROXY ( HTTPS_PROXY or/and NO_PROXY)

```bash
 #!/bin/bash

export HTTP_PROXY="http://proxyuser:proxypass@192.168.20.4:8822/"
export HTTPS_PROXY="http://proxyuser:proxypass@192.168.20.4:8822/"
export NO_PROXY="localhost,127.0.0.1"

REPO_IMAGE="chistousov"
...

```

## Example

![ory-hydra-oauth2-example](ory-hydra-oauth2-example.png)

4 Debian:
| Type                                           | DNS, Hostname             | IP            |
| -------------                                  | -------------             | ------------- |
| OAuth 2.0 Authorization Server                 | authorization-server.com  | 192.168.0.101 |
| OAuth 2.0 Client (Readonly)                    | client-readonly.com       | 192.168.0.102 |
| OAuth 2.0 Client (Write and read)              | client-write-and-read.com | 192.168.0.103 |
| OAuth 2.0 Resource Server                      | resource-server.com       | 192.168.0.104 |

Requires Docker, Docker Compose, JDK 11(keytool), openssl, jq (apt install jq), htpasswd (apt install apache2-utils) on all four Debian servers.

### OAuth 2.0 Authorization Server

Go to the folder with OAuth 2.0 Authorization Server

```bash
cd ory_hydra_oauth2_example_authorization_server
```

We edit the environment variables at the beginning of the start.bash file. To find out the IP address you can run ***ip a***.

```bash

    # ------------------!!!EDIT!!!----------------

    USER_DATA_POSTGRESQL_PASSWORD="cklGS7BNMT6Io9Yd8FKzg4ZmWLXjQnA24JbXNHbG"

    HYDRA_POSTGRESQL_PASSWORD="7pj3gK8arVwk6A1BbUD2XysfIYmKdEk0DL8BMRNx"

    HYDRA_SECRETS_COOKIE="OT9Z8I2NcBp01rP4FwQG7JEt6nuXeJ0BDpf4Bjwc"
    HYDRA_SECRETS_SYSTEM="cIsKS4VzJCDpXlwm2PNTb7v60GHh1iEYZPiiPpRS"
    
    HYDRA_INTROSPECT_USER="user_introspect"
    HYDRA_INTROSPECT_PASSWORD="hUq7Mw3fr4lFjnHQtoJucgDdAV58NbAOvuGN2OfB"

    # ------------------------------------------
    ...

```

**WARNING!** Permission to run docker is required (sudo usermod -aG docker \[user\]

Launch the application:

```bash
bash start.bash
```

We remember **HYDRA_INTROSPECT_USER**, **HYDRA_INTROSPECT_PASSWORD** and:

```bash
cat .env_readonly
#CLIENT_READONLY_CLIENT_ID="6fe1db34-b439-4fcd-b748-c86da054f8b6"
#CLIENT_READONLY_CLIENT_SECRET="FF~3b967-z6f4mlgmq_swJ6By~"
cat .env_read_and_write
#CLIENT_WRITE_AND_READ_CLIENT_ID="3f684231-e17a-4d1b-b39a-eb26af9f7d27"
#CLIENT_WRITE_AND_READ_CLIENT_SECRET="6bf9Le7LuurMd5in4B61Bt43kp"

```

### OAuth 2.0 Resource Server

Go to the folder with OAuth 2.0 Resource Server

```bash
cd ory_hydra_oauth2_example_resource_server
```

We edit the environment variables at the beginning of the start.bash file. To find out the IP address you can run ***ip a***.

```bash

   # ------------------!!!EDIT!!!----------------

    IP_AUTHORIZATION_SERVER="192.168.0.101"

    HYDRA_INTROSPECT_USER="user_introspect"
    HYDRA_INTROSPECT_PASSWORD="hUq7Mw3fr4lFjnHQtoJucgDdAV58NbAOvuGN2OfB"

    # ------------------------------------------
    ...

```

Copy the CA certificate from the OAuth 2.0 Authorization Server to the ca-certificates folder:

```text
ory_hydra_oauth2_example_authorization_server/CAForNginx/ca.crt -> ory_hydra_oauth2_example_resource_server/ca-certificates/auth.crt
```

**WARNING!** Permission to run docker is required (sudo usermod -aG docker \[user\])

Launch the application:

```bash
bash start.bash
```

### OAuth 2.0 Client (Readonly)

Go to the folder with OAuth 2.0 Client (Readonly)

```bash
cd ory_hydra_oauth2_example_client_readonly
```

We edit the environment variables at the beginning of the start.bash file. To find out the IP address you can run ***ip a***.

```bash

   # ------------------!!!EDIT!!!----------------

    IP_AUTHORIZATION_SERVER="192.168.0.101"
    IP_RESOURCE_SERVER="192.168.0.104"

    CLIENT_READONLY_CLIENT_ID="6fe1db34-b439-4fcd-b748-c86da054f8b6"
    CLIENT_READONLY_CLIENT_SECRET="FF~3b967-z6f4mlgmq_swJ6By~"

    # ------------------------------------------
    ...

```

Copy the CA certificate from the OAuth 2.0 Authorization Server to the ca-certificates folder:

```text
ory_hydra_oauth2_example_authorization_server/CAForNginx/ca.crt -> ory_hydra_oauth2_example_client_readonly/ca-certificates/auth.crt
```

Copy the CA certificate from the OAuth 2.0 Resource Server to the ca-certificates folder:

```text
ory_hydra_oauth2_example_resource_server/CAForNginx/ca.crt -> ory_hydra_oauth2_example_client_readonly/ca-certificates/resource_server.crt
```

**WARNING!** Permission to run docker is required (sudo usermod -aG docker \[user\])

Launch the application:

```bash
bash start.bash
```

### OAuth 2.0 Client (Write and read)

Go to the folder with OAuth 2.0 Client (Write and read)

```bash
cd ory_hydra_oauth2_example_client_write_and_read
```

We edit the environment variables at the beginning of the start.bash file. To find out the IP address you can run ***ip a***.

```bash

   # ------------------!!!EDIT!!!----------------

    IP_AUTHORIZATION_SERVER="192.168.0.101"
    IP_RESOURCE_SERVER="192.168.0.104"
    
    CLIENT_WRITE_AND_READ_CLIENT_ID="3f684231-e17a-4d1b-b39a-eb26af9f7d27"
    CLIENT_WRITE_AND_READ_CLIENT_SECRET="6bf9Le7LuurMd5in4B61Bt43kp"

    # ------------------------------------------
    ...

```

Copy the CA certificate from the OAuth 2.0 Authorization Server to the ca-certificates folder:

```text
ory_hydra_oauth2_example_authorization_server/CAForNginx/ca.crt -> ory_hydra_oauth2_example_client_write_and_read/ca-certificates/auth.crt
```

Copy the CA certificate from the OAuth 2.0 Resource Server to the ca-certificates folder

```text
ory_hydra_oauth2_example_resource_server/CAForNginx/ca.crt -> ory_hydra_oauth2_example_client_write_and_read/ca-certificates/resource_server.crt
```

**WARNING!** Permission to run docker is required (sudo usermod -aG docker \[user\])

Launch the application:

```bash
bash start.bash
```

### Checking

On the Resource Owner computer, DESCRIBE IP ADDRESSES IN THE FILE /etc/hosts (Linux).

```bash
echo '192.168.0.101 authorization-server.com' >> /etc/hosts
echo '192.168.0.102 client-readonly.com' >> /etc/hosts
echo '192.168.0.103 client-write-and-read.com' >> /etc/hosts
echo '192.168.0.104 resource-server.com' >> /etc/hosts
# check
ping authorization-server.com
ping client-readonly.com
ping client-write-and-read.com
ping resource-server.com
```

0. Register Resource Owner <https://authorization-server.com/registration>.
1. Go to <https://client-readonly.com>.
2. Since the user is not logged in, the user is redirected to <https://authorization-server.com/login> (Login Flow, authentication), then to <https://authorization-server.com/consent> (Consent Flow , authorization).
3. Next, the user is taken back to <https://client-readonly.com>.
4. To receive data, OAuth 2.0 Client (Readonly) contacts <https://resource-server.com> with an access token.

### Stop

Stopping containers that save data (volume).

```bash
bash stop.bash
```

### Stop and clean

We stop the containers and delete all data (volume).

```bash
bash stop_and_clean.bash
```

## Creators

Nikita Konstantinovich Chistousov

<chistousov.nik@yandex.ru>

## License

MIT
