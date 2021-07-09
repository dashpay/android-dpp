# Dash Platform Protocol (DPP) for JVM

# Build
```
git clone https://github.com/github/dashevo/android-dpp.git
cd android-dpp
/gradlew assemble
```
- After building, it will be available on the local Maven repository.
- To use it with gradle, add `mavenLocal()` to the `repositories` list in your `build.gradle` file and add `org.dashj.platform:dpp:0.20-SNAPSHOT` as a dependency. 

# Usage
Add mavenCentral() to the `repositories` list in your `build.gradle`
```groovy

dependencies {
    implementation 'org.dashj.platform:dpp:0.20-SNAPSHOT'
}
```

# KtLint
Check using ktlint:
```shell
./gradlew ktlintCheck
```
Format using ktlint:
```shell
./gradlew ktlintFormat
```

# Tests
Run tests with `gradle build test`

# Publish to Maven Central
```  
./gradlew uploadArchives
```