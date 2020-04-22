package org.sunbird.dp.functions

import java.util

import com.google.gson.Gson
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.sunbird.dp.core.cache.{DataCache, RedisConnect}
import org.sunbird.dp.core.job.{BaseProcessFunction, Metrics}
import org.sunbird.dp.core.util.RestUtil
import org.sunbird.dp.domain.Event
import org.sunbird.dp.task.ContentCacheUpdaterConfig

case class DialCodeResult(result: util.HashMap[String, Any])

class DialCodeUpdaterFunction(config: ContentCacheUpdaterConfig)
                             (implicit val mapTypeInfo: TypeInformation[Event]) extends BaseProcessFunction[Event, Event](config) {

    private[this] val logger = LoggerFactory.getLogger(classOf[DialCodeUpdaterFunction])

    private var dataCache: DataCache = _
    private val restUtil = new RestUtil()

    override def metricsList(): List[String] = {
        List(config.dialCodeCacheHit, config.dialCodeApiMissHit, config.dialCodeApiHit)
    }

    override def open(parameters: Configuration): Unit = {
        super.open(parameters)
        dataCache = new DataCache(config, new RedisConnect(config), config.dialcodeStore, config.dialcodeFields)
        dataCache.init()
    }

    override def close(): Unit = {
        super.close()
        dataCache.close()
    }

    override def processElement(event: Event, context: ProcessFunction[Event, Event]#Context, metrics: Metrics): Unit = {

        import scala.collection.JavaConverters._
        val gson = new Gson()
        val properties = event.extractProperties()
        val dialCodesList = properties.filter(p => config.dialCodeProperties.contains(p._1)).flatMap(f => {
            f._2 match {
                case a: util.ArrayList[String] => a.asScala
                case _ => List.empty
            }
        }).filter(p => !p.isEmpty)

        val headers = Map("Authorization" -> ("Bearer " + config.dialCodeApiToken))
        dialCodesList.foreach(dc => {
            if (dataCache.getWithRetry(dc).isEmpty) {
                val result = gson.fromJson[DialCodeResult](restUtil.get(config.dialCodeApiUrl + dc, Some(headers)), classOf[DialCodeResult]).result
                if (!result.isEmpty && result.containsKey("dialcode")) {
                    metrics.incCounter(config.dialCodeApiHit)
                    dataCache.setWithRetry(dc, gson.toJson(result.get("dialcode")))
                    metrics.incCounter(config.dialCodeCacheHit)
                    logger.info(dc + " updated successfully")
                }
                else {
                    metrics.incCounter(config.dialCodeApiMissHit)
                }
            }
            else {
                metrics.incCounter(config.dialCodeCacheHit)
            }
        })

        context.output(config.withDialCodeEventsTag, event)
    }

}