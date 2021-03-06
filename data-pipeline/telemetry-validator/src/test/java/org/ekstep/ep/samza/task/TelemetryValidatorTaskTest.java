package org.ekstep.ep.samza.task;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

import org.apache.samza.config.Config;
import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.ep.samza.fixtures.EventFixture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

public class TelemetryValidatorTaskTest {

    private static final String SUCCESS_TOPIC = "telemetry.valid";
    private static final String FAILED_TOPIC = "telemetry.failed";
    private static final String MALFORMED_TOPIC = "telemetry.malformed";
    private static final String SCHEMA_PATH = "src/test/resources";
    private MessageCollector collectorMock;
    private TaskContext contextMock;
    private MetricsRegistry metricsRegistry;
    private Counter counter;
    private TaskCoordinator coordinatorMock;
    private IncomingMessageEnvelope envelopeMock;
    private Config configMock;
    private TelemetryValidatorTask telemetryValidatorTask;
    
    @Before
    public void setUp() {
        collectorMock = mock(MessageCollector.class);
        contextMock = Mockito.mock(TaskContext.class);
        metricsRegistry = Mockito.mock(MetricsRegistry.class);
        counter = Mockito.mock(Counter.class);
        coordinatorMock = mock(TaskCoordinator.class);
        envelopeMock = mock(IncomingMessageEnvelope.class);
        configMock = Mockito.mock(Config.class);
        
        stub(configMock.get("output.success.topic.name", SUCCESS_TOPIC)).toReturn(SUCCESS_TOPIC);
        stub(configMock.get("output.failed.topic.name", FAILED_TOPIC)).toReturn(FAILED_TOPIC);
        stub(configMock.get("output.malformed.topic.name", MALFORMED_TOPIC)).toReturn(MALFORMED_TOPIC);
        stub(configMock.get("telemetry.schema.path", "/etc/samza-jobs/schemas")).toReturn(SCHEMA_PATH);
        
        stub(metricsRegistry.newCounter(anyString(), anyString())).toReturn(counter);
        stub(contextMock.getMetricsRegistry()).toReturn(metricsRegistry);

        telemetryValidatorTask = new TelemetryValidatorTask(configMock, contextMock);
    }

    @Test
    public void shouldSendEventToSuccessTopicIfEventIsValid() throws Exception{
    	
    	stub(envelopeMock.getMessage()).toReturn(EventFixture.VALID_GE_ERROR_EVENT);
    	
    	telemetryValidatorTask.process(envelopeMock, collectorMock, coordinatorMock);
        verify(collectorMock).send(argThat(validateOutputTopic(envelopeMock.getMessage(), SUCCESS_TOPIC)));
    }
    
    @Test
    public void shouldSendEventToFaildTopicIfEventIsNotValid() throws Exception{
    	
    	stub(envelopeMock.getMessage()).toReturn(EventFixture.INVALID_GE_ERROR_EVENT);
    	
    	telemetryValidatorTask.process(envelopeMock, collectorMock, coordinatorMock);
        verify(collectorMock).send(argThat(validateOutputTopic(envelopeMock.getMessage(), FAILED_TOPIC)));
    }
    
    @Test
    public void shouldSendEventToMalformedTopicIfEventIsNotParseable() throws Exception{
    	
    	stub(envelopeMock.getMessage()).toReturn(EventFixture.UNPARSABLE_GE_GENIE_UPDATE_EVENT);
    	
    	telemetryValidatorTask.process(envelopeMock, collectorMock, coordinatorMock);
        verify(collectorMock).send(argThat(validateOutputTopic(envelopeMock.getMessage(), MALFORMED_TOPIC)));
    }
    
    @Test
    public void shouldSendEventToMalformedTopicIfEventIsAnyRandomString() throws Exception{
    	
    	stub(envelopeMock.getMessage()).toReturn(EventFixture.ANY_STRING);
    	
    	telemetryValidatorTask.process(envelopeMock, collectorMock, coordinatorMock);
        verify(collectorMock).send(argThat(validateOutputTopic(envelopeMock.getMessage(), MALFORMED_TOPIC)));
    }
    
    @Test
    public void shouldSendEventToSuccessTopicIfEventIsEmptyJSON() throws Exception{

    	stub(envelopeMock.getMessage()).toReturn(EventFixture.EMPTY_JSON);

    	telemetryValidatorTask.process(envelopeMock, collectorMock, coordinatorMock);
        verify(collectorMock).send(argThat(validateOutputTopic(envelopeMock.getMessage(), SUCCESS_TOPIC)));
    }

    @Test
    public void shouldSendEventToSuccessTopicIfSchemaIsNotPresent() throws Exception{
    	
    	stub(envelopeMock.getMessage()).toReturn(EventFixture.VALID_GE_INTERACT_EVENT);
    	
    	telemetryValidatorTask.process(envelopeMock, collectorMock, coordinatorMock);
        verify(collectorMock).send(argThat(validateOutputTopic(envelopeMock.getMessage(), SUCCESS_TOPIC)));
    }

    @Test
    public void shouldSendEventToFailedTopicIfEidNotPresent() throws Exception {
        stub(envelopeMock.getMessage()).toReturn(EventFixture.EVENT_WITH_EID_MISSING);
        telemetryValidatorTask.process(envelopeMock, collectorMock, coordinatorMock);
        verify(collectorMock).send(argThat(validateOutputTopic(envelopeMock.getMessage(), FAILED_TOPIC)));
    }

    public ArgumentMatcher<OutgoingMessageEnvelope> validateOutputTopic(final Object message, final String stream) {
        return new ArgumentMatcher<OutgoingMessageEnvelope>() {
            @Override
            public boolean matches(Object o) {
                OutgoingMessageEnvelope outgoingMessageEnvelope = (OutgoingMessageEnvelope) o;
                SystemStream systemStream = outgoingMessageEnvelope.getSystemStream();
                assertEquals("kafka", systemStream.getSystem());
                assertEquals(stream, systemStream.getStream());
                return true;
            }
        };
    }
}
