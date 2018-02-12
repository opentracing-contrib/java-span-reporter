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
package io.opentracing.contrib.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.opentracing.ScopeManager;
import io.opentracing.Tracer;
import io.opentracing.contrib.reporter.TracerR;
import io.opentracing.contrib.reporter.slf4j.Slf4jReporter;
import io.opentracing.util.AutoFinishScopeManager;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;

public class LoggerTracerModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    protected ScopeManager scopeManager(@Named("backend") Tracer tracer) {
        return (tracer.scopeManager() == null) ? tracer.scopeManager() : new AutoFinishScopeManager();
    }

    @Provides
    @Singleton
    protected Tracer tracer(@Named("backend") Tracer tracer, ScopeManager scopeManager) {
        return new TracerR(tracer, new Slf4jReporter(LoggerFactory.getLogger("tracer"), true), scopeManager);
    }
}
