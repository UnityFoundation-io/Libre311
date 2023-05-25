# We Move API Documentation

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
java -jar build/libs/permissionsManager-0.1-all.jar 
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

## External Service Dependencies
The We Move Service expects two external services offered by Google: OAuth/Identity Provision
and Object Storage. OAuth/Identity Provision is covered in the Security section below. In the context of Permissions
Manager, Google Secrets Manager is used to store an installation's Certificate Authority and Server certificates/keys.
The client code that interacts with the Secrets Manager service is a singleton object generated from the class
`app/src/main/java/io/unityfoundation/dds/permissions/manager/security/ApplicationSecretsClient.java`.

GCP related configuration such as project-id can be modified in application.yml or set as environment variables.
For example, `gcp.project-id` in application.yml can be set as an environment variable with `GCP_PROJECT_ID`.

In order to call Secrets Manager, the client must be authenticated. Authentication is managed by the
`io.micronaut.gcp:micronaut-gcp-secret-manager` dependency which attempts to find GCP credentials in the local
environment.  Please see relevant documentation:

* [Application Default Credentials](https://cloud.google.com/docs/authentication/#adc)
* [How Application Default Credentials works](https://cloud.google.com/docs/authentication/application-default-credentials)
* [Accessing the Secrets Manager API](https://cloud.google.com/secret-manager/docs/accessing-the-api)

## Application Design

### Security

#### Authentication & Authorization
The We Move Service provides two methods of authentication each of which applies to a different
type of 'user': a traditional User and an external Application.

##### Traditional User
The traditional User authentication method leverages OAuth where the Identity Provider is Google. Once authenticated, the
backend issues a JWT token (in the form of a cookie) with embedded details as defined in
`app/src/main/java/io/unityfoundation/dds/permissions/manager/security/PermissionsManagerAuthenticationMapper.java`.
As can be drawn from the code, the user is 'fully' authenticated (against the We Move service) if the user's email already
exists in the database.

#### Session Expiration
When a User or an Application is authenticated, the JWT token administered is valid for one hour by default.
The default session expiration can be modified via the application.yml file or setting the environment variable
`MICRONAUT_SECURITY_TOKEN_JWT_COOKIE_COOKIE_MAX_AGE=[number][m|h|d|w]`. In addition to the provided JWT token, a
JWT_REFRESH_TOKEN is also provided to the client so that the end user can extend their session without logging back
in for up to 1 day since initial login. To change the default expiration for the refresh token,
`MICRONAUT_SECURITY_TOKEN_REFRESH_COOKIE_COOKIE_MAX_AGE=[number][m|h|d|w]` or application.yml can be modified accordingly.

The refresh token mechanism requires extending framework code, such code in the We Move Service repository
can be found in files prefaced with 'RefreshToken' in `app/src/main/java/io/unityfoundation/dds/permissions/manager/security/`.


### Data Layer
The We Move Service is composed of the following data models and the relationships between said data models:

* Group - A 'container' for all other entities. Has One-to-Many relationships with Users, Applications, and Topics.
* User - An authorized user of the We Move Service
* Application - A subscriber/publisher of the OpenDDS implementation. Belongs to and references a Group.
* Topic - An OpenDDS Topic. Belongs to and references a Group.
* GroupUser - Models Group memberships and is a join table that references User and Group.
  The table also stores Group authorization flags group-admin, topic-admin, and application-admin
* ApplicationPermission - Models an Application's ability to subscribe, publish, or both for a specific Topic.
  The table is a join table references Application and Topic with an additional column of `access_type` (READ, WRITE, READ_WRITE)

The above data models and their respective data
[repository interfaces](https://micronaut-projects.github.io/micronaut-data/latest/guide/#repositories)
can be found in `app/src/main/java/io/unityfoundation/dds/permissions/manager/model`.

### Service Layer
Following the pattern of CRUD capabilities per resource, a service primarily interacts with its corresponding resource;
however, each service pulls in (or rather, injects) other services/repository interfaces to provide additional features
for accommodating a users request.

### API Layer
The We Move Service is a microservice that exposes endpoints which allows users to create, read, update,
and delete (CRUD) the resources based on the above data models. In addition to managing resources via a CRUD API,
the We Move Service includes endpoints that produces OpenDDS credentials for external applications (modeled as an
Application in the We Move Service).

While running, the application serves a SwaggerUI generated page describing the available endpoints at `{baseUrl}/swagger-ui`.

#### Data Transfer Objects (DTOs)
All HTTP request and response bodies are parsed then turned into Data Transfer Objects. The classes in which the DTOs
are based on are located in `app/src/main/java/io/unityfoundation/dds/permissions/manager/model` alongside their
corresponding data models, are annotated with field-level validation annotations. With that in mind, a DTO can be
declared in a controller's method parameter like so `HttpResponse<?> save(@Body @Valid ApplicationDTO applicationDTO)`
and application will parse the request's body, turn it into an ApplicationDTO object, and validate the applicationDTO object
fields according to the validation annotations in the ApplicationDTO class.