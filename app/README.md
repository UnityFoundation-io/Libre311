# Libre311 API Documentation

## Quickstart
The project requires JDK 11 or later to build and run. There is no need to
install the Micronaut framework, or Gradle, or any tools other than the JDK in order to
build and run the app.

From the project root, the app may be run locally using gradle:

    ./gradlew app:run

To support automatic restart to support changes made while the app is running:

    ./gradlew app:run -t

To build a standalone executable `.jar` file:

    ./gradlew app:assemble

That will generate `build/libs/app-[version number]-all.jar` which may be executed as a normal executable jar:

To execute the entire test suite:

    ./gradlew app:test

```
java -jar build/libs/app-[version number]-all.jar
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
  Micronaut (v3.8.6)

09:28:40.359 [main] INFO  org.hibernate.Version - HHH000412: Hibernate ORM core version [WORKING]
09:28:40.424 [main] INFO  o.h.annotations.common.Version - HCANN000001: Hibernate Commons Annotations {5.1.2.Final}
09:28:40.540 [main] INFO  org.hibernate.dialect.Dialect - HHH000400: Using dialect: org.hibernate.dialect.H2Dialect
09:28:41.331 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 1381ms. Server Running: http://localhost:8080

```

## The Database

Currently, the Libre311 supports the following Databases:

| Database     | Versions     | Driver                   | Reference                                                                                                                 |
|--------------|--------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------|
| MySQL Server | 8.0 and 5.7  | com.mysql.cj.jdbc.Driver | [link](https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-versions.html)                                            |
| PostgreSQL   | 8.2 or newer | org.postgresql.Driver    | [link](https://jdbc.postgresql.org/documentation/#:~:text=The%20current%20version%20of%20the,(JDBC%204.2)%20and%20above.) |

To connect to a database, the following environment variables must be set for the Web API:

- `LIBRE311_JDBC_URL` - The JDBC URL of the database
- `LIBRE311_JDBC_DRIVER` - The driver to use. See Driver column for values.
- `LIBRE311_JDBC_USER` - The database user name.
- `LIBRE311_JDBC_PASSWORD` - The database user password.
- `LIBRE311_AUTO_SCHEMA_GEN` (Options include `none`, `create-only`, `drop`, `create`, `create-drop`, `validate`, and `update` (default value))

The following describes the options for `LIBRE311_AUTO_SCHEMA_GEN` environment variable in detail:

- `none` - No action will be performed.
- `create-only` - Database creation will be generated.
- `drop` - Database dropping will be generated.
- `create` - Database dropping will be generated followed by database creation.
- `create-drop` - Drop the schema and recreate it on SessionFactory startup.
  Additionally, drop the schema on SessionFactory shutdown.
- `validate` - Validate the database schema.
- `update` - Update the database schema.

The `LIBRE311_DATABASE_DEPENDENCY` environment variable must be set when building
the application to inject the correct driver.
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

## External Service Dependencies
The Libre311 Service API expects four external services offered by Google:

* Object Storage - storage for images uploaded by end user
* ReCaptcha - prevents abuse from bots
* SafeSearch - inspects images for inappropriate content
* OAuth/Identity Provision - Admin login to be able to access privileged endpoints

OAuth/Identity Provision is covered in the Security section below.

GCP related configuration such as project-id can be modified in application.yml or set as environment variables.
For example, `micronaut.object-storage.gcp.default.bucket` in application.yml can be set as an environment variable
with `MICRONAUT_OBJECT_STORAGE_GCP_DEFAULT_BUCKET`.


### Object Storage
In the context of the Libre311 application, Google Object Storage is used to store images uploaded by an end user when 
creating a Service Request (aka an Issue).
The client code that interacts with the Object Storage service is a singleton object generated from the class
`app/src/main/java/app/service/storage/StorageService.java`.

In order to call Object Storage, the client must be authenticated. Authentication is managed by the
`io.micronaut.objectstorage:micronaut-object-storage-gcp` dependency which attempts to find GCP credentials in the local
environment.  Please see relevant documentation:

* [Application Default Credentials](https://cloud.google.com/docs/authentication/#adc)
* [How Application Default Credentials works](https://cloud.google.com/docs/authentication/application-default-credentials)

### SafeSearch and ReCaptcha
Both SafeSearch and ReCaptcha rely on HTTP clients that are configured with secret values in application.yml like so:
```yaml
app:
  recaptcha:
    secret: "secret-value"
  safesearch:
    key: "key-value"
```

## Application Design

### Security

#### Authentication & Authorization
The authentication method leverages OAuth where the Identity Provider is Google. Once authenticated, the
backend issues a JWT token (in the form of a cookie) with embedded details as defined in
`app/src/main/java/app/security/CustomAuthenticationMapper.java`.
As can be drawn from the code, the user is 'fully' authenticated (against the Libre311 service) if the user's email already
exists in the database under the `app_users` table.

### Data Layer
The Libre311 Service data models draw inspiration from Open311 GeoReport API v2 specification and is composed of the 
following data models:

* User - An authorized user of the Libre311 application.
* Service - Is the Issue Type related to the type of services that a jurisdiction provides. Examples include Sidewalk or Bus Stop. 
* ServiceDefinition - Issue subtype such as 'Cracked' for Sidewalk or 'No Shelter' for Bus Stop.
* ServiceRequest - The end user reported Issue that references Service and ServiceDefinition and includes contact and 
location details. 

The above data models and their respective data
[repository interfaces](https://micronaut-projects.github.io/micronaut-data/latest/guide/#repositories)
can be found in `app/src/main/java/app/model`.

### Service Layer
Following the pattern of CRUD capabilities per resource, a service primarily interacts with its corresponding resource;
however, each service pulls in (or rather, injects) other services/repository interfaces to provide additional features
for accommodating a users request.

### API Layer
The Libre311 Service is a microservice that exposes endpoints that correspond to 
[Open311 GeoReport API v2](http://wiki.open311.org/GeoReport_v2/) specification and are coded in `app/src/main/java/app/RootController.java`.
In addition, there exists a separate controller class that accommodates image uploading (`app/src/main/java/app/ImageStorageController.java`)

#### Data Transfer Objects (DTOs)
All HTTP request and response bodies are parsed then turned into Data Transfer Objects. The classes in which the DTOs
are based on are located in `app/src/main/java/app/dto` and are annotated with field-level validation annotations. 
With that in mind, a DTO can be declared in a controller's method parameter like so 
`public List<PostResponseServiceRequestDTO> createServiceRequestJson(HttpRequest<?> request, @Valid @Body PostRequestServiceRequestDTO requestDTO)`
and the application will parse the request's body, turn it into an PostRequestServiceRequestDTO object, and validate the requestDTO object
fields according to the validation annotations in the PostRequestServiceRequestDTO class.

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
