# so-source-plugin

so-source is a gradle plugin for querying which aar the so belongs to.

### Add to your project
------
<a href="https://search.maven.org/search?q=g:com.github.salmonocean%20AND%20a:so-source"><img src="https://img.shields.io/maven-central/v/com.github.salmonocean/so-source.svg"></a>

Available on <a href="https://search.maven.org/search?q=g:com.github.salmonocean%20AND%20a:so-source">Maven Central</a>.
* `rootProject` build.gradle
```gradle
buildscript {
    dependencies {
        classpath 'com.github.salmonocean:so-source:0.0.1'
    }
}
```

* `Application` build.gradle
```gradle
apply plugin : 'so-source'
```

### How to use
------
```shell
./gradlew :app:soSourceForDebug

# the result is store at ./app/build/output/so_source_debug.txt
```
