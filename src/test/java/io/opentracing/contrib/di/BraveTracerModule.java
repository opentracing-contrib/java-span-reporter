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

import brave.opentracing.BraveTracer;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.opentracing.Tracer;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

import javax.inject.Named;
import javax.inject.Singleton;

public class BraveTracerModule extends AbstractModule {
    @Override
    protected void configure() {

    }
    @Provides
    @Singleton
    @Named("backend")
    protected Tracer tracer() {
        // Configure a reporter, which controls how often spans are sent
        //   (the dependency is io.zipkin.reporter:zipkin-sender-okhttp3)
        OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v1/spans");
        AsyncReporter reporter = AsyncReporter.builder(sender).build();

        // Now, create a Brave tracer with the service name you want to see in Zipkin.
        //   (the dependency is io.zipkin.brave:brave)
        brave.Tracer braveTracer = brave.Tracer.newBuilder()
                .localServiceName("my-service")
                .reporter(reporter)
                .build();

        // Finally, wrap this with the OpenTracing Api
        BraveTracer tracer = BraveTracer.wrap(braveTracer);
        //GlobalTracer.register(tracer)

        // You can later unwrap the underlying Brave Api as needed
        //val braveTracer = tracer.unwrap();
        return tracer;
    }
}
