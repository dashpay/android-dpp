# Dash Schema for Android

# Building
- `cd dash-schema-android`
- `./gradlew assemble`
- After building it will be available on the local Maven repository.
- To use it with gradle, add `mavenLocal()` to the `repositories` list in your `build.gradle` file and add `org.dashevo:schema:1.0-SNAPSHOT` as dependency. 

# TODO
- Publish to jcenter/maven central
