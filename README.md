# Libre311

Libre311 is a web application for service requests based on the [Open311](https://www.open311.org) standard.

## Operator Documentation

### General Application Architecture

              +------------+     +---------+     +----------+
              | Web App UI |<--->| Web API |<-+->| Database |
              +------------+     +---------+  |  +----------+
                                              |  +---------------+
                                              +->| Auth Provider |
                                                 +---------------+

The Libre311 application consists of

* A Web App UI that can either be served by the Web API or independently
* A Web API that serves data to the UI
* A Database for persistent storage
* An Auth Provider for authenticating users

The Web Application UI is built using Svelte and is served by the Web API.
The Web API is built using Micronaut.
The Web API is horizontally scalable.

### The Database

Currently, the Libre311 application supports the following Databases:

| Database     | Versions     | Driver                   | Reference                                                                                                                 |
|--------------|--------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------|
| MySQL Server | 8.0 and 5.7  | com.mysql.cj.jdbc.Driver | [link](https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-versions.html)                                            |

To connect to a database, the following environment variables must be set for the Web API:

* LIBRE311_JDBC_URL - The JDBC URL of the database
* LIBRE311_JDBC_DRIVER - The driver to use. See Driver column for values.
* LIBRE311_JDBC_USER - The database user name.
* LIBRE311_JDBC_PASSWORD - The database user password.
* LIBRE311_AUTO_SCHEMA_GEN (Options include `none`, `create-only`, `drop`, `create`, `create-drop`, `validate`, and `update` (default value))

The following describes the options for LIBRE311_AUTO_SCHEMA_GEN environment variable in detail:

* *none** - No action will be performed.
* *create-only** - Database creation will be generated.
* *drop** - Database dropping will be generated.
* *create** - Database dropping will be generated followed by database creation.
* *create-drop** - Drop the schema and recreate it on SessionFactory startup. Additionally, drop the schema on SessionFactory shutdown.
* *validate** - Validate the database schema.
* *update** - Update the database schema.

The LIBRE311_DATABASE_DEPENDENCY environment variable must be set when building the application to inject the correct driver.
Examples include `mysql:mysql-connector-java:8.0.31` and `org.postgresql:postgresql:42.4.2`.
Multiple drivers can be specified.
For example, `mysql:mysql-connector-java:8.0.31,com.google.cloud.sql:mysql-socket-factory-connector-j-8:1.7.2`.

### CSV Download Users

The database contains a table listing the email addresses of users that are authorized to download CSV reports.
To authorize a user for this activity, insert their email address as follows:

```sql
USE libre311db;
INSERT INTO app_users (email)
VALUES ('$EMAIL');
```

### Service Discovery Configuration
As outlined in Open311's Service Discovery [page](https://wiki.open311.org/Service_Discovery), an endpoint is offered
at `/discovery` which describes organization contact and the base URLs of endpoints. As a convenience, Libre311 provides 
a default set of endpoint configurations for a set of `production` and `test` environments as well as the ability to set
configuration values via the following environment variables:


* LIBRE311_DISCOVERY_CHANGESET_DATETIME
* LIBRE311_DISCOVERY_CONTACT_MESSAGE
* LIBRE311_DISCOVERY_PRODUCTION_URL
* LIBRE311_DISCOVERY_TEST_URL


Please feel free to modify `app/src/main/resources/application.yml`'s `app.discovery` content to your use case.  

### Building the Web Application UI

The Web Application UI requires the URL of the API when built.
This is set using the VITE_BACKEND_URL environment variable.
If the Web API will serve the UI, then set VITE_BACKEND_URL to `/api`.

### Configuring Google as an auth provider

Set the following environment variables to enable Google as an auth provider:

* GOOGLE_CLIENT_ID - The id of oauth client.
* GOOGLE_CLIENT_SECRET - The secret of the oauth client.
* MICRONAUT_SECURITY_OAUTH2_CLIENTS_GOOGLE_OPENID_ISSUER - Set to "https://accounts.google.com"

### Configuring the Web API

The following environment variables should be set to configure the application:

* GCP_PROJECT_ID - The GCP project ID
* STORAGE_BUCKET_ID - The ID of the bucket where user-uploaded images are hosted.
* RECAPTCHA_SECRET - Site abuse prevention.
* SAFESEARCH_KEY - Prevents explicit images from being uploaded. 
* MICRONAUT_SECURITY_TOKEN_JWT_SIGNATURES_SECRET_GENERATOR_SECRET - Secret uses to sign JWTs.
* MICRONAUT_SECURITY_TOKEN_JWT_GENERATOR_REFRESH_TOKEN_SECRET - Secret for JWT renewal tokens.
* MICRONAUT_SECURITY_REDIRECT_LOGIN_SUCCESS
* MICRONAUT_SECURITY_REDIRECT_LOGIN_FAILURE
* MICRONAUT_SECURITY_REDIRECT_LOGOUT

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
