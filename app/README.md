### The Database

Currently, the Libre311 supports the following Databases:

| Database     | Versions     | Driver                   | Reference                                                                                                                 |
|--------------|--------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------|
| MySQL Server | 8.0 and 5.7  | com.mysql.cj.jdbc.Driver | [link](https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-versions.html)                                            |
| PostgreSQL   | 8.2 or newer | org.postgresql.Driver    | [link](https://jdbc.postgresql.org/documentation/#:~:text=The%20current%20version%20of%20the,(JDBC%204.2)%20and%20above.) |

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


