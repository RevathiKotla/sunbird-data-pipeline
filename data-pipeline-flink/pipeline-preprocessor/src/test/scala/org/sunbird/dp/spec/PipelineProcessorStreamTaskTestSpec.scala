package org.sunbird.dp.spec

import java.util

import com.google.gson.Gson
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.ekstep.dp.{BaseMetricsReporter, BaseTestSpec}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.sunbird.dp.core.FlinkKafkaConnector
import org.sunbird.dp.domain.Event
import org.sunbird.dp.fixture.EventFixtures
import org.sunbird.dp.task.{PipelinePreprocessorConfig, PipelinePreprocessorStreamTask}
import redis.embedded.RedisServer

class PipelineProcessorStreamTaskTestSpec extends BaseTestSpec {

  implicit val eventTypeInfo: TypeInformation[Event] = TypeExtractor.getForClass(classOf[Event])
  implicit val stringTypeInfo: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])

  val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
    .setConfiguration(testConfiguration())
    .setNumberSlotsPerTaskManager(1)
    .setNumberTaskManagers(1)
    .build)

  var redisServer: RedisServer = _
  val config: Config = ConfigFactory.load("test.conf")
  val ppConfig: PipelinePreprocessorConfig = new PipelinePreprocessorConfig(config)

  val gson = new Gson()
  val mockKafkaUtil: FlinkKafkaConnector = mock[FlinkKafkaConnector](Mockito.withSettings().serializable())

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    redisServer = new RedisServer(6340)
    redisServer.start()

    BaseMetricsReporter.gaugeMetrics.clear()

    when(mockKafkaUtil.kafkaEventSource[Event](ppConfig.kafkaInputTopic)).thenReturn(new PipeLineProcessorEventSource)

    when(mockKafkaUtil.kafkaEventSink[Event](ppConfig.kafkaDuplicateTopic)).thenReturn(new DupEventsSink)
    when(mockKafkaUtil.kafkaEventSink[Event](ppConfig.kafkaPrimaryRouteTopic)).thenReturn(new TelemetryPrimaryEventSink)
    when(mockKafkaUtil.kafkaEventSink[Event](ppConfig.kafkaSecondaryRouteTopic)).thenReturn(new TelemetrySecondaryEventSink)
    when(mockKafkaUtil.kafkaEventSink[Event](ppConfig.kafkaFailedTopic)).thenReturn(new TelemetryFailedEventsSink)
    when(mockKafkaUtil.kafkaEventSink[Event](ppConfig.kafkaAuditRouteTopic)).thenReturn(new TelemetryAuditEventSink)
    when(mockKafkaUtil.kafkaStringSink(ppConfig.kafkaPrimaryRouteTopic)).thenReturn(new ShareItemEventSink)

    flinkCluster.before()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    redisServer.stop()
    flinkCluster.after()
  }

  "Pipline Processor job pipeline" should "process the events" in {

    val task = new PipelinePreprocessorStreamTask(ppConfig, mockKafkaUtil)
    task.process()

    ShareItemEventSink.values.size() should be(3)
    TelemetryPrimaryEventSink.values.size() should be(2)
    TelemetryFailedEventsSink.values.size() should be(1)
    DupEventsSink.values.size() should be(1)
    TelemetryAuditEventSink.values.size() should be(0)
    TelemetrySecondaryEventSink.values.size() should be(0)

    DupEventsSink.values.get(0).getFlags.get(ppConfig.DE_DUP_FLAG_NAME).booleanValue() should be(true)
    TelemetryPrimaryEventSink.values.get(0).getFlags.get(ppConfig.VALIDATION_FLAG_NAME).booleanValue() should be(true)
    TelemetryPrimaryEventSink.values.get(1).getFlags.get(ppConfig.SHARE_EVENTS_FLATTEN_FLAG_NAME).booleanValue() should be(true)
    TelemetryPrimaryEventSink.values.get(1).getFlags.get(ppConfig.VALIDATION_FLAG_NAME).booleanValue() should be(true)
    TelemetryFailedEventsSink.values.get(0).getFlags.get(ppConfig.VALIDATION_FLAG_NAME).booleanValue() should be(false)

    BaseMetricsReporter.gaugeMetrics(s"${ppConfig.jobName}.${ppConfig.validationSuccessMetricsCount}").getValue() should be (3)
    BaseMetricsReporter.gaugeMetrics(s"${ppConfig.jobName}.${ppConfig.validationFailureMetricsCount}").getValue() should be (1)
    BaseMetricsReporter.gaugeMetrics(s"${ppConfig.jobName}.${ppConfig.validationSkipMetricsCount}").getValue() should be (0)
    BaseMetricsReporter.gaugeMetrics(s"${ppConfig.jobName}.${ppConfig.primaryRouterMetricCount}").getValue() should be (1)
    BaseMetricsReporter.gaugeMetrics(s"${ppConfig.jobName}.${ppConfig.secondaryRouterMetricCount}").getValue() should be (0)
    BaseMetricsReporter.gaugeMetrics(s"${ppConfig.jobName}.${ppConfig.auditEventRouterMetricCount}").getValue() should be (0)
    BaseMetricsReporter.gaugeMetrics(s"${ppConfig.jobName}.${ppConfig.shareItemEventsMetircsCount}").getValue() should be (3)
    BaseMetricsReporter.gaugeMetrics(s"${ppConfig.jobName}.${ppConfig.shareEventsRouterMetricCount}").getValue() should be (1)
    BaseMetricsReporter.gaugeMetrics(s"${ppConfig.jobName}.unique-event-count").getValue() should be (2)
    BaseMetricsReporter.gaugeMetrics(s"${ppConfig.jobName}.duplicate-event-count").getValue() should be (4)

  }
}

