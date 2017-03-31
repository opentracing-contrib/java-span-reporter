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
package io.opentracing.contrib.reporter.slf4j;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.opentracing.contrib.reporter.LogLevel;
import io.opentracing.contrib.reporter.SpanR;
import io.opentracing.contrib.reporter.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * A Reporter to a slf4j Logger.
 * <ul>
 *   <li>report start, finish with level TRACE</li>
 *   <li>report log with level define by key "loglevel" (value is a LogLevel), by default INFO</li>
 *   <li>message are formatted as json (structured message)</li>
 * </ul>
 */
public class Slf4jReporter implements Reporter {
    private final Logger logger;
    private final JsonFactory f = new JsonFactory();

    public Slf4jReporter(Logger logger) {
        LoggerFactory.getLogger(this.getClass()).info("{reporter: 'init'}");
        this.logger = logger;
    }

    @Override
    public void start(Instant ts, SpanR span) {
        if (logger.isTraceEnabled()) {
            logger.trace(toStructuredMessage(ts, "start", span,null));
        }
    }

    @Override
    public void finish(Instant ts, SpanR span) {
        if (logger.isTraceEnabled()) {
            logger.trace(toStructuredMessage(ts, "finish", span, null));
        }
    }

    @Override
    public void log(Instant ts, SpanR span, Map<String, ?> fields) {
        LogLevel level = LogLevel.INFO;
        try {
            LogLevel level0 = (LogLevel) fields.get(LogLevel.FIELD_NAME);
            if (level0 != null) {
                level = level0;
                fields.remove(LogLevel.FIELD_NAME);
            }
        } catch (Exception exc) {
            logger.warn("fail to read value of field {}", LogLevel.FIELD_NAME, exc);
        }
        switch (level) {
            case TRACE:
                if (logger.isTraceEnabled()) {
                    logger.trace(toStructuredMessage(ts, "log", span, fields));
                }
                break;
            case DEBUG:
                if (logger.isDebugEnabled()) {
                    logger.debug(toStructuredMessage(ts, "log", span, fields));
                }
                break;
            case WARN:
                if (logger.isWarnEnabled()) {
                    logger.warn(toStructuredMessage(ts, "log", span, fields));
                }
                break;
            case ERROR:
                if (logger.isErrorEnabled()) {
                    logger.error(toStructuredMessage(ts, "log", span, fields));
                }
                break;
            default:
                if (logger.isInfoEnabled()) {
                    logger.info(toStructuredMessage(ts, "log", span, fields));
                }
        }
    }

    protected String toStructuredMessage(Instant ts, String action, SpanR span, Map<String,?> fields){
        //return "" + (timestampMicroseconds - startAt);

        try {
            StringWriter w = new StringWriter();
            JsonGenerator g = f.createGenerator(w);

            g.writeStartObject();
            g.writeNumberField("ts", ts.toEpochMilli());
            g.writeNumberField("elapsed", Duration.between(span.startAt, ts).toMillis());
            g.writeStringField("spanId", span.spanId);
            g.writeStringField("action", action);
            g.writeStringField("operation", span.operationName);
            g.writeObjectFieldStart("tags");
            for(Map.Entry<String,Object> kv : span.tags.entrySet()){
                Object v = kv.getValue();
                if (v instanceof String) {
                    g.writeStringField(kv.getKey(), (String)v);
                } else if (v instanceof Number) {
                    g.writeNumberField(kv.getKey(), ((Number) v).doubleValue());
                } else if (v instanceof Boolean) {
                    g.writeBooleanField(kv.getKey(), (Boolean) v);
                }
            }
            g.writeEndObject();
            if (fields != null && !fields.isEmpty()){
                g.writeObjectFieldStart("fields");
                for(Map.Entry<String,?> kv : fields.entrySet()){
                    Object v = kv.getValue();
                    if (v instanceof String) {
                        g.writeStringField(kv.getKey(), (String)v);
                    } else if (v instanceof Number) {
                        g.writeNumberField(kv.getKey(), ((Number) v).doubleValue());
                    } else if (v instanceof Boolean) {
                        g.writeBooleanField(kv.getKey(), (Boolean) v);
                    }
                }
                g.writeEndObject();
            } else {
                g.writeObjectFieldStart("baggage");
                for(Map.Entry<String,String> kv : span.context().baggageItems()){
                    g.writeStringField(kv.getKey(), kv.getValue());
                }
                g.writeEndObject();
                g.writeObjectFieldStart("references");
                for(Map.Entry<String,String> kv : span.references.entrySet()){
                    g.writeStringField(kv.getKey(), kv.getValue());
                }
                g.writeEndObject();
            }

            g.writeEndObject();
            g.close();
            w.close();
            return w.toString();
        } catch(Exception exc) {
            exc.printStackTrace();
        }
        return "";
    }
}
