package org.sunbird.dp.task

import java.util
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.sunbird.dp.core.BaseJobConfig
import org.apache.flink.streaming.api.scala.OutputTag
import com.typesafe.config.Config
import org.sunbird.dp.domain.Event
import collection.JavaConversions._

class ContentCacheUpdaterConfig(override val config: Config) extends BaseJobConfig(config, "content-cache-updater") {

  private val serialVersionUID = 2905979434303791379L

  implicit val mapTypeInfo: TypeInformation[Event] = TypeExtractor.getForClass(classOf[Event])
  implicit val anyTypeInfo: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])

  // Kafka Topics Configuration
  val inputTopic: String = config.getString("kafka.denorm.input.topic")

  val contentStore: Int = config.getInt("redis.database.contentstore.id")
  val dialcodeStore: Int = config.getInt("redis.database.dialcodestore.id")

  val contentFields = List("name", "objectType", "contentType", "mediaType", "language", "medium", "mimeType", "createdBy",
    "createdFor", "framework", "board", "subject", "status", "pkgVersion", "lastSubmittedOn", "lastUpdatedOn", "lastPublishedOn")
  val dialcodeFields = List("identifier", "channel", "batchcode", "publisher", "generated_on", "published_on", "status")
  


  val DENORM_EVENTS_PRODUCER = "telemetry-denorm-events-producer"
  val JOB_METRICS_PRODUCER = "telemetry-job-metrics-producer"
  
  val WITH_CONTENT_EVENTS = "with_content_events"
  val WITH_DIALCODE_EVENTS = "with_dialcode_events"
    val withDialCodeEventsTag: OutputTag[Event] = OutputTag[Event](WITH_DIALCODE_EVENTS)
  val withContentEventsTag: OutputTag[Event] = OutputTag[Event](WITH_CONTENT_EVENTS)

  val total = "content-total"
  val cacheHit = "content-cache-hit"
  val cacheMiss = "content-cache-miss"

}
