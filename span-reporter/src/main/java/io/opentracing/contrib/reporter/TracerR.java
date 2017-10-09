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
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

public class TracerR implements Tracer {
    final Tracer wrapped;
    final Reporter reporter;

    /**
     * @param wrapped the backend tracer, if you don't want a wrapped tracer use NoopTracerFactory.create()
     * @param reporter the reporter to use (eg )) )
     */
    public TracerR(Tracer wrapped, Reporter reporter) {
        this.wrapped = wrapped;
        this.reporter = reporter;
    }

    @Override
    public SpanBuilder buildSpan(String s) {
        return new SpanBuilderR(wrapped.buildSpan(s), reporter, s);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C c) {
        wrapped.inject(spanContext, format, c);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C c) {
        return wrapped.extract(format, c);
    }

    @Override
    public ActiveSpan activeSpan() {
        return wrapped.activeSpan();
    }

    @Override
    public ActiveSpan makeActive(Span span) {
        return wrapped.makeActive(span);
    }
}
