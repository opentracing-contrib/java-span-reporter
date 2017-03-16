/**
 * Copyright 2017 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.loggertracer;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class LoggerSpanBuilder implements Tracer.SpanBuilder {
    private static final String BAGGAGE_SPANID_KEY = "logger.spanId";
    private Tracer.SpanBuilder wrapped;
    private Reporter reporter;
    private final Map<String, Object> tags = new LinkedHashMap<>();
    private final Map<String, String> references = new LinkedHashMap<>();
    private String operationName;

    LoggerSpanBuilder(Tracer.SpanBuilder wrapped, Reporter reporter, String operationName){
        this.wrapped = wrapped;
        this.reporter = reporter;
        this.operationName = operationName;
    }

    String findSpanId(SpanContext context) {
        if (context instanceof LoggerSpanContext) {
            return ((LoggerSpanContext) context).spanId;
        }
        for (Map.Entry<String,?> kv: context.baggageItems()) {
            if (BAGGAGE_SPANID_KEY.equals(kv.getKey())) {
                return kv.getValue().toString();
            }
        }
        return "";
    }

    @Override
    public Tracer.SpanBuilder asChildOf(SpanContext spanContext) {
        wrapped = wrapped.asChildOf(spanContext);
        references.put(References.CHILD_OF, findSpanId(spanContext));
        return this;
    }

    @Override
    public Tracer.SpanBuilder asChildOf(Span span) {
        wrapped = wrapped.asChildOf(span);
        references.put(References.CHILD_OF, findSpanId(span.context()));
        return this;
    }

    @Override
    public Tracer.SpanBuilder addReference(String s, SpanContext spanContext) {
        wrapped = wrapped.addReference(s, spanContext);
        references.put(s, findSpanId(spanContext));
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String s, String s1) {
        wrapped = wrapped.withTag(s, s1);
        tags.put(s, s1);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String s, boolean b) {
        wrapped = wrapped.withTag(s, b);
        tags.put(s, b);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String s, Number number) {
        wrapped = wrapped.withTag(s, number);
        tags.put(s, number);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withStartTimestamp(long l) {
        wrapped = wrapped.withStartTimestamp(l);
        return this;
    }

    @Override
    public Span start() {
        Span wspan = wrapped.start();
        String spanId = UUID.randomUUID().toString();
        wspan.setBaggageItem(BAGGAGE_SPANID_KEY, spanId);
        Span span = new LoggerSpan(wspan, reporter, spanId, operationName, tags, references);
        return span;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return wrapped.baggageItems();
    }
}
