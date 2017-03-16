[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# LoggerTracer for Java
LoggerTracer forward to backend Tracer and reports trace into Reporter.

```java
public interface Reporter {
    void start(long timestampMicroseconds, LoggerSpan span);
    void finish(long timestampMicroseconds, LoggerSpan span);
    void log(long timestampMicroseconds, LoggerSpan span, Map<String, ?> event);
}
```
## Use Case

* As zero server tracer.
    * To use OpenTracing API, before selection and deployment of your Server Tracer.
    * To log with spanId, context, without using MDC.
* To add log support to some server tracer that doesn't support `.log(...)`
* To report some metrics (eg. histogram of span)

## Slf4jReporter

* Message are reported as structured message (json)
* Message at start and at finish are logged with criticity/level TRACE
```
22:34:51.913 [main] TRACE tracer - {"ts":139966202861,"elapsed":0,"spanId":"b5b7ef8c-11b9-4df1-93ee-aa56f23d1aec","action":"start","operation":"span-0","tags":{"description":"top level initial span in the original process"},"baggage":{},"references":{}}
22:34:51.927 [main] TRACE tracer - {"ts":139966299730,"elapsed":0,"spanId":"2a344443-5227-4b5f-8750-f4868269f0ea","action":"start","operation":"span-1","tags":{"description":"the first inner span in the original process"},"baggage":{},"references":{"child_of":"b5b7ef8c-11b9-4df1-93ee-aa56f23d1aec"}}
22:34:51.928 [main] TRACE tracer - {"ts":139966300031,"elapsed":0,"spanId":"3a308316-3205-4d25-ada5-f14c5dfd1539","action":"start","operation":"span-2","tags":{"description":"the second inner span in the original process"},"baggage":{},"references":{"child_of":"2a344443-5227-4b5f-8750-f4868269f0ea"}}
22:34:51.928 [main] TRACE tracer - {"ts":139966300276,"elapsed":248,"spanId":"3a308316-3205-4d25-ada5-f14c5dfd1539","action":"finish","operation":"span-2","tags":{"description":"the second inner span in the original process"},"baggage":{},"references":{"child_of":"2a344443-5227-4b5f-8750-f4868269f0ea"}}
22:34:51.928 [main] TRACE tracer - {"ts":139966300391,"elapsed":685,"spanId":"2a344443-5227-4b5f-8750-f4868269f0ea","action":"finish","operation":"span-1","tags":{"description":"the first inner span in the original process"},"baggage":{},"references":{"child_of":"b5b7ef8c-11b9-4df1-93ee-aa56f23d1aec"}}
22:34:51.928 [main] TRACE tracer - {"ts":139966300495,"elapsed":103824,"spanId":"b5b7ef8c-11b9-4df1-93ee-aa56f23d1aec","action":"finish","operation":"span-0","tags":{"description":"top level initial span in the original process","http.url":"/orders"},"baggage":{},"references":{}}
```
* Message for Span.log report with level INFO by default, the level could be set explicitly by setting the key "loglevel"
```java
span.log(Map.of("loglevel", LogLevel.WARN))
```

# Usage

## Application intialization

* Add dependency (format depends of your build tool)
```
"io.opentracing.contrib" % "java-loggertracer" % "0.1.0"
```
* Instanciate the LoggerTracer (For using DI, you can take inspiration from [Guice samples](./src/test/java/io/opentracing/contrib/di)
):
```java
tracer = ... // if not backend tracer use NoopTracerFactory.create()
tracer = new LoggerTracer(tracer, new Slf4jReporter(LoggerFactory.getLogger("tracer")));
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

Developer can provide its own [Reporter](./src/main/java/opentracing/contrib/loggertracer/Reporter.java):
* to report log in an other log system
* to report log in an other format
* to report metrics about span/operation (histogram, nb calls, duration, ...)

To report to several Reporters at once, you can use the CompositeReporter:
```java
Tracer tracer = ...
tracer = new LoggerTracer(tracer, new CompositeReporter(
    new Slf4jReporter(LoggerFactory.getLogger("tracer")),
    new DropwizardMetricsReporter(...)
));
```
