package org.sunbird.dp.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.{KeyedProcessFunction, ProcessFunction}
import org.slf4j.LoggerFactory
import org.sunbird.dp.cache.RedisConnect
import org.sunbird.dp.core.{BaseProcessFunction, DataCache, Metrics}
import org.sunbird.dp.domain.Event
import org.sunbird.dp.task.ContentCacheUpdaterConfig

class ContentUpdaterFunction(config: ContentCacheUpdaterConfig)(implicit val mapTypeInfo: TypeInformation[Event])
  extends BaseProcessFunction[Event,Event](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[ContentUpdaterFunction])

  private var dataCache: DataCache = _

  override def metricsList(): List[String] = {
    List(config.total, config.cacheHit, config.cacheMiss)
  }

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    dataCache = new DataCache(config, new RedisConnect(config), config.contentStore, config.contentFields)
    dataCache.init()
  }

  override def close(): Unit = {
    super.close()
    dataCache.close()
  }

  override def processElement(event: Event, context: ProcessFunction[Event, Event]#Context, metrics: Metrics): Unit = {
        println("teh value " + event.getNodeUniqueId())



  }
}
