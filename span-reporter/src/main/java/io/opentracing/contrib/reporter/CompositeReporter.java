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

import java.time.Instant;
import java.util.Map;

/**
 * Composite Reporter broadcast reporter's calls to several Reporter.
 * Could used to call a reporter to log system AND a reporter to metrics.
 * <code>
 *     Tracer tracer = ...
 *     tracer = new TracerR(tracer, new CompositeReporter(
 *         loggerReporter,
 *         metricsReporter
 *     ));
 * </code>
 */
public class CompositeReporter implements Reporter {
    private final Reporter[] reporters;

    public CompositeReporter(Reporter... reporters) {
        this.reporters = reporters;
    }

    @Override
    public void start(Instant ts, SpanData span) {
        for(Reporter r: reporters) {
            try {
                r.start(ts, span);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    @Override
    public void finish(Instant ts, SpanData span) {
        for(Reporter r: reporters) {
            try {
                r.finish(ts, span);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    @Override
    public void log(Instant ts, SpanData span, Map<String, ?> fields) {
        for(Reporter r: reporters) {
            try {
                r.log(ts, span, fields);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
