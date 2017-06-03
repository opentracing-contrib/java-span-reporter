[![Build Status][ci-img]][ci] [![Coverage Status][cov-img]][cov] [![Released Version][maven-img]][maven]

# SpanReporter for Java
SpanReporter forward to backend Tracer and reports trace into Reporter.

```java
public interface Reporter {
    void start(long timestampMicroseconds, SpanR span);
    void finish(long timestampMicroseconds, SpanR span);
    void log(long timestampMicroseconds, SpanR span, Map<String, ?> event);
}
```
## Use Case

* As zero server tracer.
    * To use OpenTracing API, before selection and deployment of your Server Tracer.
    * To log with spanId, context, without using MDC.
* To add log support to some server tracer that doesn't support `.log(...)`
* To report some metrics (eg. histogram of span)

## Slf4jReporter

* Message is reported as structured message (json)
* Message at start and at finish is logged with level TRACE
```
19:48:36.828 [main] TRACE tracer - {"ts":1490982516828,"elapsed":0,"spanId":"d4f6df3d-8dc0-4287-b1a0-1b2bc52978c6","action":"start","operation":"span-0","tags":{"description":"top level initial span in the original process"},"baggage":{},"references":{}}
19:48:36.828 [main] TRACE tracer - {"ts":1490982516828,"elapsed":0,"spanId":"da7d843a-4564-4480-a6cb-19550c24a344","action":"start","operation":"span-1","tags":{"description":"the first inner span in the original process"},"baggage":{},"references":{"child_of":"d4f6df3d-8dc0-4287-b1a0-1b2bc52978c6"}}
19:48:36.828 [main] TRACE tracer - {"ts":1490982516828,"elapsed":0,"spanId":"c4bf987f-2d0c-49f1-87bd-a23930570b96","action":"start","operation":"span-2","tags":{"description":"the second inner span in the original process"},"baggage":{},"references":{"child_of":"da7d843a-4564-4480-a6cb-19550c24a344"}}
19:48:36.841 [main] TRACE tracer - {"ts":1490982516841,"elapsed":13,"spanId":"c4bf987f-2d0c-49f1-87bd-a23930570b96","action":"finish","operation":"span-2","tags":{"description":"the second inner span in the original process"},"baggage":{},"references":{"child_of":"da7d843a-4564-4480-a6cb-19550c24a344"}}
19:48:36.841 [main] TRACE tracer - {"ts":1490982516841,"elapsed":13,"spanId":"da7d843a-4564-4480-a6cb-19550c24a344","action":"finish","operation":"span-1","tags":{"description":"the first inner span in the original process"},"baggage":{},"references":{"child_of":"d4f6df3d-8dc0-4287-b1a0-1b2bc52978c6"}}
19:48:36.841 [main] TRACE tracer - {"ts":1490982516841,"elapsed":13,"spanId":"d4f6df3d-8dc0-4287-b1a0-1b2bc52978c6","action":"finish","operation":"span-0","tags":{"description":"top level initial span in the original process","http.url":"/orders"},"baggage":{},"references":{}}
```
* Message for Span.log report with level INFO by default, the level could be set explicitly by setting the key "loglevel"
```java
span.log("blablabala");
span.log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.DEBUG, "k0", "v0", "k1", 42));
span.log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.WARN, "k0", "v0", "ex", new Exception("boom !")));
```
```
22:16:23.911 [main] INFO  tracer - {"ts":1490991383911,"elapsed":1,"spanId":"dbebbda2-501e-4461-b9c6-32020df8008f","action":"log","operation":"span-2","tags":{"description":"the second inner span in the original process"},"fields":{"event":"blablabala"}}
22:16:23.911 [main] DEBUG tracer - {"ts":1490991383911,"elapsed":1,"spanId":"dbebbda2-501e-4461-b9c6-32020df8008f","action":"log","operation":"span-2","tags":{"description":"the second inner span in the original process"},"fields":{"k0":"v0","k1":42.0}}
22:16:23.911 [main] WARN  tracer - {"ts":1490991383911,"elapsed":1,"spanId":"dbebbda2-501e-4461-b9c6-32020df8008f","action":"log","operation":"span-2","tags":{"description":"the second inner span in the original process"},"fields":{"ex":"java.lang.Exception: boom !\n\tat examples.Sample01.run0(Sample01.java:92)\n\tat examples.Sample01.main(Sample01.java:45)\n","k0":"v0"}}
```

# Usage

## Application intialization

* Add dependency (format depends of your build tool)
```
"io.opentracing.contrib" % "java-span-reporter" % "${span-reporter.version}"
```
* Instantiate the TracerR (For using DI, you can take inspiration from [Guice samples](./src/test/java/io/opentracing/contrib/di)
):
```java
tracer = ... // if not backend tracer use NoopTracerFactory.create()
tracer = new TracerR(tracer, new Slf4jReporter(LoggerFactory.getLogger("tracer")));
```
* Setup your logger (example below use logback, but any slf4j compatible logger should work)
    * add dependencies
    ```
    "ch.qos.logback" % "logback-classic" % "1.1.3"
    ```
    * configure le logger (simplest)
    ```xml
    <logger name="tracer" level="TRACE"/>
    ```

## Custom Reporter(s)

Developer can provide its own [Reporter](./src/main/java/opentracing/contrib/reporter/Reporter.java):
* to report log in an other log system
* to report log in an other format
* to report metrics about span/operation (histogram, nb calls, duration, ...)

To report to several Reporters at once, you can use the CompositeReporter:
```java
Tracer tracer = ...
tracer = new TracerR(tracer, new CompositeReporter(
    new Slf4jReporter(LoggerFactory.getLogger("tracer")),
    new DropwizardMetricsReporter(...)
));
```

  [ci-img]: https://travis-ci.org/opentracing-contrib/java-span-reporter.svg?branch=master
  [ci]: https://travis-ci.org/opentracing-contrib/java-span-reporter
  [cov-img]: https://coveralls.io/repos/github/opentracing-contrib/java-span-reporter/badge.svg?branch=master
  [cov]: https://coveralls.io/github/opentracing-contrib/java-span-reporter?branch=master
  [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/java-span-reporter.svg?maxAge=2592000
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Cjava-span-reporter

# Build

* build localy
    ```
    ./gradlew assemble
    ````
* publish to local maven repository
    ```
    ./gradlew publishToMavenLocal
    ````
* release
    ```
    git tag -a "${version}" -m "release"
    git push --tags
    ```
