package org.sunbird.dp.contentupdater.task

import com.typesafe.config.ConfigFactory
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.sunbird.dp.contentupdater.domain.Event
import org.sunbird.dp.contentupdater.functions.{ContentUpdaterFunction, DialCodeUpdaterFunction}
import org.sunbird.dp.core.job.FlinkKafkaConnector
import org.sunbird.dp.core.util.FlinkUtil

/**
  * Content Cache Updater task does the following pipeline processing in a sequence:
  *
  * 1. Parse the message into an event
  * 2. Skip the message if the objectType or nodeUniqueId is empty. Increment the skipped counter
  * 3. Start the cacheUpdater function
  * 4. Extract all the new properies from the graph events
  * 5. If the properties has 'dialcodes' or 'reservedDialcode'
  *       5.1  loop through the dialcodes and check if the dialcode present in cache or not
  *  	    5.2  If diacode metadata exist in cache - Increment the dial-codes-from-cache-count counter
  *       5.3  Else
  *                   5.3.1 Invoke Restutil call to fetch the dialcode from dialcode search api
  *                   5.3.2 If the call is success - Update the dialcode cache with respective metadata
  *                         and increment dial-codes-from-api-count counter
  *		              5.3.3 Else increment dial-codes-from-api-miss-count counter
  * 6. Get the content metadata from content cache for the specific nodeUniqueId
  * 7. Loop through all the new properties and
  *       6.1  If the properties contain date fields -[lastStatusChangedOn,lastUpdatedOn,createdOn] , then convert them to epoch values
  *       6.2  If the properties contain list fields -[gradeLevel,subject,medium,language] , then convert them to list of string
  * 8. Remove empty value properties and update the content cache metadata with the new converted properties ,
  *    Increment the success-message-count counter
  * 9. Common
  * 		9.1 Retry once from redis on any redis connection issues
  * 		9.2 Stop the job from proceeding further if there are any redis connection issues
  */
class ContentCacheUpdaterStreamTask(config: ContentCacheUpdaterConfig, kafkaConnector: FlinkKafkaConnector) {

  private val serialVersionUID = -7729362727131516112L

  def process(): Unit = {
    
    implicit val env: StreamExecutionEnvironment = FlinkUtil.getExecutionContext(config)
    implicit val eventTypeInfo: TypeInformation[Event] = TypeExtractor.getForClass(classOf[Event])
    val source = kafkaConnector.kafkaEventSource[Event](config.inputTopic)

    val dialCodeUpdaterStream = env.addSource(source, "learning-graph-events-consumer")
      .rebalance().process(new DialCodeUpdaterFunction(config))

       dialCodeUpdaterStream.getSideOutput(config.withDialCodeEventsTag)
         .process(new ContentUpdaterFunction(config))

    env.execute(config.jobName)
  }

}

// $COVERAGE-OFF$ Disabling scoverage as the below code can only be invoked within flink cluster
object ContentCacheUpdaterStreamTask {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("content-cache-updater.conf").withFallback(ConfigFactory.systemEnvironment())
    val eConfig = new ContentCacheUpdaterConfig(config)
    val kafkaUtil = new FlinkKafkaConnector(eConfig)
    val task = new ContentCacheUpdaterStreamTask(eConfig, kafkaUtil)
    task.process()
  }
}
// $COVERAGE-ON$
