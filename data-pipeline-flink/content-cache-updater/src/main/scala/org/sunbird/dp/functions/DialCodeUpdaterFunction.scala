package org.sunbird.dp.functions

import java.util

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.sunbird.dp.cache.RedisConnect
import org.sunbird.dp.core.{BaseProcessFunction, DataCache, Metrics}
import org.sunbird.dp.domain.Event
import org.sunbird.dp.task.ContentCacheUpdaterConfig
import org.sunbird.dp.util.RestUtil

class DialCodeUpdaterFunction(config: ContentCacheUpdaterConfig)(implicit val mapTypeInfo: TypeInformation[Event]) extends BaseProcessFunction[Event,Event](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[DialCodeUpdaterFunction])

  private var dataCache: DataCache = _
  private var restUtil: RestUtil = _

  
  override def metricsList(): List[String] = {
    println("in metrics")
    List(config.total, config.cacheHit, config.cacheMiss)
  }

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    dataCache = new DataCache(config, new RedisConnect(config), config.contentStore, config.contentFields)
    restUtil = new RestUtil()
    dataCache.init()
  }

  override def close(): Unit = {
    super.close()
    dataCache.close()
  }

  override def processElement(event: Event, context: ProcessFunction[Event, Event]#Context, metrics: Metrics): Unit = {

    println("start of extraction")
    import scala.collection.JavaConverters._
    val gson = new Gson()
    val properties = event.extractProperties()
      val dialCodes = properties.getOrElse("dialcodes",null).asInstanceOf[util.ArrayList[String]].asScala
    val headers  = Map("Authorization" -> "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkNjNiMjgwZTQ1NDE0NDU4ODk4NzcwYzZhOGZiZjQ1MCJ9.Ji-22XcRrOiVy4dFAmE68wPxLkNmX4wKbTj_IB7fG6Y")
      if(null!= dialCodes)
      dialCodes.foreach(f =>{
          println(f)
        println(dataCache.getWithRetry("f"))
        if(dataCache.getWithRetry("f").isEmpty) {
          println("in empty")
          val content = restUtil.get[LinkedTreeMap[String, Object]]("https://qa.ekstep.in/api/dialcode/v3/read/" + f, Some(headers)).get("result").asInstanceOf[LinkedTreeMap[String, Object]].get("dialcode")
          dataCache.setWithRetry(f,gson.toJson(content))
        }
      })

    context.output(config.withDialCodeEventsTag, event)
  }

}
