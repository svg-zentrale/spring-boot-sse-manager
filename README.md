# spring-boot-sse-manager
[![GitHub license](https://img.shields.io/github/license/svg-zentrale/spring-boot-sse-manager)](https://github.com/svg-zentrale/spring-boot-sse-manager/blob/master/LICENSE)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/svg-zentrale/spring-boot-sse-manager/test)
[![codecov](https://codecov.io/gh/svg-zentrale/spring-boot-sse-manager/branch/master/graph/badge.svg)](https://codecov.io/gh/svg-zentrale/spring-boot-sse-manager)


### About

SSEEmitter wrapped in some goodies for info and keep-alive

Features:
* Heartbeat
* Info / Debug / Error / Done Events

### Setup
 
Releases are published to [bintray jcenter](https://bintray.com/svg-zentrale/maven/spring-boot-sse-manager) and 
[maven central](https://maven-badges.herokuapp.com/maven-central/de.svg/spring-boot-sse-manager) 

![Bintray](https://img.shields.io/bintray/v/svg-zentrale/maven/spring-boot-sse-manager)
![Maven Central](https://img.shields.io/maven-central/v/de.svg/spring-boot-sse-manager)

Maven:

```xml
<dependency>
  <groupId>de.svg</groupId>
  <artifactId>spring-boot-sse-manager</artifactId>
  <version>0.1.14</version>
</dependency>
```

Gradle:

```groovy
implementation 'de.svg:spring-boot-sse-manager:0.1.14'
```

##### Snapshots

You can use snapshot versions through [JitPack](https://jitpack.io):

* Go to [JitPack project page](https://jitpack.io/#marvinosswald/spring-boot-sse-manager)
* Select `Commits` section and click `Get it` on commit you want to use (top one - the most recent)
* Follow displayed instruction: add repository and change dependency (NOTE: due to JitPack convention artifact group will be different)

### Usage

#### Parallel Tasks 

```java
@GetMapping(path = "/stream")
public SseEmitter downloadLink() {
        return new SSEStream((SSEStream stream) -> {
            List<Integer> collect = Stream.of(1, 2, 3)
                .map(integer -> ((Callable<Integer>) (() -> {
                    stream.info("This Service is working on number: " + integer);
                    return integer;
                })
            ))
            .map(asyncTaskExecutor::submit)
            .map(future -> {
                try {
                    return future.get();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            })
            .collect(Collectors.toList());
            stream.done(collect);

        }, 30000L);
}
```