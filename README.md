# DatasetExtractor

Various applications to extract Discovery reports

## Build
[Mvn Wrapper](https://github.com/takari/maven-wrapper)

#### Using maven-jar-plugin to create executable
**./mvnw install**

**java -jar target/{jar.file) tableName sortColumn**

#### Using maven-exec-plugin to run in maven process

**./mvnw compile exec:java -Dexec.args="{tableName} {sortColumn}"**

## Test

Note the integration tests require a docker environment to be installed as the test rig uses
[TestContainers](https://www.testcontainers.org/)

**./mvnw test**


