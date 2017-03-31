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

import io.opentracing.SpanContext;

import java.util.Map;

//HACK to allow SpanBuilderR to retrieve SpanId of span via SpanContext, take care about:
// * span.wrapped can rewrite content of its context or return a different instance during its lifecycle, so no copy
// * simpler than implements a special Iterable that will add entry for SpanContextR.BAGGAGE_SPANID_KEY
// * wrapped.context() could be shared by several wrapped span
class SpanContextR implements SpanContext {
    protected final SpanR span;

    SpanContextR(SpanR span) {
        this.span = span;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return span.wrapped.context().baggageItems();
    }
}
