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

import io.opentracing.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SpanBuilderR implements Tracer.SpanBuilder {
    private static final String BAGGAGE_SPANID_KEY = "reporter.spanId";
    private Tracer.SpanBuilder wrapped;
    private Reporter reporter;
    private ScopeManager scopeManager;
    private final Map<String, Object> tags = new LinkedHashMap<>();
    private final Map<String, String> references = new LinkedHashMap<>();
    private String operationName;


    SpanBuilderR(Tracer.SpanBuilder wrapped, Reporter reporter, String operationName, ScopeManager scopeManager){
        this.wrapped = wrapped;
        this.reporter = reporter;
        this.operationName = operationName;
        this.scopeManager = scopeManager;
    }

    String findSpanId(SpanContext context) {
        if (context.baggageItems() != null) {
            for (Map.Entry<String,String> kv: context.baggageItems()) {
                if (BAGGAGE_SPANID_KEY.equals(kv.getKey())) {
                    return kv.getValue();
                }
            }
        }
        return "";
    }

    @Override
    public Tracer.SpanBuilder asChildOf(SpanContext spanContext) {
        return addReference(References.CHILD_OF, spanContext);
    }

    @Override
    public Tracer.SpanBuilder asChildOf(Span parent) {
        return addReference(References.CHILD_OF, parent.context());
    }

    //FIXME manage reference to parent
    @Override
    public Tracer.SpanBuilder addReference(String s, SpanContext spanContext) {
        wrapped.addReference(s, spanContext);
        references.put(s, findSpanId(spanContext));
        return this;
    }

    //FIXME manage reference to parent
    @Override
    public Tracer.SpanBuilder ignoreActiveSpan() {
        wrapped.ignoreActiveSpan();
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
    public Scope startActive(boolean finishSpanOnClose) {
        return this.scopeManager.activate(this.startManual(), finishSpanOnClose);
        //String spanId = UUID.randomUUID().toString();
        //scope.span().setBaggageItem(BAGGAGE_SPANID_KEY, spanId);
        //return scope;
    }

    //FIXME add implicit link to parent
    @Override
    public Span startManual() {
        Span wspan = wrapped.startManual();
        String spanId = UUID.randomUUID().toString();
        wspan.setBaggageItem(BAGGAGE_SPANID_KEY, spanId);
        return new SpanR(wspan, reporter, spanId, operationName, tags, references);
    }

    @Override
    public Span start() {
        Span wspan = wrapped.start();
        String spanId = UUID.randomUUID().toString();
        wspan.setBaggageItem(BAGGAGE_SPANID_KEY, spanId);
        return new SpanR(wspan, reporter, spanId, operationName, tags, references);
    }

}
