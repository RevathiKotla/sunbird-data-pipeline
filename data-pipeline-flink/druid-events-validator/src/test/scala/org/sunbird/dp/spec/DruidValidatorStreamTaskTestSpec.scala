package org.sunbird.dp.spec

import java.util

import com.google.gson.Gson
import com.typesafe.config.ConfigFactory
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.sunbird.dp.domain.Event
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.sunbird.dp.core.FlinkKafkaConnector
import org.sunbird.dp.fixture.EventFixture
import org.sunbird.dp.task.{DruidValidatorConfig, DruidValidatorStreamTask}
import redis.embedded.RedisServer

class DruidValidatorStreamTaskTestSpec  extends FlatSpec with Matchers with BeforeAndAfterAll with MockitoSugar {

    implicit val eventTypeInfo: TypeInformation[Event] = TypeExtractor.getForClass(classOf[Event])
    val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
      .setNumberSlotsPerTaskManager(1)
      .setNumberTaskManagers(1)
      .build)
    var redisServer: RedisServer = _
    val config = ConfigFactory.load("test.conf");
    val druidValidatorConfig: DruidValidatorConfig = new DruidValidatorConfig(config);
    val mockKafkaUtil: FlinkKafkaConnector = mock[FlinkKafkaConnector](Mockito.withSettings().serializable())

    override protected def beforeAll(): Unit = {
        super.beforeAll()
        redisServer = new RedisServer(6341)
        redisServer.start()

        when(mockKafkaUtil.kafkaEventSource[Event](druidValidatorConfig.kafkaInputTopic)).thenReturn(new DruidValidatorEventSource)
        when(mockKafkaUtil.kafkaEventSink[Event](druidValidatorConfig.kafkaDuplicateTopic)).thenReturn(new DupEventsSink)
        when(mockKafkaUtil.kafkaEventSink[Event](druidValidatorConfig.kafkaTelemetryRouteTopic)).thenReturn(new TelemetryEventsSink)
        when(mockKafkaUtil.kafkaEventSink[Event](druidValidatorConfig.kafkaSummaryRouteTopic)).thenReturn(new SummaryEventsSink)
        when(mockKafkaUtil.kafkaEventSink[Event](druidValidatorConfig.kafkaLogRouteTopic)).thenReturn(new LogEventsSink)
        when(mockKafkaUtil.kafkaEventSink[Event](druidValidatorConfig.kafkaErrorRouteTopic)).thenReturn(new ErrorEventsSink)
        when(mockKafkaUtil.kafkaEventSink[Event](druidValidatorConfig.kafkaFailedTopic)).thenReturn(new FailedEventsSink)

        flinkCluster.before()
    }

    override protected def afterAll(): Unit = {
        super.afterAll()
        redisServer.stop()
        flinkCluster.after()
    }

    "Druid Validator job pipeline" should "validates events" in {

        val task = new DruidValidatorStreamTask(druidValidatorConfig, mockKafkaUtil);
        task.process()

        TelemetryEventsSink.values.size() should be (2)
        SummaryEventsSink.values.size() should be (1)
        FailedEventsSink.values.size() should be (1)
        DupEventsSink.values.size() should be (1)
        LogEventsSink.values.size() should be (1)
        ErrorEventsSink.values.size() should be (1)

        DupEventsSink.values.get(0).getFlags.get("dv_processed").booleanValue() should be(true)
        DupEventsSink.values.get(0).getFlags.get("dv_duplicate").booleanValue() should be(true)

        TelemetryEventsSink.values.get(0).getFlags.get("dv_processed").booleanValue() should be(true)

        LogEventsSink.values.get(0).getFlags.get("dv_validation_skipped").booleanValue() should be(true)
        LogEventsSink.values.get(0).getFlags.get("dv_dedup_skipped").booleanValue() should be(true)

        FailedEventsSink.values.get(0).getFlags.get("dv_processed").booleanValue() should be(false)
        FailedEventsSink.values.get(0).getFlags.get("dv_validation_failed").booleanValue() should be(true)

    }

}

class DruidValidatorEventSource  extends SourceFunction[Event] {

    override def run(ctx: SourceContext[Event]) {
        val gson = new Gson()
        val event1 = gson.fromJson(EventFixture.VALID_DENORM_TELEMETRY_EVENT, new util.LinkedHashMap[String, AnyRef]().getClass)
        val event2 = gson.fromJson(EventFixture.INVALID_DENORM_TELEMETRY_EVENT, new util.LinkedHashMap[String, AnyRef]().getClass)
        val event3 = gson.fromJson(EventFixture.VALID_DENORM_SUMMARY_EVENT, new util.LinkedHashMap[String, AnyRef]().getClass)
        val event4 = gson.fromJson(EventFixture.VALID_LOG_EVENT, new util.LinkedHashMap[String, AnyRef]().getClass)
        val event5 = gson.fromJson(EventFixture.VALID_ERROR_EVENT, new util.LinkedHashMap[String, AnyRef]().getClass)
        val event6 = gson.fromJson(EventFixture.VALID_SERACH_EVENT, new util.LinkedHashMap[String, AnyRef]().getClass)
        ctx.collect(new Event(event1, 0))
        ctx.collect(new Event(event2, 0))
        ctx.collect(new Event(event3, 0))
        ctx.collect(new Event(event4, 0))
        ctx.collect(new Event(event5, 0))
        ctx.collect(new Event(event1, 0))
        ctx.collect(new Event(event6, 0))
    }

    override def cancel() = {

    }

}

class TelemetryEventsSink extends SinkFunction[Event] {

    override def invoke(value: Event): Unit = {
        synchronized {
            TelemetryEventsSink.values.add(value)
        }
    }
}

object TelemetryEventsSink {
    val values: util.List[Event] = new util.ArrayList()
}

class SummaryEventsSink extends SinkFunction[Event] {

    override def invoke(value: Event): Unit = {
        synchronized {
            SummaryEventsSink.values.add(value)
        }
    }
}

object SummaryEventsSink {
    val values: util.List[Event] = new util.ArrayList()
}

class FailedEventsSink extends SinkFunction[Event] {

    override def invoke(value: Event): Unit = {
        synchronized {
            FailedEventsSink.values.add(value)
        }
    }
}

object FailedEventsSink {
    val values: util.List[Event] = new util.ArrayList()
}

class LogEventsSink extends SinkFunction[Event] {

    override def invoke(value: Event): Unit = {
        synchronized {
            LogEventsSink.values.add(value)
        }
    }
}

object LogEventsSink {
    val values: util.List[Event] = new util.ArrayList()
}

class ErrorEventsSink extends SinkFunction[Event] {

    override def invoke(value:Event): Unit = {
        synchronized {
            ErrorEventsSink.values.add(value)
        }
    }
}

object ErrorEventsSink {
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