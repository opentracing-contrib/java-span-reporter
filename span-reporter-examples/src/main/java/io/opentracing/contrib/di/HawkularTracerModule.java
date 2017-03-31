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
import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import org.hawkular.apm.api.utils.PropertyUtil;
import org.hawkular.apm.client.api.recorder.BatchTraceRecorder;
import org.hawkular.apm.client.api.recorder.TraceRecorder;
import org.hawkular.apm.client.opentracing.APMTracer;
import org.hawkular.apm.trace.publisher.rest.client.TracePublisherRESTClient;

import javax.inject.Named;
import javax.inject.Singleton;

public class HawkularTracerModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    @Named("backend")
    protected Tracer tracer(TraceRecorder traceRecorder) {
        APMTracer tracer0 = new APMTracer(traceRecorder);
        //GlobalTracer.register(tracer0)
        //GlobalTracer.get()
        return tracer0;
    }

    //Hawkular seems to failed to load TracePublisher via ServiceLoader, so Made a explicit
    @Provides
    @Singleton
    protected TraceRecorder traceRecorder() {
        TracePublisherRESTClient publisher = new TracePublisherRESTClient(
                PropertyUtil.getProperty(PropertyUtil.HAWKULAR_APM_USERNAME, "jdoe"),
                PropertyUtil.getProperty(PropertyUtil.HAWKULAR_APM_PASSWORD, "password"),
                PropertyUtil.getProperty(PropertyUtil.HAWKULAR_APM_URI, "http://localhost:8080")
        );
        BatchTraceRecorder.BatchTraceRecorderBuilder builder = new BatchTraceRecorder.BatchTraceRecorderBuilder();
        builder.withTracePublisher(publisher);

        String batchSize = PropertyUtil.getProperty(PropertyUtil.HAWKULAR_APM_COLLECTOR_BATCHSIZE);
        if (batchSize != null) {
            builder.withBatchSize(Integer.parseInt(batchSize));
        }
        String batchTime = PropertyUtil.getProperty(PropertyUtil.HAWKULAR_APM_COLLECTOR_BATCHTIME);
        if (batchTime != null) {
            builder.withBatchTime(Integer.parseInt(batchTime));
        }
        String threadPoolSize = PropertyUtil.getProperty(PropertyUtil.HAWKULAR_APM_COLLECTOR_BATCHTHREADS);
        if (threadPoolSize != null) {
            builder.withBatchPoolSize(Integer.parseInt(threadPoolSize));
        }
        builder.withTenantId(PropertyUtil.getProperty("HAWKULAR_APM_TENANTID"));
        return new BatchTraceRecorder(builder);
    }
}
