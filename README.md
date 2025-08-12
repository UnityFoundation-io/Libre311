# Libre311

Libre311 is a web application for service requests based on the [Open311](https://www.open311.org) standard.

## Quickstart
### Prerequisites
Libre311 uses the UnityAuth project (https://github.com/UnityFoundation-io/UnityAuth) as its
authentication and authorization service. Before running Libre311, make sure the UnityAuth
service is up and running.
Clone the UnityAuth project and follow the instructions in that repo to run the UnityAuth
service either locally or using Docker.

### Local Environment
* Create a `setenv.sh` from `setenv.sh.example` and update it to use the correct environment
  variable values
   - If you use a local database, also update the `DATASOURCES_DEFAULT_*` variables in that file 
   - Make sure the URLs and other configuration in `setenv.sh` is consistent with the
     environment-specific Micronaut config file, e.g., application-local.yml.

Run the Libre311 API server from the project root:
```shell
source setenv.sh
gradlew app:run
```

In another terminal window, run the Libre311 UI:
```shell
source setenv.sh
cd frontend
npm install
npm run dev
```

### Docker Environment
Libre311 services can also be started using Docker Compose.
* From the project root, copy `.env.example` into `.env.docker` and update it with the correct
  environment variable values.
  - Note: the docker compose files read from `.env.docker` so make sure to use this file name.
* Copy `frontend/.env.example` into `frontend/.env.docker` and also update the variables there.

Run `docker compose` from the project root:
```sh
docker compose -f docker-compose.local.yml up
```
This will launch containers for Libre311 API server, frontend server, and database server with
names `libre311-api`, `libre311-ui-dev`, and `libre311-db`, respectively.

The docker containers can be accessed from the host machine:
- **Libre311 API** with http://localhost:8080 (inside Docker: http://libre311-api:8080)
- **Libre311 UI** with http://localhost:3000 (inside Docker: http://libre311-ui-dev:3000)
- **MySQL Database** is open via port `23306` from `localhost`
    (within Docker, host is `libre311-db`, port is `3306`)

#### Start Sub-Components
There's also a Makefile you can use to start groups of services as follows:
```sh
make compose_api ## For API and database
# or
make compose_ui ## For UI and database
# or
make compose_all ## For UI + API and database
```

#### Hosts File Updates
For consistent internal-external service name resolution, add these to your `/etc/hosts` file:

```txt
127.0.0.1 libre311-api
127.0.0.1 libre311-db
127.0.0.1 libre311-ui
127.0.0.1 libre311-ui-dev
```
Note: If you also use the docker environment for UnityAuth services, make sure to also add
entries for them in `/etc/hosts` (see UnityAuth project documentation).

## Operator Documentation

### General Application Architecture

              +------------+     +---------+     +----------+
              | Web App UI |<--->| Web API |<-+->| Database |
              +------------+     +---------+  |  +----------+
                                              |  +---------------+
                                              +->| Auth Provider |
                                                 +---------------+

The Libre311 application consists of

- A Web App UI that can either be served by the Web API or independently
- A Web API that serves data to the UI
- A Database for persistent storage
- An Auth Provider for authenticating users

The Web Application UI is built using Svelte and is served by the Web API.
The Web API is built using Micronaut.
The Web API is horizontally scalable.

### Service Discovery Configuration

As outlined in Open311's Service Discovery [page](https://wiki.open311.org/Service_Discovery), an endpoint is offered
at `/discovery` which describes organization contact and the base URLs of endpoints.
As a convenience, Libre311 provides a default set of endpoint configurations for a set
of `production` and `test` environments as well as the ability to set
configuration values via the following environment variables:

- `LIBRE311_DISCOVERY_CHANGESET_DATETIME`
- `LIBRE311_DISCOVERY_CONTACT_MESSAGE`
- `LIBRE311_DISCOVERY_PRODUCTION_URL`
- `LIBRE311_DISCOVERY_TEST_URL`

Please feel free to modify `app/src/main/resources/application.yml`'s `app.discovery`
content to your use case.

### Configuring the Web API

The following environment variables should be set to configure the application:

- `GCP_PROJECT_ID` - The GCP project ID
- `STORAGE_BUCKET_ID` - The ID of the bucket where user-uploaded images are hosted.
- `RECAPTCHA_SECRET` - Site abuse prevention.
- `MICRONAUT_SECURITY_TOKEN_JWT_SIGNATURES_SECRET_GENERATOR_SECRET` - Secret uses to sign JWTs.
- `MICRONAUT_SECURITY_TOKEN_JWT_GENERATOR_REFRESH_TOKEN_SECRET` - Secret for JWT renewal tokens.
- `MICRONAUT_SECURITY_REDIRECT_LOGIN_SUCCESS`
- `MICRONAUT_SECURITY_REDIRECT_LOGIN_FAILURE`
- `MICRONAUT_SECURITY_REDIRECT_LOGOUT`

### Configuring the Web Application UI

The Web Application UI requires the URL of the API when built.
This is set using the `VITE_BACKEND_URL` environment variable.
If the Web API will serve the UI, then set `VITE_BACKEND_URL` to `/api`.

### Configuring Google as an Auth Provider

Set the following environment variables to enable Google as an auth provider:

- `GOOGLE_CLIENT_ID` - The id of OAuth client.
- `GOOGLE_CLIENT_SECRET` - The secret of the OAuth client.
- `MICRONAUT_SECURITY_OAUTH2_CLIENTS_GOOGLE_OPENID_ISSUER` - Set to "https://accounts.google.com"

See `app/src/main/resources/application.yml` for a complete list of configuration options.

Copyright 2023 Libre311 Authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
