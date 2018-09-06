# Dash Schema for Android

# Setup
- `git clone https://github.com/sambarboza/json-schema`
- `cd json-schema`
- `mvn clean install -DskipTests`

# Building
- `cd dash-schema-android`
- `./gradlew assemble`
- After building it will be available on the local Maven repository.
- To use it with gradle, add `mavenLocal()` to the `repositories` list in your `build.gradle` file and add `org.dashevo:schema:1.0-SNAPSHOT` as dependency. 

# TODO
- Send PR to `everit-json-schema` repo or fork `json-schema` to DashEvo org and publish Maven package.
- Publish to jcenter/maven central
