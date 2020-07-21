package org.sunbird.dp.validator.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.sunbird.dp.core.cache.{DedupEngine, RedisConnect}
import org.sunbird.dp.core.job.{BaseProcessFunction, Metrics}
import org.sunbird.dp.validator.domain.Event
import org.sunbird.dp.validator.task.DruidValidatorConfig
import org.sunbird.dp.validator.util.SchemaValidator

class DruidValidatorFunction(config: DruidValidatorConfig,
                             @transient var schemaValidator: SchemaValidator = null,
                             @transient var dedupEngine: DedupEngine = null)
                            (implicit val eventTypeInfo: TypeInformation[Event])
  extends BaseProcessFunction[Event, Event](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[DruidValidatorFunction])

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    if (dedupEngine == null) {
      val redisConnect = new RedisConnect(config.redisHost, config.redisPort, config)
      dedupEngine = new DedupEngine(redisConnect, config.dedupStore, config.cacheExpirySeconds)
    }
    if (schemaValidator == null) {
      schemaValidator = new SchemaValidator(config)
    }
  }

  override def close(): Unit = {
    super.close()
    dedupEngine.closeConnectionPool()
  }

  override def metricsList(): List[String] = {
    List(config.validationSuccessMetricsCount, config.validationFailureMetricsCount) ::: deduplicationMetrics
  }

  override def processElement(event: Event,
                              ctx: ProcessFunction[Event, Event]#Context,
                              metrics: Metrics): Unit = {

    val validationReport = schemaValidator.validate(event)

    if (validationReport.isSuccess) {
      event.markValidationSuccess()
      metrics.incCounter(config.validationSuccessMetricsCount)
      deDup[Event, Event](event.mid(), event, ctx,
        config.validEventOutputTag, config.duplicateEventOutputTag, flagName = "dv_duplicate")(dedupEngine, metrics)
    } else {
      val failedErrorMsg = schemaValidator.getInvalidFieldName(validationReport.toString)
      event.markValidationFailure(failedErrorMsg)
      metrics.incCounter(config.validationFailureMetricsCount)
      ctx.output(config.invalidEventOutputTag, event)
    }
  }
}
