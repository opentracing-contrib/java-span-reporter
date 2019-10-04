package io.opentracing.contrib.reporter;

import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.opentracing.References.CHILD_OF;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpanBuilderRTest {
    @Before
    public void setup() {
        this.reporterMock = mock(Reporter.class);
        this.spanBuilderMock = mock(Tracer.SpanBuilder.class);
        this.scopeManagerMock = mock(ScopeManager.class);
        this.newSpanMock = mock(Span.class);
        this.parentSpanMock = mock(Span.class);
        this.parentSpanContextMock = mock(SpanContext.class);

        // default mock behaviors
        when(spanBuilderMock.start()).thenReturn(newSpanMock);
        when(parentSpanMock.context()).thenReturn(parentSpanContextMock);
    }

    @Test
    public void subspan_ignoreActiveSpan() {
        final SpanR spanR = (SpanR) new SpanBuilderR(spanBuilderMock, reporterMock, "some-operation", scopeManagerMock)
                .ignoreActiveSpan().start();

        // verify
        assertTrue(spanR.references.isEmpty());
        verify(spanBuilderMock).start();
        verify(scopeManagerMock, never()).activeSpan();
    }

    @Test
    public void subspan_explicitAsChildOf() {
        final String spanId = "some-span-id-" + System.currentTimeMillis();
        final Map<String, String> parentBaggage = new HashMap<>();
        parentBaggage.put(SpanBuilderR.BAGGAGE_SPANID_KEY, spanId);

        // setup mocks
        when(parentSpanContextMock.baggageItems()).thenReturn(parentBaggage.entrySet());

        final SpanR spanR = (SpanR) new SpanBuilderR(spanBuilderMock, reporterMock, "some-operation", scopeManagerMock).asChildOf(parentSpanMock).start();

        // verify
        assertFalse(spanR.references.isEmpty());
        verify(scopeManagerMock, never()).activeSpan();
        verify(spanBuilderMock).start();
        verify(parentSpanMock).context();
        verify(parentSpanContextMock, atLeastOnce()).baggageItems();
        assertEquals(spanId, spanR.references.get(CHILD_OF));
    }

    @Test
    public void subspan_implicitAsChildOf() {
        final String spanId = "some-span-id-" + System.currentTimeMillis();
        final Map<String, String> parentBaggage = new HashMap<>();
        parentBaggage.put(SpanBuilderR.BAGGAGE_SPANID_KEY, spanId);

        // setup mocks
        when(scopeManagerMock.activeSpan()).thenReturn(parentSpanMock);
        when(parentSpanContextMock.baggageItems()).thenReturn(parentBaggage.entrySet());

        final SpanR spanR = (SpanR) new SpanBuilderR(spanBuilderMock, reporterMock, "some-operation", scopeManagerMock).start();

        // verify
        assertFalse(spanR.references.isEmpty());
        verify(scopeManagerMock, atLeastOnce()).activeSpan();
        verify(spanBuilderMock).start();
        verify(parentSpanMock).context();
        verify(parentSpanContextMock, atLeastOnce()).baggageItems();
        assertEquals(spanId, spanR.references.get(CHILD_OF));
    }

    private Reporter reporterMock;
    private Tracer.SpanBuilder spanBuilderMock;
    private ScopeManager scopeManagerMock;
    private Span newSpanMock;
    private Span parentSpanMock;
    private SpanContext parentSpanContextMock;
}