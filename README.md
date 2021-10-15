# Dash Platform Protocol (DPP) for JVM

[![License](https://img.shields.io/github/license/dashevo/dashsync-iOS)](https://github.com/dashevo/dashsync-iOS/blob/master/LICENSE)
[![dashevo/dashsync-iOS](https://tokei.rs/b1/github/dashevo/android-dpp?category=lines)](https://github.com/dashevo/dashsync-iOS)

| Branch | Tests                                                                                      | Coverage                                                                                                                             | Linting                                                                    |
|--------|--------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| master | [![Tests](https://github.com/dashevo/android-dpp/workflows/Tests/badge.svg?branch=test-improvements)](https://github.com/dashevo/android-dpp/actions) | [![codecov](https://codecov.io/gh/dashevo/android-dpp/branch/master/graph/badge.svg)](https://codecov.io/gh/dashevo/android-dpp) | ![Lint](https://github.com/dashevo/android-dpp/workflows/Kotlin%20Linter/badge.svg) |


# Build
```
git clone https://github.com/github/dashevo/android-dpp.git
cd android-dpp
/gradlew assemble
```
- After building, it will be available on the local Maven repository.
- To use it with gradle, add `mavenLocal()` to the `repositories` list in your `build.gradle` file and add `org.dashj.platform:dpp:0.21-SNAPSHOT` as a dependency. 

# Usage
Add mavenCentral() to the `repositories` list in your `build.gradle`
```groovy

dependencies {
    implementation 'org.dashj.platform:dpp:0.21-SNAPSHOT'
}
```

# KtLint
Check using ktlint:
```shell
./gradlew ktlint
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