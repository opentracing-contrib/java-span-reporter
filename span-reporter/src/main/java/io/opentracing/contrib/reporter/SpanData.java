package io.opentracing.contrib.reporter;

import io.opentracing.SpanContext;

import java.time.Instant;
import java.util.Map;

abstract public class SpanData {
    public String spanId;
    public String operationName;
    public Instant startAt;
    public Map<String, Object> tags;
    public Map<String, String> references;
    abstract public SpanContext context();
}
