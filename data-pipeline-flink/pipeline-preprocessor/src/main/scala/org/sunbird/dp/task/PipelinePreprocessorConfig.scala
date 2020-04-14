package org.sunbird.dp.task

import com.typesafe.config.Config
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.streaming.api.scala.OutputTag
import org.sunbird.dp.core.BaseJobConfig
import org.sunbird.dp.domain.Event

import scala.collection.JavaConverters._

class PipelinePreprocessorConfig(override val config: Config) extends BaseJobConfig(config, "pipeline-processor") {

  private val serialVersionUID = 2905979434303791379L

  implicit val eventTypeInfo: TypeInformation[Event] = TypeExtractor.getForClass(classOf[Event])
  implicit val stringTypeInfo: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])

  val schemaPath: String = config.getString("telemetry.schema.path")

  val dedupStore: Int = config.getInt("redis.database.duplicationstore.id")
  val cacheExpirySeconds: Int = config.getInt("redis.database.key.expiry.seconds")

  // Kafka Topic Configuration
  val kafkaInputTopic: String = config.getString("kafka.input.topic")

  val kafkaPrimaryRouteTopic: String = config.getString("kafka.output.primary.route.topic")
  val kafkaSecondaryRouteTopic: String = config.getString("kafka.output.secondary.route.topic")

  val kafkaAuditRouteTopic: String = config.getString("kafka.output.audit.route.topic")

  val kafkaFailedTopic: String = config.getString("kafka.output.failed.topic")
  val kafkaDuplicateTopic: String = config.getString("kafka.output.duplicate.topic")

  val secondaryRouteEids: List[String] = config.getStringList("router.secondary.routes.eid").asScala.toList

  val defaultChannel: String = config.getString("default.channel")

  val includedProducersForDedup: List[String] = config.getStringList("dedup.producer.included.ids").asScala.toList

  // Validation & dedup Stream out put tag
  val validationFailedEventsOutputTag: OutputTag[Event] = OutputTag[Event]("validation-failed-events")
  val uniqueEventsOutputTag: OutputTag[Event] = OutputTag[Event]("unique-events")
  val duplicateEventsOutputTag: OutputTag[Event] = OutputTag[Event]("duplicate-events")

  // Router stream out put tags
  val primaryRouteEventsOutputTag: OutputTag[Event] = OutputTag[Event]("primary-route-events")
  val secondaryRouteEventsOutputTag: OutputTag[Event] = OutputTag[Event]("secondary-route-events")
  val auditRouteEventsOutputTag: OutputTag[Event] = OutputTag[Event]("audit-route-events")

  // Share events out put tags
  val shareRouteEventsOutputTag: OutputTag[Event] = OutputTag[Event]("share-route-events")
  val shareItemEventOutTag: OutputTag[String] = OutputTag[String]("share-route-events")

  val validationParallelism: Int = config.getInt("telemetry.validation.parallelism")
  val routerParallelism: Int = config.getInt("telemetry.router.parallelism")
  val shareEventsFlattnerParallelism: Int = config.getInt("share.events.flattener.parallelism")

  val VALIDATION_FLAG_NAME = "pp_validation_processed"
  val DE_DUP_FLAG_NAME = "pp_duplicate"
  val SHARE_EVENTS_FLATTEN_FLAG_NAME = "pp_share_event_processed"

  // Router job metrics
  val primaryRouterMetricCount = "primary-route-success-count"
  val secondaryRouterMetricCount = "secondary-route-success-count"
  val auditEventRouterMetricCount = "audit-route-success-count"
  val shareEventsRouterMetricCount = "share-route-success-count"


  // Validation job metrics
  val validationSuccessMetricsCount = "validation-success-event-count"
  val validationFailureMetricsCount = "validation-failed-event-count"
  val duplicationEventMetricsCount = "duplicate-event-count"
  val uniqueEventsMetricsCount = "unique-event-count"
  val validationSkipMetricsCount = "skipped-event-count"
  // ShareEventsFlatten count
  val shareItemEventsMetircsCount = "share-item-event-success-count"

  // Producers
  val jobMetricsProducer = "telemetry-job-metrics-producer"
  val primaryRouterProducer = "kafka-primary-route-producer"
  val secondaryRouterProducer = "kafka-secondary-route-producer"
  val auditRouterProducer = "kafka-audit-route-producer"
  val invalidEventProducer = "kafka-telemetry-invalid-events-producer"
  val duplicateEventProducer = "kafka-telemetry-duplicate-producer"




}