class PipeLineProcessorEventSource extends SourceFunction[Event] {

  override def run(ctx: SourceContext[Event]) {
    val gson = new Gson()
    val event1 = gson.fromJson(EventFixtures.EVENT_WITH_MID, new util.LinkedHashMap[String, AnyRef]().getClass)
    val event2 = gson.fromJson(EventFixtures.SHARE_EVENT, new util.LinkedHashMap[String, AnyRef]().getClass)
    val event3 = gson.fromJson(EventFixtures.INVALID_EVENT, new util.LinkedHashMap[String, AnyRef]().getClass)
    val event4 = gson.fromJson(EventFixtures.INVALID_EVENT_SCHEMA_DOESNT_EXISTS, new util.LinkedHashMap[String, AnyRef]().getClass)
    val event5 = gson.fromJson(EventFixtures.DUPLICATE_SHARE_EVENT, new util.LinkedHashMap[String, AnyRef]().getClass)
    ctx.collect(new Event(event1))
    ctx.collect(new Event(event2))
    ctx.collect(new Event(event3))
    ctx.collect(new Event(event4))
    ctx.collect(new Event(event5))
  }

  override def cancel() = {}

}

class ShareItemEventSink extends SinkFunction[String] {

  override def invoke(value: String): Unit = {
    synchronized {
      ShareItemEventSink.values.add(value)
    }
  }
}

object ShareItemEventSink {
  val values: util.List[String] = new util.ArrayList()
}

class TelemetryFailedEventsSink extends SinkFunction[Event] {

  override def invoke(value: Event): Unit = {
    synchronized {
      TelemetryFailedEventsSink.values.add(value)
    }
  }
}

object TelemetryFailedEventsSink {
  val values: util.List[Event] = new util.ArrayList()
}


class TelemetryPrimaryEventSink extends SinkFunction[Event] {

  override def invoke(value: Event): Unit = {
    synchronized {
      TelemetryPrimaryEventSink.values.add(value)
    }
  }
}

object TelemetryPrimaryEventSink {
  val values: util.List[Event] = new util.ArrayList()
}


class TelemetrySecondaryEventSink extends SinkFunction[Event] {

  override def invoke(value: Event): Unit = {
    synchronized {
      TelemetrySecondaryEventSink.values.add(value)
    }
  }
}

object TelemetrySecondaryEventSink {
  val values: util.List[Event] = new util.ArrayList()
}


class TelemetryAuditEventSink extends SinkFunction[Event] {

  override def invoke(value: Event): Unit = {
    synchronized {
      TelemetryAuditEventSink.values.add(value)
    }
  }
}

object TelemetryAuditEventSink {
  val values: util.List[Event] = new util.ArrayList()
}

class DupEventsSink extends SinkFunction[Event] {

  override def invoke(value: Event): Unit = {
    synchronized {
      DupEventsSink.values.add(value)
    }
  }
}

object DupEventsSink {
  val values: util.List[Event] = new util.ArrayList()
}