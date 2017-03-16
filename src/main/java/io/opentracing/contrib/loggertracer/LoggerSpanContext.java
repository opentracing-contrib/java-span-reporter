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

import io.opentracing.SpanContext;

import java.util.Map;

class LoggerSpanContext implements SpanContext {
    private final SpanContext wrapped;
    public final String spanId;

    LoggerSpanContext(SpanContext wrapped, String spanId) {
        this.wrapped = wrapped;
        this.spanId = spanId;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return wrapped.baggageItems();
    }
}
