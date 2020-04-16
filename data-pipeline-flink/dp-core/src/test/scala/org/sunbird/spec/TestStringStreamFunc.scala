package org.sunbird.spec

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.sunbird.dp.core.{BaseProcessFunction, Metrics}


class TestStringStreamFunc(config: BaseProcessTestConfig)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseProcessFunction[String, String](config) {

  override def metricsList(): List[String] = {
    val metrics = List(config.stringEventCount)
    metrics
  }
  override def processElement(event: String,
                              context: ProcessFunction[String, String]#Context,
                              metrics: Metrics): Unit = {
    metrics.incCounter(config.stringEventCount)
    context.output(config.stringOutputTag, event)
  }
}
