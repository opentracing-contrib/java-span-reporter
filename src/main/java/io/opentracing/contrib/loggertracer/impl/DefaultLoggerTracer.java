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
package io.opentracing.contrib.loggertracer.impl;

import io.opentracing.NoopTracerFactory;
import io.opentracing.contrib.loggertracer.LoggerTracer;
import org.slf4j.LoggerFactory;

/**
 * A LoggerTracer with a zero args Constructor and default configuration.
 * It's the implementation use by ServiceLoader (META-INF/services/io.opentracing.Tracer)
 * <code>
 *    new LoggerTracer(NoopTracerFactory.create(), new Slf4jReporter(LoggerFactory.getLogger("tracer")));
 * </code>
 */
public final class DefaultLoggerTracer extends LoggerTracer{

    /**
     * No args constructor used by Service Loader
     */
    public DefaultLoggerTracer() {
        super(NoopTracerFactory.create(), new Slf4jReporter(LoggerFactory.getLogger("tracer")));
    }

}
