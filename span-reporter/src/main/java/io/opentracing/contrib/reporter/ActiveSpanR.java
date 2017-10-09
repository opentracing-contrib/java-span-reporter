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
package io.opentracing.contrib.reporter;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ActiveSpanR extends SpanData implements ActiveSpan {
    private ActiveSpan wrapped;
    protected final Reporter reporter;

    public ActiveSpanR(ActiveSpan wrapped, Reporter reporter, String spanId, String operationName, Map<String,Object> tags, Map<String, String> references) {
        this.reporter = reporter;
        this.wrapped = wrapped;
        this.spanId = spanId;
        this.operationName = operationName;
        this.tags = new ConcurrentHashMap<>(tags);
        this.references = Collections.unmodifiableMap(references);
        this.startAt = now();
        reporter.start(this.startAt, this);
    }

    protected Instant now(){
        return Instant.now();
    }

    protected Instant ts(long timestampMicroSeconds){
        // lost of precision
        return Instant.ofEpochMilli(timestampMicroSeconds / 1000);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Span

    @Override
    public SpanContext context() {
        return wrapped.context();
    }

    @Override
    public void deactivate() {
        wrapped.deactivate();
    }

    @Override
    public void close() {
        wrapped.close();
        reporter.finish(now(), this);
    }

    @Override
    public Continuation capture() {
        //FIXME Continuation should be Wrapped to return the correct ActiveSpan
        return wrapped.capture();
    }

    @Override
    public ActiveSpan setTag(String s, boolean b) {
        wrapped = wrapped.setTag(s, b);
        this.tags.put(s, b);
        return this;
    }

    @Override
    public ActiveSpan setTag(String s, String s1) {
        wrapped = wrapped.setTag(s, s1);
        this.tags.put(s, s1);
        return this;
    }

    @Override
    public ActiveSpan setTag(String s, Number number) {
        wrapped = wrapped.setTag(s, number);
        this.tags.put(s, number);
        return this;
    }

    @Override
    public ActiveSpan log(Map<String, ?> map) {
        wrapped = wrapped.log(map);
        toLogger(now(), map);
        return this;
    }

    @Override
    public ActiveSpan log(long l, Map<String, ?> map) {
        wrapped = wrapped.log(l, map);
        toLogger(ts(l), map);
        return this;
    }

    @Override
    public ActiveSpan log(String event) {
        wrapped = wrapped.log(event);
        toLogger(now(), Collections.singletonMap("event", event));
        return this;
    }

    @Override
    public ActiveSpan log(long l, String event) {
        wrapped.log(l, event);
        toLogger(ts(l), Collections.singletonMap("event", event));
        return this;
    }

    @Override
    public ActiveSpan setBaggageItem(String s, String s1) {
        wrapped = wrapped.setBaggageItem(s, s1);
        return this;
    }

    @Override
    public String getBaggageItem(String s) {
        return wrapped.getBaggageItem(s);
    }

    @Override
    public ActiveSpan setOperationName(String s) {
        wrapped = wrapped.setOperationName(s);
        this.operationName = s;
        return this;
    }

    @Override
    @Deprecated
    public ActiveSpan log(String eventName, Object payload) {
        wrapped = wrapped.log(eventName, payload);
        toLogger(now(), Collections.singletonMap(eventName, payload));
        return this;
    }

    @Override
    @Deprecated
    public ActiveSpan log(long l, String eventName, Object payload) {
        wrapped = wrapped.log(l, eventName, payload);
        toLogger(ts(l), Collections.singletonMap(eventName, payload));
        return this;
    }

    protected void toLogger(Instant ts, Map<String, ?> map) {
        reporter.log(ts, this, map);
    }
}
