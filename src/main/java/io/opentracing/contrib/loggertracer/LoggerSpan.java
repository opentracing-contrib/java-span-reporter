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

import io.opentracing.Span;
import io.opentracing.SpanContext;
import java.util.Collections;
import java.util.Map;

public class LoggerSpan implements Span {
    private Span wrapped;
    private final Reporter reporter;

    public final String spanId;
    public String operationName;
    public final long startAt;
    public final Map<String, Object> tags;
    public final Map<String, String> references;

    public LoggerSpan(Span wrapped, Reporter reporter, String spanId, String operationName, Map<String,Object> tags, Map<String, String> references) {
        this.reporter = reporter;
        this.spanId = spanId;
        this.wrapped = wrapped;
        this.operationName = operationName;
        this.tags = tags;
        this.references = references;
        startAt = now();
        reporter.start(startAt, this);
    }

    protected long now(){
        return System.nanoTime() / 1000l;
    }


    //------------------------------------------------------------------------------------------------------------------
    // Span

    @Override
    public SpanContext context() {
        return new LoggerSpanContext(wrapped.context(), spanId);
    }

    @Override
    public void finish() {
        wrapped.finish();
        reporter.finish(now(), this);
    }

    @Override
    public void finish(long l) {
        wrapped.finish(l);
        reporter.finish(l, this);
    }

    @Override
    public void close() {
        wrapped.close();
        reporter.finish(now(), this);
    }

    @Override
    public Span setTag(String s, String s1) {
        wrapped = wrapped.setTag(s, s1);
        tags.put(s, s1);
        return this;
    }

    @Override
    public Span setTag(String s, boolean b) {
        wrapped = wrapped.setTag(s, b);
        tags.put(s, b);
        return this;
    }

    @Override
    public Span setTag(String s, Number number) {
        wrapped = wrapped.setTag(s, number);
        tags.put(s, number);
        return this;
    }

    @Override
    public Span log(Map<String, ?> map) {
        wrapped = wrapped.log(map);
        toLogger(now(), map);
        return this;
    }

    @Override
    public Span log(long l, Map<String, ?> map) {
        wrapped = wrapped.log(l, map);
        toLogger(l, map);
        return this;
    }

    @Override
    public Span log(String event) {
        wrapped = wrapped.log(event);
        toLogger(now(), Collections.singletonMap("event", event));
        return this;
    }

    @Override
    public Span log(long l, String event) {
        wrapped.log(l, event);
        toLogger(l, Collections.singletonMap("event", event));
        return this;
    }

    @Override
    public Span setBaggageItem(String s, String s1) {
        wrapped = wrapped.setBaggageItem(s, s1);
        return this;
    }

    @Override
    public String getBaggageItem(String s) {
        return wrapped.getBaggageItem(s);
    }

    @Override
    public Span setOperationName(String s) {
        wrapped = wrapped.setOperationName(s);
        operationName = s;
        return this;
    }

    @Override
    @Deprecated
    public Span log(String eventName, Object payload) {
        wrapped = wrapped.log(eventName, payload);
        toLogger(now(), Collections.singletonMap(eventName, payload));
        return this;
    }

    @Override
    @Deprecated
    public Span log(long l, String eventName, Object payload) {
        wrapped = wrapped.log(l, eventName, payload);
        toLogger(l, Collections.singletonMap(eventName, payload));
        return this;
    }

    protected void toLogger(long l, Map<String, ?> map) {
        reporter.log(l, this, map);
    }
}
