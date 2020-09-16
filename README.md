# Dash Platform Protocol (DPP) for JVM

# Building
- `git clone https://github.com/github/dashevo/android-dpp.git`
- `cd android-dpp`
- `./gradlew assemble`
- After building it will be available on the local Maven repository.
- To use it with gradle, add `mavenLocal()` to the `repositories` list in your `build.gradle` file and add `org.dashevo:dpp:0.15-SNAPSHOT` as dependency. 

# Tests
Run tests with `gradle build test`

# TODO
- Publish to jcenter/maven central
